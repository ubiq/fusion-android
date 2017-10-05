package com.ubiqsmart.datasource.models

import com.ubiqsmart.domain.models.PriceExchange

data class PriceExchangeEntity(
    val btc: String,
    val usd: String,
    val eur: String,
    val aud: String,
    val rub: String,
    val chf: String,
    val cad: String,
    val jpy: String
) {

  fun toDomain(): PriceExchange = PriceExchange(btc, usd, eur, aud, rub, chf, cad, jpy)

}