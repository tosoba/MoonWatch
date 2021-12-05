package com.moonwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
  private val _tokenAddress = MutableStateFlow("")
  val tokenAddress: Flow<String> = _tokenAddress

  init {
    tokenAddress.debounce(500L).onEach {}.launchIn(viewModelScope)
  }

  fun setTokenAddress(address: String) {
    _tokenAddress.value = address
  }
}
