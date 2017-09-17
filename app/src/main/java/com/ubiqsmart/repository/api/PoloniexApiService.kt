package com.ubiqsmart.repository.api

import io.reactivex.Observable
import retrofit2.http.GET

interface PoloniexApiService {

    @GET("public?command=returnChartData&currencyPair={pair}&start={startTime}&&end=9999999999&period={period}")
    fun getPriceChart(): Observable<Int>

}
