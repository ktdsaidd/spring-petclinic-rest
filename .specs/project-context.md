---
project_name: 'spring-petclinic-rest'
user_name: '82023013'
date: '2026-10-04'
sections_completed: ['technology_stack', 'language_rules', 'framework_rules', 'testing_rules', 'quality_style_rules', 'workflow_rules', 'dont_miss_rules']
existing_patterns_found: 9
status: 'complete'
rule_count: 47
optimized_for_llm: true
---

# Project Context for AI Agents

_이 파일은 AI 에이전트가 이 프로젝트의 코드를 구현할 때 반드시 따라야 할 핵심 규칙과 패턴을 담고 있습니다. 에이전트가 놓치기 쉬운 자명하지 않은 세부 사항에 집중합니다._

---

## Technology Stack & Versions

| 항목 | 선택 | 버전 |
|------|------|------|
| 언어 | Java | `java.version` (parent 상속, enforcer가 최소 버전 강제) |
| 프레임워크 | Spring Boot (parent: `spring-boot-starter-parent`) | 4.1.1 |
| 웹 계층 | `spring-boot-starter-webmvc` (MVC, WebFlux 아님) | parent 관리 |
| 데이터 | `spring-boot-starter-data-jpa`, `spring-boot-starter-jdbc` | parent 관리 |
| 보안 | `spring-boot-starter-security` (기본 비활성, `petclinic.security.enable` 토글) | parent 관리 |
| 검증 | `spring-boot-starter-validation` (Jakarta Bean Validation) | parent 관리 |
| 매핑 | MapStruct | 1.6.3 |
| API 규약 | OpenAPI First — `src/main/resources/openapi.yml` | OAS 3.1 |
| 코드 생성 | openapi-generator-maven-plugin (DTO + API 인터페이스) | 7.25.0 |
| API 문서 | springdoc-openapi-starter-webmvc-ui | 3.1.0 |
| 직렬화 | Jackson (`tools.jackson.core` — Jackson 3 네임스페이스) | parent 관리 |
| DB | H2(기본) / HSQLDB / MySQL / PostgreSQL (프로필 전환) | parent 관리 |
| 테스트 | JUnit 5 (spring-boot-starter-test), Mockito, spring-security-test, JsonPath | parent 관리 |
| 커버리지 | JaCoCo — 라인 85% / 브랜치 66% 최소 게이트 (`mvn check` 실행 시 강제) | 0.8.15 |
| 빌드 | Maven (mvnw 래퍼 포함), maven-enforcer(Java/Maven 버전 강제) | Maven 3.9.9 |
| 컨테이너 | Google Jib → `springcommunity/spring-petclinic-rest` | 3.5.2 |

## Critical Implementation Rules

### 언어별 규칙 (Java)

- Jakarta EE 네임스페이스 필수: `jakarta.validation`, `jakarta.transaction`, `jakarta.persistence` — `javax.*` 금지.
- Jackson 3 사용: import는 `tools.jackson.databind.*` (`com.fasterxml.jackson` 아님). `ObjectMapper`는 `JsonMapper` 빌더로 생성.
- Nullability 표기는 `org.jspecify.annotations.NonNull/Nullable` (openapi-generator import 매핑과 일관 유지).
- 의존성 주입은 생성자 주입 + `final` 필드. 필드/세터 `@Autowired` 금지. Lombok 미사용.
- 로깅은 SLF4J: `private static final Logger logger = LoggerFactory.getLogger(...)`.
- 엔티티는 `BaseEntity` → `NamedEntity`/`Person` 상속 계층을 재사용할 것 (id 필드 중복 정의 금지).
- 컨트롤러는 예외를 직접 던지지 말고 `ResponseEntity` + 명시적 상태 코드를 반환 (없으면 404, 생성은 Location 헤더 + 201).
- 전역 예외 처리는 `ExceptionControllerAdvice`가 `ProblemDetail`(RFC 7807)로 담당 — 컨트롤러에 try/catch 추가 금지.

### 프레임워크별 규칙 (Spring Boot + API-first)

