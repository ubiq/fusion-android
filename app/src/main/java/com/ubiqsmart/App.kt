package com.ubiqsmart

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.autoAndroidModule
import com.github.salomonbrys.kodein.lazy
import com.ubiqsmart.di.Modules
import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : MultiDexApplication(), KodeinAware {

  override val kodein by Kodein.lazy {
    import(autoAndroidModule(this@App))
    import(Modules.Deprecated(this@App))
    import(Modules.DataSource(this@App))
    import(Modules.Networking)
  }

  override fun onCreate() {
    super.onCreate()
    onSetupStetho()
    onSetupCalligraphy()
    onSetupCrashlytics()
  }

  private fun onSetupStetho() {
    if (BuildConfig.DEBUG) {
      Stetho.initializeWithDefaults(this)
    }
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
    val kit = Crashlytics.Builder()
        .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
        .build()

    Fabric.with(this, kit)
  }
}