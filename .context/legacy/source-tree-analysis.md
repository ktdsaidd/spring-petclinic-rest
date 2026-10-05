# Source Tree Analysis — spring-petclinic-rest

> 스캔 수준: Quick (디렉터리 구조 + 파일명 기반). 생성일: 2026-10-04

## 전체 구조

```
spring-petclinic-rest/
├── pom.xml                      # Maven 빌드 (Spring Boot 4.1.1 parent, OpenAPI Generator, MapStruct, Jib, JaCoCo)
├── mvnw / mvnw.cmd              # Maven Wrapper
├── docker-compose.yml           # MySQL/PostgreSQL 로컬 컨테이너 (profile 기반)
├── postman-tests.sh             # Newman 기반 API 회귀 테스트 실행 스크립트
├── readme.md                    # 프로젝트 개요·실행·API·DB·보안 문서
├── .github/workflows/           # CI/CD (아래 참조)
│
├── src/main/java/org/springframework/samples/petclinic/
│   ├── PetClinicApplication.java            # ★ 엔트리포인트 (Spring Boot main)
│   ├── config/
│   │   └── SwaggerConfig.java               # springdoc/OpenAPI 커스텀 설정
│   ├── rest/                                # ★ REST API 레이어
│   │   ├── controller/
│   │   │   ├── v1/                          # v1 컨트롤러 8개 (Owner/Pet/PetType/Vet/Specialty/Visit/User/Root)
│   │   │   ├── v2/                          # v2 컨트롤러 2개 (Owner/Pet)
│   │   │   └── BindingErrorsResponse.java   # 바인딩 오류 응답 DTO
│   │   ├── advice/
│   │   │   └── ExceptionControllerAdvice.java # 전역 예외 처리
│   │   ├── validation/
│   │   │   ├── PetAgeValidation.java        # 커스텀 검증 애노테이션
│   │   │   └── PetAgeValidator.java         # 검증 구현
│   │   ├── dto/                             # (빌드 타임 생성) *Dto 클래스 — openapi.yml에서
│   │   └── api/                             # (빌드 타임 생성) API 인터페이스 — 컨트롤러가 구현
│   ├── service/                             # ★ 서비스 레이어
│   │   ├── ClinicService.java / ClinicServiceImpl.java      # 핵심 진료 도메인 서비스
│   │   └── UserService.java / UserServiceImpl.java          # 사용자 관리 서비스
│   ├── repository/                          # ★ 리포지토리 레이어 (인터페이스 + 3중 구현)
│   │   ├── *.java                           # 7개 인터페이스: Owner/Pet/PetType/Vet/Specialty/Visit/User
│   │   ├── jdbc/                            # JdbcTemplate 구현 (+RowMapper/Extractor, JdbcPet)
│   │   ├── jpa/                             # 수동 JPA(EntityManager) 구현
│   │   └── springdatajpa/                   # Spring Data JPA 구현 (+Impl 오버라이드)
│   ├── mapper/                              # MapStruct 엔티티↔DTO 매퍼 7개
│   ├── model/                               # 도메인 모델: BaseEntity, NamedEntity, Person→Owner/Vet,
│   │                                        #   Pet, PetType, Specialty, Visit, User, Role
│   ├── security/                            # BasicAuthenticationConfig, DisableSecurityConfig, Roles
│   └── util/                                # EntityUtils, CallMonitoringAspect(AOP 모니터링)
│
├── src/main/resources/
│   ├── openapi.yml                          # ★ API-first OAS 3.1 스펙 (DTO/API 인터페이스 생성 소스)
│   ├── application.properties               # ★ 기본 설정 (port 9966, context /petclinic/, profile h2+spring-data-jpa)
│   ├── application-{h2,hsqldb,mysql,postgres}.properties  # DB 프로필별 설정
│   ├── db/{h2,hsqldb,mysql,postgres}/       # 플랫폼별 schema.sql + data.sql (+DB 설정 가이드 txt)
│   ├── messages/messages*.properties        # i18n (기본/en/de)
│   └── logback.xml                          # 로깅 설정
│
├── src/test/java/org/springframework/samples/petclinic/
│   ├── SpringConfigTests.java               # 컨텍스트 로드 테스트
│   ├── model/ValidatorTests.java            # 모델 검증 테스트
│   ├── service/clinicService/               # AbstractClinicServiceTests + 4개 프로필 변형
│   ├── service/userService/                 # AbstractUserServiceTests + 4개 프로필 변형
│   └── rest/controller/                     # 컨트롤러별 @WebMvcTest 8개 + V2 테스트
├── src/test/jmeter/                         # JMeter 성능 테스트 (CRUD 벤치마크)
├── src/test/postman/                        # Postman 컬렉션 + Newman 회귀 테스트
│
└── target/generated-sources/openapi/        # (생성물) DTO·API 인터페이스 소스
```

## 핵심 디렉터리 요약

| 디렉터리 | 목적 |
|---|---|
| `rest/controller/v1|v2` | HTTP 엔드포인트. 생성된 API 인터페이스 구현. v1=전체, v2=Owner/Pet 확장 |
| `service` | 트랜잭션 경계·도메인 로직 (ClinicService, UserService) |
| `repository` | 데이터 접근 인터페이스 + jdbc/jpa/springdatajpa 3중 구현 (프로필로 선택) |
| `mapper` | MapStruct: JPA 엔티티 ↔ OpenAPI DTO 변환 |
| `model` | 도메인 엔티티 (상속: Person→Owner/Vet, BaseEntity/NamedEntity) |
| `security` | 프로필/프로퍼티 기반 인증 on/off, 역할 정의 |
| `resources/db/*` | 4개 DB 플랫폼의 스키마·시드 데이터 |

## 엔트리포인트

- `PetClinicApplication.java` — `main()` 부트스트랩
- 실행: `./mvnw spring-boot:run` → `http://localhost:9966/petclinic/`
- 헬스체크: `/petclinic/actuator/health`
