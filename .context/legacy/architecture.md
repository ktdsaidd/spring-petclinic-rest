# Architecture — spring-petclinic-rest

> 스캔 수준: Quick. 생성일: 2026-10-04

## Executive Summary

Spring PetClinic의 REST API 전용 백엔드. 소유자(Owner)·반려동물(Pet)·수의사(Vet)·방문(Visit)·전문분야(Specialty)·사용자(User)를 관리하는 수의 클리닉 도메인 API를 제공한다. UI는 없으며 Angular 프론트엔드(spring-petclinic-angular)가 소비한다. API-first 접근으로 OpenAPI 스펙에서 DTO와 컨트롤러 인터페이스를 자동 생성한다.

## 기술 스택

| 카테고리 | 기술 | 버전 | 근거 |
|---|---|---|---|
| 언어 | Java | 17+ (CI 기준) | pom.xml enforcer, GitHub Actions |
| 프레임워크 | Spring Boot | 4.1.1 | pom.xml parent |
| 웹 | spring-boot-starter-webmvc | (parent 관리) | pom.xml |
| 검증 | Bean Validation (jakarta) | — | starter-validation, PetAgeValidator |
| 데이터 접근 | Spring Data JPA / JPA / JdbcTemplate | — | 3중 리포지토리 구현, 프로필 선택 |
| DB | H2(기본) / HSQLDB / MySQL / PostgreSQL | — | application.properties 프로필 |
| API 문서 | springdoc-openapi (OAS 3.1) | 3.1.0 | SwaggerConfig, openapi.yml |
| 코드 생성 | OpenAPI Generator (interfaceOnly) | 7.25.0 | pom.xml, target/generated-sources |
| 매퍼 | MapStruct | 1.6.3 | mapper/ 패키지 |
| 보안 | Spring Security (HTTP Basic) | — | petclinic.security.enable 토글 |
| 캐시/AOP/Actuator | spring-boot-starter-cache/aspectj/actuator | — | pom.xml, CallMonitoringAspect |
| 빌드 | Maven (+ Wrapper) | 3.9.9 | maven-enforcer |
| 컨테이너 | Google Jib | 3.5.2 | pom.xml, docker-build.yml |
| 커버리지 | JaCoCo (라인 85%, 브랜치 66% 강제) | 0.8.15 | pom.xml check 규칙 |

## 아키텍처 패턴: 레이어드 (Layered, API-first)

```
HTTP 요청
   │
   ▼
[rest/controller v1·v2]  ← 생성된 rest/api 인터페이스 구현 (openapi.yml → codegen)
   │  · DTO(*Dto) ↔ Entity 변환: mapper/ (MapStruct)
   │  · 전역 예외: advice/ExceptionControllerAdvice
   ▼
[service]  ClinicService / UserServiceImpl  ← 트랜잭션 경계
   ▼
[repository] 7개 인터페이스
   ├── jdbc/          (profile: jdbc)          JdbcTemplate + RowMapper
   ├── jpa/           (profile: jpa)           EntityManager 수동 구현
   └── springdatajpa/ (profile: spring-data-jpa, 기본) Spring Data + 커스텀 Impl
   ▼
DB (profile: h2 | hsqldb | mysql | postgres)  ← spring.sql.init으로 schema/data 로드
```

### 핵심 설계 결정

1. **이중 프로필 시스템** (`application.properties`):
   - DB 선택: `h2`(기본) / `hsqldb` / `mysql` / `postgres`
   - 리포지토리 구현 선택: `jdbc` / `jpa` / `spring-data-jpa`(기본)
   - 예: `spring.profiles.active=h2,spring-data-jpa`
2. **API-first**: `openapi.yml`이 단일 진실 공급원. DTO·API 인터페이스는 빌드 시 생성되며 손으로 수정하지 않음. 컨트롤러는 생성된 인터페이스만 구현.
3. **3중 리포지토리 구현**: 동일 `repository` 인터페이스를 3개 방식으로 구현해 테스트·성능 비교 가능 (테스트도 프로필 변형으로 동일 추상 테스트 상속).
4. **보안 토글**: `petclinic.security.enable=false` 기본 비활성. 활성화 시 Basic 인증 + 역할 기반 인가(`Roles.java`, README의 역할 매핑).
5. **다중 API 버전**: v1(전체 리소스) + v2(Owner/Pet). `RootRestControllerV1`이 루트 안내.

## 데이터 아키텍처

9개 테이블: vets, specialties, vet_specialties(N:M), types, owners, pets, visits, users, roles.
관계: owners 1:* pets 1:* visits, pets *→1 types, vets *:* specialties, users 1:* roles.
상세: [data-models-main.md](./data-models-main.md)

## API 설계

REST/JSON, Base `http://localhost:9966/petclinic/api`, 리소스당 표준 CRUD + 중첩 서브리소스(owners→pets→visits). Swagger UI: `/petclinic/swagger-ui.html`.
상세: [api-contracts-main.md](./api-contracts-main.md)

## 컴포넌트 개요

| 레이어 | 개수 | 위치 |
|---|---|---|
| 컨트롤러 | 11 (v1: 9, v2: 2) | `rest/controller/` |
| 서비스 | 2 (인터페이스+구현) | `service/` |
| 리포지토리 인터페이스 | 7 | `repository/` |
| 리포지토리 구현 | 3세트 (jdbc/jpa/springdatajpa) | `repository/*/` |
| 매퍼 | 7 | `mapper/` |
| 도메인 엔티티 | 10 | `model/` |
| 보안 설정 | 3 | `security/` |

## 소스 트리

상세: [source-tree-analysis.md](./source-tree-analysis.md)

## 개발 워크플로

- 빌드/생성: `mvn clean install` (DTO·API·Mapper 생성)
- 실행: `./mvnw spring-boot:run` (포트 9966, 컨텍스트 `/petclinic/`)
- 테스트: `./mvnw test` (JaCoCo 게이트: 라인 85% / 브랜치 66%)
- 상세: [development-guide-main.md](./development-guide-main.md)

## 배포 아키텍처

- Jib로 Docker Hub에 이미지 빌드·배포: `mvn compile jib:build` → `springcommunity/spring-petclinic-rest`
- 로컬 DB: `docker-compose.yml --profile mysql|postgres up`
- CI: GitHub Actions 4개 워크플로 (master 빌드+Sonar, PR 빌드, Docker 배포, Newman API 테스트)
- 상세: [deployment-guide.md](./deployment-guide.md)

## 테스트 전략

| 계층 | 방식 | 파일 |
|---|---|---|
| 서비스 | 추상 테스트 + 4개 프로필 변형 (H2 JDBC, HSQL JDBC, JPA, Spring Data JPA) | `service/clinicService/`, `service/userService/` |
| 컨트롤러 | MockMvc 기반 컨트롤러 테스트 (리소스별 1개 + V2 통합) | `rest/controller/` |
| 모델/검증 | 단위 테스트 | `model/ValidatorTests`, `PetAgeValidatorTest` |
| 컨텍스트 | 스프링 설정 로드 검증 | `SpringConfigTests` |
| E2E/회귀 | Postman + Newman (HTML 리포트) | `src/test/postman/` |
| 성능 | JMeter CRUD 벤치마크 | `src/test/jmeter/` |
