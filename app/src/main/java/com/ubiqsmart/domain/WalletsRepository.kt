package com.ubiqsmart.domain

import com.ubiqsmart.datasource.db.WalletsDbDataSource
import com.ubiqsmart.domain.models.Wallet
import io.reactivex.Single

class WalletsRepository(private val dataSource: WalletsDbDataSource) {

  fun getWallets(): Single<List<Wallet>> {
    TODO()
  }

  fun getWallet(): Single<Wallet> {
    TODO()
  }

  fun saveWallet(wallet: Wallet): Single<Wallet> {
    TODO()
  }

  fun removeWallet(wallet: Wallet): Single<Wallet> {
    TODO()
  }

}