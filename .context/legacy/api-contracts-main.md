# API Contracts — spring-petclinic-rest

> 스캔 수준: Quick (패턴 기반). 소스 파일 본문 미확인 — OpenAPI 명세(`src/main/resources/openapi.yml`)와 컨트롤러 파일명/README 기반으로 추출.
> 생성일: 2026-10-04

## 개요

- **프로토콜**: REST (HTTP/JSON)
- **API-first**: `src/main/resources/openapi.yml` (OAS 3.1) 스펙에서 OpenAPI Generator가 DTO(`rest.dto.*Dto`)와 API 인터페이스(`rest.api.*`)를 빌드 타임에 생성
- **Base URL**: `http://localhost:9966/petclinic/api`
- **Swagger UI**: `/petclinic/swagger-ui.html`, 스펙: `/petclinic/v3/api-docs`
- **컨트롤러 위치**: `src/main/java/org/springframework/samples/petclinic/rest/controller/v1|v2/`

## 컨트롤러 인벤토리 (파일명 기반)

| 컨트롤러 | 버전 | 담당 리소스 |
|---|---|---|
| `OwnerRestControllerV1` | v1 | 소유자(Owner) 및 소유자 산하 pets/visits |
| `PetRestControllerV1` | v1 | 반려동물 |
| `PetTypeRestControllerV1` | v1 | 반려동물 타입 |
| `VetRestControllerV1` | v1 | 수의사 |
| `SpecialtyRestControllerV1` | v1 | 전문분야 |
| `VisitRestControllerV1` | v1 | 방문(진료) 기록 |
| `UserRestControllerV1` | v1 | 사용자 생성 |
| `RootRestControllerV1` | v1 | 루트/인덱스 엔드포인트 |
| `OwnerRestControllerV2` | v2 | 소유자 (V2 변형) |
| `PetRestControllerV2` | v2 | 반려동물 (V2 변형) |

## 엔드포인트 카탈로그

### Owners (`/api/owners`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/owners` | 전체 소유자 조회 |
| GET | `/api/owners/{ownerId}` | ID로 소유자 조회 |
| POST | `/api/owners` | 소유자 등록 |
| PUT | `/api/owners/{ownerId}` | 소유자 수정 |
| DELETE | `/api/owners/{ownerId}` | 소유자 삭제 |
| GET | `/api/owners/{ownerId}/pets/{petId}` | 소유자의 반려동물 조회 |
| PUT | `/api/owners/{ownerId}/pets/{petId}` | 소유자의 반려동물 수정 |
| POST | `/api/owners/{ownerId}/pets` | 소유자에게 반려동물 등록 |
| POST | `/api/owners/{ownerId}/pets/{petId}/visits` | 반려동물 방문 기록 등록 |

### Pets (`/api/pets`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/pets` | 전체 반려동물 조회 |
| GET | `/api/pets/{petId}` | ID로 반려동물 조회 |
| PUT | `/api/pets/{petId}` | 반려동물 수정 |
| DELETE | `/api/pets/{petId}` | 반려동물 삭제 |

### Vets (`/api/vets`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/vets` | 전체 수의사 조회 (페이징/검색 지원, openapi.yml 참조) |
| GET | `/api/vets/{vetId}` | ID로 수의사 조회 |
| POST | `/api/vets` | 수의사 등록 |
| PUT | `/api/vets/{vetId}` | 수의사 수정 |
| DELETE | `/api/vets/{vetId}` | 수의사 삭제 |

### Pet Types (`/api/pettypes`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/pettypes` | 전체 반려동물 타입 조회 |
| GET | `/api/pettypes/{petTypeId}` | ID로 타입 조회 |
| POST | `/api/pettypes` | 타입 등록 |
| PUT | `/api/pettypes/{petTypeId}` | 타입 수정 |
| DELETE | `/api/pettypes/{petTypeId}` | 타입 삭제 |

### Specialties (`/api/specialties`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/specialties` | 전체 전문분야 조회 |
| GET | `/api/specialties/{specialtyId}` | ID로 전문분야 조회 |
| POST | `/api/specialties` | 전문분야 등록 |
| PUT | `/api/specialties/{specialtyId}` | 전문분야 수정 |
| DELETE | `/api/specialties/{specialtyId}` | 전문분야 삭제 |

### Visits (`/api/visits`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/visits` | 전체 방문 기록 조회 |
| GET | `/api/visits/{visitId}` | ID로 방문 기록 조회 |
| POST | `/api/visits` | 방문 기록 등록 |
| PUT | `/api/visits/{visitId}` | 방문 기록 수정 |
| DELETE | `/api/visits/{visitId}` | 방문 기록 삭제 |

### Users (`/api/users`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/users` | 사용자 생성 (역할 포함) |

## 인증/인가

- 기본 비활성 (`petclinic.security.enable=false`). 활성화 시 HTTP Basic 인증.
- 설정 클래스: `security/BasicAuthenticationConfig.java`(활성 시), `security/DisableSecurityConfig.java`(기본), `security/Roles.java`
- 역할 → 권한 매핑 (README 기준):
  - `OWNER_ADMIN` → OwnerController, PetController, PetTypeController(getAllPetTypes/getPetType), VisitController
  - `VET_ADMIN` → PetTypeController, SpecialityController, VetController
  - `ADMIN` → UserController
- 기본 계정: `admin/admin`

## 에러/검증

- `rest/advice/ExceptionControllerAdvice.java` — 전역 예외 처리
- `rest/controller/BindingErrorsResponse.java` — 바인딩 오류 응답 구조
- `rest/validation/PetAgeValidator.java` — 커스텀 검증 (반려동물 나이)

## DTO / 코드 생성

- DTO: `org.springframework.samples.petclinic.rest.dto.*Dto` (빌드 타임 생성, `target/generated-sources`)
- API 인터페이스: `org.springframework.samples.petclinic.rest.api.*` (interfaceOnly 모드, 컨트롤러가 구현)
- Mapper: `org.springframework.samples.petclinic.mapper.*` (MapStruct, Entity ↔ DTO)

## 참고

- 정확한 요청/응답 스키마는 `src/main/resources/openapi.yml` 원본 참조 (Quick 스캔에서는 본문 미파싱).
- V2 컨트롤러(`OwnerRestControllerV2`, `PetRestControllerV2`)의 상세 스펙은 openapi.yml 및 소스 확인 필요.
