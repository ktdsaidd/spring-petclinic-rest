---
sr_id: DR-2026-00001
sr_name: 방문 예약 시 담당 수의사 지정 기능 추가
define_sr: DR-2026-00001_define.md
analysis_date: 2026-10-04
author: 82023013
---

# 방문 예약 시 담당 수의사 지정 기능 추가 — 영향도 분석

## 목차
| § | 섹션 | 한눈에 보기 |
|---|---|---|
| 1 | 문서 개요 | SR 식별 정보 + 종합 영향도 Medium |
| 2 | 요구사항 요약 | REQ-001/002/003 — 방문(Visit)에 veterinarian_id 추가 |
| 3 | 작업 분해 | TASK-01 ~ 05 (5개) |
| 4 | As-Is / To-Be | TASK별 As-Is/To-Be/변경점 |
| 5 | 영향도 분석 | 5.1~5.6 — DB·DTO·API·컨트롤러·리포지토리·테스트·파급 |
| 6 | 개발 항목 | DEV-01 ~ 08 (8개) |
| 7 | 영향 요약 및 리스크 | 영역별 리스크 6개 |
| 8 | 미결 사항 / 가정 | 미결 0 |
| 9 | 참고 자료 | SR 1건 + 프로젝트 소스 14개 파일 |

## 1. 문서 개요
| 항목 | 내용 |
|---|---|
| SR ID / 명 | DR-2026-00001 / 방문 예약 시 담당 수의사 지정 기능 추가 |
| 종합 영향도 | **Medium** — 단일 엔티티(Visit)에 컬럼 추가 + 관련 DTO/API 변경. 신규 테이블 또는 비즈니스 로직 복잡도 없음. |
| 요구사항 → 개발 항목 | 3개 REQ → TASK 5개 → DEV 8개 |
| 핵심 변경점 | `visits` 테이블에 `veterinarian_id` 컬럼 신규 추가. Visit DTO에 `veterinarianId` 필드 추가. API 엔드포인트 시맨틱 변경 없음 (기존 엔드포인트 유지). |
| 미결 사항 | 미결 0 |

## 2. 요구사항 요약
- **REQ-001**: 방문 등록(Add Visit) 시 등록된 수의사 목록에서 담당 수의사 1명 선택 (필수). 선택한 수의사가 방문 기록에 저장됨.
- **REQ-002**: 방문 목록(Visit List)에 담당 수의사 이름 열을 추가하여 즉시 확인 가능하게 함. 기존 방문(수의사 미지정)은 "-" 표시.
- **REQ-003**: 기존 방문 수정(Edit Visit) 화면에서 담당 수의사 변경 가능.

## 3. 작업 분해
| TASK ID | 원 요구사항 | 내용 | 유형 | 수용 기준 |
|---|---|---|---|---|
| TASK-01 | REQ-001 | DB 스키마 변경: `visits` 테이블에 `veterinarian_id` 컬럼 추가 (FK → vets) | 데이터 | 컬럼 추가 후 INSERT 시 값 저장 확인 |
| TASK-02 | REQ-001 | OpenAPI 스키마 변경: `VisitFields` / `Visit` 스키마에 `veterinarianId` 필드 추가 (required: REQ-001) | API/DTO | `mvn clean install`로 DTO 재생성 확인 |
| TASK-03 | REQ-001, REQ-003 | VisitMapper 변경: `toVisit()` / `toVisitDto()`에 `veterinarianId` 매핑 추가 | 로직 | DTO↔Entity 변환 시 값 누락 없음 |
| TASK-04 | REQ-001, REQ-003 | VisitRestControllerV1 변경: `addVisit()` / `updateVisit()`에서 `veterinarianId` 처리 로직 추가 (REQ-001 필수, REQ-003 NULL 허용) | 로직 | API 호출 시 수의사 ID 저장/변경 확인 (REQ-003에서는 NULL 허용) |
| TASK-05 | REQ-002 | VisitDto 응답에 수의사 정보 포함: `veterinarianId` + 수의사 이름(N+1 조회 또는 JOIN) | 로직/API | GET /api/visits 응답에 수의사 정보 포함 |

## 4. As-Is / To-Be
### TASK-01: DB 스키마 변경
- **As-Is:** `visits` 테이블은 `id`, `pet_id`, `visit_date`, `description` 컬럼만 존재. 수의사 정보 없음.
  - 출처: `src/main/resources/db/h2/schema.sql#L54-L60`
