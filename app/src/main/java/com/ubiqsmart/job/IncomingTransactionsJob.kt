package com.ubiqsmart.job

import com.evernote.android.job.Job

class IncomingTransactionsJob : Job() {

  override fun onRunJob(params: Params?): Result {
    return Result.SUCCESS
  }

  companion object {
    const val TAG = "IncomingTransactionsJob"
  }

}