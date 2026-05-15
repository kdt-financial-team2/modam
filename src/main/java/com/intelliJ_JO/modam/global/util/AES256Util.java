package com.intelliJ_JO.modam.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AES256Util {

    // application.yml에 세팅된 키를 가져오되, 없다면 기본 32바이트 키를 사용합니다.
    @Value("${encryption.secret-key:0123456789abcdef0123456789abcdef}")
    private String secretKey;

    /**
     * 평문을 AES-256으로 암호화
     */
    public String encrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("카드 번호 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 암호화된 문자를 다시 평문으로 복호화
     */
    public String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("카드 번호 복호화 중 오류가 발생했습니다.", e);
        }
    }
}