- **To-Be:** `visits` 테이블에 `veterinarian_id INTEGER` 컬럼 신규 추가. `FOREIGN KEY (veterinarian_id) REFERENCES vets(id) ON DELETE SET NULL`.
- **변경점:** 컬럼 신규 추가. 기존 데이터는 NULL 허용.
- **As-Is 출처:** `src/main/resources/db/h2/schema.sql`

### TASK-02: OpenAPI 스키마 변경
- **As-Is:** `VisitFields` 스키마는 `date`, `description`만 포함. `Visit` 스키마는 `id`, `petId`를 추가로 포함.
  - 출처: `src/main/resources/openapi.yml#L2263-L2307`
- **To-Be:** `VisitFields`에 `veterinarianId` (integer, required: REQ-001 기준) 추가. `Visit` 응답에 `Vet` 객체 중첩 포함 (Pet→PetType 패턴 참조).
- **변경점:** 스키마 필드 추가. API 계약 변경.
- **As-Is 출처:** `src/main/resources/openapi.yml`

### TASK-03: VisitMapper 변경
- **As-Is:** `VisitMapper.toVisit()`은 `petId→pet.id`만 매핑. `toVisitDto()`는 `pet.id→petId`만 매핑.
  - 출처: `src/main/java/org/springframework/samples/petclinic/mapper/VisitMapper.java#L14-L27`
- **To-Be:** `veterinarianId` → `veterinarian.id` 매핑 추가. DTO→Entity 변환 시 `veterinarianId` 처리.
- **변경점:** MapStruct 매핑 규칙 추가.
- **As-Is 출처:** `src/main/java/org/springframework/samples/petclinic/mapper/VisitMapper.java`

### TASK-04: VisitRestControllerV1 변경
- **As-Is:** `addVisit()`은 `VisitDto`를 받아 `visitMapper.toVisit()`으로 변환 후 `clinicService.saveVisit()` 호출. `updateVisit()`은 `VisitFieldsDto`의 `date`, `description`만 업데이트.
  - 출처: `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/VisitRestControllerV1.java#L77-L97`
- **To-Be:** `addVisit()` / `updateVisit()`에서 `veterinarianId`를 `Vet` 엔티티로 조회하여 `visit.setVeterinarian()` 호출.
- **변경점:** 컨트롤러 로직에 수의사 조회 + 설정 추가.
- **As-Is 출처:** `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/VisitRestControllerV1.java`

### TASK-05: VisitDto 응답에 수의사 정보 포함
- **As-Is:** `VisitDto` 응답은 `id`, `petId`, `date`, `description`만 반환. 수의사 정보 없음.
- **To-Be:** 방문 목록 조회 시 수의사 정보 포함 반환. `Vet` 객체 중첩 포함 (Pet→PetType 패턴 참조).
- **변경점:** 조회 로직에 수의사 JOIN 또는 N+1 조회 추가.
- **As-Is 출처:** `src/main/java/org/springframework/samples/petclinic/repository/jdbc/JdbcVisitRepositoryImpl.java#L147-L177`

## 5. 영향도 분석

### 5.1 영향 프로그램/모듈
| 프로그램/모듈 ID | 명칭 | 변경 유형 | 변경 내용 | 관련 TASK | 영향도 | 출처 |
|---|---|---|---|---|---|---|
| `model.Visit` | Visit 엔티티 | 수정 | `veterinarian` 필드 + getter/setter 신규 추가 | TASK-01,03 | Medium | `src/main/java/org/springframework/samples/petclinic/model/Visit.java` |
| `mapper.VisitMapper` | MapStruct 매퍼 | 수정 | `veterinarianId` 매핑 규칙 추가 | TASK-03 | Low | `src/main/java/org/springframework/samples/petclinic/mapper/VisitMapper.java` |
| `rest.controller.v1.VisitRestControllerV1` | 방문 REST 컨트롤러 | 수정 | `addVisit`/`updateVisit`에 수의사 조회/저장 로직 추가 | TASK-04 | Medium | `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/VisitRestControllerV1.java` |
| `rest.controller.v1.OwnerRestControllerV1` | 보호자 REST 컨트롤러 | 수정 | `addVisitToOwner()`에 수의사 조회/저장 로직 추가 | TASK-04 | Medium | `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/OwnerRestControllerV1.java#L177-L188` |
| `service.ClinicService` / `ClinicServiceImpl` | 서비스 계층 | 수정 | `saveVisit()`에서 수의사 참조 처리 (필요시) | TASK-04 | Low | `src/main/java/org/springframework/samples/petclinic/service/ClinicServiceImpl.java#L221-L223` |
| `repository.VisitRepository` / 구현체 3개 | 리포지토리 | 수정 | `veterinarian_id` 컬럼 처리 (JDBC/JPA/SpringData 모두) | TASK-01 | Medium | `src/main/java/org/springframework/samples/petclinic/repository/` |
| `openapi.yml` | API 스키마 | 수정 | `VisitFields`/`Visit`에 `veterinarianId` 필드 추가 | TASK-02 | Medium | `src/main/resources/openapi.yml` |

