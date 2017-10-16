package com.ubiqsmart.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class NotificationJobCreator : JobCreator {

  override fun create(tag: String): Job? {
    return when (tag) {
      IncomingTransactionsJob.TAG -> IncomingTransactionsJob()
      else -> null
    }
  }

}