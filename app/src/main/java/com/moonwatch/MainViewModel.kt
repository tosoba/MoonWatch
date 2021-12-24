package com.moonwatch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.moonwatch.core.android.model.*
import com.moonwatch.core.ext.withLatestFrom
import com.moonwatch.core.usecase.*
import com.moonwatch.exception.InvalidAddressException
import com.moonwatch.model.TokenAlertWithValue
import com.moonwatch.model.TokenWithValue
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val getTokenWithValue: GetTokenWithValue,
    private val saveTokenWithValue: SaveTokenWithValue,
    private val deleteToken: DeleteToken,
    private val addAlert: AddAlert,
    private val updateAlert: UpdateAlert,
    private val deleteAlert: DeleteAlert,
    private val toggleAlertActive: ToggleAlertActive,
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

  private val _tokenAlertWithValueBeingViewed = mutableStateOf<TokenAlertWithValue?>(null)
  val tokenAlertWithValueBeingViewed: State<TokenAlertWithValue?>
    get() = _tokenAlertWithValueBeingViewed

  private val _toggleRetryLoadingToken = MutableSharedFlow<Unit>()

  val alertsFlow: Flow<PagingData<TokenAlertWithValue>>
    get() =
        getAlertsFlow(pageSize = 20).map { it.map(::TokenAlertWithValue) }.distinctUntilChanged()

  val tokensFlow: Flow<PagingData<TokenWithValue>>
    get() = getTokensFlow(pageSize = 20).map { it.map(::TokenWithValue) }.distinctUntilChanged()

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

  fun setTokenAlertWithValueBeingViewed(tokenAlertWithValue: TokenAlertWithValue) {
    _tokenAlertWithValueBeingViewed.value = tokenAlertWithValue
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

  fun addAlert(address: String, sellPriceTargetUsd: BigDecimal?, buyPriceTargetUsd: BigDecimal?) {
    viewModelScope.launch {
      addAlert.invoke(
          address,
          sellPriceTargetUsd = sellPriceTargetUsd,
          buyPriceTargetUsd = buyPriceTargetUsd,
      )
    }
  }

  fun editAlert(id: Long, sellPriceTargetUsd: BigDecimal?, buyPriceTargetUsd: BigDecimal?) {
    viewModelScope.launch {
      updateAlert.invoke(
          id,
          sellPriceTargetUsd = sellPriceTargetUsd,
          buyPriceTargetUsd = buyPriceTargetUsd,
      )
    }
  }

  fun deleteAlert(id: Long) {
    viewModelScope.launch { deleteAlert.invoke(id) }
  }

  fun toggleAlertActive(id: Long) {
    viewModelScope.launch { toggleAlertActive.invoke(id) }
  }

  private fun isBscAddressValid(address: String) = address.matches(Regex("^0x\\S{40}\$"))
}