### 5.2 영향 화면/UI
| 화면 ID | 화면명 | 변경 내용 | 관련 TASK | 출처 |
|---|---|---|---|---|
| New Visit (Add Visit) | 방문 등록 화면 | 담당 수의사 드롭다운/선택 UI 신규 추가 (필수) | TASK-01,02 | REQ-001 |
| Visit List (Pets and Visits) | 반려동물 방문 목록 | 수의사 이름 열 신규 추가 | TASK-05 | REQ-002 |
| Edit Visit | 방문 수정 화면 | 담당 수의사 선택/변경 UI 추가 | TASK-04 | REQ-003 |

> **참고:** 이 프로젝트는 백엔드 REST API 전용 (UI 없음). 프론트엔드(spring-petclinic-angular)는 별도 레포지토리에서 구현.

### 5.3 영향 데이터(DB)
| 테이블 | 컬럼 | 변경 유형 | 변경 내용 | 관련 TASK | 출처 |
|---|---|---|---|---|---|
| `visits` | `veterinarian_id` INTEGER | 신규 추가 | 수의사 FK. NULL 허용 (기존 데이터). `ON DELETE SET NULL` | TASK-01 | `src/main/resources/db/h2/schema.sql` |
| `visits` | `pet_id` | 기존 유지 | 변경 없음. 기존 FK 유지 | - | `src/main/resources/db/h2/schema.sql` |

**4개 플랫폼 (h2, hsqldb, mysql, postgres) 모두 스키마 변경 필요:**
- `src/main/resources/db/h2/schema.sql`
- `src/main/resources/db/hsqldb/schema.sql`
- `src/main/resources/db/mysql/schema.sql`
- `src/main/resources/db/postgres/schema.sql`

### 5.4 영향 인터페이스/연계
| 인터페이스 ID | 연계 대상 | 변경 내용 | 관련 TASK | 출처 |
|---|---|---|---|---|
| `POST /api/visits` | 방문 등록 API | `VisitFields`RequestBody에 `veterinarianId` 추가 (REQ-001) | TASK-02,04 | `openapi.yml#L1127-L1176` |
| `PUT /api/visits/{visitId}` | 방문 수정 API | `VisitFields`RequestBody에 `veterinarianId` 추가 (REQ-003) | TASK-02,04 | `openapi.yml#L1231-L1290` |
| `GET /api/visits` | 방문 목록 API | 응답에 수의사 정보 추가 | TASK-05 | `openapi.yml#L1093-L1126` |
| `POST /api/owners/{ownerId}/pets/{petId}/visits` | 보호자-반려동물 방문 등록 API | `veterinarianId` 처리 추가 | TASK-02,04 | `openapi.yml#L528-L585` |

### 5.5 영향 배치/권한/마이그레이션/설정/비기능
| 구분 | 대상 | 변경 내용 | 관련 TASK |
|---|---|---|---|
| 권한 | `@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")` | 기존 권한 유지. 수의사 정보 접근 권한 별도 필요 없음 (OWNER_ADMIN이 전체 방문 접근) | - |
| 마이그레이션 | 기존 방문 데이터 | `veterinarian_id` NULL로 초기화. 소급 입력 없음 (REQ 명시) | TASK-01 |
| 테스트 | `VisitRestControllerV1Tests.java` | 수의사 관련 테스트 케이스 신규 추가 (유효성 검사, 등록, 수정, 목록) | TASK-02,04 |
| 테스트 | `AbstractClinicServiceTests.java` | `saveVisit` / `findVisitById` 테스트에 수의사 관련 검증 추가 | TASK-03 |

