package com.intelliJ_JO.modam.domain.spendrecord.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpendRecordUpdateRequestDto {

    private String imageUrl;
    private String title;
    private String memo;
    private String emoticon;
}
