package com.wtc.pureit.data.dto.helper;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encoder {

    public static byte[] sha256Encoding(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secret_key);

        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Encoding(byte[] input) throws NoSuchAlgorithmException {
        return new String(Base64.encodeBase64(input), StandardCharsets.US_ASCII);
    }

    public static String urlEncoding(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
    }

    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

}
