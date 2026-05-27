package com.intelliJ_JO.modam.global.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointHistoryView {
    private Long id;
    private String type;      // "EARN" or "USE"
    private Integer amt;      // 절댓값
    private Integer aftBal;
    private String descrip;
    private String createdAt; // "yyyy.MM.dd HH:mm"
    private Boolean isSpecial;
    private Boolean isNew;
}