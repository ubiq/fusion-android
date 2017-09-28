package com.ubiqsmart.domain.models

import com.ubiqsmart.app.interfaces.StorableWallet
import java.io.Serializable

data class WatchWallet(override val pubKey: String) : StorableWallet, Serializable {

  val dateAdded: Long = System.currentTimeMillis()

}
