package com.ubiqsmart.app.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseActivity
import com.ubiqsmart.extensions.doTransaction
import com.ubiqsmart.extensions.setupActionBar

class SettingsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onSetupActionBar()
    onSetupPreferenceFragment(savedInstanceState)
  }

  private fun onSetupActionBar() {
    val rootView = findViewById<ViewGroup>(R.id.action_bar_root)
    rootView?.apply {
      val view = layoutInflater.inflate(R.layout.activity_settings, rootView, false)
      rootView.addView(view, 0)

      setupActionBar(R.id.toolbar) {
        setDisplayHomeAsUpEnabled(true)
        setHomeButtonEnabled(true)
      }
    }
  }

  private fun onSetupPreferenceFragment(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      supportFragmentManager.doTransaction {
        replace(android.R.id.content, SettingsFragment())
      }
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  override fun onBackPressed() {
    super.onBackPressed()
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out_slide_out_left)
  }

  companion object {

    val REQUEST_CODE = 800

    fun getStartIntent(context: Context) = Intent(context, SettingsActivity::class.java)
  }

}