package com.moonwatch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonwatch.core.android.model.*
import com.moonwatch.core.ext.withLatestFrom
import com.moonwatch.core.model.ITokenWithValue
import com.moonwatch.core.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val getTokenWithValue: GetTokenWithValue,
    private val saveTokenWithValue: SaveTokenWithValue,
    private val deleteToken: DeleteToken,
    val getAlertsFlow: GetAlertsFlow,
    val getTokensFlow: GetTokensFlow,
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
            tokenAddress.drop(1).debounce(500L).distinctUntilChanged(),
            _toggleRetryLoadingToken.debounce(500L).withLatestFrom(tokenAddress) { _, address ->
              address
            },
        )
        .transformLatest { address ->
          if (address.isEmpty()) {
            emit(Empty)
            return@transformLatest
          }

          if (!isBscAddressValid(address)) {
            emit(FailedFirst(InvalidAddressException))
            return@transformLatest
          }

          emit(LoadingFirst)
          try {
            val tokenWithValue = withTimeout(10_000L) { getTokenWithValue(address) }
            emit(Ready(tokenWithValue))
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
    saveTokenWithValue(
        token = currentTokenWithValue.value.token,
        value = currentTokenWithValue.value.value,
    )
  }

  fun deleteToken(address: String) {
    viewModelScope.launch { deleteToken.invoke(address) }
  }

  private fun isBscAddressValid(address: String) = address.matches(Regex("^0x\\S{40}\$"))
}

object InvalidAddressException : IllegalArgumentException()
