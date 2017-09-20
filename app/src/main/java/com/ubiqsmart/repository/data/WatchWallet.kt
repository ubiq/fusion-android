package com.ubiqsmart.repository.data

import com.ubiqsmart.interfaces.StorableWallet
import java.io.Serializable

data class WatchWallet(override val pubKey: String) : StorableWallet, Serializable {

    val dateAdded: Long = System.currentTimeMillis()

}
