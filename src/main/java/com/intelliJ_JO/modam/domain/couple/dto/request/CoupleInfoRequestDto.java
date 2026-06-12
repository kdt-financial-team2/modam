package com.intelliJ_JO.modam.domain.couple.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 커플 정보 저장/수정 요청 DTO
 * - 대시보드 온보딩 폼에서 POST /dashboard/couple-info 로 전송
 */
@Schema(description = "커플 정보 저장/수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
public class CoupleInfoRequestDto {

    @Schema(description = "커플 시작일 (yyyy-MM-dd 형식)", example = "2025-01-14")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dDay;

    @Schema(description = "모임 계좌 애칭", example = "우리의 여행통장")
    private String acctAlias;
}
