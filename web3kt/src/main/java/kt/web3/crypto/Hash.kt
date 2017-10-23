package kt.web3.crypto

import org.bouncycastle.jcajce.provider.digest.Keccak

/**
 * Cryptographic hash functions.
 */
object Hash {

  /**
   * Keccak-256 hash function.
   *
   * @param input binary encoded input data
   * @param offset of start of data
   * @param length of data
   *
   * @return hash value
   */
  @JvmOverloads
  fun sha3(input: ByteArray, offset: Int = 0, length: Int = input.size): ByteArray {
    val kecc = Keccak.Digest256()
    kecc.update(input, offset, length)
    return kecc.digest()
  }

  fun sha3(hexInput: String): String {
    val bytes = Numeric.hexStringToByteArray(hexInput)
    val result = sha3(bytes)
    return Numeric.toHexString(result)
  }

}