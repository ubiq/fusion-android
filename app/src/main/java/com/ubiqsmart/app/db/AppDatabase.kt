package com.ubiqsmart.app.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.ubiqsmart.datasource.db.AppStateDbDataSource
import com.ubiqsmart.datasource.models.AppStateEntity

@Database(entities = arrayOf(AppStateEntity::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

  abstract fun appStateDbDataSource(): AppStateDbDataSource

  companion object {

    private var instance: AppDatabase? = null

    private const val DATABASE_NAME = "fusion-android-db"

    fun get(context: Context): AppDatabase {
      return instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME).build().also { instance = it }
    }

  }

}