- **API-first**: API 변경은 `src/main/resources/openapi.yml`만 수정 후 `mvn clean install`로 재생성. `target/generated-sources`의 `rest.dto.*Dto`/`rest.api.*Api`는 직접 수정 금지 (빌드 시 소실).
- 컨트롤러는 생성된 `*Api` 인터페이스를 `implements` + `@Override`만 사용. 컨트롤러에 매핑 어노테이션을 재정의하지 않음.
- DTO 쌍 규칙: `*Dto`(응답/전체) vs `*FieldsDto`(입력, id 제외). MapStruct 입력 매핑 시 `@Mapping(target="id", ignore=true)` 사용.
- 레이어 흐름: Controller(`rest/controller/v1|v2`) → `ClinicService`/`UserService` → Repository 인터페이스 → 3중 구현(`jdbc`/`jpa`/`springdatajpa`). 컨트롤러에서 리포지토리 직접 주입 금지.
- 리포지토리 구현은 `@Profile("jdbc"|"jpa"|"spring-data-jpa")` 분기. 리포지토리 추가/변경 시 **3개 구현 모두** 업데이트 대상.
- 서비스 트랜잭션: 읽기 `@Transactional(readOnly = true)`, 쓰기 `@Transactional`.
- Entity↔DTO 변환은 MapStruct `@Mapper` 인터페이스 전용 (`defaultComponentModel=spring` — 생성자 주입 가능). 수동 필드 복사 금지.
- 권한 체크: `@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")` 등 `Roles` 빈 참조만 사용, 역할 문자열 리터럴 금지.
- API 버저닝: 클래스명 `Vn` 접미사 (`OwnerRestControllerV1`), base path `/api`. 신규 엔드포인트는 v1 컨벤션에 먼저 반영.
- CORS: 클래스 레벨 `@CrossOrigin(exposedHeaders = "errors, content-type")` 유지.

### 테스트 규칙

- 컨트롤러 테스트: `@SpringBootTest` + `@ContextConfiguration(classes = ApplicationTestConfig.class)` + `@WebAppConfiguration`. `ClinicService`는 `@MockitoBean`으로 모킹, `MockMvc`는 standalone 빌드. 클래스명 `*RestControllerVnTests`.
- 서비스/리포지토리 테스트: `AbstractClinicServiceTests`/`AbstractUserServiceTests` 상속 후 구현별 하위 클래스(`*JpaTests`, `*SpringDataJpaTests`, `*H2JdbcTests`, `*HsqlJdbcTests`)가 프로필 지정. 신규 서비스 메서드는 Abstract 테스트에 1회 추가 → 4개 구현에 자동 적용.
- 권한 테스트는 `@WithMockUser(roles = "...")` (spring-security-test).
- Mockito BDD 스타일 통일: `given(...).willReturn(...)`, `doThrow(...)`.
- JSON 검증: `JsonMapper`로 수동 직렬화 후 `content().json(...)`. 날짜 포맷은 `yyyy-MM-dd` 고정.
- **JaCoCo 게이트**: 라인 85% / 브랜치 66% 미만 시 빌드 실패. 생성 코드(`rest/dto`, `rest/api`)는 제외 — 작성 코드만 대상. 신규 로직은 테스트 필수.

### 코드 품질 & 스타일 규칙

- EditorConfig 강제: UTF-8, LF, 파일 끝 개행, java/xml 4-space 들여쓰기, 후행 공백 제거.
- 네이밍: 컨트롤러 `{Entity}RestControllerV{n}` / 테스트 `{Entity}RestControllerV{n}Tests` / 매퍼 `{Entity}Mapper` / 입력 DTO `{Entity}FieldsDto`.
- 패키지 배치: 컨트롤러→`rest/controller/v1|v2`, 예외→`rest/advice`, 커스텀 검증→`rest/validation`, 매퍼→`mapper`, 엔티티→`model`, 서비스→`service`, 리포지토리→`repository/{jdbc|jpa|springdatajpa}`.
- 기존 파일의 Apache 2.0 라이선스 헤더 유지. public 매퍼/서비스 인터페이스에 한 줄 Javadoc 용도 설명.
- 도메인 검증은 `rest/validation`의 Jakarta Constraint 구성(예: `@PetAgeValidation` + `Validator`)으로 구현 — 컨트롤러 내부 검증 로직 금지.

