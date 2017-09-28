package com.ubiqsmart.domain.models

import com.ubiqsmart.datasource.models.AppStateEntity

data class AppState(
    val firstRun: Boolean = true
) {

  fun toDataSource(): AppStateEntity = AppStateEntity(firstRun = firstRun)

}