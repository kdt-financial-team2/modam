package com.intelliJ_JO.modam.global.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecentTransactionDto {
    private String icon;
    private String name;
    private String date;
    private String time;
    private String category;
    private long amount;
}
