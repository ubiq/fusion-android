package com.ubiqsmart.ui.base

import android.support.v7.app.AppCompatActivity
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein

abstract class BaseActivity : AppCompatActivity(), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)
}
