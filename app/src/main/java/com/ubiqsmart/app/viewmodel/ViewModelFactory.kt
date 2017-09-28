package com.ubiqsmart.app.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.github.salomonbrys.kodein.android.appKodein
import com.ubiqsmart.app.ui.splash.SplashViewModel

class ViewModelFactory private constructor(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

  override fun <T : ViewModel> create(modelClass: Class<T>) =
      with(modelClass) {
        when {
          isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(application, application.appKodein.invoke())
          else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
      } as T

  companion object {

    @SuppressLint("StaticFieldLeak")
    @Volatile private var INSTANCE: ViewModelFactory? = null

    fun getInstance(application: Application) =
        INSTANCE ?: synchronized(ViewModelFactory::class.java) {
          INSTANCE ?: ViewModelFactory(application).also { INSTANCE = it }
        }
  }
}
