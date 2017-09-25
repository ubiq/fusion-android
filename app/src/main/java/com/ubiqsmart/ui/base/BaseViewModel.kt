package com.ubiqsmart.ui.base

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel<out N> : ViewModel() {

  protected abstract val navigator: N

  protected val compositeDisposable: CompositeDisposable = CompositeDisposable()

  protected abstract fun onViewCreated()

  protected abstract fun onDestroyView()

}