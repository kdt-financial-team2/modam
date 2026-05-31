package com.intelliJ_JO.modam.domain.account.repository;

import com.intelliJ_JO.modam.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    // 계좌 번호 중복 검사용 메서드 (AccountService에서 사용)
    boolean existsByAccountNumber(String accountNumber);
}