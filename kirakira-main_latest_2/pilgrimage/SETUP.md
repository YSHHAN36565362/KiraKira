# 프로젝트 실행 가이드 (팀원용)

이 문서는 git에서 프로젝트를 받아 로컬에서 실행하기까지의 전체 과정을 정리한 것입니다.

## 0. 준비물

- **JDK 17**
- **MariaDB** (로컬 설치 후 실행 상태여야 함)
- **IntelliJ IDEA** (Community Edition으로 충분)

## 1. 프로젝트 클론

```
git clone https://github.com/JaeGeun298/kirakira.git
cd kirakira/pilgrimage
```

> ⚠️ GitHub의 "Download ZIP"으로 받지 말고 꼭 `git clone`을 사용하세요. ZIP은 받은 시점의 스냅샷이라 이후 수정사항이 자동 반영되지 않습니다. 이미 ZIP으로 받았다면 `git clone`으로 다시 받거나, 그 폴더에서 `git init` 대신 최신 코드로 교체하세요.

## 2. MariaDB에 DB / 계정 생성

MariaDB 클라이언트(mysql CLI, HeidiSQL, DBeaver 등)로 접속해서 아래 SQL 실행:

```sql
CREATE DATABASE pilgrimage CHARACTER SET utf8mb4;
CREATE USER 'pilgrimage'@'localhost' IDENTIFIED BY 'pilgrimage';
GRANT ALL PRIVILEGES ON pilgrimage.* TO 'pilgrimage'@'localhost';
FLUSH PRIVILEGES;
```

### ⚠️ 포트 확인 필수

프로젝트 기본 설정(`application.yml`)은 **MariaDB 포트 3307**을 사용합니다. 새로 설치한 MariaDB는 보통 기본 포트가 **3306**이므로, 본인 PC의 실제 포트를 꼭 확인하세요.

```sql
SHOW VARIABLES LIKE 'port';
```

포트가 다르면 둘 중 하나로 맞춰줍니다.

- MariaDB 설정 자체를 3307로 변경, 또는
- IntelliJ Run 설정 → Environment variables 에 `DB_PORT=3306` (실제 포트 값) 추가

다른 접속 정보도 필요하면 환경변수로 덮어쓸 수 있습니다: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.

## 3. IntelliJ에서 프로젝트 열기

1. IntelliJ → `File > Open` → 클론 받은 `pilgrimage` 폴더 선택
2. Gradle Wrapper가 포함되어 있어 자동으로 동기화됨 (최초 1회 인터넷 필요, JDK 17 자동 설정)
3. 이때 "Spring Security / Spring Web / Spring Data / Tailwind CSS 등은 Ultimate 구독이 있어야 사용 가능" 이라는 플러그인 추천 팝업이 뜰 수 있습니다.
   → **그냥 취소(닫기)** 하면 됩니다. 프로젝트를 열고 실행하는 것과는 무관한, IDE의 선택적 기능 추천일 뿐입니다.

## 4. 초기 더미 데이터 넣기 (최초 1회, 수동)

- 테이블 자체는 `ddl-auto: update` 설정으로 앱을 처음 실행하면 자동 생성됩니다.
- 하지만 더미 데이터(장소 목록, 관리자 계정)가 담긴 `src/main/resources/data.sql`은 **MariaDB에는 자동 실행되지 않습니다** (H2를 쓰는 테스트에만 자동 적용).
- 앱을 한 번 실행해서 테이블을 생성한 뒤, `data.sql` 파일 내용을 그대로 복사해서 MariaDB 클라이언트로 직접 실행하세요.

## 5. 실행

1. `PilgrimageApplication` 클래스를 우클릭 → Run
2. 브라우저에서 `http://localhost:8080` 접속
3. 장소 목록(더미 데이터)이 보이면 정상

## 부록: 실행 버튼을 누르면 벌어지는 일 (파일 로드 순서)

`PilgrimageApplication` 실행 버튼을 누르면 아래 순서로 진행됩니다.

1. **Gradle 빌드** — `build.gradle`에 정의된 의존성(Spring Boot, JPA, Security 등)을 내려받고 컴파일
2. **`PilgrimageApplication.main()` 실행** — `@SpringBootApplication`이 붙어있어 스프링 부트 부팅 시작
3. **`application.yml` 읽기** — DB 접속 정보(`DB_HOST`, `DB_PORT` 등), JPA 설정, 서버 포트(8080) 등 전체 설정값을 여기서 읽음
4. **DB 연결 시도** — `application.yml`의 `datasource` 설정으로 MariaDB에 접속
5. **컴포넌트 스캔 & 빈 등록** — `controller/`, `service/`, `repository/`, `security/` 패키지 안의 클래스들을 자동으로 찾아서 등록 (`SecurityConfig`, `AuthController`, `PlaceController` 등)
6. **JPA/Hibernate가 테이블 생성·수정** — `domain/` 패키지의 엔티티(`User.java`, `Place.java` 등) 기준으로 `ddl-auto: update` 설정에 따라 테이블 자동 생성
7. **`data.sql` 실행 시도** — 단, `mode: embedded`라서 MariaDB에는 실행 안 되고 건너뜀 (그래서 4번 단계처럼 수동으로 넣어야 함)
8. **내장 톰캣(Tomcat) 서버 기동** — `server.port: 8080`으로 웹서버 오픈
9. **정적 파일 서빙 준비** — `src/main/resources/static/`의 `index.html`, `css/`, `js/`가 브라우저 요청 시 그대로 서빙됨 (프론트엔드)

즉 흐름은: `PilgrimageApplication.java` → `application.yml`(설정) → DB 연결 → 컨트롤러/서비스/DB 엔티티 로딩 → 웹서버 오픈 → `static/` 폴더 화면 파일들이 브라우저에 응답하는 순서입니다.

## 6. 로그인 확인

- 관리자 계정으로 로그인해서 관리자 기능(장소 등록/수정/삭제)까지 확인
- 계정 정보는 팀 내부에서 별도 공유

## 문제 해결 체크리스트

| 증상 | 원인 | 해결 |
|---|---|---|
| Gradle 동기화 실패 | `gradle.properties`에 남 PC의 JDK 경로가 남아있음 | 최신 버전 pull (이미 수정됨) |
| Ultimate 플러그인 요구 팝업 | Spring/Tailwind 의존성 감지에 따른 추천일 뿐 | 취소하고 무시 |
| 웹은 뜨는데 데이터가 안 보임 | DB는 연결됐지만 시드 데이터 미삽입 | 위 4번 단계대로 `data.sql` 수동 실행 |
| DB 연결 자체가 안 됨 (앱 구동 실패) | MariaDB 미실행 또는 포트 불일치 | MariaDB 실행 확인, 포트 확인 후 `DB_PORT` 환경변수로 맞추기 |
