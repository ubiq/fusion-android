package com.ubiqsmart.domain.models

import com.ubiqsmart.datasource.models.AppStateEntity

data class AppState(
    var firstRun: Boolean? = false
) {

  fun toDataSource(): AppStateEntity = AppStateEntity(firstRun = firstRun?: false)

  companion object {
    fun default(): AppState = AppState(true)
  }

}