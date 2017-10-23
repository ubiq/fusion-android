package kt.web3.crypto

/**
 * String utility functions.
 */
object Strings {

  fun zeros(n: Int): String {
    return repeat('0', n)
  }

  private fun repeat(value: Char, n: Int): String {
    return String(CharArray(n)).replace("\u0000", value.toString())
  }
}