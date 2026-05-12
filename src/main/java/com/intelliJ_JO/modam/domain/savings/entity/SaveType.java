package com.intelliJ_JO.modam.domain.savings.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaveType {
    FREE("자유"),
    TRAVEL("여행"),
    GIFT("선물"),
    ETC("기타");

    private final String description;
}