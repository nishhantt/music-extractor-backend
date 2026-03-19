package com.example.musicplayer.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState(initial = PlayerUiState.Idle)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration = viewModel.getDuration()
    
    // Vinyl rotation
    val rotation = remember { mutableStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                rotation.value = (rotation.value + 0.5f) % 360f
                delay(16L)
            }
        }
    }

    val currentTitle = if (state is PlayerUiState.Playing) {
        (state as PlayerUiState.Playing).song.title
    } else "No Track"
    
    val currentArtist = if (state is PlayerUiState.Playing) {
        (state as PlayerUiState.Playing).song.artist
    } else ""

    val thumb = if (state is PlayerUiState.Playing) {
        (state as PlayerUiState.Playing).song.image
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: glassy search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0x33FFFFFF))
                .clickable { onSearch() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search songs, artists...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }
        }

        // Middle: rotating vinyl
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer(rotationZ = rotation.value)
                    .background(Color.DarkGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (thumb != null) {
                    AsyncImage(
                        model = thumb,
                        contentDescription = currentTitle,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                    )
                }
            }
        }

        // Song info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = currentArtist,
                color = Color.Gray,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            if (state is PlayerUiState.Loading) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Buffering...", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            if (state is PlayerUiState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = (state as PlayerUiState.Error).message,
                    color = Color.Red.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Progress bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            val sliderValue = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            
            Slider(
                value = sliderValue,
                onValueChange = { percent ->
                    if (duration > 0) {
                        viewModel.seekTo((percent * duration).toLong())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(duration),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previous() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { viewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = { viewModel.next() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
