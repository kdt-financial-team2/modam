package com.intelliJ_JO.modam.domain.attendance.entity;

import com.intelliJ_JO.modam.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class) // 생성 시간을 자동으로 찍기 위해 필요
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false)
    private Member member;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate; // 출석한 날짜 (시간 제외)

    // 생성자나 빌더를 통해 데이터를 넣을 때 날짜를 수동으로 넣거나,
    // 서비스 로직에서 LocalDate.now()를 주입해주면 됩니다.
}