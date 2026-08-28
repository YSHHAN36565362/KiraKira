# 키라키라 성지순례 - pilgrimage 백엔드

Spring Boot 3 / Java 17 / Gradle / MariaDB 기반. 장소(Place) CRUD, 회원가입/로그인(세션 기반), 관리자 권한 분리, 즐겨찾기, 리뷰/별점 API + 심플한 프론트엔드.

## DB 준비 (MariaDB)

로컬에 MariaDB가 설치·실행 중이어야 합니다. 아래 SQL로 전용 계정과 스키마를 만드세요.

```sql
CREATE DATABASE pilgrimage CHARACTER SET utf8mb4;
CREATE USER 'pilgrimage'@'localhost' IDENTIFIED BY 'pilgrimage';
GRANT ALL PRIVILEGES ON pilgrimage.* TO 'pilgrimage'@'localhost';
FLUSH PRIVILEGES;
```

접속 정보는 환경변수로 덮어쓸 수 있습니다 (`application.yml` 참고): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (기본값: localhost/3307/pilgrimage/pilgrimage/pilgrimage).

> 이 PC에는 MySQL80(3306)과 MariaDB(3307)가 함께 설치되어 있어 **MariaDB는 3307 포트**를 사용합니다. `netstat`/`SHOW VARIABLES LIKE 'port'`로 실제 포트를 꼭 확인하세요.

테이블은 `ddl-auto: update`로 자동 생성/갱신되며, 최초 1회 시드 데이터(장소 4건 + 관리자 계정)를 수동으로 넣어야 합니다 (`data.sql` 참고 — MariaDB에는 자동 실행되지 않음, H2를 쓰는 테스트에만 자동 적용됨).

## IntelliJ에서 열기

1. IntelliJ IDEA → `File > Open` → 이 `pilgrimage` 폴더 선택
2. Gradle Wrapper가 포함되어 있어 별도 설치 없이 자동으로 빌드됩니다 (최초 동기화 시 인터넷 연결 필요)
3. `PilgrimageApplication`을 실행 (Run)

## 실행 확인

- 서버 / 프론트엔드: http://localhost:8080
- 시드 관리자 계정: `admin@kirakira.com` / `admin1234` (최초 1회 수동 시드 필요, ROLE_ADMIN)

## API

### 장소

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/places?mediaType=ANIME&keyword=너의` | 전체 공개 | 매체/키워드 필터 검색 |
| GET | `/api/places/{id}` | 전체 공개 | 단건 조회 (평균 별점/리뷰 수 포함) |
| POST | `/api/admin/places` | ADMIN | 장소 등록 |
| PUT | `/api/admin/places/{id}` | ADMIN | 장소 수정 |
| DELETE | `/api/admin/places/{id}` | ADMIN | 장소 삭제 |

### 인증 (세션 기반)

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | 전체 공개 | 회원가입 (ROLE_USER로 생성) |
| POST | `/api/auth/login` | 전체 공개 | 로그인, 세션 쿠키 발급 |
| POST | `/api/auth/logout` | - | 로그아웃, 세션 무효화 |
| GET | `/api/auth/me` | - | 현재 로그인 사용자 조회 (미로그인 시 401) |

### 즐겨찾기 (로그인 필요)

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/favorites` | 내 즐겨찾기 목록 |
| POST | `/api/favorites/{placeId}` | 즐겨찾기 추가 (중복 시 409) |
| DELETE | `/api/favorites/{placeId}` | 즐겨찾기 해제 |

### 리뷰/별점

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/places/{placeId}/reviews` | 전체 공개 | 장소별 리뷰 목록 |
| POST | `/api/places/{placeId}/reviews` | 로그인 필요 | 리뷰 작성 (사용자당 장소별 1건, 중복 시 409) |
| PUT | `/api/reviews/{id}` | 작성자 본인 | 리뷰 수정 (아니면 403) |
| DELETE | `/api/reviews/{id}` | 작성자 본인 또는 ADMIN | 리뷰 삭제 |

`mediaType` 값: `ANIME`, `MOVIE`, `DRAMA`

## 현재 범위 / 다음 단계

- Google Maps JS API 연동은 아직 포함하지 않았습니다 (API 키 발급 후 프론트엔드에 지도 마커 표시 추가 필요). 현재는 카드 리스트 + 상세 모달로 위경도만 텍스트로 표시합니다.
- MySQL 전환은 아직 하지 않았습니다 (현재는 H2 인메모리 — 재시작 시 데이터 초기화됨). `application.yml`의 `datasource`만 교체하면 됩니다.
- CSRF 보호는 이 프로젝트 범위상 비활성화되어 있습니다 (세션 쿠키 기반 REST API + 정적 프론트엔드 조합에서 프로덕션 배포 시에는 CSRF 토큰 적용을 권장합니다).

## 테스트

```
./gradlew test
```
