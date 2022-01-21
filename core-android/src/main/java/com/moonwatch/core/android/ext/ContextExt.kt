package com.moonwatch.core.android.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase

inline fun <reified T : RoomDatabase> Context.buildRoom(
    inMemory: Boolean = false,
    name: String = T::class.java.simpleName,
    noinline configure: (RoomDatabase.Builder<T>.() -> RoomDatabase.Builder<T>)? = null
): T {
  val builder =
      if (inMemory) Room.inMemoryDatabaseBuilder(this, T::class.java)
      else Room.databaseBuilder(this, T::class.java, name)
  if (configure != null) builder.configure()
  return builder.build()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
