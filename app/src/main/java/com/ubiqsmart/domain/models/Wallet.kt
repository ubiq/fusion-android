package com.ubiqsmart.domain.models

data class Wallet(
    val address: String,
    val crypto: Crypto,
    val id: String,
    val version: Int
)

data class Crypto(
    val cipher: String,
    val cipherText: String,
    val cipherParams: CipherParams,
    val kdf: String,
    val kdfparams: ScryptKdfParams,
    val mac: String
)

data class CipherParams(
    val iv: String
)

data class ScryptKdfParams(
    val dklen: Int,
    val n: Int,
    val p: Int,
    val r: Int,
    val salt: String
)