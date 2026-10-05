# YUM-YUM Backend

React 프론트엔드와 연동할 Spring Boot 백엔드의 기본 프로젝트입니다.
Firebase 계정 생성 API와 Firebase ID 토큰을 검증하는 Spring Security 인증을 포함합니다.
회원가입 시 Firebase UID와 프로필·목표·동의·최초 체중을 PostgreSQL에 저장하고, 성공 후 로그인 페이지로 이동합니다.

## 구성

- Java 21 호환 바이트코드 (`options.release = 21`)
- Spring Boot 4.1.1
- Gradle 9.7.1 / Groovy DSL / Gradle Wrapper
- Spring Web MVC, Validation
- Spring Security OAuth2 Resource Server, Firebase Admin SDK
- 기본 패키지: `com.yumyum.backend`

Gradle 실행에는 JDK 21 이상 중 Gradle 9.7.1과 Spring Boot 4.1.1이 지원하는 버전을 사용하세요.
Java toolchain을 특정 버전으로 고정하지 않고 설치된 JDK로 Java 21 호환 코드를 컴파일합니다.
처음 실행할 때는 Gradle과 의존성을 다운로드하기 위한 인터넷 연결이 필요합니다.

## 실행

이 디렉토리에서 실행합니다. Windows에서는 `./gradlew` 대신 `gradlew.bat`을 사용합니다.

```bash
set -a
source .env
set +a
./gradlew bootRun
```

`.env`에는 `GOOGLE_APPLICATION_CREDENTIALS="/실제/절대경로/serviceAccountKey.json"`을 설정합니다.
JSON은 저장소 밖에 보관하고, 프론트엔드와 같은 Firebase 프로젝트의 서비스 계정을 사용합니다.
기본 포트는 `8080`입니다. 회원가입 API를 제외한 일반 요청은 인증이 필요합니다.

## 프론트엔드 연결 확인

별도 터미널에서 저장소 루트 기준으로 프론트엔드를 실행합니다.

```bash
cd yum-yum
npm install
npm run dev
```

Vite가 출력하는 주소에서 `/signup`으로 접속하여 가입 폼을 제출합니다.
Vite 개발 서버가 `/api` 요청을 `http://localhost:8080`으로 전달하므로 별도 CORS 설정은 필요하지 않습니다.
이 프록시는 개발 서버에만 적용되며, 배포 환경의 API 연결은 이후 설정합니다.

- `POST /api/auth/signup`: Firebase Admin SDK로 이메일·비밀번호 계정을 생성하고 이름을 displayName으로 설정합니다.
- 성공하면 `/login`으로 이동하며 자동 로그인은 수행하지 않습니다.
- 중복 이메일은 `409`, 입력값 오류는 `400`, Firebase 서비스 오류는 `503`을 반환합니다.
- PostgreSQL의 `users`, `user_settings`, `user_consents`, `weight_logs`를 한 트랜잭션으로 저장합니다. 비밀번호는 DB에 저장하지 않습니다.
- DB 저장 또는 커밋 실패 시 이번 요청에서 생성한 Firebase 계정을 삭제합니다. 삭제까지 실패하면 로그의 `회원가입 복구 필요` UID로 수동 정리해야 합니다.
- 비밀번호와 건강 정보는 로그에 출력하지 않습니다.
- 목표 체중을 비워두면 가입 체중을 사용합니다. 가입일과 연도는 `Asia/Seoul` 기준입니다.
- 현재 API의 키·체중은 정수입니다. 소수 체중 지원은 요청 DTO와 프론트 변환을 함께 변경해야 합니다.
- 기존 테이블이 필요하며 `spring.jpa.hibernate.ddl-auto=validate`로 `users` 매핑을 검사합니다. Hibernate는 테이블을 생성하거나 변경하지 않습니다.
- `signup.terms-version`은 현재 약관 스냅샷을 식별하는 `2026-10-05`로 설정했습니다. 약관 변경 시 이 값도 갱신하세요.
- Firebase Console의 Authentication에서 이메일/비밀번호 로그인을 활성화하고, 프론트엔드와 백엔드가 같은 프로젝트를 사용해야 합니다.
- 응답 성공 시 프론트엔드에 요청 수신 안내가 표시됩니다. 입력한 값은 저장되지 않습니다.

## Firebase JWT와 Spring Security

