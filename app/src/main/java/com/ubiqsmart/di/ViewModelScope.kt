package com.ubiqsmart.di

import android.arch.lifecycle.AndroidViewModel
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import java.util.*

object ViewModelScope : AndroidScope<AndroidViewModel> {

  private val scopes = WeakHashMap<AndroidViewModel, ScopeRegistry>()

  override fun getRegistry(context: AndroidViewModel): ScopeRegistry = synchronized(scopes) { scopes.getOrPut(context) { ScopeRegistry() } }

  override fun removeFromScope(context: AndroidViewModel): ScopeRegistry? = scopes.remove(context)

}