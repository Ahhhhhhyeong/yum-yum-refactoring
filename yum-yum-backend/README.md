# YUM-YUM Backend

React 프론트엔드와 연동할 Spring Boot 백엔드의 기본 프로젝트입니다.
회원가입 요청 수신 API와 Firebase ID 토큰을 검증하는 Spring Security 인증을 포함합니다.
회원가입 시 실제 Firebase 계정 생성과 DB 저장은 이후 추가합니다.

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

- `POST /api/auth/signup`: 폼 값을 수신하고 백엔드 콘솔에 `회원가입 요청 수신` 로그를 출력합니다.
- 비밀번호는 수신 여부만 출력하며, 응답에도 포함하지 않습니다.
- 테스트 단계에서는 이메일 중복확인을 생략하고 Auth 계정 생성, Firestore 저장, 자동 로그인을 수행하지 않습니다.
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
| `POST /api/auth/signup` | 비로그인 허용, 현재는 요청 수신만 수행 |
| `GET /api/users/me` | 유효한 Firebase ID 토큰과 `ROLE_USER` 필요 |
| 그 외 일반 요청 | 인증 필요 |

토큰 누락 또는 검증 실패는 `401`, 인증 후 권한 부족은 `403`으로 응답합니다.
인증은 Authorization 헤더만 사용하므로 CSRF는 비활성화했습니다. 추후 인증 쿠키를 사용하면 재검토해야 합니다.

### 프론트엔드에서 확인

1. 백엔드와 Vite 개발 서버를 실행합니다.
2. 기존 Firebase 계정으로 `/login`에서 로그인합니다. 요청 수신용 회원가입 API는 아직 계정을 생성하지 않습니다.
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
테스트에서는 FirebaseApp과 FirebaseAuth를 Mock으로 대체하므로 서비스 계정 JSON이나 실제 Firebase 연결이 필요하지 않습니다.
공개 회원가입, 토큰 누락/검증 실패, 유효한 토큰의 사용자 조회, 요청 사이 인증 분리, 권한 부족을 확인합니다.
애플리케이션 설정은 `src/main/resources/application.properties`에서 관리합니다.
