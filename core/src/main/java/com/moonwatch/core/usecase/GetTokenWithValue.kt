package com.moonwatch.core.usecase

import com.moonwatch.core.exception.InvalidAddressException
import com.moonwatch.core.model.*
import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@ExperimentalCoroutinesApi
@Reusable
class GetTokenWithValue @Inject constructor(private val repo: ITokenRepo) {
  operator fun invoke(addresses: Flow<String>): Flow<Loadable<ITokenWithValue>> =
      addresses.transformLatest { address ->
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
          val tokenWithValue = withTimeout(10_000L) { invoke(address) }
          emit(Ready(tokenWithValue))
        } catch (ex: Exception) {
          emit(FailedFirst(ex))
        }
      }

  suspend operator fun invoke(address: String): ITokenWithValue =
      repo.getTokenWithValueByAddress(address)

  private fun isBscAddressValid(address: String) = address.matches(Regex("^0x\\S{40}\$"))
}
