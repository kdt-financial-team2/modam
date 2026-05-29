package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.spendrecord.dto.ConsumptionDetailDto;
import com.intelliJ_JO.modam.domain.spendrecord.dto.ConsumptionHistoryItemDto;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.spendrecord.service.SpendRecordService;
import com.intelliJ_JO.modam.domain.transaction.dto.TransactionResponseDto;
import com.intelliJ_JO.modam.domain.transaction.repository.TransactionRepository;
import com.intelliJ_JO.modam.domain.transaction.service.TransactionService;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SpendingViewController {

    private final DashboardService dashboardService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final SpendRecordRepository spendRecordRepository;
    private final SpendRecordService spendRecordService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ===== 소비기록 목록 =====
    @GetMapping("/consumption-history")
    public String consumptionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");

        Account account = getGroupAccount(userDetails.getMember().getId());
        if (account == null) {
            model.addAttribute("records", List.of());
            model.addAttribute("totalAmount", 0L);
            model.addAttribute("writtenCount", 0L);
            model.addAttribute("totalCount", 0);
            return "domain/spendrecord/consumption-history";
        }

        // 최근 50건 트랜잭션 (PAYMENT/WITHDRAW만)
        List<TransactionResponseDto> txList = transactionService.getTransactions(account.getId(), null, 50)
                .stream()
                .filter(tx -> !"deposit".equals(tx.getType()))
                .collect(Collectors.toList());

        // 계좌 기준 SpendRecord 맵 (transactionId → SpendRecord)
        Map<Long, SpendRecord> recordMap = spendRecordRepository
                .findByTransaction_AccountIdOrderByIdDesc(account.getId(), PageRequest.of(0, 200))
                .stream()
                .collect(Collectors.toMap(r -> r.getTransaction().getId(), r -> r, (a, b) -> a));

        List<ConsumptionHistoryItemDto> records = txList.stream()
                .map(tx -> {
                    SpendRecord record = recordMap.get(tx.getTransactionId());
                    boolean hasImage = record != null && record.getImageUrl() != null;
                    return ConsumptionHistoryItemDto.builder()
                            .id(record != null ? record.getId() : null)
                            .transactionId(tx.getTransactionId())
                            .category(tx.getCategory())
                            .date(tx.getDate())
                            .time(tx.getTime())
                            .place(tx.getMerchant() != null ? tx.getMerchant() : tx.getCategory())
                            .memo(record != null ? record.getMemo() : null)
                            .iconName(tx.getIconName())
                            .emoticon(record != null ? record.getEmoticon() : null)
                            .isUpdated(record != null && record.getUpdatedAt() != null
                                    && !record.getCreatedAt().equals(record.getUpdatedAt()))
                            .hasImage(hasImage)
                            .imageDesc(hasImage ? "[사진]" : null)
                            .hasRecord(record != null)
                            .commentCount(0)
                            .amount(tx.getAmount())
                            .build();
                })
                .collect(Collectors.toList());

        long totalAmount   = records.stream().mapToLong(ConsumptionHistoryItemDto::getAmount).sum();
        long writtenCount  = records.stream().filter(ConsumptionHistoryItemDto::isHasRecord).count();

        model.addAttribute("records", records);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("writtenCount", writtenCount);
        model.addAttribute("totalCount", records.size());
        return "domain/spendrecord/consumption-history";
    }

    // ===== 소비 내역 선택 =====
    @GetMapping("/consumption-select")
    public String consumptionSelect(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");

        Account account = getGroupAccount(userDetails.getMember().getId());
        if (account == null) {
            model.addAttribute("transactions", List.of());
            model.addAttribute("hasMore", false);
            model.addAttribute("lastTxId", null);
            return "domain/spendrecord/consumption-select";
        }

        List<ConsumptionHistoryItemDto> transactions = buildSelectItems(account.getId(), null, 20);
        boolean hasMore = transactions.size() == 20;
        Long lastTxId = hasMore ? transactions.get(transactions.size() - 1).getTransactionId() : null;

        model.addAttribute("transactions", transactions);
        model.addAttribute("hasMore", hasMore);
        model.addAttribute("lastTxId", lastTxId);
        return "domain/spendrecord/consumption-select";
    }

    // ===== 무한 스크롤 JSON =====
    @GetMapping(value = "/consumption-select/more", produces = "application/json")
    @ResponseBody
    public List<ConsumptionHistoryItemDto> consumptionSelectMore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long lastTxId) {
        Account account = getGroupAccount(userDetails.getMember().getId());
        if (account == null) return List.of();
        return buildSelectItems(account.getId(), lastTxId, 20);
    }

    private List<ConsumptionHistoryItemDto> buildSelectItems(Long accountId, Long lastTxId, int size) {
        List<TransactionResponseDto> txList = transactionService.getTransactions(accountId, lastTxId, size + 10)
                .stream()
                .filter(tx -> !"deposit".equals(tx.getType()))
                .limit(size)
                .collect(Collectors.toList());

        Map<Long, SpendRecord> recordMap = spendRecordRepository
                .findByTransaction_AccountIdOrderByIdDesc(accountId, PageRequest.of(0, 200))
                .stream()
                .collect(Collectors.toMap(r -> r.getTransaction().getId(), r -> r, (a, b) -> a));

        return txList.stream()
                .map(tx -> {
                    SpendRecord record = recordMap.get(tx.getTransactionId());
                    return ConsumptionHistoryItemDto.builder()
                            .id(record != null ? record.getId() : null)
                            .transactionId(tx.getTransactionId())
                            .category(tx.getCategory())
                            .date(tx.getDate())
                            .time(tx.getTime())
                            .place(tx.getMerchant() != null ? tx.getMerchant() : tx.getCategory())
                            .iconName(tx.getIconName())
                            .hasRecord(record != null)
                            .amount(tx.getAmount())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===== 소비기록 작성 폼 =====
    @GetMapping("/consumption-upload")
    public String consumptionUploadForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long txId,
            Model model) {

        if (txId == null) {
            return "redirect:/consumption-select";
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");

        transactionRepository.findById(txId).ifPresent(tx -> {
            model.addAttribute("txId", tx.getId());
            model.addAttribute("txPlace", tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory());
            model.addAttribute("txAmount", tx.getAmount());
            model.addAttribute("txDate", tx.getCreatedAt().format(DATE_FMT));
            model.addAttribute("txCategory", tx.getCategory());

            spendRecordRepository.findByTransactionId(txId).ifPresent(record ->
                    model.addAttribute("record", record));
        });

        return "domain/spendrecord/consumption-upload";
    }

    // ===== 소비기록 저장 (POST) =====
    @PostMapping("/consumption-upload")
    public String consumptionUploadSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long transactionId,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String emoticon,
            @RequestParam(required = false) String imageUrl,
            Model model) {

        if (transactionId == null) {
            return "redirect:/consumption-history";
        }

        Long memberId = userDetails.getMember().getId();
        try {
            // 기존 기록 있으면 수정, 없으면 생성
            spendRecordRepository.findByTransactionId(transactionId).ifPresentOrElse(
                    record -> record.updateRecord(imageUrl, memo, emoticon),
                    () -> {
                        com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordCreateRequestDto req =
                                new com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordCreateRequestDto();
                        req.setTransactionId(transactionId);
                        req.setMemo(memo);
                        req.setEmoticon(emoticon);
                        req.setImageUrl(imageUrl);
                        spendRecordService.createSpendRecord(memberId, req);
                    }
            );
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "domain/spendrecord/consumption-upload";
        }

        return "redirect:/consumption-history";
    }

    // ===== 소비기록 상세 =====
    @GetMapping("/consumption-detail/{id}")
    public String consumptionDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");

        SpendRecord record = spendRecordService.getSpendRecordEntityById(id);
        var tx = record.getTransaction();

        ConsumptionDetailDto consumption = ConsumptionDetailDto.builder()
                .id(record.getId())
                .title(tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory())
                .place(tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory())
                .date(tx.getCreatedAt().format(DATE_FMT))
                .time(tx.getCreatedAt().format(TIME_FMT))
                .category(tx.getCategory())
                .amount(tx.getAmount())
                .memo(record.getMemo())
                .imageUrl(record.getImageUrl())
                .emoticon(record.getEmoticon())
                .author(tx.getMember().getName())
                .likes(0)
                .build();

        model.addAttribute("consumption", consumption);
        model.addAttribute("comments", List.of());
        model.addAttribute("liked", false);
        model.addAttribute("currentUsername", userDetails.getMember().getName());
        return "domain/spendrecord/consumption-detail";
    }

    // ===== 좋아요 (스텁) =====
    @PostMapping("/consumption-detail/{id}/like")
    public String consumptionLike(@PathVariable Long id) {
        return "redirect:/consumption-detail/" + id;
    }

    // ===== 댓글 (스텁) =====
    @PostMapping("/consumption-detail/{id}/comment")
    public String consumptionComment(@PathVariable Long id,
                                     @RequestParam(required = false) String content) {
        return "redirect:/consumption-detail/" + id;
    }

    // ===== 소비 제한 =====
    @GetMapping("/spending-limit")
    public String spendingLimit(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        return "domain/spending/spending-limit";
    }

    // ===== 소비 내역 (기존 라우트 유지) =====
    @GetMapping("/transaction-history")
    public String transactionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");
        populateTransactionData(userDetails, model);
        return "domain/transaction/transaction-history";
    }

    @GetMapping("/spend-record")
    public String spendRecord(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "consumption");
        Account account = getGroupAccount(userDetails.getMember().getId());
        model.addAttribute("accountId", account != null ? account.getId() : null);
        return "redirect:/consumption-history";
    }

    // ===== 공통 헬퍼 =====
    private Account getGroupAccount(Long memberId) {
        return accountMemberRepository
                .findByMemberId(memberId).stream()
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .filter(am -> am.getAccount().getAccountType() == AccountType.GROUP)
                .findFirst()
                .map(am -> am.getAccount())
                .orElse(null);
    }

    private void populateTransactionData(CustomUserDetails userDetails, Model model) {
        try {
            Account account = getGroupAccount(userDetails.getMember().getId());

            if (account == null) {
                model.addAttribute("transactions", List.of());
                model.addAttribute("groupedTransactions", Map.of());
                model.addAttribute("totalDeposit", 0L);
                model.addAttribute("totalWithdrawal", 0L);
                model.addAttribute("currentBalance", 0L);
                return;
            }

            List<TransactionResponseDto> transactions =
                    transactionService.getTransactions(account.getId(), null, 50);

            Map<String, List<TransactionResponseDto>> groupedTransactions = transactions.stream()
                    .collect(Collectors.groupingBy(
                            TransactionResponseDto::getDate,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            long totalDeposit    = transactions.stream().filter(tx -> "deposit".equals(tx.getType())).mapToLong(TransactionResponseDto::getAmount).sum();
            long totalWithdrawal = transactions.stream().filter(tx -> "withdrawal".equals(tx.getType())).mapToLong(TransactionResponseDto::getAmount).sum();

            model.addAttribute("transactions", transactions);
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("totalDeposit", totalDeposit);
            model.addAttribute("totalWithdrawal", totalWithdrawal);
            model.addAttribute("currentBalance", account.getAvailableBalance());

        } catch (Exception e) {
            model.addAttribute("transactions", List.of());
            model.addAttribute("groupedTransactions", Map.of());
            model.addAttribute("totalDeposit", 0L);
            model.addAttribute("totalWithdrawal", 0L);
            model.addAttribute("currentBalance", 0L);
        }
    }
}
