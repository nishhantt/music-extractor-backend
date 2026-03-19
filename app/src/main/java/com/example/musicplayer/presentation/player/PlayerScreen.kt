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
    
    // In a real app, you'd observe player state from viewModel
    val isPlaying = state is PlayerUiState.Playing

    // Vinyl rotation
    val rotation = remember { mutableStateOf(0f) }
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            rotation.value = (rotation.value + 0.5f) % 360f
            delay(16L)
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
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
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
                    text = "Search songs…",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = currentArtist,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            if (state is PlayerUiState.Loading) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Loading…", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
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

        // Progress bar (Simplified for this refactor)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            Slider(
                value = 0f,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* viewModel.previous() */ }) {
                Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White)
            }
            IconButton(onClick = { /* viewModel.togglePlayPause() */ }, modifier = Modifier.size(72.dp)) {
                if (isPlaying) {
                    Icon(Icons.Filled.Pause, "Pause", tint = Color.White, modifier = Modifier.size(48.dp))
                } else {
                    Icon(Icons.Filled.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
            IconButton(onClick = { /* viewModel.next() */ }) {
                Icon(Icons.Filled.SkipNext, "Next", tint = Color.White)
            }
        }
    }
}
