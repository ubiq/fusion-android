package com.ubiqsmart.di

import android.app.Application
import android.arch.lifecycle.ViewModel
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidContextScope
import com.github.salomonbrys.kodein.bindings.Scope
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.ubiqsmart.app.db.AppDatabase
import com.ubiqsmart.app.services.NotificationLauncher
import com.ubiqsmart.app.utils.AddressNameConverter
import com.ubiqsmart.app.utils.ExchangeCalculator
import com.ubiqsmart.app.utils.WalletStorage
import com.ubiqsmart.datasource.api.CryptoCompareApi
import com.ubiqsmart.datasource.api.EtherscanAPI
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

interface IViewModelScope<in T> : Scope<T> {

  fun removeFromScope(viewModel: T): ScopeRegistry?
}

object ViewModelScope : IViewModelScope<ViewModel> {

  private val scopes = WeakHashMap<ViewModel, ScopeRegistry>()

  override fun getRegistry(context: ViewModel): ScopeRegistry = synchronized(scopes) { scopes.getOrPut(context) { ScopeRegistry() } }

  override fun removeFromScope(viewModel: ViewModel): ScopeRegistry? = scopes.remove(viewModel)

}

private fun _inject(injector: KodeinInjected, componentModule: Kodein.Module, superKodein: Kodein) {
  val kodein = Kodein {
    extend(superKodein, allowOverride = true)
    import(componentModule, allowOverride = true)
  }

  injector.inject(kodein)
}

interface ViewModelInjector<T, out S : IViewModelScope<T>> : KodeinInjected {

  @Suppress("UNCHECKED_CAST")
  val kodeinComponent: T
    get() = this as T

  val kodeinScope: S

  fun initializeInjector() {
    _inject(this, fragmentModule, (parent as KodeinInjected).injector.kodein().value)
  }

  fun destroyInjector() = kodeinScope.removeFromScope(kodeinComponent)

}

object Modules {

  val Networking = Kodein.Module {

    // Parsing
    bind<Moshi>() with singleton {
      Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
    }

    // Networking

    bind<Cache>() with scopedSingleton(androidContextScope) {
      val dir = it.applicationContext.cacheDir
      Cache(dir, 50)
    }

    bind<OkHttpClient>() with singleton {
      OkHttpClient.Builder()
          .cache(instance())
          .build()
    }

    bind<Retrofit>() with singleton {
      Retrofit.Builder()
          .client(instance())
          .addConverterFactory(MoshiConverterFactory.create(instance()))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build()
    }

    bind<CryptoCompareApi>() with singleton {
      instance<Retrofit>().create(CryptoCompareApi::class.java)
    }

  }

  fun DataSource(app: Application) = Kodein.Module {

    bind<AppDatabase>() with eagerSingleton { AppDatabase.get(app) }

  }

  fun Deprecated(app: Application) = Kodein.Module {

    bind<ExchangeCalculator>() with singleton { ExchangeCalculator.getInstance() }

    bind<EtherscanAPI>() with singleton { EtherscanAPI.getInstance() }

    bind<WalletStorage>() with singleton { WalletStorage.getInstance(app, instance()) }

    bind<AddressNameConverter>() with singleton { AddressNameConverter.getInstance(app) }

    bind<NotificationLauncher>() with singleton { NotificationLauncher.getInstance(app, instance()) }

  }

}
