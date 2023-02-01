package com.example.proyectoceti.Misc;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encrypter {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String Encrypt(String msg) throws Exception{
        byte[] decodedKey = Base64.getDecoder().decode("BhCZnpg54OIQQnmBIdEwxw==");
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        Log.d(TAG, encodedKey + " esta es la key");
        byte[] bytesSecretKey = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesSecretKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] EncryptedMSG = cipher.doFinal(msg.getBytes());
        byte[] base64EncryptedMSG = Base64.getEncoder().encode(EncryptedMSG);
        return new String(base64EncryptedMSG);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String Decrypt(String EncryptedMSG) throws Exception{
        byte[] decodedKey = Base64.getDecoder().decode("BhCZnpg54OIQQnmBIdEwxw==");
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        byte[] bytesSecretKey = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytesSecretKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] msg = cipher.doFinal(Base64.getDecoder().decode(EncryptedMSG));
        return new String(msg);
    }
}
