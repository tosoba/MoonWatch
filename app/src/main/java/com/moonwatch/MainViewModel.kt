package com.moonwatch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonwatch.core.android.ext.transformLatestTryingTo
import com.moonwatch.core.android.model.Empty
import com.moonwatch.core.android.model.Loadable
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.usecase.GetTokenWithValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val getTokenWithValue: GetTokenWithValue,
) : ViewModel() {
  private val _tokenAddress = MutableStateFlow("")
  val tokenAddress: Flow<String>
    get() = _tokenAddress

  private val _tokenWithValue = mutableStateOf<Loadable<ITokenWithValue>>(Empty)
  val tokenWithValue: State<Loadable<ITokenWithValue>>
    get() = _tokenWithValue

  init {
    tokenAddress
        .debounce(500L)
        .filter { address -> address.length == 42 && address.startsWith("0x") }
        .transformLatestTryingTo(getTokenWithValue::invoke)
        .onEach(_tokenWithValue::value::set)
        .launchIn(viewModelScope)
  }

  fun setTokenAddress(address: String) {
    _tokenAddress.value = address
  }
}
