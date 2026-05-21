package com.intelliJ_JO.modam.domain.shop.dto;

import com.intelliJ_JO.modam.domain.point.entity.PointHistory;
import com.intelliJ_JO.modam.domain.point.entity.PointReason;
import com.intelliJ_JO.modam.domain.point.entity.PointType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
@Builder
public class HistoryItemDto {

    private Long id;
    private String type;      // "EARN" or "USE"
    private String descrip;
    private String createdAt;
    private Integer amt;
    private Integer aftBal;
    private boolean isSpecial;
    private boolean isNew;

    private static final Set<PointReason> SPECIAL_REASONS = Set.of(
            PointReason.ITEM_PURCHASE, PointReason.THEME_PURCHASE);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static HistoryItemDto from(PointHistory h) {
        boolean special = SPECIAL_REASONS.contains(h.getReason());
        boolean newItem = h.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7));
        return HistoryItemDto.builder()
                .id(h.getId())
                .type(h.getType() == PointType.SAVE ? "EARN" : "USE")
                .descrip(h.getDescrip())
                .createdAt(h.getCreatedAt().format(FORMATTER))
                .amt(Math.abs(h.getAmt()))
                .aftBal(h.getAftBal())
                .isSpecial(special)
                .isNew(newItem)
                .build();
    }
}
