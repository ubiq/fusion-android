package com.ubiqsmart.di

import android.app.Application
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidContextScope
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.ubiqsmart.repository.api.CryptoCompareApi
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.services.NotificationLauncher
import com.ubiqsmart.utils.AddressNameConverter
import com.ubiqsmart.utils.ExchangeCalculator
import com.ubiqsmart.utils.WalletStorage
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

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

  fun Deprecated(app: Application) = Kodein.Module {

    bind<ExchangeCalculator>() with singleton { ExchangeCalculator.getInstance() }

    bind<EtherscanAPI>() with singleton { EtherscanAPI.getInstance() }

    bind<WalletStorage>() with singleton { WalletStorage.getInstance(app, instance()) }

    bind<AddressNameConverter>() with singleton { AddressNameConverter.getInstance(app) }

    bind<NotificationLauncher>() with singleton { NotificationLauncher.getInstance(app, instance()) }

  }

}
