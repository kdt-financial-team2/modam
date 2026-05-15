package com.intelliJ_JO.modam.domain.account.service;

import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequestDto request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String passwordHash = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword()) : null;

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .passwordHash(passwordHash)
                .accountType(request.getAccountType())
                .deliveryAddress(request.getDeliveryAddress())
                .jobInfo(request.getJobInfo())
                .tradePurpose(request.getTradePurpose())
                .fundSource(request.getFundSource())
                .build();

        Account saved = accountRepository.save(account);

        // 개설자는 바로 ACCEPT 상태로 AccountMember 등록
        accountMemberRepository.save(AccountMember.builder()
                .account(saved)
                .member(member)
                .inviteStatus(InviteStatus.ACCEPT)
                .build());

        return new AccountResponseDto(saved);
    }

    public AccountResponseDto getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        return new AccountResponseDto(account);
    }

    @Transactional
    public AccountResponseDto updateAccount(Long accountId, AccountUpdateRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        account.updateDetails(
                request.getDeliveryAddress(),
                request.getJobInfo(),
                request.getTradePurpose(),
                request.getFundSource(),
                request.getSpendLimitAmount()
        );
        return new AccountResponseDto(account);
    }

    @Transactional
    public void closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        account.close();
    }

    public List<AccountMemberResponseDto> getAccountMembers(Long accountId) {
        return accountMemberRepository.findByAccountId(accountId).stream()
                .map(AccountMemberResponseDto::new)
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }
}