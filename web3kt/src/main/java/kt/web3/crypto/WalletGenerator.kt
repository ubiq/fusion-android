package kt.web3.crypto

import org.bouncycastle.crypto.generators.SCrypt
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import javax.crypto.Cipher

/**
 *
 * Ethereum wallet file management. For reference, refer to
 * [
 * Web3 Secret Storage Definition](https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition) or the
 * [
 * Go Ethereum client implementation](https://github.com/ethereum/go-ethereum/blob/master/accounts/key_store_passphrase.go).
 *
 * **Note:** the Bouncy Castle Scrypt implementation
 * [SCrypt], fails to comply with the following
 * Ethereum reference
 * [
 * Scrypt test vector](https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition#scrypt):
 *
 *
 * <pre>
 * `// Only value of r that cost (as an int) could be exceeded for is 1
 * if (r == 1 && N_STANDARD > 65536)
 * {
 * throw new IllegalArgumentException("Cost parameter N_STANDARD must be > 1 and < 65536.");
 * }
` *
</pre> *
 */
object WalletGenerator : Wallets {

  private const val N_STANDARD = 1 shl 18
  private const val P_STANDARD = 1

  private const val R = 8
  private const val DKLEN = 32

  private const val CURRENT_VERSION = 3

  private const val CIPHER = "aes-128-ctr"
  private const val SCRYPT = "scrypt"

  @Throws(CipherException::class)
  fun create(password: String, ecKeyPair: ECKeyPair): Wallet {
    val salt = generateRandomBytes(32)

    val derivedKey = generateDerivedScryptKey(password.toByteArray(), salt, N_STANDARD, R, P_STANDARD, DKLEN)

    val encryptKey = Arrays.copyOfRange(derivedKey, 0, 16)
    val iv = generateRandomBytes(16)
    val privateKeyBytes = Numeric.toBytesPadded(ecKeyPair.privateKey, Keys.PRIVATE_KEY_SIZE)
    val cipherText = performCipherOperation(Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes)
    val mac = generateMac(derivedKey, cipherText)

    return createWallet(ecKeyPair, cipherText, iv, salt, mac, N_STANDARD, P_STANDARD)
  }

  private fun createWallet(ecKeyPair: ECKeyPair, cipherText: ByteArray, iv: ByteArray, salt: ByteArray, mac: ByteArray, n: Int, p: Int): Wallet {
    return Wallet.builder()
        .withId(UUID.randomUUID().toString())
        .withVersion(CURRENT_VERSION)
        .withAddress(Keys.getAddress(ecKeyPair))
        .withCrypto(Wallet.Crypto.builder()
            .withCipher(CIPHER)
            .withCiphertext(Numeric.toHexStringNoPrefix(cipherText))
            .withCipherparams(Wallet.CipherParams(Numeric.toHexStringNoPrefix(iv)))
            .withKdf(SCRYPT)
            .withKdfparams(Wallet.ScryptKdfParams(DKLEN, n, p, R, Numeric.toHexStringNoPrefix(salt)))
            .withMac(Numeric.toHexStringNoPrefix(mac))
            .build())
        .build()
  }

  fun generateRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    SecureRandomUtils.secureRandom().nextBytes(bytes)
    return bytes
  }
}