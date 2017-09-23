package com.ubiqsmart

import android.support.multidex.MultiDexApplication
import com.chibatching.kotpref.Kotpref
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.lazy
import com.ubiqsmart.di.Modules
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : MultiDexApplication(), KodeinAware {

  override val kodein by Kodein.lazy {
    import(androidModule)
    import(Modules.networkingModule)
    import(Modules.toDeprecateModule)
  }

  override fun onCreate() {
    super.onCreate()
    onSetupKotPref()
    onSetupCalligraphy()
    onSetupCrashlytics()
  }

  private fun onSetupKotPref() {
    Kotpref.init(this)
  }

  private fun onSetupCalligraphy() {
    CalligraphyConfig.initDefault(
        CalligraphyConfig.Builder()
            .setDefaultFontPath(getString(R.string.regular_font))
            .setFontAttrId(R.attr.fontPath)
            .build()
    )
  }

  private fun onSetupCrashlytics() {
  }

}