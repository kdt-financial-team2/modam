# 로그인 및 모임통장 개설 흐름 테스트 — LoginAndAccountCreateTest

- **파일**: `src/test/java/com/intelliJ_JO/modam/feat/LoginAndAccountCreateTest.java`
- **대상 API**: `POST /login`, `GET /api/accounts/me/group-status`, `GET /api/accounts/preview-number`, `POST /api/accounts`
- **Security 필터**: 활성화 (실제 로그인 흐름 검증)

## 화면 흐름

```
[화면 1: 스플래시]
    ↓ 로그인 버튼 클릭
[화면 2: 로그인]
    ├─ 모임통장 있음 → [메인 대시보드]
    └─ 모임통장 없음 ↓
[화면 3: 서비스 약관 동의]
    ↓ 계좌개설 버튼 클릭 (계좌번호 미리 생성)
[화면 4: 계좌 개설 상세 입력]
    ↓ 계좌개설 버튼 클릭
[화면 5: 완료 → 메인 대시보드]
```

## 클래스 설정

```java
@SpringBootTest
@AutoConfigureMockMvc           // 보안 필터 활성화 — 실제 로그인 흐름 테스트
@Transactional                  // 테스트 종료 후 DB 롤백
@DisplayName("로그인 및 모임통장 개설 흐름 테스트 (화면 1→2→3→4→5)")
class LoginAndAccountCreateTest { ... }
```

## @BeforeEach — 테스트 회원 생성

모든 테스트 실행 전 아래 회원을 DB에 직접 저장합니다.

```
userId: testuser / password: password123
```

## 헬퍼 메서드

### loginAndGetSession()

```java
// 로그인 후 세션 반환 — 이후 인증 필요 요청에 .session(session)으로 첨부
MvcResult result = mockMvc.perform(formLogin("/login")
        .user("userId", "testuser")
        .password("password", "password123"))
    .andExpect(authenticated())
    .andReturn();
MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
```

### baseAccountRequest()

```
accountType: GROUP
password: "1234" / passwordConfirm: "1234"
onceTransferLimit: 1,000,000 / dailyTransferLimit: 5,000,000
jobInfo: "직장인" / tradePurpose: "모임비 관리" / fundSource: "급여"
agreeService: true / agreePrivacy: true
agreeMarketing: false / agreeThirdParty: false
```

---

## 화면 2 — 로그인

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `정상_로그인_성공` | 올바른 아이디·비밀번호 | `authenticated()` |
| `존재하지_않는_아이디_로그인_실패` | 없는 아이디 | `unauthenticated()` |
| `비밀번호_오류_로그인_실패` | 틀린 비밀번호 | `unauthenticated()` |
| `로그인_후_모임통장_없으면_계좌개설_화면으로` | 모임통장 미보유 | 200, `hasGroupAccount = false` |
| `로그인_후_모임통장_있으면_대시보드로` | 모임통장 보유 | 200, `hasGroupAccount = true`, `accountNumber` 반환 |

---

## 화면 3 → 4번 화면 진입 — 계좌번호 미리 보기

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `계좌번호_미리보기_반환` | 로그인 세션 보유 | 200, `accountNumber` 16자리 대문자+숫자 |
| `비로그인_계좌번호_미리보기_접근_불가` | 세션 없음 | 3xx 리다이렉트 |

---

## 화면 4 — 계좌 개설 상세 입력

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `정상_계좌_개설_성공` | 모든 항목 정상 입력 | 200, `accountType = GROUP`, 이체한도 포함 응답 |
| `계좌_비밀번호_자리수_부족` | `password = "123"` (3자리) | 400 |
| `계좌_비밀번호_자리수_초과` | `password = "12345"` (5자리) | 400 |
| `계좌_비밀번호_문자_포함` | `password = "12ab"` | 400, `message = "계좌 비밀번호는 숫자 4자리여야 합니다."` |
| `계좌_비밀번호_불일치` | `password ≠ passwordConfirm` | 400, `message = "계좌 비밀번호가 일치하지 않습니다."` |
| `서비스_이용약관_미동의` | `agreeService = false` | 400, `message = "서비스 이용약관 동의는 필수입니다."` |
| `개인정보_처리방침_미동의` | `agreePrivacy = false` | 400, `message = "개인정보 처리방침 동의는 필수입니다."` |
| `선택_약관_미동의_계좌_개설_성공` | `agreeMarketing = false`, `agreeThirdParty = false` | 200 |
| `비로그인_계좌_개설_시도` | 세션 없음 | 3xx 리다이렉트 |

---

## 화면 5 — 완료

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `계좌개설_완료_후_대시보드_이동` | 계좌 개설 직후 `group-status` 재조회 | 200, `hasGroupAccount = true` |

---

## 실행 방법

```bash
./gradlew test --tests "com.intelliJ_JO.modam.feat.LoginAndAccountCreateTest"
```
