# 키라키라 성지순례 (KiraKira Pilgrimage)

애니메이션/영화/드라마 성지순례 장소를 지도에서 검색하고, 즐겨찾기·리뷰를 남길 수 있는 웹 서비스.

## 기술 스택 / 사용 툴

**백엔드**
- Java 17 / Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security)
- Hibernate (JPA 구현체)
- Apache POI (엑셀 다운로드 기능)
- Gradle

**프론트엔드**
- 순수 HTML/CSS/JavaScript (프레임워크 없음)
- Leaflet.js (지도)

**데이터베이스**
- PostgreSQL, [Supabase](https://supabase.com)에서 호스팅 (Session Pooler 연결)

**배포/인프라**
- [Render](https://render.com) — Docker 기반 웹 서비스로 배포 (`render.yaml`)
- GitHub — 소스 저장소, Render와 연동해 push 시 자동 재배포

**개발 지원**
- Claude Code — 배포 트러블슈팅, 코드 병합/리뷰, 버그 수정 보조

## 이번 작업 요약

- 로컬 개발용 MariaDB 구성을 걷어내고 Supabase PostgreSQL로 마이그레이션, Render 배포 환경변수(`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`) 설정
- 시드 데이터(장소 31건, 관리자 계정) Supabase SQL Editor로 직접 삽입
- 장소 검색 API 500 에러 수정 — 키워드 파라미터에 명시적 타입 캐스트(`cast(:keyword as string)`) 누락으로 `function lower(bytea) does not exist` 발생하던 버그
- `description` 컬럼 매핑 오류 수정 — `@Lob` → `@Column(columnDefinition = "TEXT")`
- 관리자 CSV/엑셀 다운로드 기능 추가 (Apache POI)
- CSV/엑셀 전체 다운로드를 로그인 없이도 가능하도록 권한 완화 (장소 등록/수정/삭제는 여전히 관리자 권한 필요)
- 관리자 계정 정보 변경 (`admin@gmail.com` / 비밀번호 재설정)

## 알려진 이슈

- macOS 브라우저에서 CSV/엑셀 다운로드가 잘 동작하지 않는 경우 확인됨 — `Content-Disposition` 헤더가 `setContentDispositionFormData()`로 생성되어 `form-data; name="attachment"; filename=...` 형식으로 내려가고 있음(정상적인 첨부파일 다운로드 형식인 `attachment; filename=...`이 아님). 브라우저별로 다운로드 동작이 다르게 처리될 수 있어 원인으로 의심됨. 아직 수정 전.
