package com.moonwatch.core.android.delegate

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import kotlin.reflect.KProperty

class MutableSavedState<T>(
    initialValue: T,
    private val savedStateHandle: SavedStateHandle,
    private val savedStateKey: String? = null
) {
  private val state: MutableState<T> = mutableStateOf(initialValue)

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    state.value = value
    if (value != null) savedStateHandle[savedStateKey ?: property.name] = value
  }

  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): MutableSavedState<T> {
    savedStateHandle.get<T>(savedStateKey ?: property.name)?.let(state::value::set)
    return this
  }
}

inline fun <reified T> SavedStateHandle.mutableStateOf(
    initialValue: T,
    savedStateKey: String? = null
): MutableSavedState<T> = MutableSavedState(initialValue, this, savedStateKey)
