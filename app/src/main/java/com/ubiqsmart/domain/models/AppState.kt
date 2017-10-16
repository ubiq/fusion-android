package com.ubiqsmart.domain.models

import com.ubiqsmart.datasource.models.AppStateEntity

data class AppState(
    var firstRun: Boolean? = false,
    var currency: String? = "EUR",
    var incomingTxSyncFreq: Int? = 0
) {

  fun toDataSource(): AppStateEntity = AppStateEntity(
      firstRun = firstRun ?: false,
      currency = currency ?: "EUR",
      incomingTxSyncFreq = incomingTxSyncFreq ?: 0
  )

  companion object {
    fun default(): AppState = AppState(true)
  }

}