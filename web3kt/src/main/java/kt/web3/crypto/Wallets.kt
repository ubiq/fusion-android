package kt.web3.crypto

import org.bouncycastle.crypto.generators.SCrypt
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

interface Wallets {

  @Throws(CipherException::class)
  fun generateDerivedScryptKey(password: ByteArray, salt: ByteArray, n: Int, r: Int, p: Int, dkLen: Int): ByteArray {
    return SCrypt.generate(password, salt, n, r, p, dkLen)
  }

  fun generateMac(derivedKey: ByteArray, cipherText: ByteArray): ByteArray {
    val result = ByteArray(16 + cipherText.size)

    System.arraycopy(derivedKey, 16, result, 0, 16)
    System.arraycopy(cipherText, 0, result, 16, cipherText.size)

    return Hash.sha3(result)
  }

  @Throws(CipherException::class)
  fun performCipherOperation(mode: Int, iv: ByteArray, encryptKey: ByteArray, text: ByteArray): ByteArray {
    try {
      val ivParameterSpec = IvParameterSpec(iv)
      val cipher = Cipher.getInstance("AES/CTR/NoPadding")

      val secretKeySpec = SecretKeySpec(encryptKey, "AES")
      cipher.init(mode, secretKeySpec, ivParameterSpec)

      return cipher.doFinal(text)
    } catch (e: Exception) {
      throw CipherException("Error performing cipher operation", e)
    }
  }

}