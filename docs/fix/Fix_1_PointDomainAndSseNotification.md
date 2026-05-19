# Fix #1 — 포인트 도메인 보안·동시성 버그 수정 및 SSE 알림 트랜잭션 안전성 확보

- **커밋**: `77d9c2c`, `be41463`
- **수정 파일**:
  - `domain/point/controller/PointController.java`
  - `domain/point/service/PointService.java`
  - `domain/point/repository/PointRepository.java`
  - `domain/point/entity/PointHistory.java`
  - `domain/point/dto/request/PointSaveRequest.java`
  - `domain/point/dto/request/PointSpendRequest.java`
  - `domain/savings/service/SavingsService.java`
  - `domain/notification/service/NotificationService.java`

---

## 문제 1 — 컨트롤러 인증 없음 (보안 취약점)

### 발생 원인

`PointController`의 모든 엔드포인트가 URL 경로 변수나 요청 바디의 `memberId`를 그대로 사용했다.
인증된 사용자 정보를 검증하지 않아, 누구든 다른 유저의 포인트를 조회하거나 적립·사용할 수 있었다.

```java
// 수정 전 — memberId를 외부에서 그대로 받음
@GetMapping("/{memberId}")
public List<PointResponse> getPointHistories(@PathVariable Long memberId) { ... }

@PostMapping("/save")
public PointResponse savePoint(@RequestBody PointSaveRequest request) {
    // request.getMemberId() 를 신뢰해서 그대로 사용
}
```

### 해결 방법

`@AuthenticationPrincipal CustomUserDetails`로 인증된 세션에서 `memberId`를 추출하도록 변경했다.
요청 DTO에서 `memberId` 필드를 제거하고, 서비스 메서드 시그니처도 `(Long memberId, DTO request)` 형태로 분리했다.

```java
// 수정 후 — 세션에서 인증된 사용자 ID 사용
@GetMapping
public GlobalResponse<List<PointResponse>> getPointHistories(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    return GlobalResponse.ok(
            pointService.getPointHistories(userDetails.getMember().getId()));
}
```

---

## 문제 2 — 잔액 계산의 Race Condition (동시성 버그)

### 발생 원인

`savePoint` / `spendPoint` 메서드가 잔액을 조회한 뒤 계산해서 저장하는 두 단계로 나뉘어 있었다.
동시 요청이 들어오면 두 스레드가 동일한 잔액을 조회한 후 각자 계산해서 저장해, 잔액이 잘못 기록될 수 있었다.

```
Thread A: 잔액 조회 → 1000
Thread B: 잔액 조회 → 1000   ← 같은 값을 읽음
Thread A: 1000 + 100 = 1100 저장
Thread B: 1000 + 100 = 1100 저장  ← 실제론 1200이어야 함
```

### 해결 방법

`PointRepository`에 `PESSIMISTIC_WRITE` 락을 사용하는 잔액 조회 메서드를 추가했다.
쓰기 트랜잭션이 끝날 때까지 다른 트랜잭션의 동일 행 접근을 차단한다.

```java
// PointRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM PointHistory p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
List<PointHistory> findLatestByMemberIdWithLock(@Param("memberId") Long memberId, Pageable pageable);

// PointService — 잔액 조회 시 락 적용
int currentBalance = pointRepository
        .findLatestByMemberIdWithLock(memberId, PageRequest.of(0, 1))
        .stream().findFirst()
        .map(PointHistory::getAftBal)
        .orElse(0);
```

---

## 문제 3 — `@Transactional` 잘못된 import

### 발생 원인

`PointService`에 `jakarta.transaction.Transactional`이 import되어 있었다.
Spring의 트랜잭션 관리 기능(`readOnly`, propagation, isolation 등)을 제대로 활용하지 못하고,
모든 메서드가 쓰기 트랜잭션으로 동작하고 있었다.

```java
import jakarta.transaction.Transactional;  // 잘못된 import

@Transactional  // readOnly 옵션 없음 — 조회도 쓰기 트랜잭션
public class PointService { ... }
```

### 해결 방법

`org.springframework.transaction.annotation.Transactional`로 교체하고,
클래스 레벨에 `readOnly = true`를 적용했다. 쓰기 메서드에만 별도로 `@Transactional`을 선언했다.

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)  // 기본값: 조회 전용
public class PointService {

