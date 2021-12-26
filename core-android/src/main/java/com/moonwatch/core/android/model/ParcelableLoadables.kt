package com.moonwatch.core.android.model

import android.os.Parcel
import android.os.Parcelable
import com.moonwatch.core.model.*

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

inline fun <reified T : Parcelable> Loadable<T>.parcelize() = LoadableParcelable(this)
