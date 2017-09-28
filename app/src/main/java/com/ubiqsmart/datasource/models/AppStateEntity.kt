package com.ubiqsmart.datasource.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ubiqsmart.domain.models.AppState

@Entity(tableName = "app_state")
class AppStateEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
    @ColumnInfo(name = "first_run") val firstRun: Boolean
) {

  fun toDomain(): AppState = AppState(firstRun)

}
