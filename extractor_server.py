from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import StreamingResponse
import yt_dlp
import requests
import json
import uvicorn
from typing import List

app = FastAPI()

@app.get("/")
async def root():
    return {"status": "online", "message": "Skibidi Music Backend is ready."}

# Configuration for yt-dlp
# You can add 'cookiefile': 'cookies.txt' here later for "Serious Use"
YDL_OPTIONS = {
    'format': 'bestaudio/best',
    'noplaylist': True,
    'quiet': True,
    'no_warnings': True,
}

@app.get("/search")
async def search(q: String):
    """
    Searches YouTube using yt-dlp search engine.
    This is keyless and avoids Google API quotas.
    """
    if not q:
        raise HTTPException(status_code=400, detail="Query is required")
    
    # Use yt-dlp to find videos
    search_opts = {
        'extract_flat': True,
        'quiet': True,
        'no_warnings': True,
    }
    
    try:
        with yt_dlp.YoutubeDL(search_opts) as ydl:
            # search: prefixes are used by yt-dlp for search
            result = ydl.extract_info(f"ytsearch10:{q}", download=False)
            entries = result.get('entries', [])
            
            songs = []
            for entry in entries:
                songs.append({
                    "id": f"yt_{entry['id']}",
                    "title": entry.get('title'),
                    "artist": entry.get('uploader'),
                    "image": f"https://img.youtube.com/vi/{entry['id']}/maxresdefault.jpg",
                    "audioUrl": "" # Client will call /audio with id
                })
            return {"songs": songs}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/audio")
async def audio(videoId: String):
    """
    Proxies the audio stream from YouTube to the mobile app.
    Bypasses signature and location locks.
    """
    clean_id = videoId.replace("yt_", "")
    url = f"https://www.youtube.com/watch?v={clean_id}"
    
    try:
        with yt_dlp.YoutubeDL(YDL_OPTIONS) as ydl:
            info = ydl.extract_info(url, download=False)
            
            # Filter for best M4A for A21s hardware
            formats = info.get('formats', [])
            best_audio = None
            for f in formats:
                if f.get('vcodec') == 'none' and (f.get('ext') == 'm4a' or 'mp4a' in f.get('acodec', '')):
                    best_audio = f
                    break
            
            if not best_audio:
                best_audio = next((f for f in formats if f.get('vcodec') == 'none'), None)
            
            if not best_audio:
                raise HTTPException(status_code=404, detail="No audio stream found")
            
            audio_url = best_audio['url']
            headers = best_audio.get('http_headers', {})

            # Proxy the data
            def stream_file():
                with requests.get(audio_url, headers=headers, stream=True) as resp:
                    resp.raise_for_status()
                    for chunk in resp.iter_content(chunk_size=128 * 1024):
                        yield chunk

            return StreamingResponse(
                stream_file(), 
                media_type=best_audio.get('mime_type', 'audio/mp4')
            )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8080)
