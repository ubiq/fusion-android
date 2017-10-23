package kt.web3.crypto

import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.util.*

/**
 * Transaction signing logic.
 *
 * Adapted from the [BitcoinJ ECKey](https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java) implementation.
 */
object Sign {

  private val CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1")
  private val CURVE = ECDomainParameters(CURVE_PARAMS.curve, CURVE_PARAMS.g, CURVE_PARAMS.n, CURVE_PARAMS.h)

  /**
   * Returns public key from the given private key.
   *
   * @param privateKey the private key to derive the public key from
   *
   * @return BigInteger encoded public key
   */
  fun publicKeyFromPrivate(privateKey: BigInteger): BigInteger {
    val point = publicPointFromPrivate(privateKey)
    val encoded = point.getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
  }

  /**
   * Returns public key point from the given private key.
   */
  private fun publicPointFromPrivate(pk: BigInteger): ECPoint {
    var privateKey = pk

    // TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order, but that could change in future versions.
    if (privateKey.bitLength() > CURVE.n.bitLength()) {
      privateKey = privateKey.mod(CURVE.n)
    }

    return FixedPointCombMultiplier().multiply(CURVE.g, privateKey)
  }

}