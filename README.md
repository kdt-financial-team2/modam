## 📌 프로젝트 개요

본 프로젝트는 K-디지털 트레이닝 단기 심화 부트캠프 과정의 일환으로, Spring Boot와 AWS 클라우드를 활용하여 **안정적이고 확장 가능한 금융 서비스 백엔드 아키텍처를 설계 및 구현**하는 팀 프로젝트입니다.

- **📅 개발 기간:** 2026.03.15 ~ 2026.06.12
- **👥 팀명:** 인텔리제이조 (총 6명)
- **🏫 교육 과정:** K-디지털 트레이닝 (단기 심화 부트캠프)
- **🎯 핵심 목표:** 단일 책임 원칙(SRP)을 준수하는 MVC 패턴 적용, 안전한 트랜잭션 처리, 클라우드 환경 배포
- **⚙️ 기술:** Spring Boot, AWS

<br/>

## 👥 팀원 및 역할

| 이름 | 포지션 | 역할 및 담당 업무 | GitHub Profile |
| :---: | :---: | :--- | :--- |
| **김병현** | Backend<br>*(PM · TechReader)* | • 팀 운영 & 프로젝트 관리<br>• PR 관리: 전체 140개 PR 중 48개 직접 리뷰 및 병합 (팀 코드 게이트 역할)<br>• API 스펙 관리: Swagger 문서 설계 및 유지 (팀 병렬 개발 가능)<br>• 개발 환경 표준화: .env 기반 환경변수 체계 도입 (팀원 로컬 환경 통일)<br>• 품질 관리: 12개 도메인 통합 테스트 41개 설계, 243개 전원 통과<br>• 브랜치 전략: dev / feat / refactor 전략 수립 및 운영 | [@bhkim-fullstack](https://github.com/bhkim-fullstack) |
| **이원석** | Backend<br>*(DB Architect · Infra Lead)* | • 데이터베이스 스키마 설계 및 형상 관리 (16개 핵심 도메인)<br>• AWS 클라우드 인프라 구축 및 운영 (RDS, EC2)<br>• 데이터베이스 명세서 및 ERD 다이어그램 산출물 설계 및 유지보수 | [@wonseok5577](https://github.com/wonseok5577) |
| **이지원** | Frontend<br>*(Frontend Lead)* | • 프론트엔드 아키텍처 총괄 및 UI/UX 리드 | [@jiwon0822](https://github.com/jiwon0822) |
| **박지연** | Backend<br>*(Presentation Designer)* | • 소비 제한(Spending Limit) 기능 개발<br>• 포인트(Point) 기능 개발<br>• JPA 일부 엔티티 작성 및 수정<br>• 계좌 개설 과정 내 개인정보 수정 기능 개선<br>• DB 설계서 및 요구사항 정의서 수정<br>• Item / Inventory 도메인 API 명세서 작성<br>• Figma UI 디자인 및 일부 화면 통합 작업<br>• QA 검증 및 오류 수정<br>• 프로젝트 PPT 제작 | [@jyeon64](https://github.com/jyeon64) |
| **임수아** | Frontend<br>*(UI/UX Designer)* | • 와이어프레임 설계 (로그인·회원가입·소비분석·소비제한·목표저축 등 전 페이지)<br>• Figma UI 디자인 및 통합 작업 (여러 화면 설계 및 반복 수정)<br>• 플로우차트 작성<br>• 소비내역·저축목표 페이지 백엔드 연결<br>• 회의록 전체 통합 정리<br>• QA 검증 및 오류 수정 (마이페이지 알림 설정 부분)<br>• Oracle DB 더미데이터 연결<br>• 포트폴리오 작성 | [@lSOOAH](https://github.com/lSOOAH) |
| **권순우** | Frontend | • 웹 페이지 UI 설계 (포인트 상점) | [@Soon5](https://github.com/soon5) |

<br/>

## 🛠 기술 스택

### Backend & DB
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white)

### Frontend
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)

### Infra & Tools
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

<br/>

## 🚀 주요 기능 및 사용자 (User Persona)

### 👤 주요 사용자
- **모임 개설자 (호스트):** 모임 통장을 최초로 개설하고 파트너에게 초대 코드를 발송하며, 초기 예산 한도와 공동 저축 목표를 세팅하는 주도적 커플 유저
- **모임 참여자 (파트너):** 전달받은 난수 초대 코드를 수락하여 모임 통장에 합류하며, 함께 금융 자산을 공유하고 소비 내역에 추억을 남기며 소통하는 커플 유저

### ✨ 핵심 기능
1. **회원 인증 및 맞춤형 온보딩:** - Spring Security 기반의 안전한 로그인 및 BCrypt 비밀번호 암호화
   - 로그인 성공 시 모임 통장 보유 여부에 따른 지능적 화면 라우팅
   - `SecureRandom` 기반 6자리 난수 발급 및 커스텀 HTML 이메일 초대장 발송

2. **공동 금융 자산 및 예산 관리:** - 실시간 계좌 잔액 동기화 및 누적 기여도(지분율) 산출
   - AES-256 양방향 암호화를 적용한 안전한 카드 정보 관리
   - 카테고리별 월간 소비 한도(예산) 설정 및 초과 시 경고 시스템

3. **소비 추억 기록 및 소통 (Memory & Communication):** - 단순한 결제 내역을 넘어, 결제 건별로 사진(CLOB)과 메모를 남기는 추억 타임라인 제공
   - 파트너의 소비 스토리에 대한 즐겨찾기(하트) 토글 및 상점 이모티콘 피커를 활용한 댓글 소통

4. **공동 저축 미션 (Savings Goals):** - 여행, 선물 등 커플 공동의 목적을 위한 저축 목표(Goal) 생성
   - 직접 수동 납입 및 스케줄러 기반의 주기적 자동 이체 기능
   - 달성률(50%, 100%)에 따른 리워드 포인트 자동 지급 및 중복 수령 방어

5. **게이미피케이션 및 커플 감성 (상점 & 인벤토리):** - 일일 출석 체크 및 저축 달성을 통해 시스템 포인트 획득
   - 포인트 상점에서 커플 화면 테마 및 소통용 이모티콘 구매 (DB 복합 유니크 제약으로 중복 결제 원천 차단)
   - 마이페이지 인벤토리에서 구매한 테마 장착 및 실시간 UI 반영

6. **SSE 기반 실시간 푸시 알림:** - 입출금 발생, 예산 한도 초과 위험, 저축 목표 달성, 파트너 초대 등 주요 비즈니스 이벤트 발생 시 Server-Sent Events(SSE)를 활용한 실시간 무중단 푸시 알림 전송

<br/>

## 🏗 시스템 아키텍처 및 ERD

### System Architecture
<img width="3352" height="2924" alt="image" src="https://github.com/user-attachments/assets/9b9d4292-256e-4f74-a827-e3727d115991" />

### ERD (Entity Relationship Diagram)
<img width="4416" height="2228" alt="github업로드ERD" src="https://github.com/user-attachments/assets/e52a5f8b-610f-4e54-b61c-37a1f452f644" />




<br/>

## 🔒 보안 및 정책
- **인증/인가:** Spring Security를 활용한 세션/토큰 기반 인증 처리 및 관리자 URL/권한 분리
- **정보 보호:** DB 비밀번호 및 API Key는 `application.properties` (또는 `.env`)로 분리하여 관리
- **보안 수칙:** 민감 정보(Secret Key 등)는 절대 GitHub에 커밋하지 않도록 `.gitignore` 적용 철저

<br/>

## 🗓 개발 일정 (Milestones)

| 기간 | 마일스톤 | 세부 내용 |
| :--- | :--- | :--- |
| **1~2주차**<br>(3월 중순) | 기획 및 설계 | 요구사항 분석, 화면 설계, DB 모델링(ERD), API 명세서 작성 |
| **3~6주차**<br>(4월 ~ 5월 초) | 핵심 기능 구현 | 회원 인증, 금융 거래 기능(트랜잭션), 데이터 연동 로직 개발 |
| **7~9주차**<br>(5월 중순) | 프론트 연동 & 심화 | Thymeleaf 뷰 템플릿 연동, 추가 도메인 기능 구현 및 리팩토링 |
| **10주차**<br>(6월 초) | 배포 및 테스트 | AWS 배포, 버그 픽스, 최종 발표 준비 |

<br/>

## 📂 프로젝트 구조 (Project Structure)

```text
📦 src
 ┣ 📂 main
 ┃ ┣ 📂 java
 ┃ ┃ ┗ 📂 com/intelliJ_JO/modam
 ┃ ┃   ┣ 📂 config                     # 전역 환경 설정 및 보안 아키텍처
 ┃ ┃   ┃ ┣ 📂 security                 # Spring Security 인증/인가 제어
 ┃ ┃   ┃ ┃ ┣ 📜 SecurityConfig
 ┃ ┃   ┃ ┃ ┣ 📜 CustomUserDetails
 ┃ ┃   ┃ ┃ ┣ 📜 CustomUserDetailsService
 ┃ ┃   ┃ ┃ ┣ 📜 CustomAuthenticationSuccessHandler
 ┃ ┃   ┃ ┃ ┗ 📜 CustomAuthenticationFailureHandler
 ┃ ┃   ┃ ┣ 📜 PasswordEncoderConfig    # BCrypt 단방향 암호화 설정
 ┃ ┃   ┃ ┣ 📜 SwaggerConfig            # REST API 문서 자동화 설정
 ┃ ┃   ┃ ┗ 📜 WebMvcConfig             # 인터셉터 및 정적 리소스 매핑
 ┃ ┃   ┣ 📂 domain                     # 비즈니스 도메인 계층 (DDD 패턴)
 ┃ ┃   ┃ ┣ 📂 member                   # 회원 도메인
 ┃ ┃   ┃ ┣ 📂 couple                   # 커플 메타 관리 도메인
 ┃ ┃   ┃ ┣ 📂 account                  # 개인/모임 계좌 도메인
 ┃ ┃   ┃ ┣ 📂 transaction              # 입출금/결제 금융 거래 원장 도메인
 ┃ ┃   ┃ ┣ 📂 spendrecord              # 소비 스토리(추억) 기록 도메인
 ┃ ┃   ┃ ┣ 📂 comment                  # 소비 기록 하위 소통 댓글 도메인
 ┃ ┃   ┃ ┣ 📂 analysis                 # 통계 및 소비 인사이트 분석 도메인
 ┃ ┃   ┃ ┣ 📂 savings                  # 공동 저축 목표 및 자동이체 도메인
 ┃ ┃   ┃ ┣ 📂 point                    # 포인트 적립 및 가감 이력 도메인
 ┃ ┃   ┃ ┣ 📂 shop                     # 상품 추천 및 포인트 상점 도메인
 ┃ ┃   ┃ ┣ 📂 item                     # 상점 테마/이모티콘 상품 원장 도메인
 ┃ ┃   ┃ ┣ 📂 inventory                # 유저별 아이템 보유 및 장착 도메인
 ┃ ┃   ┃ ┣ 📂 attendance               # 1일 1회 출석 체크 도메인
 ┃ ┃   ┃ ┗ 📂 notification             # SSE 기반 실시간 알림 푸시 도메인
 ┃ ┃   ┗ 📂 global                     # 전역 공통 인프라 계층
 ┃ ┃     ┣ 📂 exception                # @RestControllerAdvice 전역 에러 핸들링
 ┃ ┃     ┣ 📂 init                     # 초기 목업 데이터 세팅 및 CLOB 마이그레이션
 ┃ ┃     ┣ 📂 interceptor              # Tomcat 버퍼 조기 커밋 대응 CSRF 인터셉터
 ┃ ┃     ┣ 📂 response                 # GlobalResponse 규격화된 공통 API 응답
 ┃ ┃     ┣ 📂 util                     # AES256 금융 데이터 양방향 암호화 유틸
 ┃ ┃     ┗ 📂 view                     # 각 도메인별 Thymeleaf 뷰 레이어 컨트롤러
 ┃ ┗ 📂 resources
 ┃   ┣ 📂 templates                   # Thymeleaf HTML 뷰 템플릿 영역
 ┃   ┃ ┣ 📂 domain                    # 각 도메인별 화면단 레이아웃 (auth, dashboard 등)
 ┃   ┃ ┗ 📂 layout                    # 공통 GNB, 헤더, 푸터 프레그먼트
 ┃   ┣ 📂 static                      # 정적 리소스 리소스 (CSS, JS, Images, uploas)
 ┃   ┗ 📜 application.properties       # Oracle 21c XE DB 연결 및 암호화 시크릿 키 설정
 ┗ 📜 build.gradle                    # Spring Boot 3.x 및 시큐리티, JPA 의존성 관리
