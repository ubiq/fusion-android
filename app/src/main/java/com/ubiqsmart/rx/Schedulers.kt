package com.ubiqsmart.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

object Schedulers {

  fun io(): Scheduler {
    return Schedulers.io()
  }

  fun computation(): Scheduler {
    return Schedulers.computation()
  }

  fun ui(): Scheduler {
    return AndroidSchedulers.mainThread()
  }

  fun direct(): Scheduler {
    return Schedulers.direct()
  }

}