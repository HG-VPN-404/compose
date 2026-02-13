package com.tera.down.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tera.down.data.api.ApiClient
import com.tera.down.domain.model.TeraFileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val videos: List<TeraFileItem>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Default URL untuk testing (sesuai prompt)
    var urlInput = MutableStateFlow("https://1024terabox.com/s/1MQm0fOGj-nhdzEZ8wqn3qw")

    fun fetchFiles() {
        val currentUrl = urlInput.value
        if (currentUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = ApiClient.service.getFiles(currentUrl)
                if (response.ok && response.data?.list != null) {
                    // Filter hanya video atau folder, sesuaikan kebutuhan. 
                    // Di sini kita ambil semua tapi nanti di UI dibedakan.
                    _uiState.value = UiState.Success(response.data.list)
                } else {
                    _uiState.value = UiState.Error("Gagal memuat data atau folder kosong.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}