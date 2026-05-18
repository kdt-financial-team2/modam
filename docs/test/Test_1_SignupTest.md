# 회원가입 흐름 테스트 — SignupTest

- **파일**: `src/test/java/com/intelliJ_JO/modam/feat/SignupTest.java`
- **대상 API**: `POST /api/member`
- **Security 필터**: 비활성화 (`addFilters = false`)

## 화면 흐름

```
[화면 2: 약관 동의] → [화면 3: 정보 입력] → [화면 4: 계좌 연결]
```

## 클래스 설정

```java
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)   // 보안 필터 비활성화 — 인증 없이 API 호출
@Transactional                              // 테스트 종료 후 DB 롤백
@DisplayName("회원가입 흐름 테스트 (화면 1→2→3→4→5)")
class SignupTest { ... }
```

## baseRequest() — 기본 유효 요청

각 테스트는 아래 기본값에서 필요한 필드만 변경해 사용합니다.

```java
MemberCreateRequest.builder()
    .userId("testuser1")
    .pw("password123").pwConfirm("password123")
    .name("홍길동").enFirst("Gildong").enLast("Hong")
    .email("test1@modam.com")
    .phoneNo("01012345671")
    .zipCode("06236").address("서울시 강남구 테헤란로 123").addressDetail("101호")
    .bankName("신한은행").persAcctNo("11012345678901")
    .rrn("900101123456")
    .agreeAge(true).agreeService(true).agreePrivacy(true).agreeFinance(true)
    .notif(false).agreeThirdParty(false)
    .build();
```

---

## 화면 2 — 약관 동의

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `정상_회원가입_성공` | 모든 조건 충족 | 200, `data.userId = testuser1` |
| `필수_약관_만14세_미동의` | `agreeAge = false` | 400 |
| `필수_약관_서비스_미동의` | `agreeService = false` | 400 |
| `필수_약관_개인정보_미동의` | `agreePrivacy = false` | 400 |
| `필수_약관_전자금융_미동의` | `agreeFinance = false` | 400 |
| `선택_약관_미동의_가입_성공` | `notif = false`, `agreeThirdParty = false` | 200 |

---

## 화면 3 — 정보 입력

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `비밀번호_확인_불일치` | `pw ≠ pwConfirm` | 400, `message = "비밀번호가 일치하지 않습니다."` |
| `아이디_중복_가입` | 동일 `userId`로 재가입 시도 | 400, `message = "이미 사용 중인 아이디입니다."` |
| `이메일_중복_가입` | 동일 `email`로 재가입 시도 | 400, `message = "이미 사용 중인 이메일입니다."` |
| `휴대폰번호_중복_가입` | 동일 `phoneNo`로 재가입 시도 | 400, `message = "이미 사용 중인 휴대폰 번호입니다."` |
| `필수_필드_이름_누락` | `name = ""` | 400 |
| `이메일_형식_오류` | `email = "not-an-email"` | 400 |
| `휴대폰번호_형식_오류` | `phoneNo = "010-1234-5678"` (하이픈 포함) | 400 |

---

## 화면 4 — 계좌 연결

| 테스트명 | 조건 | 예상 결과 |
|---|---|---|
| `계좌번호_문자_포함` | `persAcctNo = "1101-234-5678"` (하이픈 포함) | 400, `message = "계좌번호는 숫자만 입력 가능합니다."` |
| `우편번호_형식_오류` | `zipCode = "062"` (5자리 미만) | 400 |

---

## 실행 방법

```bash
./gradlew test --tests "com.intelliJ_JO.modam.feat.SignupTest"
```
