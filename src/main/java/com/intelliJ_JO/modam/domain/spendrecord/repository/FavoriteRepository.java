package com.intelliJ_JO.modam.domain.spendrecord.repository;

import com.intelliJ_JO.modam.domain.spendrecord.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 특정 회원-기록 쌍 조회 (토글용)
    Optional<Favorite> findByMemberIdAndSpendRecordId(Long memberId, Long recordId);

    // 특정 회원이 즐겨찾기한 SpendRecord id 목록
    @Query("SELECT f.spendRecord.id FROM Favorite f WHERE f.member.id = :memberId")
    Set<Long> findRecordIdsByMemberId(@Param("memberId") Long memberId);

    // 회원별 전체 즐겨찾기 (화면 렌더용)
    List<Favorite> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 커플 계좌 전원이 즐겨찾기한 SpendRecord id 목록 (파트너 즐겨찾기도 포함)
    @Query("SELECT DISTINCT f.spendRecord.id FROM Favorite f " +
           "WHERE f.member.id IN (" +
           "  SELECT am.member.id FROM AccountMember am " +
           "  WHERE am.account.id = :accountId AND am.inviteStatus = com.intelliJ_JO.modam.domain.account.entity.InviteStatus.ACCEPT" +
           ")")
    Set<Long> findRecordIdsByAccountId(@Param("accountId") Long accountId);
}
