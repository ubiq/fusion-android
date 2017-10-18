package com.ubiqsmart.datasource.models

data class WalletEntity(
    val address: String,
    val crypto: CryptoEntity,
    val id: String,
    val version: Int
)

data class CryptoEntity(
    val cipher: String,
    val cipherText: String,
    val cipherParams: CipherParamsEntity,
    val kdf: String,
    val kdfparams: ScryptKdfParamsEntity,
    val mac: String
)

data class CipherParamsEntity(
    val iv: String
)

data class ScryptKdfParamsEntity(
    val dklen: Int,
    val n: Int,
    val p: Int,
    val r: Int,
    val salt: String
)