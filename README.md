# キラキラ聖地巡礼 (KiraKira Pilgrimage)

アニメ・映画・ドラマの実際のロケ地(聖地巡礼スポット)を地図上で検索し、お気に入り登録やレビュー投稿ができるWebサービスです。

**デモサイト**: [https://kirakira-lshm.onrender.com](https://kirakira-lshm.onrender.com)
**リポジトリ**: [github.com/YSHHAN36565362/KiraKira](https://github.com/YSHHAN36565362/KiraKira)

> 本サービスはRender / Supabaseの無料プランでホスティングしております。一定時間アクセスがない場合、サーバーがスリープ状態になるため、初回アクセス時の読み込みに30秒〜1分ほどかかる場合がございます。あらかじめご了承ください。なお、本READMEに掲載しているスクリーンショットは、実際にデプロイされている環境を撮影したものです。

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7?logo=render&logoColor=white)
![License](https://img.shields.io/badge/status-personal%20project-lightgrey)

---

## プレビュー

| 地図検索(ページ1) | スポット詳細・レビュー |
|---|---|
| ![地図検索](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/01-map-overview.jpg) | ![スポット詳細](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/03-place-detail.jpg) |

| ログイン | 管理者ダッシュボード(ページ2) |
|---|---|
| ![ログイン](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/02-login.jpg) | ![管理者ダッシュボード](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/04-admin-dashboard.jpg) |

---

## プロジェクトの目的

K-Move研修にて学習いたしましたFrontend(HTML/CSS/JS)、Backend(Java/Spring)、DB(SQL)の知識を統合し、一つのサービスとして自ら実装・デプロイすることを目的とした個人プロジェクトです。日本のサブカルチャー(アニメ・映画・ドラマ)ファンの方々に実際にご利用いただける聖地巡礼情報サイトを、企画からデプロイまで一貫して担当いたしました。

---

## 主な機能

- **地図ベースのスポット検索**: Leaflet.jsおよびOpenStreetMapを用いて聖地巡礼スポットを地図上にマーカー表示し、メディア種別(アニメ/映画/ドラマ)・作品名・地域キーワードによるリアルタイム検索・絞り込みが可能です
- **お気に入り・レビュー機能**: 会員登録/ログイン(セッションベース認証)後、関心のあるスポットをお気に入りに登録し、評価および口コミを投稿・管理できます
- **管理者ダッシュボード**: 地図表示に代えてテーブル形式で全スポットを一覧確認でき、スポットの登録・編集・削除を管理者権限で一元管理いたします
- **データエクスポート機能**: 全スポットデータをCSVおよびExcel(.xlsx)形式でダウンロードできる機能をログイン不要でご利用いただけるように実装しております(登録・編集・削除は管理者のみに権限を制限しております)
- **認証・セキュリティ**: Spring Securityによるセッション認証、BCryptによるパスワードハッシュ化、エンドポイント単位でのきめ細かなアクセス制御を実装しております

---

## 技術スタック

| 区分 | 使用技術 |
|---|---|
| **Frontend** | HTML5 / CSS3 / Vanilla JavaScript、Leaflet.js(地図表示) |
| **Backend** | Java 17、Spring Boot 3(Web、Data JPA、Security)、Hibernate、Apache POI(Excel生成) |
| **Database** | PostgreSQL([Supabase](https://supabase.com)にてホスティング、Session Pooler経由で接続) |
| **Infra / Deploy** | [Render](https://render.com)(Dockerベースのウェブサービス、GitHub連携による自動デプロイ) |
| **開発支援ツール** | Git & GitHub、Claude Code(AIペアプログラミング・デバッグ支援) |

---

## 担当業務および課題解決の経験

企画、バックエンドAPI設計、データベース連携からデプロイまで、全工程を単独で担当いたしました。特に「ローカル環境では動作するが、デプロイすると動作しない」という問題に幾度も直面し、その都度原因を特定・解決してまいりました。

### 1. ローカルDBの限界からクラウドDBへの移行

開発初期はローカル環境のMariaDBを使用しておりましたが、**第三者が実際にアクセスできるサービス**を提供するにはローカルDBでは限界があると判断し、無料のクラウドDB(Supabase上のPostgreSQL)へ移行いたしました。JDBCドライバの変更、`application.yml`における接続情報の環境変数化を行い、Renderのデプロイ環境に`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`を設定することで、ローカル環境だけでなく実際のインターネット環境でもサービスが正常に稼働するよう対応いたしました。

### 2. デプロイ後にのみ発生した500エラーの原因調査

デプロイ後、スポット検索APIが500エラーを返す不具合が発生いたしました。ログを詳細に追跡した結果、`function lower(bytea) does not exist`というPostgreSQLのエラーであることが判明し、原因は検索キーワードのパラメータに明示的な型キャスト(`cast(:keyword as string)`)が欠落していたことで、Hibernateがnullパラメータの型を誤って推論していたことにございました。原因を特定し、キャストを復元することで解決いたしました。

### 3. HibernateとPostgreSQLのマッピングに起因する不具合の解消

説明文(description)カラムに`@Lob`アノテーションを使用したところ、PostgreSQLとHikariCP(自動コミットモード)の組み合わせにおいてデータ取得自体が失敗する不具合が発生いたしました。`@Lob`の代わりに`@Column(columnDefinition = "TEXT")`と明示的に指定することで、この問題を解決いたしました。

### 4. きめ細かなアクセス制御の設計

「CSV/Excelのダウンロードはログイン不要とし、スポットの登録・編集・削除は管理者のみ許可する」という要件を実装するにあたり、単純に`/api/admin/**`配下を全て公開してしまうと、データの登録・編集・削除に関する権限まで同時に公開されてしまう**セキュリティ上の重大な懸念**があることに気づきました。ダウンロード用のエンドポイント(GET)2件のみを個別に指定して公開し、その他の管理者用APIについては引き続き`ADMIN`権限を要求するよう、Spring Securityの設定を精緻に調整いたしました。

### 5. デプロイパイプラインのトラブルシューティング

GitHubへコミット・プッシュを行ったにもかかわらず、実際のデプロイ環境に変更内容が反映されない事象が発生いたしました。HTTPレスポンスヘッダ(`Last-Modified`)を分析することで、「Renderの自動デプロイが最新のコミットを反映できていない」ことを原因として特定するなど、コードレベルだけでなく**デプロイインフラ層における問題**の診断にも取り組んでまいりました。

---

## プロジェクト構成

```
kirakiramain/
├── pilgrimage/            # Spring Bootバックエンド + 静的フロントエンド
│   ├── src/main/java/     # Controller / Service / Repository / Domain / Security
│   └── src/main/resources # application.yml、data.sql、static(HTML/CSS/JS)
└── render.yaml             # Renderデプロイ設定
```

---

# 키라키라 성지순례 (KiraKira Pilgrimage)

애니메이션 · 영화 · 드라마 속 실제 배경지(성지순례 장소)를 지도에서 찾고, 즐겨찾기·리뷰를 남길 수 있는 웹 서비스입니다.

**배포 링크**: [https://kirakira-lshm.onrender.com](https://kirakira-lshm.onrender.com)
**저장소**: [github.com/YSHHAN36565362/KiraKira](https://github.com/YSHHAN36565362/KiraKira)

> Render / Supabase 무료 플랜으로 배포되어 있어 일정 시간 미접속 시 서버가 슬립 모드로 전환됩니다. 첫 요청 시 로딩에 30초~1분 정도 걸릴 수 있으니 참고해 주세요. 이 README의 스크린샷은 실제 배포 환경을 직접 캡처한 화면입니다.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7?logo=render&logoColor=white)
![License](https://img.shields.io/badge/status-personal%20project-lightgrey)

---

## 미리보기

| 지도 탐색 (Page 1) | 장소 상세 & 리뷰 |
|---|---|
| ![지도 탐색](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/01-map-overview.jpg) | ![장소 상세](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/03-place-detail.jpg) |

| 로그인 | 관리자 대시보드 (Page 2) |
|---|---|
| ![로그인](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/02-login.jpg) | ![관리자 대시보드](https://raw.githubusercontent.com/YSHHAN36565362/KiraKira/main/images/04-admin-dashboard.jpg) |

---

## 프로젝트 목적

K-Move 연수 과정에서 배운 **Frontend(HTML/CSS/JS), Backend(Java/Spring), DB(SQL)**를 하나의 서비스로 직접 통합·배포해보는 것을 목표로 진행한 개인 프로젝트입니다. 일본 서브컬처(애니메이션·영화·드라마) 팬들이 실제로 사용할 수 있는 성지순례 정보 사이트를 기획부터 배포까지 직접 구현했습니다.

---

## 핵심 기능

- **지도 기반 장소 탐색**: Leaflet.js + OpenStreetMap으로 성지순례 장소를 지도 위 마커로 표시하고, 매체(애니/영화/드라마)·작품명·지역 키워드로 실시간 검색·필터링
- **즐겨찾기 & 리뷰**: 회원가입/로그인(세션 기반 인증) 후 관심 장소를 즐겨찾기에 담고, 별점과 텍스트 리뷰를 남기고 관리
- **관리자 대시보드**: 지도 대신 테이블 뷰로 전체 장소를 한눈에 조회하며, 장소 등록·수정·삭제를 관리자 권한으로 중앙 관리
- **데이터 내보내기**: 전체 장소 데이터를 CSV / Excel(.xlsx)로 다운로드하는 기능을 로그인 없이 누구나 이용 가능하도록 구현(등록·수정·삭제는 관리자만 가능하도록 권한 분리)
- **인증/보안**: Spring Security 기반 세션 인증, BCrypt 비밀번호 해싱, 엔드포인트별 세분화된 접근 제어

---

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| **Frontend** | HTML5 / CSS3 / Vanilla JavaScript, Leaflet.js (지도) |
| **Backend** | Java 17, Spring Boot 3 (Web, Data JPA, Security), Hibernate, Apache POI(엑셀 생성) |
| **Database** | PostgreSQL ([Supabase](https://supabase.com) 호스팅, Session Pooler 연결) |
| **Infra / Deploy** | [Render](https://render.com) (Docker 기반 웹 서비스, GitHub 연동 자동 배포) |
| **협업 / 도구** | Git & GitHub, Claude Code (AI 페어 프로그래밍 · 디버깅 보조) |

---

## 내가 기여한 점 & 문제 해결

기획, 백엔드 API 설계, DB 연동, 배포까지 전 과정을 직접 담당했습니다. 특히 "로컬에서 되던 게 배포하니 안 된다"는 문제를 여러 차례 마주치며 원인을 추적하고 해결한 경험이 많습니다.

### 1. 로컬 DB 한계 → 클라우드 DB로 전환

처음에는 로컬 MariaDB로 개발했지만, **다른 사람이 실제로 접속할 수 있는 서비스**를 만들려면 로컬 DB로는 불가능하다는 걸 깨닫고 무료 클라우드 DB(Supabase PostgreSQL)로 전환했습니다. JDBC 드라이버 교체, `application.yml` 접속 정보를 환경변수화하고, Render 배포 환경에 `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`를 설정해 로컬 호스트뿐 아니라 실제 인터넷 환경에서 서비스가 동작하도록 만들었습니다.

### 2. 배포 후에만 발생한 500 에러 원인 추적

배포 후 장소 검색 API가 500 에러를 던지는 문제가 발생했습니다. 로그를 끝까지 추적한 결과 `function lower(bytea) does not exist`라는 PostgreSQL 에러였고, 원인은 검색 키워드 파라미터에 명시적 타입 캐스트(`cast(:keyword as string)`)가 빠져 있어 Hibernate가 null 파라미터의 타입을 잘못 추론한 것이었습니다. 원인을 특정해 캐스트를 복원하는 것으로 해결했습니다.

### 3. Hibernate ↔ PostgreSQL 매핑 이슈 해결

설명(description) 컬럼에 `@Lob` 애노테이션을 사용했더니 PostgreSQL + HikariCP(autocommit) 조합에서 조회 자체가 실패하는 문제가 있었습니다. `@Lob` 대신 `@Column(columnDefinition = "TEXT")`로 명시해 문제를 해결했습니다.

### 4. 세분화된 접근 제어 설계

"CSV/Excel 다운로드는 로그인 없이, 장소 등록·수정·삭제는 관리자만" 이라는 요구사항을 구현하면서, 단순히 `/api/admin/**` 전체를 열어버리면 데이터 조작(등록/수정/삭제) 권한까지 함께 열려버리는 **보안 허점**이 될 수 있음을 확인했습니다. 다운로드 엔드포인트(GET) 두 개만 정확히 지정해 공개하고, 나머지 관리자 API는 그대로 `ADMIN` 권한을 요구하도록 Spring Security 설정을 세밀하게 조정했습니다.

### 5. 배포 파이프라인 트러블슈팅

GitHub에 커밋·푸시했는데도 실제 배포 사이트에 반영되지 않는 현상을 HTTP 응답 헤더(`Last-Modified`)를 분석해 "Render의 자동 배포가 최신 커밋을 반영하지 못하고 있다"는 것을 원인으로 특정하는 등, 코드뿐 아니라 **배포 인프라 레벨의 이슈**까지 진단하는 경험을 쌓았습니다.

---

## 프로젝트 구조

```
kirakiramain/
├── pilgrimage/            # Spring Boot 백엔드 + 정적 프론트엔드
│   ├── src/main/java/     # Controller / Service / Repository / Domain / Security
│   └── src/main/resources # application.yml, data.sql, static (HTML/CSS/JS)
└── render.yaml             # Render 배포 설정
```
