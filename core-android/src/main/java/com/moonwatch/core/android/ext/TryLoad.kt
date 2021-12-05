package com.moonwatch.core.android.ext

import com.moonwatch.core.android.model.FailedFirst
import com.moonwatch.core.android.model.Loadable
import com.moonwatch.core.android.model.LoadingFirst
import com.moonwatch.core.android.model.Ready
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest

fun <T> tryTo(load: suspend () -> T): Flow<Loadable<T>> = flow {
  emit(LoadingFirst)
  try {
    emit(Ready(load()))
  } catch (ex: Exception) {
    emit(FailedFirst(ex))
  }
}

fun <T, R> Flow<T>.transformLatestTryingTo(
    load: suspend (T) -> R,
): Flow<Loadable<R>> = transformLatest { tryTo { load(it) } }
