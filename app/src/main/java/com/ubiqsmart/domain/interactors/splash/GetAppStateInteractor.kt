package com.ubiqsmart.domain.interactors.splash

import com.ubiqsmart.domain.models.AppState
import com.ubiqsmart.domain.repositories.AppStateRepository
import io.reactivex.Single

class GetAppStateInteractor(private val repository: AppStateRepository) {

  fun getAppState(): Single<AppState> {
    return repository.getAppState()
  }

}