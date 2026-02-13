package com.tera.down.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tera.down.data.api.ApiClient
import com.tera.down.domain.model.TeraFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val videos: List<TeraFileItem>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // URL default sesuai request
    var urlInput = MutableStateFlow("https://1024terabox.com/s/1MQm0fOGj-nhdzEZ8wqn3qw")

    fun fetchFiles() {
        val currentUrl = urlInput.value
        if (currentUrl.isBlank()) return

        _uiState.value = UiState.Loading
        
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di IO Thread agar tidak freeze UI
            try {
                val response = ApiClient.service.getFiles(currentUrl)
                withContext(Dispatchers.Main) {
                    if (response.ok && !response.data?.list.isNullOrEmpty()) {
                        _uiState.value = UiState.Success(response.data!!.list!!)
                    } else {
                        _uiState.value = UiState.Error("Folder kosong atau URL salah.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Log error untuk debugging dan tampilkan ke user
                    e.printStackTrace()
                    _uiState.value = UiState.Error("Gagal: ${e.message ?: "Unknown Error"}")
                }
            }
        }
    }
}