package com.moonwatch.core.android.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class LoadableParcelable<out T : Parcelable>(val loadable: Loadable<T>) : Parcelable {
  constructor(
      parcel: Parcel
  ) : this(
      when (val className = parcel.readString()) {
        null -> throw IllegalStateException()
        Empty::class.java.name -> Empty
        LoadingFirst::class.java.name -> LoadingFirst
        LoadingNext::class.java.name -> LoadingNext(parcel.readParcelableByClassName<T>(className))
        FailedFirst::class.java.name -> FailedFirst(parcel.readSerializableThrowable())
        FailedNext::class.java.name -> {
          FailedNext(
              value = parcel.readParcelableByClassName<T>(className),
              error = parcel.readSerializableThrowable(),
          )
        }
        Ready::class.java.name -> Ready(parcel.readParcelableByClassName<T>(className))
        else -> throw IllegalStateException()
      },
  )

  override fun writeToParcel(parcel: Parcel, flag: Int) {
    parcel.writeString(loadable::class.java.name)
    if (loadable is WithValue) parcel.writeParcelable(loadable.value, 0)
    if (loadable is Failed) loadable.error?.let(parcel::writeSerializable)
  }

  override fun describeContents(): Int = 0

  companion object CREATOR : Parcelable.Creator<LoadableParcelable<Parcelable>> {
    override fun createFromParcel(parcel: Parcel): LoadableParcelable<Parcelable> =
        LoadableParcelable(parcel)
    override fun newArray(size: Int): Array<LoadableParcelable<Parcelable>?> = arrayOfNulls(size)
  }
}

private fun <T : Parcelable> Parcel.readParcelableByClassName(className: String): T =
    readParcelable(Class.forName(className).classLoader) ?: throw IllegalStateException()

private fun Parcel.readSerializableThrowable(): Throwable? =
    readSerializable()?.let { it as? Throwable? }

sealed class Loadable<out T> {
  open val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingFirst

  open val copyWithClearedError: Loadable<T>
    get() = Empty

  open fun copyWithError(error: Throwable?): Loadable<T> = FailedFirst(error)

  abstract fun <R> map(block: (T) -> R): Loadable<R>

  inline fun <reified E> isFailedWith(): Boolean = (this as? Failed)?.error is E
}

inline fun <reified T : Parcelable> Loadable<T>.parcelize() = LoadableParcelable(this)

sealed class WithValue<T> : Loadable<T>() {
  abstract val value: T
}

sealed class WithoutValue : Loadable<Nothing>()

@Parcelize
object Empty : WithoutValue(), Parcelable {
  override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

interface LoadingInProgress

@Parcelize
object LoadingFirst : WithoutValue(), LoadingInProgress, Parcelable {
  override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

data class LoadingNext<T>(
    override val value: T,
) : WithValue<T>(), LoadingInProgress {
  override val copyWithLoadingInProgress: Loadable<T>
    get() = this

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

  override fun <R> map(block: (T) -> R): LoadingNext<R> = LoadingNext(block(value))
}

interface Failed {
  val error: Throwable?
}

@Parcelize
data class FailedFirst(override val error: Throwable?) : WithoutValue(), Failed, Parcelable {
  override val copyWithLoadingInProgress: LoadingFirst
    get() = LoadingFirst

  override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

data class FailedNext<T>(
    override val value: T,
    override val error: Throwable?,
) : WithValue<T>(), Failed {
  override val copyWithClearedError: Ready<T>
    get() = Ready(value)

  override val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingNext(value)

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

  override fun <R> map(block: (T) -> R): FailedNext<R> = FailedNext(block(value), error)
}

data class Ready<T>(override val value: T) : WithValue<T>() {
  override val copyWithLoadingInProgress: LoadingNext<T>
    get() = LoadingNext(value)

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

  override fun <R> map(block: (T) -> R): WithValue<R> = Ready(block(value))
}
