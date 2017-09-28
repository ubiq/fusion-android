package com.ubiqsmart.domain.repositories

import com.ubiqsmart.domain.models.AppState
import io.reactivex.Single

class AppStateRepository {

  fun getAppState(): Single<AppState> {
    return Single.just(AppState())
  }

  fun saveAppState(appState: AppState): Single<AppState> {
    return Single.just(appState)
  }

}