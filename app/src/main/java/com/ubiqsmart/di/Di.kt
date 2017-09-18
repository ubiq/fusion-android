package com.ubiqsmart.di

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.ubiqsmart.repository.api.CryptoCompareApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

val networkingModule = Kodein.Module {

    // Parsing
    bind<Moshi>() with singleton {
        Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }

    // Networking

//    bind<Cache>() with scopedSingleton(androidContextScope) {
//        Cache()
//    }

    bind<OkHttpClient>() with singleton { OkHttpClient.Builder().build() }

    bind<Retrofit>() with singleton {
        Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(instance()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    bind<CryptoCompareApi>() with singleton { Retrofit.Builder().build().create(CryptoCompareApi::class.java) }

}

val datasourceModule = Kodein.Module {

}