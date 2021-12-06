package com.moonwatch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonwatch.core.android.model.*
import com.moonwatch.core.ext.withLatestFrom
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.usecase.GetTokenWithValue
import com.moonwatch.core.usecase.SaveToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val getTokenWithValue: GetTokenWithValue,
    private val saveToken: SaveToken,
) : ViewModel() {
  private val _tokenAddress = MutableStateFlow("")
  val tokenAddress: Flow<String>
    get() = _tokenAddress

  private val _tokenWithValue = mutableStateOf<Loadable<ITokenWithValue>>(Empty)
  val tokenWithValue: State<Loadable<ITokenWithValue>>
    get() = _tokenWithValue

  private val _toggleRetryLoadingToken = MutableSharedFlow<Unit>()

  init {
    merge(
            tokenAddress
                .drop(1)
                .filter(::isBscAddressValid)
                .debounce(timeoutMillis = 500L)
                .distinctUntilChanged(),
            _toggleRetryLoadingToken.debounce(500L).withLatestFrom(
                    tokenAddress.filter(::isBscAddressValid)) { _, address -> address },
        )
        .transformLatest { address ->
          emit(LoadingFirst)
          try {
            emit(Ready(getTokenWithValue(address)))
          } catch (ex: Exception) {
            emit(FailedFirst(ex))
          }
        }
        .onEach(_tokenWithValue::value::set)
        .launchIn(viewModelScope)
  }

  fun setTokenAddress(address: String) {
    _tokenAddress.value = address
  }

  suspend fun retryLoadingToken() {
    _toggleRetryLoadingToken.emit(Unit)
  }

  suspend fun saveCurrentToken() {
    val currentTokenWithValue = _tokenWithValue.value
    if (currentTokenWithValue !is Ready<ITokenWithValue>) throw IllegalStateException()
    saveToken(currentTokenWithValue.value.token)
  }

  private fun isBscAddressValid(address: String) = address.length == 42 && address.startsWith("0x")
}
