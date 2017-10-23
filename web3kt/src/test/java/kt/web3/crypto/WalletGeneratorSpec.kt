package kt.web3.crypto

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object WalletGeneratorSpec : Spek({

  describe("wallet generator") {

    it("should generate correctly a wallet") {

    }

  }

  //private static final String PASSWORD = "Insecure Pa55w0rd";
  //private static final String SECRET = "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6";
  //
  //private static final String PRIVATE_KEY_STRING = "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6";
  //private static final String PUBLIC_KEY_STRING =
  //    "0x506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba645c0b7b58158babbfa6c6cd5a48aa7340a8749176b120e8516216787a13dc76";
  //
  //private static final BigInteger PRIVATE_KEY = Numeric.INSTANCE.toBigInt(PRIVATE_KEY_STRING);
  //private static final BigInteger PUBLIC_KEY = Numeric.INSTANCE.toBigInt(PUBLIC_KEY_STRING);
  //private static final ECKeyPair KEY_PAIR = new ECKeyPair(PRIVATE_KEY, PUBLIC_KEY);
  //
  //private static final String ADDRESS = "0xef678007d18427e6022059dbc264f27507cd1ffc";
  //private static final String ADDRESS_NO_PREFIX = Numeric.INSTANCE.cleanHexPrefix(ADDRESS);
  //
  //@Test public void testCreateStandard() throws Exception {
  //  final Wallet wallet = WalletGenerator.INSTANCE.create(PASSWORD, KEY_PAIR);
  //  assertThat(wallet.getAddress(), is(ADDRESS_NO_PREFIX));
  //}
  //
  //@Test public void testGenerateRandomBytes() {
  //  assertThat(WalletGenerator.INSTANCE.generateRandomBytes(0), is(new byte[] {}));
  //  assertThat(WalletGenerator.INSTANCE.generateRandomBytes(10).length, is(10));
  //}

})