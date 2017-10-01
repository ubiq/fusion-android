package com.ubiqsmart.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

object Schedulers {

  fun io(): Scheduler {
    return io.reactivex.schedulers.Schedulers.io()
  }

  fun computation(): Scheduler {
    return io.reactivex.schedulers.Schedulers.computation()
  }

  fun ui(): Scheduler {
    return AndroidSchedulers.mainThread()
  }

  fun direct(): Scheduler {
    return io.reactivex.schedulers.Schedulers.from({ it.run() })
  }

}