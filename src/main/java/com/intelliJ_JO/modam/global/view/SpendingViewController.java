package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import com.intelliJ_JO.modam.domain.account.entity.Account;
import com.intelliJ_JO.modam.domain.account.entity.AccountMember;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.entity.InviteStatus;
import com.intelliJ_JO.modam.domain.account.repository.AccountMemberRepository;
import com.intelliJ_JO.modam.domain.member.repository.MemberRepository;
import com.intelliJ_JO.modam.domain.comment.dto.request.CommentCreateRequestDto;
import com.intelliJ_JO.modam.domain.comment.dto.response.CommentResponseDto;
import com.intelliJ_JO.modam.domain.comment.service.CommentService;
import com.intelliJ_JO.modam.domain.couple.repository.CoupleRepository;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import com.intelliJ_JO.modam.domain.spendrecord.dto.ConsumptionDetailDto;
import com.intelliJ_JO.modam.domain.spendrecord.dto.ConsumptionHistoryItemDto;
import com.intelliJ_JO.modam.domain.spendrecord.entity.SpendRecord;
import com.intelliJ_JO.modam.domain.spendrecord.repository.SpendRecordRepository;
import com.intelliJ_JO.modam.domain.inventory.repository.InventoryRepository;
import com.intelliJ_JO.modam.domain.spendrecord.service.FavoriteService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SpendingViewController {

    private final MemberRepository memberRepository;
    private final DashboardService dashboardService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final AccountMemberRepository accountMemberRepository;
    private final CoupleRepository coupleRepository;
    private final CommentService commentService;
    private final SpendRecordRepository spendRecordRepository;
    private final SpendRecordService spendRecordService;
    private final FavoriteService favoriteService;
    private final InventoryRepository inventoryRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ===== 소비기록 목록 (인스타그램 스타일 그리드) =====
    @GetMapping("/consumption-history")
    public String consumptionHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "story");

        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElse(userDetails.getMember());

        // 내 프로필 정보
        model.addAttribute("myName", member.getName());
        model.addAttribute("myProfileImg", member.getProfileImg());

        // 파트너 연결 여부 확인 — 미연결 시 잠금 화면 렌더
        boolean partnerConnected = isPartnerConnected(member.getId());
        model.addAttribute("partnerConnected", partnerConnected);

        if (!partnerConnected) {
            model.addAttribute("partnerName", "");
            model.addAttribute("partnerProfileImg", null);
            model.addAttribute("coupleAcctAlias", "");
            model.addAttribute("records", List.of());
            model.addAttribute("storyRecords", List.of());
            model.addAttribute("totalAmount", 0L);
            model.addAttribute("writtenCount", 0L);
            model.addAttribute("favoriteCount", 0L);
            model.addAttribute("totalCount", 0);
            return "domain/spendrecord/consumption-history";
        }

        Account account = getGroupAccount(member.getId());

        // 파트너 정보
        AccountMember partnerMembership = accountMemberRepository.findByAccountId(account.getId()).stream()
                .filter(am -> !am.getMember().getId().equals(member.getId()))
                .filter(am -> am.getInviteStatus() == InviteStatus.ACCEPT)
                .findFirst()
                .orElse(null);
        model.addAttribute("partnerName", partnerMembership != null ? partnerMembership.getMember().getName() : "");
        model.addAttribute("partnerProfileImg", partnerMembership != null ? partnerMembership.getMember().getProfileImg() : null);

        // 커플 D-Day 정보
        coupleRepository.findByAccountId(account.getId()).ifPresentOrElse(couple -> {
            model.addAttribute("coupleStartDate", couple.getDDay() != null
                    ? couple.getDDay().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) : "");
            model.addAttribute("daysTogether", couple.getDDay() != null
                    ? ChronoUnit.DAYS.between(couple.getDDay(), LocalDate.now()) : 0L);
            model.addAttribute("coupleAcctAlias", couple.getAccountAlias() != null ? couple.getAccountAlias() : "");
        }, () -> {
            model.addAttribute("coupleStartDate", "");
            model.addAttribute("daysTogether", 0L);
            model.addAttribute("coupleAcctAlias", "");
        });

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

        // 현재 회원의 즐겨찾기 SpendRecord id 집합
        java.util.Set<Long> favIds = favoriteService.getFavoriteRecordIds(member.getId());

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
                            .title(record != null ? record.getTitle() : null)
                            .memo(record != null ? record.getMemo() : null)
                            .iconName(tx.getIconName())
                            .emoticon(record != null ? record.getEmoticon() : null)
                            .isUpdated(record != null && record.getUpdatedAt() != null
                                    && !record.getCreatedAt().equals(record.getUpdatedAt()))
                            .hasImage(hasImage)
                            .imageUrl(record != null ? record.getImageUrl() : null)
                            .imageDesc(hasImage ? "[사진]" : null)
                            .hasRecord(record != null)
                            .commentCount(0)
                            .amount(tx.getAmount())
                            .favorited(record != null && favIds.contains(record.getId()))
                            .createdAt(record != null ? record.getCreatedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());

        long totalAmount    = records.stream().mapToLong(ConsumptionHistoryItemDto::getAmount).sum();
        long writtenCount   = records.stream().filter(ConsumptionHistoryItemDto::isHasRecord).count();
        // 현재 회원이 즐겨찾기한 스토리 수
        long favoriteCount  = records.stream().filter(ConsumptionHistoryItemDto::isFavorited).count();

        // 스토리가 있는 기록만 그리드용 분리 — 작성일 내림차순 정렬
        List<ConsumptionHistoryItemDto> storyRecords = records.stream()
                .filter(ConsumptionHistoryItemDto::isHasRecord)
                .sorted(Comparator.comparing(ConsumptionHistoryItemDto::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        model.addAttribute("records", records);
        model.addAttribute("storyRecords", storyRecords);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("writtenCount", writtenCount);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("totalCount", records.size());
        model.addAttribute("partnerConnected", true);
        return "domain/spendrecord/consumption-history";
    }

    // ===== 소비 내역 선택 =====
    @GetMapping("/consumption-select")
    public String consumptionSelect(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // 파트너 미연결 시 소비 스토리 기능 차단
        if (!isPartnerConnected(userDetails.getMember().getId())) {
            return "redirect:/consumption-history";
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "story");

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
            @RequestParam(value = "lastTxId", required = false) Long lastTxId) {
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
            @RequestParam(value = "txId", required = false) Long txId,
            Model model) {

        // 파트너 미연결 시 소비 스토리 기능 차단
        if (!isPartnerConnected(userDetails.getMember().getId())) {
            return "redirect:/consumption-history";
        }

        if (txId == null) {
            return "redirect:/consumption-select";
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "story");

        transactionRepository.findById(txId).ifPresent(tx -> {
            model.addAttribute("txId", tx.getId());
            model.addAttribute("txPlace", tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory());
            model.addAttribute("txAmount", tx.getAmount());
            model.addAttribute("txDate", tx.getCreatedAt().format(DATE_FMT));
            model.addAttribute("txCategory", tx.getCategory());

            spendRecordRepository.findByTransactionId(txId).ifPresent(record ->
                    model.addAttribute("record", record));
        });

        // 보유 이모티콘 목록 (포인트 샵에서 구매한 것만)
        List<String> ownedEmojis = inventoryRepository
                .findEmojiItemsByMemberId(userDetails.getMember().getId())
                .stream()
                .map(inv -> inv.getItem().getImage())
                .collect(Collectors.toList());
        model.addAttribute("ownedEmojis", ownedEmojis);

        return "domain/spendrecord/consumption-upload";
    }

    // ===== 소비기록 저장 (POST) =====
    @PostMapping("/consumption-upload")
    public String consumptionUploadSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "transactionId", required = false) Long transactionId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam(value = "emoticon", required = false) String emoticon,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        if (transactionId == null) {
            return "redirect:/consumption-history";
        }

        Long memberId = userDetails.getMember().getId();
        try {
            // 기존 기록 있으면 수정, 없으면 생성
            var existing = spendRecordRepository.findByTransactionId(transactionId);
            if (existing.isPresent()) {
                var record = existing.get();
                record.updateRecord(imageUrl, title, memo, emoticon);
                spendRecordRepository.save(record);
            } else {
                com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordCreateRequestDto req =
                        new com.intelliJ_JO.modam.domain.spendrecord.dto.SpendRecordCreateRequestDto();
                req.setTransactionId(transactionId);
                req.setTitle(title);
                req.setMemo(memo);
                req.setEmoticon(emoticon);
                req.setImageUrl(imageUrl);
                spendRecordService.createSpendRecord(memberId, req);
            }
        } catch (Exception e) {
            // 에러 시 폼 재렌더링에 필요한 트랜잭션 정보 복원
            transactionRepository.findById(transactionId).ifPresent(tx -> {
                model.addAttribute("txId", tx.getId());
                model.addAttribute("txPlace", tx.getMerchantName() != null ? tx.getMerchantName() : tx.getCategory());
                model.addAttribute("txAmount", tx.getAmount());
                model.addAttribute("txDate", tx.getCreatedAt().format(DATE_FMT));
                model.addAttribute("txCategory", tx.getCategory());
            });
            List<String> ownedEmojis = inventoryRepository
                    .findEmojiItemsByMemberId(memberId)
                    .stream()
                    .map(inv -> inv.getItem().getImage())
                    .collect(Collectors.toList());
            model.addAttribute("ownedEmojis", ownedEmojis);
            model.addAttribute("errorMessage", e.getMessage());
            dashboardService.populateHeader(userDetails.getMember(), model);
            return "domain/spendrecord/consumption-upload";
        }

        return "redirect:/consumption-history";
    }

    // ===== 소비기록 상세 =====
    @GetMapping("/consumption-detail/{id}")
    public String consumptionDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            Model model) {
        // 파트너 미연결 시 소비 스토리 기능 차단
        if (!isPartnerConnected(userDetails.getMember().getId())) {
            return "redirect:/consumption-history";
        }

        dashboardService.populateHeader(userDetails.getMember(), model);
        model.addAttribute("currentPage", "story");
        SpendRecord record = spendRecordService.getSpendRecordEntityById(id);
        var tx = record.getTransaction();

        boolean liked = favoriteService.getFavoriteRecordIds(userDetails.getMember().getId())
                .contains(record.getId());

        ConsumptionDetailDto consumption = ConsumptionDetailDto.builder()
                .id(record.getId())
                .transactionId(tx.getId())
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

        // 댓글 목록 실제 조회
        List<CommentResponseDto> comments = commentService.getComments(
                userDetails.getMember().getId(), record.getId());

        model.addAttribute("consumption", consumption);
        model.addAttribute("comments", comments);
        model.addAttribute("liked", liked);
        model.addAttribute("currentUsername", userDetails.getMember().getName());
        return "domain/spendrecord/consumption-detail";
    }

    // ===== 좋아요 (스텁) =====
    @PostMapping("/consumption-detail/{id}/like")
    public String consumptionLike(@PathVariable("id") Long id) {
        return "redirect:/consumption-detail/" + id;
    }

    // ===== 댓글 저장 =====
    @PostMapping("/consumption-detail/{id}/comment")
    public String consumptionComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @RequestParam(value = "content", required = false) String content) {
        if (content != null && !content.isBlank()) {
            CommentCreateRequestDto dto = new CommentCreateRequestDto();
            dto.setContent(content);
            commentService.createComment(userDetails.getMember().getId(), id, dto);
        }
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
        model.addAttribute("currentPage", "story");
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

    // 파트너가 수락된 상태인지 확인 (소비 스토리 기능 잠금 용도)
    private boolean isPartnerConnected(Long memberId) {
        Account account = getGroupAccount(memberId);
        if (account == null) return false;
        return accountMemberRepository.findByAccountId(account.getId()).stream()
                .anyMatch(am -> !am.getMember().getId().equals(memberId)
                        && am.getInviteStatus() == InviteStatus.ACCEPT);
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

            // 🔥 [수정됨] 레거시 메서드 대신 표준 getTransactions(accountId) 호출로 100% 오류 해결
            List<TransactionResponseDto> transactions =
                    transactionService.getTransactions(account.getId(), null, 500);

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