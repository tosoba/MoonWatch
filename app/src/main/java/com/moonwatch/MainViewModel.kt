package com.moonwatch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonwatch.core.android.model.*
import com.moonwatch.core.ext.withLatestFrom
import com.moonwatch.core.usecase.*
import com.moonwatch.model.TokenAlertWithValue
import com.moonwatch.model.TokenWithValue
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
    private val addAlert: AddAlert,
    private val getAlertsFlow: GetAlertsFlow,
    private val getTokensFlow: GetTokensFlow,
) : ViewModel() {
  // TODO: inject saved state handle and save current state in it

  private val _tokenAddress = MutableStateFlow("")
  val tokenAddress: Flow<String>
    get() = _tokenAddress

  private val _tokenWithValueBeingAdded =
      mutableStateOf<LoadableParcelable<TokenWithValue>>(LoadableParcelable(Empty))
  val tokenWithValueBeingAdded: State<LoadableParcelable<TokenWithValue>>
    get() = _tokenWithValueBeingAdded

  private val _tokenWithValueBeingViewed = mutableStateOf<TokenWithValue?>(null)
  val tokenWithValueBeingViewed: State<TokenWithValue?>
    get() = _tokenWithValueBeingViewed

  private val _toggleRetryLoadingToken = MutableSharedFlow<Unit>()

  val alertsFlow: Flow<List<TokenAlertWithValue>>
    get() = getAlertsFlow().map { it.map(::TokenAlertWithValue) }

  val tokensFlow: Flow<List<TokenWithValue>>
    get() = getTokensFlow().map { it.map(::TokenWithValue) }

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
        .map { loadable -> loadable.map(::TokenWithValue).parcelize() }
        .onEach(_tokenWithValueBeingAdded::value::set)
        .launchIn(viewModelScope)
  }

  fun setTokenAddress(address: String) {
    _tokenAddress.value = address
  }

  fun setTokenWithValueBeingViewed(tokenWithValue: TokenWithValue) {
    _tokenWithValueBeingViewed.value = tokenWithValue
  }

  suspend fun retryLoadingToken() {
    _toggleRetryLoadingToken.emit(Unit)
  }

  suspend fun saveCurrentToken() {
    val currentTokenWithValue = _tokenWithValueBeingAdded.value.loadable
    if (currentTokenWithValue !is Ready<TokenWithValue>) throw IllegalStateException()
    saveTokenWithValue(
        token = currentTokenWithValue.value.token,
        value = currentTokenWithValue.value.value,
    )
    clearTokenBeingAddedAddress()
  }

  fun clearTokenBeingAddedAddress() {
    _tokenAddress.value = ""
  }

  fun deleteToken(address: String) {
    viewModelScope.launch { deleteToken.invoke(address) }
  }

  private fun isBscAddressValid(address: String) = address.matches(Regex("^0x\\S{40}\$"))
}

object InvalidAddressException : IllegalArgumentException()
