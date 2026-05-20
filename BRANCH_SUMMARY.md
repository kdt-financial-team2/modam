# merge/front-backend 브랜치 작업 요약

> 기준 브랜치: `main` → `merge/front-backend`
> 작업 기간: 2026-05 기준

---

## 1. 프론트엔드 연동 (Thymeleaf MVC)

### 뷰 컨트롤러 구조 구축
- `global/view/` 하위에 도메인별 뷰 컨트롤러 생성
  - `AuthViewController`, `DashboardViewController`, `TransactionViewController`
  - `RecordViewController`, `MypageViewController`, `CardViewController`
  - `SavingsViewController`, `ShopViewController`, `InviteViewController`
- 각 컨트롤러에서 HTML 템플릿 반환 (MVC 패턴 유지)

### Thymeleaf 템플릿 뼈대 생성
- `templates/domain/` 하위 전체 화면 HTML 파일 생성
  - 인증: `login.html`, `signup.html`, `terms.html`, `account-setup.html`
  - 대시보드: `dashboard.html`, `user_dashboard.html`
  - 거래: `transfer.html`, `transaction-history.html`
  - 소비기록: `consumption-history.html`, `consumption-upload.html`, `consumption-detail.html`, `spending-limit.html`, `spending-analysis.html`
  - 저축: `savings.html`, `savings-goal-setup.html`
  - 카드: `card-step1~8.html`, `card_apply.html`
  - 포인트샵: `point-shop.html`, `point-shop-product.html`, `point-shop-purchase.html`
  - 마이페이지: `mypage.html`, `my_profile.html`, `theme-settings.html`
  - 공통 레이아웃: `header.html`, `footer.html`, `bottom_nav.html`

### MVC POST 핸들러 연결
- **테마 설정** (`/theme-settings`): `MyPageService.updateTheme()` 연동
- **소비 한도** (`/spending-limit`): `SpendingLimitService.saveSpendingLimits()` 연동

### 회원가입 다단계 폼
- 세션 기반 4단계 회원가입 흐름 구현 (`SignupForm` DTO 신규 생성)
  - Step1: 약관 동의
  - Step2: 개인 정보 입력 (아이디/비밀번호/이름/주민번호/연락처/주소)
  - Step3: 연결 은행 계좌 입력 → `MemberService.createMember()` 호출
  - Step4: 완료 화면

---

## 2. 인증 / 보안 (Spring Security)

### 로그인 버그 수정
- `login.html` 폼 필드명 `name="username"` → `name="userId"` 수정
  - `SecurityConfig.usernameParameter("userId")`와 불일치 문제 해결
- 로그인 실패 시 한국어 에러 메시지 화면 표시
  - `SPRING_SECURITY_LAST_EXCEPTION` 세션에서 읽어 `loginError` 모델 전달
  - `DisabledException`, `BadCredentialsException` 구분 처리

### SecurityConfig 설정 추가
- `frameOptions().sameOrigin()` — H2 콘솔 iframe 허용
- CSRF 예외 경로 추가: `/h2-console/**`, `/api/**`
  - REST API는 `Content-Type: application/json` 사용으로 CSRF 불필요
- `permitAll` 경로 추가: `/swagger-ui/**`, `/v3/api-docs/**`, `/h2-console/**`
- 로그인 성공 후 이동 경로: `/` → `/dashboard` 변경

### 접근 권한 정비
- 공개 경로 명시: `/`, `/login`, `/signup/**`, `/terms`, `/css/**`, `/js/**`, `/images/**`

---

## 3. Swagger (springdoc-openapi)

### 버전 업그레이드
- `springdoc-openapi` `2.0.2` → `2.7.0` (Spring Boot 3.5.x 호환)
  - 기존 버전에서 `NoSuchMethodError: ControllerAdviceBean` 발생

### 설정 수정
- `SwaggerConfig`에서 잘못된 `@RestController` 어노테이션 제거
- `application.yaml` swagger-ui URL 수정: `/swagger.yml` → `/v3/api-docs`

