# Project Documentation Index

> 생성일: 2026-10-04 · 스캔 수준: Quick (initial_scan) · AI 어시스티드 개발의 주요 진입점

## Project Overview

- **Type:** monolith (1 part: `main`)
- **Primary Language:** Java 17+
- **Architecture:** 레이어드 (Controller → Service → Repository → DB) + API-first (OpenAPI 코드 생성)

## Quick Reference

- **Tech Stack:** Spring Boot 4.1.1 · Spring Data JPA/JPA/JDBC(3중 구현) · H2/HSQldb/MySQL/PostgreSQL · springdoc-openapi(OAS 3.1) · MapStruct · Maven · Jib
- **Entry Point:** `src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java`
- **Architecture Pattern:** Layered + 프로필 기반 DB/리포지토리 전환 (h2,spring-data-jpa 기본)
- **Base URL:** `http://localhost:9966/petclinic/api` (Swagger UI: `/petclinic/swagger-ui.html`)

## Generated Documentation

- [Project Overview](./project-overview.md)
- [Architecture](./architecture.md)
- [Source Tree Analysis](./source-tree-analysis.md)
- [Component Inventory](./component-inventory-main.md)
- [Development Guide](./development-guide-main.md)
- [Deployment Guide](./deployment-guide.md)
- [API Contracts](./api-contracts-main.md)
- [Data Models](./data-models-main.md)

## Existing Documentation

- [README](../../readme.md) - 프로젝트 개요, 실행법, API 테이블, DB 전환, 보안 설정
- [OpenAPI Spec](../../src/main/resources/openapi.yml) - OAS 3.1 API 스펙 (단일 진실 공급원, DTO/API 인터페이스 생성 소스)
- [JMeter 성능 테스트](../../src/test/jmeter/README.md) - CRUD 벤치마크
- [Postman+Newman 회귀 테스트](../../src/test/postman/README.md) - API 비회귀 테스트 + HTML 리포트
- [MySQL 설정 가이드](../../src/main/resources/db/mysql/petclinic_db_setup_mysql.txt)
- [PostgreSQL 설정 가이드](../../src/main/resources/db/postgres/petclinic_db_setup_postgres.txt)

## Getting Started

```sh
./mvnw clean install        # DTO/API 인터페이스/매퍼 생성 포함
./mvnw spring-boot:run      # → http://localhost:9966/petclinic/
./mvnw test                 # 서비스 4프로필 변형 + 컨트롤러 MockMvc 테스트
```

- DB 전환: `spring.profiles.active=[h2|hsqldb|mysql|postgres],[jdbc|jpa|spring-data-jpa]`
- 보안 활성화: `petclinic.security.enable=true` (기본 계정 admin/admin)
- 상세: [Development Guide](./development-guide-main.md)

## AI 컨텍스트 사용 가이드

| 작업 | 참고 문서 |
|---|---|
| 신규 API 추가 | api-contracts-main.md + openapi.yml + development-guide-main.md "일반 개발 작업" |
| DB 스키마 변경 | data-models-main.md (4개 플랫폼 schema.sql 동시 수정 주의) |
| 아키텍처 결정 | architecture.md (3중 리포지토리·코드 생성 규칙 준수) |
| 파일 위치 파악 | source-tree-analysis.md, component-inventory-main.md |
| Brownfield PRD | 본 index.md를 PRD 워크플로우 입력으로 제공 |
