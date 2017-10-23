package com.ubiqsmart.app.utils

import android.content.Context
import com.ubiqsmart.domain.models.WalletEntry

import java.io.*
import java.util.*

@Deprecated("Replace with room entities and repository")
class AddressNameConverter private constructor(context: Context) {

  private var mapdb: HashMap<String, String>? = null

  private val context: Context = context.applicationContext

  val asAddressBook: List<WalletEntry>
    get() {
      val erg = ArrayList<WalletEntry>()
      for ((key, value) in mapdb!!) {
        erg.add(WalletEntry(value, key))
      }
      Collections.sort(erg)
      return erg
    }

  init {
    try {
      load()
    } catch (e: Exception) {
      mapdb = HashMap()
    }

  }

  @Synchronized
  fun put(address: String, name: String?) {
    if (name == null || name.isEmpty()) {
      mapdb!!.remove(address)
    } else {
      mapdb!!.put(address, if (name.length > 22) name.substring(0, 22) else name)
    }
    save()
  }

  operator fun get(address: String): String? {
    return mapdb!![address]
  }

  operator fun contains(address: String): Boolean {
    return mapdb!!.containsKey(address)
  }

  @Synchronized
  private fun save() {
    try {
      val fout = FileOutputStream(File(context.filesDir, "namedb.dat"))
      val oos = ObjectOutputStream(fout)
      oos.writeObject(mapdb)
      oos.close()
      fout.close()
    } catch (ignored: Exception) {
    }

  }

  @Synchronized
  @Throws(IOException::class, ClassNotFoundException::class)
  private fun load() {
    val fout = FileInputStream(File(context.filesDir, "namedb.dat"))
    val oos = ObjectInputStream(BufferedInputStream(fout))
    mapdb = oos.readObject() as HashMap<String, String>
    oos.close()
    fout.close()
  }

  companion object {

    private var instance: AddressNameConverter? = null

    fun getInstance(context: Context): AddressNameConverter {
      return instance ?: AddressNameConverter(context).also { instance = it }
    }
  }

}
