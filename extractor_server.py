"""
Extractor backend for the Music Player app.
Handles YouTube extraction via yt-dlp and JioSaavn search proxying.
"""
from flask import Flask, request, jsonify, Response, stream_with_context
import yt_dlp
import requests
import json

app = Flask(__name__)

# JioSaavn API Configuration
SAAVN_API_URL = "https://saavn.dev/api/search/songs"

@app.route("/saavn/search")
def saavn_search():
    query = request.args.get("query")
    if not query:
        return jsonify({"success": False, "message": "Query is required"}), 400

    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Accept": "application/json"
    }

    try:
        # Calling Saavn API from the server side bypasses mobile-specific bot detection
        params = {"query": query}
        response = requests.get(SAAVN_API_URL, params=params, headers=headers, timeout=10)

        if response.status_code != 200:
            return jsonify({"success": False, "message": f"Upstream API error: {response.status_code}"}), response.status_code

        return response.text, 200, {"Content-Type": "application/json"}

    except Exception as e:
        return jsonify({"success": False, "message": str(e)}), 500

def _resolve_audio_format(video_id: str):
    ydl_opts = {
        "format": "bestaudio/best",
        "quiet": True,
        "no_warnings": True,
        "extract_flat": False,
    }
    url = f"https://www.youtube.com/watch?v={video_id}"
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)
        if not info:
            return None, None, None

        formats = info.get("formats") or []
        picked = None
        for f in formats:
            if f.get("vcodec") == "none" and f.get("url"):
                picked = f
                break
        if not picked:
            for f in formats:
                if f.get("url"):
                    picked = f
                    break
        if not picked and info.get("url"):
            picked = {"url": info.get("url")}
        if not picked or not picked.get("url"):
            return info, None, None

        direct_url = picked["url"]
        if direct_url.startswith("http://"):
            direct_url = "https://" + direct_url[len("http://"):]

        headers = picked.get("http_headers") or {}
        return info, direct_url, headers


@app.route("/stream")
def stream():
    video_id = request.args.get("videoId")
    if not video_id:
        return jsonify({"error": "missing videoId"}), 400
    try:
        return jsonify({
            "url": f"/audio?videoId={video_id}",
            "title": None,
            "mimeType": "audio/*",
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/audio")
def audio():
    video_id = request.args.get("videoId")
    if not video_id:
        return jsonify({"error": "missing videoId"}), 400
    try:
        info, direct_url, headers = _resolve_audio_format(video_id)
        if not info or not direct_url:
            return jsonify({"error": "no audio url"}), 404

        r = requests.get(direct_url, headers=headers, stream=True, timeout=30)
        r.raise_for_status()

        content_type = r.headers.get("Content-Type", "audio/*")

        @stream_with_context
        def generate():
            for chunk in r.iter_content(chunk_size=64 * 1024):
                if chunk:
                    yield chunk

        return Response(generate(), content_type=content_type)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    import os
    port = int(os.environ.get("PORT", 8080))
    app.run(host="0.0.0.0", port=port)