    @Transactional  // 쓰기 메서드에만 오버라이드
    public PointResponse savePoint(...) { ... }
}
```

---

## 문제 4 — 포인트 내역 조회 정렬 없음

### 발생 원인

`findByMemberId(Long memberId)`에 정렬 조건이 없어, DB 내부 저장 순서에 따라 내역이 제각각으로 반환됐다.

### 해결 방법

메서드명을 `findByMemberIdOrderByCreatedAtDesc`로 변경해 최신 내역이 먼저 오도록 했다.

---

## 문제 5 — DTO validation 미동작

### 발생 원인

컨트롤러에서 `@Valid`를 사용했지만 `PointSaveRequest` / `PointSpendRequest`에 제약 어노테이션이 없어
실질적인 입력값 검증이 이루어지지 않았다. `null`, `0`, 빈 문자열이 그대로 서비스까지 전달됐다.

### 해결 방법

각 DTO 필드에 `@NotNull`, `@Min(1)`, `@NotBlank` 어노테이션을 추가했다.

```java
@NotNull(message = "포인트 지급 사유를 입력해주세요.")
private PointReason reason;

@NotNull(message = "적립 포인트를 입력해주세요.")
@Min(value = 1, message = "적립 포인트는 1 이상이어야 합니다.")
private Integer amt;

@NotBlank(message = "포인트 설명을 입력해주세요.")
private String descrip;
```

---

## 문제 6 — 포인트 적립 트리거 미연결

### 발생 원인

`SavingsService.checkAndAwardPoints()`에 저축 목표 50%·100% 달성 시 포인트를 지급하는 코드가
TODO 주석으로만 남아 있었다. 저축 달성이 일어나도 실제 포인트가 적립되지 않았다.

```java
// TODO: 50% 달성 포인트 지급 API 호출부
// pointHistoryService.earnPoint(memberId, "50% 저축 달성", 100);
```

### 해결 방법

`SavingsService`에 `PointService` 의존성을 추가하고 실제 호출로 교체했다.

```java
// 50% 달성 — 100 포인트 지급
pointService.savePoint(memberId, PointSaveRequest.builder()
        .reason(PointReason.SAVINGS_50)
        .amt(100)
        .descrip("저축 목표 50% 달성 보상")
        .build());

// 100% 달성 — 500 포인트 지급
pointService.savePoint(memberId, PointSaveRequest.builder()
        .reason(PointReason.SAVINGS_100)
        .amt(500)
        .descrip("저축 목표 100% 달성 보상")
        .build());
```

---

## 문제 7 — SSE 알림이 트랜잭션 롤백과 무관하게 발송되는 문제

### 발생 원인

`NotificationService.send()`가 외부 트랜잭션에 참여(`REQUIRED`)하면서 `emitter.send()`를
**트랜잭션 커밋 전에** 즉시 호출했다. 이후 외부 트랜잭션이 롤백되면 DB의 알림 기록은 사라지지만
SSE는 이미 클라이언트에 전달된 상태가 된다.

```
depositToSavings() ← @Transactional 시작
  └─ notificationService.send()   ← 같은 트랜잭션 참여
       └─ notificationRepository.save()  ← DB 저장 (미커밋)
       └─ emitter.send()                 ← SSE 즉시 발송 ⚠️
  └─ 이후 예외 발생 → 롤백
       └─ DB 기록: 롤백됨
       └─ SSE: 이미 나간 상태 (취소 불가)
```

### 해결 방법

`TransactionSynchronizationManager`를 사용해 `afterCommit()` 콜백에서만 SSE push가 실행되도록 했다.
트랜잭션이 없는 환경에서의 직접 호출은 기존처럼 즉시 발송한다.

```java
if (TransactionSynchronizationManager.isActualTransactionActive()) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                pushToEmitter(memberId, notification);  // 커밋 후에만 실행
            }
        }
    );
} else {
    pushToEmitter(memberId, notification);  // 트랜잭션 없으면 즉시 발송
}
```

| 상황 | DB 알림 기록 | SSE 발송 |
|------|------------|---------|
| 트랜잭션 커밋 | 저장됨 | 발송됨 |
| 트랜잭션 롤백 | 롤백됨 | 발송 안 됨 |
| 트랜잭션 없이 직접 호출 | 저장됨 | 즉시 발송 |
