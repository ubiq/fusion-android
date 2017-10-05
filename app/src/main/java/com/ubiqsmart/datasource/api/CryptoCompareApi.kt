package com.ubiqsmart.datasource.api

import com.ubiqsmart.datasource.models.PriceExchangeEntity
import io.reactivex.Single
import retrofit2.http.GET

interface CryptoCompareApi {

  @GET("price?fsym=UBQ&tsyms=BTC,USD,EUR,GPB,AUD,RUB,CHF,CAD,JPY")
  fun getPriceChart(): Single<PriceExchangeEntity>

}
