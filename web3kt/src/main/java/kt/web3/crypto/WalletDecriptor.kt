package kt.web3.crypto

import java.util.*
import javax.crypto.Cipher

object WalletDecriptor : Wallets {

  private const val CIPHER = "aes-128-ctr"
  private const val SCRYPT = "scrypt"

  private const val CURRENT_VERSION = 3

  @Throws(CipherException::class)
  fun decrypt(wallet: Wallet, password: String = ""): ECKeyPair {
    validate(wallet)

    val crypto = wallet.crypto

    val mac = Numeric.hexStringToByteArray(crypto.mac)
    val iv = Numeric.hexStringToByteArray(crypto.cipherparams.iv)
    val cipherText = Numeric.hexStringToByteArray(crypto.ciphertext)

    val derivedKey: ByteArray

    val kdfParams = crypto.kdfparams
    if (kdfParams is Wallet.ScryptKdfParams) {
      val scryptKdfParams = crypto.kdfparams as Wallet.ScryptKdfParams
      val dklen = scryptKdfParams.dklen
      val n = scryptKdfParams.n
      val p = scryptKdfParams.p
      val r = scryptKdfParams.r
      val salt = Numeric.hexStringToByteArray(scryptKdfParams.salt)
      derivedKey = generateDerivedScryptKey(password.toByteArray(), salt, n, r, p, dklen)
    } else {
      throw CipherException("Unable to deserialize params: " + crypto.kdf)
    }

    val derivedMac = generateMac(derivedKey, cipherText)

    if (!Arrays.equals(derivedMac, mac)) {
      throw CipherException("Invalid password provided")
    }

    val encryptKey = Arrays.copyOfRange(derivedKey, 0, 16)
    val privateKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, cipherText)

    return ECKeyPair.create(privateKey)
  }

  @Throws(CipherException::class)
  private fun validate(wallet: Wallet) {
    val crypto = wallet.crypto

    if (wallet.version != CURRENT_VERSION) {
      throw CipherException("Wallet version is not supported")
    }

    if (crypto.cipher != CIPHER) {
      throw CipherException("Wallet cipher is not supported")
    }

    if (crypto.kdf != SCRYPT) {
      throw CipherException("KDF type is not supported")
    }
  }

}