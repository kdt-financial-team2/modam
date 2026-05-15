package com.intelliJ_JO.modam.domain.attendance.repository;

import com.intelliJ_JO.modam.domain.attendance.entity.Attendance;
import com.intelliJ_JO.modam.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // 특정 멤버가 특정 날짜에 출석한 기록이 있는지 조회
    Optional<Attendance> findByMemberAndAttendanceDate(Member member, LocalDate date);
}