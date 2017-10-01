package com.ubiqsmart.app.ui.base

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.github.salomonbrys.kodein.KodeinInjector
import com.ubiqsmart.di.ViewModelInjector
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel(app: Application) : AndroidViewModel(app), ViewModelInjector {

  final override val injector: KodeinInjector = KodeinInjector()

  protected val disposables: CompositeDisposable = CompositeDisposable()

  open fun onViewCreated() {
    initializeInjector()
  }

  open fun onDestroyView() {
    disposables.clear()
    destroyInjector()
  }

}