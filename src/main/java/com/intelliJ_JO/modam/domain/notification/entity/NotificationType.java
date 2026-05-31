package com.intelliJ_JO.modam.domain.notification.entity;

public enum NotificationType {
    DEPOSIT,       // 입금 알림
    WITHDRAW,      // 출금 알림
    LIMIT_WARNING, // 한도 초과 경고
    INVITE,        // 커플 초대
    SAVINGS_GOAL,  // 저축 달성
    STORY_CREATED, // 파트너가 소비 스토리 작성
    FAVORITE,      // 파트너가 내 스토리 즐겨찾기
    POINT_SPEND    // 포인트 사용
}