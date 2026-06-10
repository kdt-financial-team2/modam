package com.intelliJ_JO.modam.feat;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 우리가 테스트용으로 사용할 비밀번호 "1234"를 암호화합니다.
        String rawPassword = "Hello123!";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("생성된 BCrypt 해시값: " + encodedPassword);
    }
}
