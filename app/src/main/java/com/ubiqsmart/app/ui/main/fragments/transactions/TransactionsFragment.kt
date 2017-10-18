package com.ubiqsmart.app.ui.main.fragments.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseFragment

class TransactionsFragment : BaseFragment() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_transaction, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
  }
}