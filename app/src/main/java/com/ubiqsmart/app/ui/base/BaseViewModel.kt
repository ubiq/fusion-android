package com.ubiqsmart.app.ui.base

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.ubiqsmart.di.ViewModelInjector
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel(app: Application) : AndroidViewModel(app), ViewModelInjector {

  protected val disposables: CompositeDisposable = CompositeDisposable()

  open protected fun onViewCreated() {
  }

  open protected fun onDestroyView() {
    disposables.clear()
  }

}