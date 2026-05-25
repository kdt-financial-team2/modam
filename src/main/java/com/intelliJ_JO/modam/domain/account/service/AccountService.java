package com.intelliJ_JO.modam.domain.account.service;

import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountUpdateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountMemberResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.dto.GroupAccountStatusDto;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountStatus;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.account.repository.AccountRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
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
    private final BCryptPasswordEncoder passwordEncoder;

    // 계좌 개설 — 세션의 인증된 사용자를 개설자로 등록
    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequestDto request, Member member) {
        // 초기 입금액 (미입력 시 0)
        long deposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : 0L;

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .accountType(request.getAccountType())
                .acctAlias(request.getAcctAlias())
                .balance(deposit)
                .availableBalance(deposit)
                .deliveryAddress(request.getDeliveryAddress())
                .jobInfo(request.getJobInfo())
                .tradePurpose(request.getTradePurpose())
                .fundSource(request.getFundSource())
                .onceTransferLimit(request.getOnceTransferLimit())
                .dailyTransferLimit(request.getDailyTransferLimit())
                .agreeService(request.isAgreeService())
                .agreeFinance(request.isAgreeFinance())
                .agreePrivacy(request.isAgreePrivacy())
                .agreeMarketing(request.isAgreeMarketing())
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

    // 로그인 후 분기용 — 모임통장 보유 여부 반환
    public GroupAccountStatusDto getGroupAccountStatus(Member member) {
        return accountMemberRepository.findByMemberId(member.getId()).stream()
                .filter(am -> am.getAccount().getAccountType() == AccountType.GROUP)
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .findFirst()
                .map(am -> new GroupAccountStatusDto(true, am.getAccount().getId(), am.getAccount().getAccountNumber()))
                .orElse(new GroupAccountStatusDto(false, null, null));
    }

    // 4번 화면 진입 시 계좌번호 미리 보기 — 저장하지 않고 번호만 반환
    public String generatePreviewAccountNumber() {
        return generateAccountNumber();
    }

    // 계좌 단건 조회
    public AccountResponseDto getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        return new AccountResponseDto(account);
    }

    // 계좌 정보 수정
    @Transactional
    public AccountResponseDto updateAccount(Long accountId, AccountUpdateRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("해지된 계좌는 수정할 수 없습니다.");
        }

        account.updateDetails(
                request.getDeliveryAddress(),
                request.getJobInfo(),
                request.getTradePurpose(),
                request.getFundSource(),
                request.getSpendLimitAmount(),
                request.getOnceTransferLimit(),
                request.getDailyTransferLimit()
        );
        return new AccountResponseDto(account);
    }

    // 계좌 해지
    @Transactional
    public void closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("이미 해지된 계좌입니다.");
        }
        if (account.getBalance() > 0) {
            throw new IllegalStateException("잔액이 남아 있는 계좌는 해지할 수 없습니다.");
        }

        account.close();
    }

    // 모임 통장 참여 회원 전체 조회
    public List<AccountMemberResponseDto> getAccountMembers(Long accountId) {
        return accountMemberRepository.findByAccountId(accountId).stream()
                .map(AccountMemberResponseDto::new)
                .collect(Collectors.toList());
    }

    // 계좌 번호 생성 (UUID 기반 16자리 대문자, 중복 시 재생성)
    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }
}
