# Project Overview — spring-petclinic-rest

> 생성일: 2026-10-04 · 스캔 수준: Quick

## 프로젝트 목적

Spring PetClinic 샘플 애플리케이션의 **REST API 전용 백엔드**. 수의 클리닉 도메인(소유자, 반려동물, 수의사, 방문 기록, 전문분야, 사용자)을 관리하는 CRUD API를 제공한다. UI는 없으며 Angular 프론트엔드(spring-petclinic-angular)가 이 API를 소비하도록 설계되었다. API-first(OpenAPI 스펙 우선) 및 다중 DB/리포지토리 프로필 지원이 핵심 특징이다.

## 기술 스택 요약

| 항목 | 값 |
|---|---|
| 언어 / 런타임 | Java 17+ |
| 프레임워크 | Spring Boot 4.1.1 (webmvc, data-jpa, jdbc, security, validation, actuator, cache, aspectj) |
| 빌드 | Maven 3.9.9 (+ Wrapper) |
| DB | H2(기본 인메모리) / HSQLDB / MySQL / PostgreSQL (프로필 전환) |
| API 스펙 | OpenAPI 3.1 (`src/main/resources/openapi.yml`), springdoc-openapi |
| 코드 생성 | OpenAPI Generator 7.25.0 (DTO·API 인터페이스), MapStruct 1.6.3 (매퍼) |
| 테스트 | JUnit + MockMvc, JaCoCo(라인 85%/브랜치 66% 게이트), Postman+Newman, JMeter |
| 배포 | Google Jib → Docker Hub (`springcommunity/spring-petclinic-rest`) |
| CI | GitHub Actions (Maven+Sonar, PR 빌드, Docker, Newman) |

## 아키텍처 분류

- **유형**: 백엔드 모놀리스 (backend, 단일 파트)
- **패턴**: 레이어드 (Controller → Service → Repository → DB) + API-first 코드 생성
- **특이점**: 리포지토리 3중 구현(jdbc/jpa/springdatajpa), DB 4종 프로필, API v1/v2 공존

## 저장소 구조

- **저장소 유형**: Monolith (단일 코드베이스)
- **파트**: 1개 (`main`) — `src/main/java/org/springframework/samples/petclinic/`
- **소스 규모**: 메인 Java 87개 파일, 테스트 Java 22개 파일

## 문서 링크

| 문서 | 내용 |
|---|---|
| [architecture.md](./architecture.md) | 아키텍처·설계 결정·테스트 전략 |
| [api-contracts-main.md](./api-contracts-main.md) | REST 엔드포인트 카탈로그·인증 |
| [data-models-main.md](./data-models-main.md) | 9개 테이블 스키마·관계·엔티티 매핑 |
| [source-tree-analysis.md](./source-tree-analysis.md) | 주석 달린 디렉터리 트리 |
| [development-guide-main.md](./development-guide-main.md) | 개발 환경·빌드·테스트 명령 |
| [deployment-guide.md](./deployment-guide.md) | Docker·CI/CD 배포 |

## 시작하기 (Getting Started)

```sh
git clone https://github.com/spring-petclinic/spring-petclinic-rest.git
cd spring-petclinic-rest
./mvnw spring-boot:run
# → http://localhost:9966/petclinic/  (Swagger UI: /petclinic/swagger-ui.html)
```

자세한 설정(DB 전환, 보안 활성화 등)은 [development-guide-main.md](./development-guide-main.md) 참조.
