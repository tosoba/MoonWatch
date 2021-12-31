package com.moonwatch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.moonwatch.core.android.delegate.mutableStateOf
import com.moonwatch.core.android.model.LoadableParcelable
import com.moonwatch.core.android.model.parcelize
import com.moonwatch.core.ext.withLatestFrom
import com.moonwatch.core.model.*
import com.moonwatch.core.usecase.*
import com.moonwatch.model.TokenAlertWithValue
import com.moonwatch.model.TokenWithValue
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MainViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val getTokenWithValue: GetTokenWithValue,
    private val saveTokenWithValue: SaveTokenWithValue,
    private val deleteToken: DeleteToken,
    private val addAlert: AddAlert,
    private val updateAlert: UpdateAlert,
    private val deleteAlert: DeleteAlert,
    private val toggleAlertActive: ToggleAlertActive,
    getAlertsFlow: GetAlertsFlow,
    getTokensFlow: GetTokensFlow,
) : ViewModel() {
  private val _tokenAddress = MutableStateFlow("")
  val tokenAddress: Flow<String>
    get() = _tokenAddress

  var tokenWithValueBeingAdded: LoadableParcelable<TokenWithValue> by
      savedStateHandle.mutableStateOf(LoadableParcelable(Empty))
  var tokenWithValueBeingViewed: TokenWithValue? by savedStateHandle.mutableStateOf(null)
  var tokenAlertWithValueBeingViewed: TokenAlertWithValue? by savedStateHandle.mutableStateOf(null)

  val alertsFlow: Flow<PagingData<TokenAlertWithValue>> =
      getAlertsFlow(pageSize = 20).map { it.map(::TokenAlertWithValue) }.distinctUntilChanged()

  val tokensFlow: Flow<Loadable<PagingData<TokenWithValue>>> =
      getTokensFlow(pageSize = 20)
          .map { it.map(::TokenWithValue) }
          .distinctUntilChanged()
          .map(PagingData<TokenWithValue>::loadable)
          .onStart { emit(LoadingFirst) }

  private val _toggleRetryLoadingToken = MutableSharedFlow<Unit>()

  private val tokenAddressFlow: Flow<String> =
      merge(
          tokenAddress.drop(1).debounce(500L).distinctUntilChanged(),
          _toggleRetryLoadingToken.debounce(500L).withLatestFrom(tokenAddress) { _, address ->
            address
          },
      )

  private val tokenWithValueBeingAddedFlow: Flow<LoadableParcelable<TokenWithValue>>
    get() =
        getTokenWithValue(addresses = tokenAddressFlow)
            .map { loadable -> loadable.map(::TokenWithValue).parcelize() }
            .onEach(::tokenWithValueBeingAdded::set)

  init {
    tokenWithValueBeingAddedFlow.launchIn(viewModelScope)
  }

  suspend fun retryLoadingToken() {
    _toggleRetryLoadingToken.emit(Unit)
  }

  suspend fun saveTokenCurrentlyBeingAdded() {
    when (val tokenWithValue = tokenWithValueBeingAdded.loadable) {
      is Ready<TokenWithValue> -> {
        saveTokenWithValue(
            token = tokenWithValue.value.token,
            value = tokenWithValue.value.value,
        )
        clearTokenBeingAddedAddress()
      }
      else -> throw IllegalStateException()
    }
  }

  fun deleteToken(address: String) {
    viewModelScope.launch { deleteToken.invoke(address) }
  }

  fun addAlert(
      address: String,
      createdValueId: Long,
      sellPriceTargetUsd: BigDecimal?,
      buyPriceTargetUsd: BigDecimal?
  ) {
    viewModelScope.launch {
      addAlert.invoke(
          address,
          createdValueId,
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

  fun setTokenAddress(address: String) {
    _tokenAddress.value = address
  }

  fun clearTokenBeingAddedAddress() {
    _tokenAddress.value = ""
  }
}
