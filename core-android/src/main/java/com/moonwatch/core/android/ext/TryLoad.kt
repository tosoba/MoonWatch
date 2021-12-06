package com.moonwatch.core.android.ext

import com.moonwatch.core.android.model.FailedFirst
import com.moonwatch.core.android.model.Loadable
import com.moonwatch.core.android.model.LoadingFirst
import com.moonwatch.core.android.model.Ready
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

fun <T, R> Flow<T>.transformLatestTryingTo(
    load: suspend (T) -> R,
): Flow<Loadable<R>> = transformLatest {
  emit(LoadingFirst)
  try {
    emit(Ready(load(it)))
  } catch (ex: Exception) {
    emit(FailedFirst(ex))
  }
}
