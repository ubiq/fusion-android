package com.ubiqsmart.repository.data

import com.ubiqsmart.interfaces.StorableWallet
import java.io.Serializable

data class FullWallet(override val pubKey: String, var path: String?) : StorableWallet, Serializable {

  private var dateAdded: Long = System.currentTimeMillis()
}
