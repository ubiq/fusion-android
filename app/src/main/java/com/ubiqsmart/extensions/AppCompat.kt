package com.ubiqsmart.extensions

import android.arch.lifecycle.ViewModelProviders
import android.support.annotation.IdRes
import android.support.v4.app.FragmentActivity
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.ubiqsmart.app.ui.base.BaseViewModel
import com.ubiqsmart.app.viewmodel.ViewModelFactory

inline fun AppCompatActivity.setupActionBar(toolbar: Toolbar, action: ActionBar.() -> Unit) {
  setSupportActionBar(toolbar)
  supportActionBar?.run(action)
}

inline fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
  val toolbar: Toolbar = findViewById(toolbarId)
  setupActionBar(toolbar, action)
}

fun <T : BaseViewModel> FragmentActivity.obtainViewModel(viewModelClass: Class<T>): T =
    ViewModelProviders.of(this, ViewModelFactory.getInstance(application)).get(viewModelClass)