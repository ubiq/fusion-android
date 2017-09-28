package com.ubiqsmart.datasource.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ubiqsmart.datasource.models.AppStateEntity

@Dao
interface AppStateDbDataSource {

  @Query("SELECT * FROM app_state WHERE id = 1")
  fun getAppState(): List<AppStateEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveAppState(appState: AppStateEntity)

}