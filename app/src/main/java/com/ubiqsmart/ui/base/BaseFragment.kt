package com.ubiqsmart.ui.base

import android.support.v4.app.Fragment
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein

abstract class BaseFragment : Fragment(), LazyKodeinAware {

  override val kodein = LazyKodein(appKodein)

}
