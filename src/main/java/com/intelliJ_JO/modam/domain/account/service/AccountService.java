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
import com.intelliJ_JO.modam.domain.couple.entity.Couple;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final CoupleRepository coupleRepository;
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
                .totalDeposit(deposit) // 🔥 [수정] 개설 시 초기 입금액도 기여도로 인정
                .build());

        // GROUP 계좌이면 Couple 레코드도 함께 생성 (초대코드 포함)
        if (saved.getAccountType() == AccountType.GROUP) {
            coupleRepository.save(Couple.builder()
                    .account(saved)
                    .inviteCode(UUID.randomUUID().toString())
                    .build());
        }

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

    // 계좌 해지 (잔액 0원일 때만 가능)
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

    // 계좌 번호 생성 (5050-XXXXXXXX-XXX 형식, 중복 시 재생성)
    private String generateAccountNumber() {
        Random rnd = new Random();
        String candidate;
        do {
            // 8자리 + 3자리 랜덤 숫자
            String mid   = String.format("%08d", rnd.nextInt(100_000_000));
            String check = String.format("%03d", rnd.nextInt(1_000));
            candidate = "5050-" + mid + "-" + check;
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    // 계좌 비밀번호 검증 메서드
    public void verifyAccountPassword(Long accountId, String rawPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("계좌 비밀번호가 일치하지 않습니다.");
        }
    }

    // =========================================================================
    // 🔥 [추가/수정] 모임 통장 해지 동의 요청 및 상태 판단 로직
    // =========================================================================
    @Transactional
    public String requestAccountClosure(Long accountId, Long requestMemberId) {
        Account groupAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌를 찾을 수 없습니다."));

        if (groupAccount.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("이미 해지된 계좌입니다.");
        }

        List<AccountMember> activeMembers = accountMemberRepository.findByAccountId(accountId).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .collect(Collectors.toList());

        if (activeMembers.isEmpty()) {
            throw new IllegalStateException("해지할 계좌에 활성 멤버가 없습니다.");
        }

        if (activeMembers.size() == 1) {
            // 혼자일 경우: 파트너 동의 필요 없이 즉시 해지 및 정산
            executeClosureAndRefund(groupAccount, activeMembers);
            return "CLOSED";
        } else {
            // 두 명일 경우: 동의 프로세스 진행
            AccountMember me = activeMembers.stream()
                    .filter(am -> am.getMember().getId().equals(requestMemberId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("본인의 계좌 정보를 찾을 수 없습니다."));

            AccountMember partner = activeMembers.stream()
                    .filter(am -> !am.getMember().getId().equals(requestMemberId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("파트너의 계좌 정보를 찾을 수 없습니다."));

            // 나의 해지 동의 상태를 'Y'로 변경
            me.updateAgreeClose("Y");

            if ("Y".equals(partner.getAgreeClose())) {
                // 파트너도 이미 동의('Y')했다면 최종 해지 및 정산 진행
                executeClosureAndRefund(groupAccount, activeMembers);
                return "CLOSED";
            } else {
                // 파트너가 아직 'N'이라면 대기 상태로 반환
                return "PENDING";
            }
        }
    }

    // 🔥 [추가] 해지 요청 취소 로직
    @Transactional
    public void cancelAccountClosure(Long accountId, Long requestMemberId) {
        AccountMember me = accountMemberRepository.findByAccountId(accountId).stream()
                .filter(am -> am.getMember().getId().equals(requestMemberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("계좌 멤버 정보를 찾을 수 없습니다."));
        me.updateAgreeClose("N"); // 취소 시 다시 'N'으로 원복
    }

    // =========================================================================
    // 🔥 [수정] 실제 환급 및 계좌 상태 폐쇄 로직 ('실제 기여 금액' 비례 분배 적용)
    // =========================================================================
    private void executeClosureAndRefund(Account groupAccount, List<AccountMember> activeMembers) {
        Long totalBalance = groupAccount.getBalance();

        // 1. 멤버들이 그동안 입금했던 누적 금액의 총합 계산
        long totalGroupDeposit = activeMembers.stream().mapToLong(AccountMember::getTotalDeposit).sum();
        long remainingBalanceToDistribute = totalBalance;

        // 2. 각 멤버별로 기여도에 맞춰 잔액 분배 및 개인 계좌로 송금
        for (int i = 0; i < activeMembers.size(); i++) {
            AccountMember am = activeMembers.get(i);
            Account personalAccount = accountMemberRepository.findByMemberId(am.getMember().getId()).stream()
                    .filter(pam -> pam.getAccount().getAccountType() == AccountType.PERSONAL)
                    .map(AccountMember::getAccount)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(am.getMember().getName() + "님의 환급받을 개인 주계좌가 존재하지 않습니다."));

            long refundAmount = 0L;

            if (totalGroupDeposit > 0) {
                // 마지막 멤버는 소수점 계산 오차(자투리 금액)를 모두 가져가서 정확히 0원을 만듦
                if (i == activeMembers.size() - 1) {
                    refundAmount = remainingBalanceToDistribute;
                } else {
                    double shareRatio = (double) am.getTotalDeposit() / totalGroupDeposit;
                    refundAmount = (long) Math.floor(totalBalance * shareRatio);
                    remainingBalanceToDistribute -= refundAmount;
                }
            } else {
                // 예외 케이스: 입금액이 0원인데 이벤트 등으로 잔액만 있는 경우 1/N 배분
                refundAmount = totalBalance / activeMembers.size();
            }

            personalAccount.updateBalance(refundAmount);
        }

        // 3. 공동 원장 잔액 청산 및 계좌 상태 폐쇄
        groupAccount.updateBalance(-totalBalance);
        groupAccount.close();
    }

    // 🔥 [추가됨] 계좌 비밀번호 변경 비즈니스 로직
    @Transactional
    public void updateAccountPassword(Long accountId, String currentPassword, String newPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        // 기존 비밀번호가 맞는지 BCrypt 검증
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("기존 계좌 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 업데이트
        account.updatePassword(passwordEncoder.encode(newPassword));
    }
}