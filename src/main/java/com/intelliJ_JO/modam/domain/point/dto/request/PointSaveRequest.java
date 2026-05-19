package com.intelliJ_JO.modam.domain.point.dto.request;

import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointSaveRequest {

    @NotNull(message = "포인트 지급 사유를 입력해주세요.")
    private PointReason reason;

    @NotNull(message = "적립 포인트를 입력해주세요.")
    @Min(value = 1, message = "적립 포인트는 1 이상이어야 합니다.")
    private Integer amt;

    @NotBlank(message = "포인트 설명을 입력해주세요.")
    private String descrip;
}
