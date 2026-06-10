package com.intelliJ_JO.modam.feat;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AccountPasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 테스트용 통장 비밀번호 4자리 (예: 0000)
        String accountPin = "0000";
        String encodedPin = encoder.encode(accountPin);

        System.out.println("========================================");
        System.out.println("평문 PIN 번호: " + accountPin);
        System.out.println("생성된 BCrypt 해시값: " + encodedPin);
        System.out.println("========================================");
    }
}