### 5.6 타 기능 파급 영향
| 공통/공유 모듈 | 영향 호출부 | 파급 내용 | 관련 TASK |
|---|---|---|---|
| `ClinicService.saveVisit()` | 모든 Visit 생성/수정 경로 | 수의사 엔티티 참조 처리 필요. `saveVisit()` 시 Vet 엔티티 조회 후 Visit에 설정 | TASK-04 |
| `VisitMapper` | `toVisit()`, `toVisitDto()` | MapStruct 자동 생성 코드 재생성. `PetMapper` 의존성 유지 | TASK-03 |
| `VetRepository` | 수의사 목록 조회 | `findAllVets()` 또는 `findVetById()` 호출 필요 (수의사 선택 목록) | TASK-02 |
| `OwnerRestControllerV1.addVisitToOwner()` | 보호자-반려동물 방문 등록 | `VisitRestControllerV1.addVisit()`과 동일한 수의사 처리 로직 필요 | TASK-04 |

## 6. 개발 항목
| 개발 항목 ID | 개발 항목(작업 단위) | 유형 | 변경 유형 | 대상 ID | 관련 TASK | 출처 |
|---|---|---|---|---|---|---|
| DEV-01 | DB 스키마 변경 (4개 플랫폼) | DB | 수정 | `visits.veterinarian_id` | TASK-01 | `src/main/resources/db/*/schema.sql` |
| DEV-02 | Visit 엔티티에 `veterinarian` 필드 추가 | 로직 | 수정 | `model.Visit` | TASK-01 | `src/main/java/org/springframework/samples/petclinic/model/Visit.java` |
| DEV-03 | OpenAPI 스키마에 `veterinarianId` 필드 추가 | API | 수정 | `openapi.yml` | TASK-02 | `src/main/resources/openapi.yml` |
| DEV-04 | `mvn clean install`로 DTO/API 클래스 재생성 | 빌드 | 재생성 | `target/generated-sources` | TASK-02 | - |
| DEV-05 | VisitMapper에 `veterinarianId` 매핑 추가 | 로직 | 수정 | `mapper.VisitMapper` | TASK-03 | `src/main/java/org/springframework/samples/petclinic/mapper/VisitMapper.java` |
| DEV-06 | VisitRepository 구현체 3개에 `veterinarian_id` 처리 추가 | 로직 | 수정 | `repository/*/*RepositoryImpl` | TASK-01 | `src/main/java/org/springframework/samples/petclinic/repository/` |
| DEV-07 | VisitRestControllerV1 / OwnerRestControllerV1에 수의사 처리 로직 추가 | 로직 | 수정 | `rest/controller/v1/*` | TASK-04 | `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/` |
| DEV-08 | 테스트 케이스 수정/신규 (VisitRestControllerV1Tests, AbstractClinicServiceTests) | 테스트 | 수정 | `src/test/java/...` | TASK-02,04 | `src/test/java/...` |

## 7. 영향 요약 및 리스크
| 영역 | 영향 범위 | 리스크 | 완화 방안 |
|---|---|---|---|
| DB | visits 테이블 4개 플랫폼 스키마 | NULL 허용 컬럼 추가이므로 기존 데이터 무해. 하지만 MySQL에서 ALTER TABLE이 테이블 잠금 발생할 수 있음 (대용량 시). | 테스트 환경에서 스키마 변경 먼저 검증. |
| API | openapi.yml → DTO 자동 생성 | `target/generated-sources` 직접 수정 금지. 반드시 openapi.yml 수정 후 `mvn clean install`. | 프로젝트 규칙 준수. |
| 리포지토리 | JDBC/JPA/SpringData 3중 구현 | 3개 구현체 모두 동시 수정 필요. 하나만 수정 시 프로필 전환 시 버그 발생. | 3개 파일 모두 변경 확인. |
| Mapper | MapStruct 자동 코드 생성 | `@Mapping` 규칙 누락 시 null 값 매핑. | MapStruct 컴파일 테스트로 검증. |
| 테스트 | 기존 테스트 변경 | 기존 테스트가 수의사 필드 없이 동작 중이므로 테스트 실패 가능성. | 기존 테스트 수정 + 신규 테스트 추가. |
| 권한 | 기존 OWNER_ADMIN 권한 | 수의사 정보 조회에 별도 권한 필요 없음. 기존 권한 체계 유지 가능. | 변경 없음. |

