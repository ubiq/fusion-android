package com.ubiqsmart

import android.support.multidex.MultiDexApplication
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.lazy
import com.ubiqsmart.di.networkingModule
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : MultiDexApplication(), KodeinAware {

    override val kodein by Kodein.lazy {
        import(androidModule)
        import(networkingModule)
    }

    override fun onCreate() {
        super.onCreate()
        onCreateCalligraphy()
    }

    private fun onCreateCalligraphy() {
        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath(getString(R.string.regular_font))
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        )
    }

}