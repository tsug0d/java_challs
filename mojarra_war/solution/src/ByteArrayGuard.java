import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public final class ByteArrayGuard {
    private static final int MAC_LENGTH = 32;
    private static final int KEY_LENGTH = 128;
    private static final int IV_LENGTH = 16;
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_CODE = "AES/CBC/PKCS5Padding";
    private static final String MAC_CODE = "HmacSHA256";
    private static final String SK_SESSION_KEY = "com.sun.faces.SK";
    private SecretKey sk;

    public ByteArrayGuard() {
        try {
            this.setupKeyAndMac();
        } catch (Exception var2) {
        }
    }

    public byte[] encrypt(byte[] bytes) {
        Object var3 = null;

        try {
            SecureRandom rand = new SecureRandom();
            byte[] iv = new byte[16];
            rand.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey secKey = this.getSecretKey();
            encryptCipher.init(1, secKey, ivspec);
            Mac encryptMac = Mac.getInstance("HmacSHA256");
            encryptMac.init(secKey);
            encryptMac.update(iv);
            byte[] encdata = encryptCipher.doFinal(bytes);
            byte[] macBytes = encryptMac.doFinal(encdata);
            byte[] tmp = concatBytes(macBytes, iv);
            byte[] securedata = concatBytes(tmp, encdata);
            return securedata;
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalStateException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException var13) {


            return null;
        }
    }

    public byte[] decrypt(byte[] bytes) {
        try {
            byte[] macBytes = new byte[32];
            System.arraycopy(bytes, 0, macBytes, 0, macBytes.length);
            byte[] iv = new byte[16];
            System.arraycopy(bytes, macBytes.length, iv, 0, iv.length);
            byte[] encdata = new byte[bytes.length - macBytes.length - iv.length];
            System.arraycopy(bytes, macBytes.length + iv.length, encdata, 0, encdata.length);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKey secKey = this.getSecretKey();
            Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher.init(2, secKey, ivspec);
            Mac decryptMac = Mac.getInstance("HmacSHA256");
            decryptMac.init(secKey);
            decryptMac.update(iv);
            decryptMac.update(encdata);
            byte[] macBytesCalculated = decryptMac.doFinal();
            if (this.areArrayEqualsConstantTime(macBytes, macBytesCalculated)) {
                byte[] plaindata = decryptCipher.doFinal(encdata);
                return plaindata;
            } else {
                System.err.println("ERROR: MAC did not verify!");
                return null;
            }
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalStateException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException var12) {
            System.err.println("ERROR: Decrypting:" + var12.getCause());
            return null;
        }
    }

    private boolean areArrayEqualsConstantTime(byte[] array1, byte[] array2) {
        boolean result = true;

        for(int i = 0; i < array1.length; ++i) {
            if (array1[i] != array2[i]) {
                result = false;
            }
        }

        return result;
    }

    private void setupKeyAndMac() {
        try {
            String encodedKeyArray = "ZmluZF90aGVfa2V5X3Bscw==";
            byte[] keyArray = DatatypeConverter.parseBase64Binary(encodedKeyArray);
            this.sk = new SecretKeySpec(keyArray, "AES");
        } catch (Exception var5) {
        }

        if (this.sk == null) {
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(128);
                this.sk = kg.generateKey();
            } catch (Exception var4) {

            }
        }

    }

    private static byte[] concatBytes(byte[] array1, byte[] array2) {
        byte[] cBytes = new byte[array1.length + array2.length];

        try {
            System.arraycopy(array1, 0, cBytes, 0, array1.length);
            System.arraycopy(array2, 0, cBytes, array1.length, array2.length);
            return cBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cBytes;
    }

    private SecretKey getSecretKey() {
        SecretKey result = this.sk;
        return result;
    }

}