## 8. 미결 사항 / 가정
### 8.1 분석 미결 / 가정
- [x] ~~**(확인필요) REQ-003 방문 수정 화면에서 수의사 필수 여부:** REQ-001은 "방문 등록 시 필수"로 명확하지만, REQ-003 "기존 방문 수정 화면"에서 수의사를 필수로 유지할지, 선택으로 허용할지 (NULL 허용) 미확정. → 수정 시 NULL 허용 시 "수의사 미지정"으로 변경 가능.~~ — **해결(2026-10-04 / 사용자 확정):** REQ-003 수정 화면에서는 수의사 선택을 NULL 허용 (수의사 미지정 가능). 수정 시 NULL 허용 시 "수의사 미지정"으로 변경 가능. **반영:** §3 TASK-04 수용 기준, §4 TASK-04 To-Be
- [x] ~~**(추정) 수의사 정보 응답 형식:** `VisitDto` 응답에 `veterinarianId` (Integer)만 포함할지, `veterinarianName` (String)을 함께 포함할지, 아니면 `Vet` 객체를 중첩 포함할지 — 현재 `Pet`이 `type: PetType`을 중첩 포함하는 패턴을 참조하면 `Vet` 객체 중첩 포함이 일관됨. (추정 — Pet→PetType 패턴 참조)~~ — **해결(2026-10-04 / 사용자 확정):** `VisitDto` 응답에 `Vet` 객체를 중첩 포함 (Pet→PetType 패턴 참조). **반영:** §4 TASK-02/05 To-Be
- [x] ~~**(추정) `veterinarian_id` NULLABLE 여부:** 기존 방문 데이터가 "-" 표시되므로 NULL 허용 필요. FK 제약 시 `ON DELETE SET NULL`로 설정 (수의사 삭제 시 방문의 수의사 정보 유지而非 삭제). (추정 — 일반적인 참조 무결성 패턴)~~ — **해결(2026-10-04 / 사용자 확정):** NULL 허용, FK `ON DELETE SET NULL` (수의사 삭제 시 방문의 수의사 정보 유지). **반영:** §4 TASK-01 To-Be, §5.3 DB 테이블

## 9. 참고 자료
- **SR (요구사항정의서):** DR-2026-00001_define.md (2026-10-04)
- **reference/ (전역 문서):** N/A — baseline feature/reference 문서 없음
- **project-context.md:** `.specs/project-context.md` — 기술 스택, 구현 규칙, 테스트 규칙, 워크플로우 규칙 참조
- **source code:**
  - `src/main/java/org/springframework/samples/petclinic/model/Visit.java` — Visit 엔티티 (현재 필드: id, date, description, pet)
  - `src/main/java/org/springframework/samples/petclinic/model/Vet.java` — Vet 엔티티 (수의사)
  - `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/VisitRestControllerV1.java` — 방문 REST API 컨트롤러
  - `src/main/java/org/springframework/samples/petclinic/rest/controller/v1/OwnerRestControllerV1.java` — 보호자 API (방문 등록 포함)
  - `src/main/java/org/springframework/samples/petclinic/mapper/VisitMapper.java` — MapStruct 매퍼
  - `src/main/java/org/springframework/samples/petclinic/service/ClinicService.java` — 서비스 인터페이스
  - `src/main/java/org/springframework/samples/petclinic/service/ClinicServiceImpl.java` — 서비스 구현체
  - `src/main/java/org/springframework/samples/petclinic/repository/VisitRepository.java` — 리포지토리 인터페이스
  - `src/main/java/org/springframework/samples/petclinic/repository/jdbc/JdbcVisitRepositoryImpl.java` — JDBC 구현
  - `src/main/java/org/springframework/samples/petclinic/repository/jpa/JpaVisitRepositoryImpl.java` — JPA 구현
  - `src/main/java/org/springframework/samples/petclinic/repository/springdatajpa/SpringDataVisitRepository.java` — Spring Data 구현
  - `src/main/resources/openapi.yml` — OpenAPI 스키마 (VisitFields, Visit 스키마 정의)
  - `src/main/resources/db/h2/schema.sql` — H2 스키마 (visits 테이블 정의)
  - `src/test/java/org/springframework/samples/petclinic/rest/controller/VisitRestControllerV1Tests.java` — 컨트롤러 테스트
  - `src/test/java/org/springframework/samples/petclinic/service/clinicService/AbstractClinicServiceTests.java` — 서비스 테스트
