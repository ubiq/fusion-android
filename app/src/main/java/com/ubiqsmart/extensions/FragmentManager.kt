package com.ubiqsmart.extensions

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction

inline fun FragmentManager.doTransaction(block: FragmentTransaction.() -> Unit) {
  val fragmentTransaction = beginTransaction()
  fragmentTransaction.block()
  fragmentTransaction.commit()
}