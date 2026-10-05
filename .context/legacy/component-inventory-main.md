# Component Inventory — spring-petclinic-rest

> 스캔 수준: Quick (파일명/패키지 기반). 생성일: 2026-10-04
> 백엔드 프로젝트이므로 UI 컴포넌트 없음 — 소프트웨어 컴포넌트(클래스) 인벤토리.

## REST 컨트롤러 (11)

| 컴포넌트 | 위치 | 역할 |
|---|---|---|
| OwnerRestControllerV1 | `rest/controller/v1/` | 소유자 CRUD + 소유자 산하 pets/visits 처리 |
| PetRestControllerV1 | `rest/controller/v1/` | 반려동물 CRUD |
| PetTypeRestControllerV1 | `rest/controller/v1/` | 반려동물 타입 CRUD |
| VetRestControllerV1 | `rest/controller/v1/` | 수의사 CRUD (검색/페이징) |
| SpecialtyRestControllerV1 | `rest/controller/v1/` | 전문분야 CRUD |
| VisitRestControllerV1 | `rest/controller/v1/` | 방문 기록 CRUD |
| UserRestControllerV1 | `rest/controller/v1/` | 사용자 생성 |
| RootRestControllerV1 | `rest/controller/v1/` | API 루트 안내 |
| OwnerRestControllerV2 | `rest/controller/v2/` | Owner v2 변형 |
| PetRestControllerV2 | `rest/controller/v2/` | Pet v2 변형 |
| BindingErrorsResponse | `rest/controller/` | 바인딩 오류 응답 구조 (공용 DTO) |

## 서비스 (2)

| 컴포넌트 | 위치 | 역할 |
|---|---|---|
| ClinicService / ClinicServiceImpl | `service/` | 진료 도메인 핵심 서비스 (owner/pet/vet/visit/specialty/petType) |
| UserService / UserServiceImpl | `service/` | 사용자·역할 관리 |

## 리포지토리 (인터페이스 7 + 구현 3세트)

| 인터페이스 | jdbc | jpa | springdatajpa |
|---|---|---|---|
| OwnerRepository | JdbcOwnerRepositoryImpl | JpaOwnerRepositoryImpl | SpringDataOwnerRepository |
| PetRepository | JdbcPetRepositoryImpl | JpaPetRepositoryImpl | SpringDataPetRepository(+Impl) |
| PetTypeRepository | JdbcPetTypeRepositoryImpl | JpaPetTypeRepositoryImpl | SpringDataPetTypeRepository(+Impl) |
| VetRepository | JdbcVetRepositoryImpl | JpaVetRepositoryImpl | SpringDataVetRepository |
| SpecialtyRepository | JdbcSpecialtyRepositoryImpl | JpaSpecialtyRepositoryImpl | SpringDataSpecialtyRepository(+Impl) |
| VisitRepository | JdbcVisitRepositoryImpl | JpaVisitRepositoryImpl | SpringDataVisitRepository(+Impl) |
| UserRepository | JdbcUserRepositoryImpl | JpaUserRepositoryImpl | SpringDataUserRepository |

- springdatajpa의 `*Override` 인터페이스(PetRepositoryOverride 등)는 커스텀 메서드 시그니처 확장용.
- JDBC 보조: `JdbcPet`(조인 전용 모델), `JdbcPetRowMapper`, `JdbcVisitRowMapper`, `JdbcPetVisitExtractor`.

## 도메인 모델 (10)

| 엔티티 | 상위 | 설명 |
|---|---|---|
| BaseEntity | — | id 공통 추상 |
| NamedEntity | BaseEntity | name 공통 추상 |
| Person | BaseEntity | first/last name 추상 |
| Owner | Person | 소유자 (주소/도시/전화, pets 컬렉션) |
| Vet | Person | 수의사 (specialties N:M) |
| Pet | NamedEntity | 반려동물 (birth_date, type, owner, visits) |
| PetType | NamedEntity | 반려동물 타입 |
| Specialty | NamedEntity | 수의 전문분야 |
| Visit | BaseEntity | 방문 기록 (date, description) |
| User / Role | — | 인증 사용자(username PK)와 역할 |

## 매퍼 — MapStruct (7)

OwnerMapper, PetMapper, PetTypeMapper, VetMapper, SpecialtyMapper, VisitMapper, UserMapper (`mapper/`)
— JPA 엔티티 ↔ OpenAPI 생성 DTO 변환. `defaultComponentModel=spring`으로 Bean 주입.

## 보안 (3)

| 컴포넌트 | 역할 |
|---|---|
| BasicAuthenticationConfig | `petclinic.security.enable=true` 시 Basic 인증 + 역할 인가 |
| DisableSecurityConfig | 기본(`false`) 시 모든 요청 허용 |
| Roles | 역할 상수 (OWNER_ADMIN, VET_ADMIN, ADMIN) |

## 횡단 관심사 (Cross-cutting)

| 컴포넌트 | 위치 | 역할 |
|---|---|---|
| ExceptionControllerAdvice | `rest/advice/` | 전역 예외 → HTTP 오류 매핑 |
| PetAgeValidation / PetAgeValidator | `rest/validation/` | 커스텀 Bean Validation (반려동물 나이) |
| CallMonitoringAspect | `util/` | AOP 호출 모니터링 |
| EntityUtils | `util/` | 엔티티 비교/신규 판별 유틸 |
| SwaggerConfig | `config/` | springdoc/OpenAPI 커스텀 설정 |
| PetClinicApplication | 루트 | Spring Boot 부트스트랩 |

## 생성 코드 (빌드 타임, 수정 금지)

| 패키지 | 생성 도구 | 내용 |
|---|---|---|
| `rest.dto.*Dto` | OpenAPI Generator 7.25.0 | 요청/응답 DTO |
| `rest.api.*` | OpenAPI Generator (interfaceOnly) | 컨트롤러가 구현하는 API 인터페이스 |
| `mapper/*Impl` | MapStruct processor | 매퍼 구현체 |

생성 위치: `target/generated-sources/openapi/src/main/java` (build-helper로 소스 경로에 추가).
