package com.ubiqsmart.di

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.androidContextScope
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.ubiqsmart.App
import com.ubiqsmart.app.db.AppDatabase
import com.ubiqsmart.app.services.NotificationLauncher
import com.ubiqsmart.app.utils.AddressNameConverter
import com.ubiqsmart.app.utils.ExchangeCalculator
import com.ubiqsmart.app.utils.WalletStorage
import com.ubiqsmart.datasource.api.CryptoCompareApi
import com.ubiqsmart.datasource.api.EtherscanAPI
import com.ubiqsmart.datasource.db.AppStateDbDataSource
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

interface ViewModelInjector : AndroidInjector<AndroidViewModel, AndroidScope<AndroidViewModel>> {

  override val kodeinScope: AndroidScope<AndroidViewModel> get() = ViewModelScope

  override fun initializeInjector() {
    val viewModelModule = Kodein.Module {
      Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@ViewModelInjector)
      import(provideOverridingModule(), allowOverride = true)
    }

    val appKodein = kodeinComponent.getApplication<App>().kodein

    val kodein = Kodein {
      extend(appKodein, allowOverride = true)
      import(viewModelModule, allowOverride = true)
    }

    injector.inject(kodein)
  }

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

    bind< AppStateDbDataSource>() with eagerSingleton { instance<AppDatabase>().appStateDbDataSource() }
  }

  fun Deprecated(app: Application) = Kodein.Module {

    bind<ExchangeCalculator>() with singleton { ExchangeCalculator.getInstance() }

    bind<EtherscanAPI>() with singleton { EtherscanAPI.getInstance() }

    bind<WalletStorage>() with singleton { WalletStorage.getInstance(app, instance()) }

    bind<AddressNameConverter>() with singleton { AddressNameConverter.getInstance(app) }

    bind<NotificationLauncher>() with singleton { NotificationLauncher.getInstance(app, instance()) }

  }

}
