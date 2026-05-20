package com.intelliJ_JO.modam.domain.member.controller;


import com.intelliJ_JO.modam.domain.member.dto.*;
import com.intelliJ_JO.modam.domain.member.service.MemberService;
import com.intelliJ_JO.modam.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "회원", description = "회원 가입/조회/수정/탈퇴 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 가입")
    @PostMapping
    public GlobalResponse<MemberCreateResponse> createMember(@Valid @RequestBody MemberCreateRequest request) {
        return GlobalResponse.ok(memberService.createMember(request));
    }

    @Operation(summary = "회원 전체 조회")
    @GetMapping
    public GlobalResponse<MemberListResponse> getMembers() {
        return GlobalResponse.ok(memberService.getMembers());
    }

    @Operation(summary = "회원 상세 조회")
    @GetMapping("/{memberId}")
    public GlobalResponse<MemberResponse> getMember(@PathVariable Long memberId) {
        return GlobalResponse.ok(memberService.getMember(memberId));
    }

    @Operation(summary = "회원 정보 수정")
    @PatchMapping("/{memberId}")
    public GlobalResponse<MemberUpdateResponse> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        return GlobalResponse.ok(memberService.updateMember(memberId, request));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/{memberId}")
    public GlobalResponse<String> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return GlobalResponse.ok("회원 삭제 완료");
    }
}