# Use a lightweight Python base image
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies for yt-dlp
RUN apt-get update && apt-get install -y ffmpeg curl && rm -rf /var/lib/apt/lists/*

# Copy requirements and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the server code
COPY extractor_server.py .

# Expose the port (Render/Koyeb use PORT environment variable)
EXPOSE 8080

# Run the server
CMD ["python", "extractor_server.py"]
