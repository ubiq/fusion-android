package com.ubiqsmart.di

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

  val networkingModule = Kodein.Module {

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
      val retrofit: Retrofit = instance()
      retrofit.create(CryptoCompareApi::class.java)
    }

  }

  val toDeprecateModule = Kodein.Module {

    bind<ExchangeCalculator>() with singleton { ExchangeCalculator.getInstance() }

    bind<WalletStorage>() with scopedSingleton(androidContextScope) { WalletStorage.getInstance(it, instance(), instance()) }

    bind<NotificationLauncher>() with scopedSingleton(androidContextScope) { NotificationLauncher.getInstance(it, instance()) }

    bind<AddressNameConverter>() with scopedSingleton(androidContextScope) { AddressNameConverter.getInstance(it) }

    bind<EtherscanAPI>() with singleton { EtherscanAPI.getInstance() }
  }

}
