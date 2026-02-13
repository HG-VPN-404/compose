package com.tera.down.ui.screen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tera.down.domain.model.TeraFileItem
import com.tera.down.ui.component.VideoItemView
import com.tera.down.ui.component.VideoPlayer
import com.tera.down.viewmodel.MainViewModel
import com.tera.down.viewmodel.UiState

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    
    // State sederhana untuk navigasi internal (Home vs Player)
    var selectedVideo by remember { mutableStateOf<TeraFileItem?>(null) }

    if (selectedVideo != null) {
        // Player Screen
        PlayerScreen(
            videoItem = selectedVideo!!,
            onBack = { selectedVideo = null }
        )
    } else {
        // Home Screen
        HomeScreenContent(
            urlInput = urlInput,
            onUrlChange = { viewModel.urlInput.value = it },
            onSubmit = { viewModel.fetchFiles() },
            uiState = uiState,
            onVideoSelect = { selectedVideo = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
    uiState: UiState,
    onVideoSelect: (TeraFileItem) -> Unit
) {
    Scaffold(
        containerColor = Color.Black, // Netflix-like background
        topBar = {
            TopAppBar(
                title = { Text("TeraStream", color = Color.Red) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Input Area
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Paste TeraBox URL here") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("GO")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content Area
            Text("Trending / Results", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is UiState.Idle -> {
                    Text("Masukkan URL dan tekan GO", color = Color.Gray)
                }
                is UiState.Loading -> {
                    CircularProgressIndicator(color = Color.Red)
                }
                is UiState.Error -> {
                    Text(uiState.message, color = Color.Red)
                }
                is UiState.Success -> {
                    LazyRow {
                        items(uiState.videos) { item ->
                            VideoItemView(item = item) {
                                if (item.category == "video") {
                                    onVideoSelect(item)
                                } else {
                                    // Handle folder click logic if needed later
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(
    videoItem: TeraFileItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            videoItem.links?.proxy?.let { url ->
                VideoPlayer(videoUrl = url)
            } ?: Text("Invalid Video URL", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(videoItem.filename, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { 
                        startDownload(context, videoItem.links?.proxy, videoItem.filename) 
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Download")
                }
            }
        }
    }
}

fun startDownload(context: Context, url: String?, filename: String) {
    if (url == null) return
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setDescription("Downloading video...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}