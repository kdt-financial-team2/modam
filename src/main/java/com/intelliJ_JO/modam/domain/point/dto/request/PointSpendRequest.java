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
public class PointSpendRequest {

    @NotNull(message = "포인트 사용 사유를 입력해주세요.")
    private PointReason reason;

    @NotNull(message = "사용 포인트를 입력해주세요.")
    @Min(value = 1, message = "사용 포인트는 1 이상이어야 합니다.")
    private Integer amt;

    @NotBlank(message = "포인트 사용 설명을 입력해주세요.")
    private String descrip;
}
