package com.ubiqsmart.domain.interactors.splash

import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.domain.repositories.AppStateRepository
import com.ubiqsmart.extensions.merge
import io.reactivex.Single

class SaveAppStateInteractor(private val repository: AppStateRepository) {

  fun execute(appState: AppState): Single<AppState> {
    return repository.getAppState().flatMap { repository.saveAppState(appState.merge(it)) }
  }

}
