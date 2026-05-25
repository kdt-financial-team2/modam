package com.intelliJ_JO.modam.domain.couple.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 커플 정보 저장/수정 요청 DTO
 * - 대시보드 온보딩 폼에서 POST /dashboard/couple-info 로 전송
 */
@Getter
@Setter
@NoArgsConstructor
public class CoupleInfoRequestDto {

    // 커플 시작일 (yyyy-MM-dd 형식 input[type=date] 값)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dDay;

    // 계좌 애칭 (예: 우리의 여행통장)
    private String acctAlias;
}
