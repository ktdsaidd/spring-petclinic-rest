# Development Guide — spring-petclinic-rest

> 생성일: 2026-10-04 · 소스: README.md, pom.xml, application.properties, CI 워크플로

## 사전 요구사항

| 도구 | 버전 | 비고 |
|---|---|---|
| JDK | 17+ | CI는 Temurin 17 사용; maven-enforcer가 `java.version` 이상 강제 |
| Maven | 3.9.9+ | `./mvnw`(Wrapper) 사용 권장 |
| git | 최신 | |
| Docker (선택) | — | MySQL/PostgreSQL 로컬 구동 시 |

## 설치 및 빌드

```sh
git clone https://github.com/spring-petclinic/spring-petclinic-rest.git
cd spring-petclinic-rest

# 전체 빌드 + 생성 소스(DTO/API 인터페이스/MapStruct 매퍼) 생성
./mvnw clean install
```

> **중요**: `rest.dto.*Dto`, `rest.api.*`는 `src/main/resources/openapi.yml`에서 빌드 타임에 생성됨
> (`target/generated-sources/openapi/`). 손으로 생성/수정하지 말 것. openapi.yml 변경 후 재빌드 필요.

## 로컬 실행

```sh
./mvnw spring-boot:run
```

- 앱: `http://localhost:9966/petclinic/`
- Swagger UI: `http://localhost:9966/petclinic/swagger-ui.html`
- OAS 스펙: `http://localhost:9966/petclinic/v3/api-docs`
- 헬스체크: `http://localhost:9966/petclinic/actuator/health`
- H2 콘솔: `http://localhost:9966/petclinic/h2-console` (JDBC URL `jdbc:h2:mem:petclinic`, user `sa`, 비밀번호 빈칸)

Docker 실행: `docker run -p 9966:9966 springcommunity/spring-petclinic-rest`

## 설정 (application.properties)

| 속성 | 기본값 | 설명 |
|---|---|---|
| `spring.profiles.active` | `h2,spring-data-jpa` | [DB 프로필],[리포지토리 프로필] 조합 |
| `server.port` | `9966` | |
| `server.servlet.context-path` | `/petclinic/` | |
| `spring.sql.init.mode` | `always` | 기동 시마다 schema.sql/data.sql 로드 |
| `petclinic.security.enable` | `false` | `true` 시 Basic 인증 + 역할 인가 |

### DB 전환

`spring.profiles.active`의 DB 부분 변경 (+ `application-<db>.properties` 존재):

| DB | 프로필 | 준비 |
|---|---|---|
| H2 (기본) | `h2,spring-data-jpa` | 불필요 (인메모리) |
| HSQLDB | `hsqldb,spring-data-jpa` | 불필요 (인메모리) |
| MySQL | `mysql,spring-data-jpa` | `docker-compose --profile mysql up` |
| PostgreSQL | `postgres,spring-data-jpa` | `docker-compose --profile postgres up` |

### 리포지토리 구현 전환

프로필 두 번째 값: `jdbc` / `jpa` / `spring-data-jpa` (동일 인터페이스 3중 구현, 코드 변경 없음)

### 보안 활성화

```properties
petclinic.security.enable=true
```
- 기본 계정: `admin/admin` (전 API 접근)
- 역할: `OWNER_ADMIN`(Owner/Pet/PetType 읽기/Visit), `VET_ADMIN`(PetType/Specialty/Vet), `ADMIN`(User)
- 사용자 추가: `POST /api/users` with `{"username","password","enabled",roles:[{name}]}`

## 테스트

```sh
./mvnw test         # 단위/통합 테스트 (서비스 4프로필 변형 + 컨트롤러 MockMvc)
./mvnw verify       # JaCoCo 게이트 포함: 라인 85%, 브랜치 66% (생성 dto/api 제외)
```

- 서비스 테스트: `AbstractClinicServiceTests`/`AbstractUserServiceTests` 상속 → H2 JDBC, HSQL JDBC, JPA, Spring Data JPA 4개 변형이 동일 케이스 실행
- 커버리지 리포트: `target/site/jacoco/` (XML은 SonarCloud용)
- API 회귀 (앱 기동 후): `bash postman-tests.sh` (Newman + HTML 리포트, `src/test/postman/`)
- 성능: `jmeter -n -t src/test/jmeter/petclinic-jmeter-crud-benchmark.jmx -Jthreads=100 ...` (`src/test/jmeter/README.md`)

## 일반 개발 작업

| 작업 | 방법 |
|---|---|
| API 엔드포인트 추가 | ① `openapi.yml`에 스펙 추가 → ② `./mvnw clean install`로 인터페이스/DTO 생성 → ③ `rest/controller/v1|v2`에서 인터페이스 구현 → ④ 필요 시 `mapper/`에 MapStruct 매퍼 추가 |
| DB 테이블 추가 | 4개 플랫폼 `db/<platform>/schema.sql`(+data.sql) 및 `model/` 엔티티·리포지토리 3종 구현 전부 업데이트 |
| 검증 로직 추가 | `rest/validation/` 패턴 참고 (애노테이션 + Validator) |
| i18n 메시지 | `src/main/resources/messages/messages*.properties` (기본/en/de) |

## IDE 설정 (Eclipse/STS)

- Maven 3, m2e, MapStruct 플러그인 필요
- `File -> Import -> Maven -> Existing Maven project`
- 생성 소스 인식 안 되면 `mvn clean install` 후 소스 폴더 리프레시 (`target/generated-sources`)

## 코드 품질 도구 (pom.xml 설정)

Checkstyle, SpotBugs, JaCoCo, apache-rat, license-maven-plugin, refactor-first-plugin, SonarCloud (`sonarcloud.io`, 조직 `spring-petclinic-rest`)
