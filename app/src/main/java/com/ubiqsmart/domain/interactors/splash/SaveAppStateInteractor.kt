package com.ubiqsmart.domain.interactors.splash

import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.domain.repositories.AppStateRepository
import io.reactivex.Single

class SaveAppStateInteractor(private val repository: AppStateRepository) {

  fun saveAppState(appState: AppState): Single<AppState> {
    return repository.saveAppState(appState)
  }

}
