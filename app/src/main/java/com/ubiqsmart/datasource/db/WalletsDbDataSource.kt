package com.ubiqsmart.datasource.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.ubiqsmart.datasource.models.WalletEntity
import io.reactivex.Single

@Dao
interface WalletsDbDataSource {

  @Query("SELECT * FROM wallets")
  fun getWallets(): Single<List<WalletEntity>>

}