### 전체 컨트롤러 Swagger 어노테이션 추가
| 컨트롤러 | `@Tag` |
|---------|--------|
| MemberController | 회원 |
| AccountController | 계좌 |
| TransactionController | 거래 내역 |
| CardController | 카드 |
| SavingsController | 저축 |
| InviteController | 초대 |
| SpendRecordController | 소비 기록 |
| CommentController | 소비 기록 댓글 |
| PointController | 포인트 |
| NotificationController | 알림 |
| AnalysisController | 소비 분석 |

---

## 4. 신규 도메인 구현

### 소비 한도 (SpendingLimit)
- `SpendingLimit` 엔티티 — `(mem_id, category)` 복합 유니크 제약
- `SpendingLimitRepository` — `findByMemberId`, `findByMemberIdAndCategory`
- `SpendingLimitService` — 카테고리별 이번 달 지출 합계 + 예산 병합
- `SpendingLimitDto` — 카테고리, 아이콘, 지출액, 예산, 달성률
- 사전 정의 카테고리: 식비/교통/쇼핑/의료/문화여가/기타

### 테마 설정 (MyPage)
- `Member.theme` 필드 추가 (`@Builder.Default = "pink"`)
- `MyPageService.getTheme()`, `updateTheme()` 구현

---

## 5. 백엔드 기능 구현 (기존 브랜치 포함)

| 기능 | 내용 |
|------|------|
| 포인트 도메인 | `PointHistory`, `PointType` 엔티티, CRUD 서비스/컨트롤러 |
| 소비 분석 | `AnalysisController` — 월별 요약, 트렌드 API |
| 소비 기록 댓글 | `CommentController` — 작성/조회/수정/삭제 |
| SSE 실시간 알림 | `NotificationController` — SSE 구독, 읽음 처리 |
| Savings 납입 | `TransactionService` 연동, 이중 차감 방지 |
| 카드 발급 | 암호화 카드번호 생성 |
| 이체 한도 | `Account`에 `onceTransferLimit`, `dailyTransferLimit` 컬럼 추가 |
| 전역 예외 핸들러 | `GlobalExceptionHandler` — 400/409 공통 응답 |

---

## 6. 설정 변경

| 항목 | 변경 전 | 변경 후 |
|------|--------|--------|
| H2 데이터소스 | 인메모리 | 파일 기반 (`jdbc:h2:file:./data/modamdb`) |
| ddl-auto | create | update |
| show-sql | true | false |
| H2 콘솔 | 비활성 | 활성 (`/h2-console`) |

---

## 7. 테스트

- `LoginAndAccountCreateTest` — 로그인/계좌 개설 통합 테스트 (15개)
  - CSRF 설정 수정으로 10개 실패 → 전체 통과

---

## 8. 미구현 / 잔여 작업

| 항목 | 상태 |
|------|------|
| `POST /dashboard/couple-info` | 핸들러 없음 — 커플 정보 엔티티 미구현 |
| `POST /dashboard/check-in` | 핸들러 없음 — 출석 체크 엔티티 미구현 |
| 대시보드 실제 데이터 연동 | 현재 전부 기본값(0, 빈 리스트) |
| 카드 발급 POST 핸들러 (step1~8) | 뷰 컨트롤러 미연결 |
| 계좌 개설 POST 핸들러 (`/account-setup`) | 뷰 컨트롤러 미연결 |
| 이체 POST 핸들러 (`/transfer`) | 뷰 컨트롤러 미연결 |
| 소비 기록 업로드/삭제 POST 핸들러 | 뷰 컨트롤러 미연결 |
| 저축 목표 설정 POST 핸들러 | 뷰 컨트롤러 미연결 |
| 포인트샵 구매 POST 핸들러 | 뷰 컨트롤러 미연결 |
| 마이페이지 회원탈퇴 POST 핸들러 | 뷰 컨트롤러 미연결 |
| 초대 연결 POST 핸들러 | 뷰 컨트롤러 미연결 |
