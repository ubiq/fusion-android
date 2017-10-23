package kt.web3.crypto

/**
 * Cipher exception wrapper.
 */
class CipherException : Exception {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}