package com.tera.down.ui.screen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
// --- BAGIAN IMPORT INI YANG DIPERBAIKI ---
import androidx.compose.foundation.shape.RoundedCornerShape // <--- INI YANG HILANG TADI
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items 
// -----------------------------------------
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
    var selectedVideo by remember { mutableStateOf<TeraFileItem?>(null) }

    if (selectedVideo != null) {
        PlayerScreen(videoItem = selectedVideo!!, onBack = { selectedVideo = null })
    } else {
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
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("TeraStream", color = Color(0xFFE50914), style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Input Bar
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Paste TeraBox URL", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141414),
                        unfocusedContainerColor = Color(0xFF141414),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.Red,
                        focusedBorderColor = Color.Red,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp) // Error sebelumnya ada di sini
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                    shape = RoundedCornerShape(8.dp), // Dan di sini
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("GO", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status & List
            when (uiState) {
                is UiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Siap Streaming", color = Color.DarkGray)
                    }
                }
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE50914))
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.message}", color = Color.Red)
                    }
                }
                is UiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.videos) { item ->
                            VideoItemView(item = item) {
                                if (item.category == "video") {
                                    if (item.links?.proxy != null) {
                                        onVideoSelect(item)
                                    } else {
                                        Toast.makeText(context, "Link video tidak tersedia", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ini Folder: ${item.filename}", Toast.LENGTH_SHORT).show()
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
fun PlayerScreen(videoItem: TeraFileItem, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            VideoPlayer(videoUrl = videoItem.links?.proxy ?: "")
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(videoItem.filename ?: "Unknown", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { startDownload(context, videoItem.links?.proxy, videoItem.filename ?: "video.mp4") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                ) {
                    Text("Download")
                }
            }
        }
    }
}

fun startDownload(context: Context, url: String?, filename: String) {
    if (url.isNullOrEmpty()) return
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        Toast.makeText(context, "Mulai download...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}