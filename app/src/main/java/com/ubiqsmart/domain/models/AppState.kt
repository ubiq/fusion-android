package com.ubiqsmart.domain.models

import com.ubiqsmart.datasource.models.AppStateEntity

data class AppState(
    var firstRun: Boolean? = false,
    var currency: String? = "EUR"
) {

  fun toDataSource(): AppStateEntity = AppStateEntity(firstRun = firstRun ?: false, currency = currency ?: "EUR")

  companion object {
    fun default(): AppState = AppState(true)
  }

}