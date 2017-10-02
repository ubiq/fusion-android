package com.ubiqsmart.domain.repositories

import com.ubiqsmart.datasource.db.AppStateDbDataSource
import com.ubiqsmart.domain.models.AppState
import io.reactivex.Single

class AppStateRepository(private val dataSource: AppStateDbDataSource) {

  fun getAppState(): Single<AppState> {
    return dataSource.getAppState().map { it.toDomain() }.onErrorReturnItem(AppState.default())
  }

  fun saveAppState(appState: AppState): Single<AppState> {
    return Single.fromCallable({
      dataSource.saveAppState(appState.toDataSource())
      appState
    })
  }

}