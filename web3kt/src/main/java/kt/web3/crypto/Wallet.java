package kt.web3.crypto;

/**
 * Ethereum wallet file.
 */
public class Wallet {

  private final String address;
  private final Crypto crypto;
  private final String id;
  private final int version;

  public static Builder builder() {
    return new Builder();
  }

  private Wallet(Builder builder) {
    this.address = builder.address;
    this.crypto = builder.crypto;
    this.id = builder.id;
    this.version = builder.version;
  }

  public String getAddress() {
    return address;
  }

  public Crypto getCrypto() {
    return crypto;
  }

  public String getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Wallet)) {
      return false;
    }

    Wallet that = (Wallet) o;

    if (getAddress() != null ? !getAddress().equals(that.getAddress()) : that.getAddress() != null) {
      return false;
    }

    if (getCrypto() != null ? !getCrypto().equals(that.getCrypto()) : that.getCrypto() != null) {
      return false;
    }

    return (getId() != null ? getId().equals(that.getId()) : that.getId() == null) && version == that.version;
  }

  @Override public int hashCode() {
    int result = getAddress() != null ? getAddress().hashCode() : 0;
    result = 31 * result + (getCrypto() != null ? getCrypto().hashCode() : 0);
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    result = 31 * result + version;
    return result;
  }

  public static final class Builder {

    private String address;
    private Crypto crypto;
    private String id;
    private int version;

    private Builder() {
    }

    public Builder withAddress(String val) {
      address = val;
      return this;
    }

    public Builder withCrypto(Crypto val) {
      crypto = val;
      return this;
    }

    public Builder withId(String val) {
      id = val;
      return this;
    }

    public Builder withVersion(int val) {
      version = val;
      return this;
    }

    public Wallet build() {
      return new Wallet(this);
    }
  }

  public static class Crypto {

    private final String cipher;
    private final String ciphertext;
    private final CipherParams cipherparams;

    private final String kdf;
    private final KdfParams kdfparams;

    private final String mac;

    public static Builder builder() {
      return new Builder();
    }

    private Crypto(Builder builder) {
      cipher = builder.cipher;
      ciphertext = builder.ciphertext;
      cipherparams = builder.cipherparams;
      kdf = builder.kdf;
      kdfparams = builder.kdfparams;
      mac = builder.mac;
    }

    public String getCipher() {
      return cipher;
    }

    public String getCiphertext() {
      return ciphertext;
    }

    public CipherParams getCipherparams() {
      return cipherparams;
    }

    public String getKdf() {
      return kdf;
    }

    public KdfParams getKdfparams() {
      return kdfparams;
    }

    public String getMac() {
      return mac;
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof Crypto)) {
        return false;
      }

      Crypto that = (Crypto) o;

      if (getCipher() != null ? !getCipher().equals(that.getCipher()) : that.getCipher() != null) {
        return false;
      }

      if (getCiphertext() != null ? !getCiphertext().equals(that.getCiphertext()) : that.getCiphertext() != null) {
        return false;
      }

      if (getCipherparams() != null ? !getCipherparams().equals(that.getCipherparams()) : that.getCipherparams() != null) {
        return false;
      }

      if (getKdf() != null ? !getKdf().equals(that.getKdf()) : that.getKdf() != null) {
        return false;
      }

      if (getKdfparams() != null ? !getKdfparams().equals(that.getKdfparams()) : that.getKdfparams() != null) {
        return false;
      }

      return getMac() != null ? getMac().equals(that.getMac()) : that.getMac() == null;
    }

    @Override public int hashCode() {
      int result = getCipher() != null ? getCipher().hashCode() : 0;
      result = 31 * result + (getCiphertext() != null ? getCiphertext().hashCode() : 0);
      result = 31 * result + (getCipherparams() != null ? getCipherparams().hashCode() : 0);
      result = 31 * result + (getKdf() != null ? getKdf().hashCode() : 0);
      result = 31 * result + (getKdfparams() != null ? getKdfparams().hashCode() : 0);
      result = 31 * result + (getMac() != null ? getMac().hashCode() : 0);
      return result;
    }

    public static final class Builder {

      private String cipher;
      private String ciphertext;
      private CipherParams cipherparams;
      private String kdf;
      private KdfParams kdfparams;
      private String mac;

      private Builder() {
      }

      public Builder withCipher(String val) {
        cipher = val;
        return this;
      }

      public Builder withCiphertext(String val) {
        ciphertext = val;
        return this;
      }

      public Builder withCipherparams(CipherParams val) {
        cipherparams = val;
        return this;
      }

      public Builder withKdf(String val) {
        kdf = val;
        return this;
      }

      public Builder withKdfparams(KdfParams val) {
        kdfparams = val;
        return this;
      }

      public Builder withMac(String val) {
        mac = val;
        return this;
      }

      public Crypto build() {
        return new Crypto(this);
      }
    }
  }

  public static class CipherParams {

    private final String iv;

    public CipherParams(final String iv) {
      this.iv = iv;
    }

    public String getIv() {
      return iv;
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof CipherParams)) {
        return false;
      }

      CipherParams that = (CipherParams) o;

      return getIv() != null ? getIv().equals(that.getIv()) : that.getIv() == null;
    }

    @Override public int hashCode() {
      return getIv() != null ? getIv().hashCode() : 0;
    }

  }

  interface KdfParams {

    int getDklen();

    String getSalt();
  }

  public static class ScryptKdfParams implements KdfParams {

    private final int dklen;
    private final int n;
    private final int p;
    private final int r;
    private final String salt;

    public ScryptKdfParams(int dklen, int n, int p, int r, String salt) {
      this.dklen = dklen;
      this.n = n;
      this.p = p;
      this.r = r;
      this.salt = salt;
    }

    public int getDklen() {
      return dklen;
    }

    public int getN() {
      return n;
    }

    public int getP() {
      return p;
    }

    public int getR() {
      return r;
    }

    public String getSalt() {
      return salt;
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ScryptKdfParams)) {
        return false;
      }

      ScryptKdfParams that = (ScryptKdfParams) o;

      if (dklen != that.dklen) {
        return false;
      }

      if (n != that.n) {
        return false;
      }

      if (p != that.p) {
        return false;
      }

      if (r != that.r) {
        return false;
      }

      return getSalt() != null ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
    }

    @Override public int hashCode() {
      int result = dklen;
      result = 31 * result + n;
      result = 31 * result + p;
      result = 31 * result + r;
      result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
      return result;
    }
  }

}