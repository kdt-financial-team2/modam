package com.intelliJ_JO.modam.global.view;

import com.intelliJ_JO.modam.domain.account.dto.AccountCreateRequestDto;
import com.intelliJ_JO.modam.domain.account.dto.AccountResponseDto;
import com.intelliJ_JO.modam.domain.account.entity.AccountType;
import com.intelliJ_JO.modam.domain.account.service.AccountService;
import com.intelliJ_JO.modam.config.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountViewController {

    private final AccountService accountService;

    // 1. 계좌 개설 화면 열기 (AuthViewController에서 가져옴)
    @GetMapping("/account-setup")
    public String accountSetup() {
        return "domain/auth/account-setup";
    }

    // 2. 계좌 개설 폼 제출 (원석님이 새로 작성할 POST 핵심 로직!)
    @PostMapping("/account-setup")
    public String processAccountSetup(
            @RequestParam String accountPassword,
            @RequestParam String accountPasswordConfirm,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // DTO 조립 (html 폼에서 넘어온 데이터 세팅)
        AccountCreateRequestDto request = new AccountCreateRequestDto();
        request.setPassword(accountPassword);
        request.setPasswordConfirm(accountPasswordConfirm);
        request.setAccountType(AccountType.GROUP); // 모임 통장이므로 GROUP 타입 설정

        // 서비스 호출하여 계좌 생성 및 개설자를 AccountMember로 등록
        AccountResponseDto savedAccount = accountService.createAccount(request, userDetails.getMember());

        // 생성된 계좌번호를 다음 완료 화면으로 1회성으로 전달 (Flash Attribute)
        redirectAttributes.addFlashAttribute("accountNumber", savedAccount.getAccountNumber());

        return "redirect:/account-setup-complete";
    }

    // 3. 계좌 개설 완료 화면 (AuthViewController에서 가져옴)
    @GetMapping("/account-setup-complete")
    public String accountSetupComplete(Model model) {
        // RedirectAttributes로 넘어온 accountNumber는 Thymeleaf에서 바로 사용 가능
        return "domain/auth/account-setup-complete";
    }

    // (선택) /account-setup-complete 화면 하단의 "모담 시작하기" POST 핸들러
    @PostMapping("/account-setup-complete")
    public String startModam() {
        return "redirect:/dashboard"; // 대시보드로 이동
    }
}