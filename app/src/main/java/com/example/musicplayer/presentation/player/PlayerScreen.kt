import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.musicplayer.R
import com.example.musicplayer.presentation.player.components.VinylPlayer
import com.example.musicplayer.presentation.player.components.QueueBottomSheet
import com.example.musicplayer.presentation.ui.theme.rememberDynamicThemeState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState(initial = PlayerUiState.Idle)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    val duration = viewModel.getDuration()
    
    val context = LocalContext.current
    val dynamicThemeState = rememberDynamicThemeState()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // Extract colors when song changes
    val currentSong = (state as? PlayerUiState.Playing)?.song
    LaunchedEffect(currentSong) {
        currentSong?.image?.let { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                dynamicThemeState.updateFromBitmap(bitmap)
            }
        }
    }

    val animatedBgStart by animateColorAsState(targetValue = dynamicThemeState.primaryColor, animationSpec = tween(1000), label = "bg_start")
    val animatedBgEnd by animateColorAsState(targetValue = dynamicThemeState.secondaryColor, animationSpec = tween(1000), label = "bg_end")

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            QueueBottomSheet(
                songs = playlist,
                currentSongId = currentSong?.id ?: "",
                onSongClick = { song ->
                    viewModel.playSong(song, playlist)
                    scope.launch { sheetState.hide() }
                }
            )
        },
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedBgStart, animatedBgEnd)
                    )
                )
        ) {
            // Main Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Nav
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Back", tint = Color.White.copy(alpha = 0.8f))
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentSong?.title ?: "Skibidi",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentSong?.artist ?: "Music Player",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onSearch) {
                        Icon(Icons.Filled.Search, "Search", tint = Color.White.copy(alpha = 0.8f))
                    }
                }

                // Center focus: Vinyl
                VinylPlayer(
                    imageUrl = currentSong?.image ?: "",
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .weight(1f)
                )

                // Info and Controls Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Song Info
                    Text(
                        text = currentSong?.title ?: "Select a song",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = currentSong?.artist ?: "Search to begin",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Progress Slider
                    val sliderValue = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                    Slider(
                        value = sliderValue,
                        onValueChange = { percent ->
                            if (duration > 0) viewModel.seekTo((percent * duration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPosition), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(text = formatTime(duration), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Player Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previous() }) {
                            Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(42.dp))
                        }

                        // Large Play/Pause
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { viewModel.togglePlayPause() }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize()
                            )
                        }

                        IconButton(onClick = { viewModel.next() }) {
                            Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(42.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Extra Controls: Repeat & Queue Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Repeat One logic */ }) {
                            Icon(Icons.Filled.RepeatOne, "Repeat One", tint = Color.White.copy(alpha = 0.6f))
                        }

                        IconButton(onClick = { scope.launch { sheetState.show() } }) {
                            Icon(Icons.Filled.QueueMusic, "View Queue", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
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

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
