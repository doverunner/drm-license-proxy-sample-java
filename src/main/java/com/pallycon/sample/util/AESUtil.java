package com.pallycon.sample.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESUtil {
    private static Cipher initCipher(int mode, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }

    public static byte[] encryptAES256(byte[] plainByte, String key, String iv) throws Exception {
        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(plainByte);
    }

    public static byte[] decryptAES256(byte[] encryptedByte, String key, String iv) throws Exception {
        Cipher cipher = initCipher(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(encryptedByte);
    }
}