### 개발 워크플로우 규칙

- Maven Wrapper(`mvnw`) 사용. enforcer가 Maven 3.9.9+ / 최소 Java 버전 강제.
- DTO/API 재생성 포함 풀 빌드는 `mvn clean install`. 생성 소스 없이는 부분 빌드 실패 가능.
- 로컬 실행: `./mvnw spring-boot:run` → `http://localhost:9966/petclinic/` (port 9966, context-path `/petclinic/`).
- 프로필은 2개 동시 활성: `spring.profiles.active=<db>,<repo-layer>` (db: h2|hsqldb|mysql|postgres / layer: jdbc|jpa|spring-data-jpa).
- 스키마/시드 변경은 `src/main/resources/db/<platform>/schema.sql|data.sql` 4개 플랫폼(h2/hsqldb/mysql/postgres) 모두 반영.
- 보안은 `petclinic.security.enable=false` 기본. 활성화 시 Basic Auth + 역할 인가 (시드 `admin/admin`).
- 검증 커맨드: `mvn test`(단위/통합), `mvn verify`(커버리지 게이트 포함). 보조 스위트: Postman+Newman(`postman-tests.sh`), JMeter(`src/test/jmeter`).
- 이미지 빌드는 Jib(`mvn compile jib:build`), Dockerfile 없음. 로컬 DB는 `docker-compose.yml --profile mysql|postgres`.

### Critical Don't-Miss Rules

- ❌ `target/generated-sources`의 `rest/dto/*`·`rest/api/*` 직접 생성/수정 금지 — `openapi.yml`만 수정.
- ❌ 컨트롤러에 `@GetMapping`/`@PostMapping` 등 매핑 어노테이션 추가 금지 — 생성된 인터페이스 `@Override`로만 구현.
- ❌ 컨트롤러의 리포지토리 직접 주입·서비스 우회 접근 금지.
- ❌ 리포지토리 단일 구현만 수정 금지 — `jdbc`/`jpa`/`springdatajpa` 3개 동기화 필수.
- ❌ `javax.*` import, `com.fasterxml.jackson` import 금지 (Jakarta EE + Jackson 3 마이그레이션 완료).
- ❌ 필드 주입(`@Autowired` on field)·Lombok 도입 금지.
- ❌ `spring.jpa.open-in-view`를 `true`로 되돌리기 금지 (현 `false`).
- 목록 조회가 비면 빈 배열이 아닌 **404 NOT_FOUND** 반환이 기존 관례 — 신규 엔드포인트도 동일 유지.
- 연쇄 삭제는 컨트롤러 메서드에 `@Transactional` 명시 (`deleteOwner` 패턴).
- DTO→Entity 변환 후 부분 필드 초기화(예: `pet.getType().setName(null)`)는 매퍼가 아닌 컨트롤러 레벨 보정으로 존재 — 이동시키지 말 것.
- 신규 엔드포인트는 반드시 `@PreAuthorize` 부여(기본 허용 금지), 역할은 `Roles` 빈 상수만 사용. 비밀번호는 PasswordEncoder 경유만.
- JPA 컬렉션이 EAGER 위주 — 루프 내 서비스 재호출(N+1) 주의. 페이징은 V2 패턴(`Page` → `{Entity}PageDto`) 사용.

---

## Usage Guidelines

**AI 에이전트용:**

- 코드 구현 전 이 파일을 반드시 읽으세요.
- 기록된 모든 규칙을 그대로 따르세요.
- 판단이 애매하면 더 제한적인 옵션을 선택하세요.
- 새로운 패턴이 등장하면 이 파일을 업데이트하세요.

**사람용:**

- 에이전트 필요에 집중하여 파일을 간결하게 유지하세요.
- 기술 스택 변경 시 업데이트하세요.
- 분기별로 낡은 규칙을 검토하세요.
- 시간이 지나 자명해진 규칙은 제거하세요.

Last Updated: 2026-10-04