```text
React에서 Firebase 로그인 → ID 토큰(JWT) 발급
→ Authorization: Bearer <ID 토큰>으로 API 요청
→ BearerTokenAuthenticationFilter → AuthenticationManager
→ FirebaseAuthenticationProvider → FirebaseAuth.verifyIdToken(token, true)
→ Authentication / SecurityContext → Controller
```

Firebase Admin SDK가 JWT 서명, 만료, 발급자, 대상 프로젝트를 검증합니다.
`checkRevoked=true`로 토큰 폐기와 계정 사용 중지도 확인하므로 검증 시 Firebase 서버 조회가 추가됩니다.
Spring Security 기본 필터가 요청마다 인증을 수행하고 인증 컨텍스트를 정리하며, HTTP 세션은 생성하지 않습니다.
현재 검증된 사용자에게는 `ROLE_USER`만 부여하며, 관리자 권한은 아직 구현하지 않았습니다.

| 요청 | 접근 조건 |
| --- | --- |
| `POST /api/auth/signup` | 비로그인 허용, Firebase 계정 생성 및 PostgreSQL 저장 |
| `GET /api/users/me` | 유효한 Firebase ID 토큰과 `ROLE_USER` 필요 |
| 그 외 일반 요청 | 인증 필요 |

토큰 누락 또는 검증 실패는 `401`, 인증 후 권한 부족은 `403`으로 응답합니다.
인증은 Authorization 헤더만 사용하므로 CSRF는 비활성화했습니다. 추후 인증 쿠키를 사용하면 재검토해야 합니다.

### 프론트엔드에서 확인

1. 백엔드와 Vite 개발 서버를 실행합니다.
2. `/signup`에서 가입 후 `/login`으로 이동하는지 확인하고, 생성한 계정으로 로그인합니다. Firebase Console의 Authentication → Users에서도 생성 여부를 확인할 수 있습니다.
3. 브라우저 개발자 도구의 Network에서 `GET /api/users/me`의 `200` 응답과 `uid`, `email`을 확인합니다.
4. 로그아웃하면 Firebase 세션과 프론트엔드 로그인 상태를 함께 해제합니다.

`authApi.js`의 `authenticatedRequest`는 Firebase SDK에서 ID 토큰을 받아 헤더에 추가합니다.
SDK가 만료된 ID 토큰을 갱신하며, 애플리케이션이 별도로 JWT를 localStorage에 저장하지 않습니다.
기존 식단·프로필 서비스의 Firebase 직접 호출은 아직 유지되며, 앞으로 백엔드 API로 옮길 때 이 함수를 사용합니다.
프론트엔드에는 기존 웹 앱용 `VITE_FIREBASE_*` 설정이 필요합니다.

### 터미널에서 확인

```bash
# 토큰 없음: 401
curl -i http://localhost:8080/api/users/me

# 잘못된 토큰: 401
curl -i -H 'Authorization: Bearer invalid-token' http://localhost:8080/api/users/me
```

## 주요 파일

- `config/FirebaseConfig.java`: FirebaseApp 및 FirebaseAuth Bean 등록
- `config/SecurityConfig.java`: 접근 정책, 세션 정책, AuthenticationManager 구성
- `security/FirebaseAuthenticationProvider.java`: Firebase 토큰 검증 및 Authentication 생성
- `security/FirebasePrincipal.java`: 검증된 UID와 이메일
- `user/UserController.java`: 인증된 사용자 조회

## 테스트 및 빌드

```bash
./gradlew build
```

테스트를 실행하고 `build/libs/`에 실행 가능한 JAR를 생성합니다.
기본 테스트에서는 Firebase와 프로필 저장 서비스를 Mock으로 대체하고 DB 자동 설정을 제외하므로 실제 Firebase/DB 연결이나 서비스 계정 JSON이 필요하지 않습니다.
계정 생성, 중복 이메일·입력값·서비스 오류, DB 실패 시 계정 삭제와 삭제 실패 처리, 토큰 인증을 확인합니다.

실제 PostgreSQL 저장과 롤백 검증은 네 가입 테이블이 있는 개발 DB에서 별도로 실행합니다. Firebase는 Mock을 사용하며 테스트 프로필은 롤백됩니다. ID 시퀀스 값은 증가할 수 있습니다.

```bash
set -a
source .env
set +a
RUN_DB_TESTS=true ./gradlew test
```

IntelliJ로 백엔드를 실행할 때는 Run Configuration의 환경변수에 `DB_USER`, `DB_PASSWORD`, `GOOGLE_APPLICATION_CREDENTIALS`를 설정하세요.
애플리케이션 설정은 `src/main/resources/application.properties`에서 관리합니다.
