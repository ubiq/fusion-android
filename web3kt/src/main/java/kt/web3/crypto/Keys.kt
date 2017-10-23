package kt.web3.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec

object Keys {

  const val PRIVATE_KEY_SIZE = 32

  const private val ADDRESS_LENGTH_IN_HEX = 40
  const private val PUBLIC_KEY_SIZE = 64
  const private val PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE shl 1

  init {
    Security.addProvider(BouncyCastleProvider())
  }

  @Throws(InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class)
  fun createEcKeyPair(): ECKeyPair {
    val keyPair = createSecp256k1KeyPair()
    return ECKeyPair.create(keyPair)
  }

  /**
   * Create a keypair using SECP-256k1 curve.
   * Private keypairs are encoded using PKCS8
   * Private keys are encoded using X.509
   */
  @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
  private fun createSecp256k1KeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC")
    val ecGenParameterSpec = ECGenParameterSpec("secp256k1")
    keyPairGenerator.initialize(ecGenParameterSpec, SecureRandomUtils.secureRandom())
    return keyPairGenerator.generateKeyPair()
  }

  fun getAddress(ecKeyPair: ECKeyPair): String {
    return getAddress(ecKeyPair.publicKey)
  }

  private fun getAddress(publicKey: BigInteger): String {
    return getAddress(Numeric.toHexStringWithPrefixZeroPadded(publicKey, PUBLIC_KEY_LENGTH_IN_HEX))
  }

  private fun getAddress(publicKey: String): String {
    var publicKeyNoPrefix = Numeric.cleanHexPrefix(publicKey)
    if (publicKeyNoPrefix.length < PUBLIC_KEY_LENGTH_IN_HEX) {
      publicKeyNoPrefix = Strings.zeros(PUBLIC_KEY_LENGTH_IN_HEX - publicKeyNoPrefix.length) + publicKeyNoPrefix
    }
    val hash = Hash.sha3(publicKeyNoPrefix)
    return hash.substring(hash.length - ADDRESS_LENGTH_IN_HEX)  // right most 160 bits
  }

}
