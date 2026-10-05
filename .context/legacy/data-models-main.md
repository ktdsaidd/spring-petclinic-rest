# Data Models — spring-petclinic-rest

> 스캔 수준: Quick. 스키마 SQL(`src/main/resources/db/h2/schema.sql`)과 모델 클래스 파일명 기반으로 추출.
> 생성일: 2026-10-04

## 개요

- **마이그레이션 전략**: Flyway/Liquibase 미사용. Spring `spring.sql.init` 방식 — 프로필별 `schema.sql` + `data.sql`을 기동 시마다 로드 (`spring.sql.init.mode=always`).
- **DB 프로필**: `h2`(기본, 인메모리), `hsqldb`, `mysql`, `postgres` — 각 `src/main/resources/db/<platform>/schema.sql`, `data.sql`
- **영속성 레이어**: 프로필로 구현체 선택 — `jdbc` / `jpa` / `spring-data-jpa` (동일 인터페이스 3중 구현)
- **ORM 매핑**: JPA 엔티티는 `model/` 패키지에 정의 (`@Entity`, spring-data-jpa/jpa 프로필에서 사용)

## 테이블 카탈로그 (H2 스키마 기준, 9개 테이블)

### vets — 수의사

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| first_name | VARCHAR(30) | NOT NULL |
| last_name | VARCHAR(30) | NOT NULL (인덱스 idx_vets_last_name) |

### specialties — 전문분야

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| name | VARCHAR(80) | NOT NULL (인덱스) |

### vet_specialties — 수의사↔전문분야 (N:M 조인 테이블)

| 컬럼 | 타입 | 제약 |
|---|---|---|
| vet_id | INTEGER | FK→vets.id (CASCADE), UNIQUE(vet_id, specialty_id) |
| specialty_id | INTEGER | FK→specialties.id (CASCADE) |

### types — 반려동물 타입

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| name | VARCHAR(80) | NOT NULL (인덱스) |

### owners — 소유자

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| first_name / last_name | VARCHAR(30) | NOT NULL (last_name 인덱스) |
| address | VARCHAR(255) | NOT NULL |
| city | VARCHAR(80) | NOT NULL |
| telephone | VARCHAR(20) | NOT NULL |

### pets — 반려동물

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| name | VARCHAR(30) | NOT NULL (인덱스) |
| birth_date | DATE | NOT NULL |
| type_id | INTEGER | FK→types.id (CASCADE) |
| owner_id | INTEGER | FK→owners.id (CASCADE) |

### visits — 방문(진료) 기록

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| pet_id | INTEGER | FK→pets.id (CASCADE) |
| visit_date | DATE | NOT NULL |
| description | VARCHAR(255) | NOT NULL |

### users — 사용자

| 컬럼 | 타입 | 제약 |
|---|---|---|
| username | VARCHAR(20) | PK |
| password | VARCHAR(255) | NOT NULL |
| enabled | BOOLEAN | NOT NULL, 기본 TRUE |

### roles — 사용자 역할

| 컬럼 | 타입 | 제약 |
|---|---|---|
| id | INTEGER | PK, IDENTITY |
| username | VARCHAR(20) | FK→users.username (CASCADE), UNIQUE(role, username) |
| role | VARCHAR(20) | NOT NULL |

## 관계 다이어그램 (ER 요약)

```
owners 1──* pets 1──* visits
              │
types 1───────┘

vets *──* specialties   (vet_specialties 조인 테이블)
users 1──* roles
```

- JPA 모델 클래스 기준 (`model/` 패키지):
  - `Person`(추상) → `Owner`, `Vet` 상속
  - `BaseEntity` → `NamedEntity` → `Specialty`, `PetType`
  - `Owner` 1:* `Pet`, `Pet` 1:* `Visit`, `Vet` *:* `Specialty`
  - `User` 1:* `Role`

## JPA 엔티티 ↔ 테이블 매핑

| 엔티티 (model/) | 테이블 |
|---|---|
| Owner | owners |
| Vet | vets |
| Pet | pets |
| PetType | types |
| Specialty | specialties |
| Visit | visits |
| User | users |
| Role | roles |

## 참고

- JDBC 프로필 전용 보조 클래스: `repository/jdbc/JdbcPet.java`(조인 결과용), `JdbcPetRowMapper`, `JdbcVisitRowMapper`, `JdbcPetVisitExtractor`
- 시드 데이터: `db/<platform>/data.sql` (샘플 owners/pets/vets/visits/users)
- 정확한 컬럼 상세는 플랫폼별 schema.sql 원본 참조 (mysql/postgres/hsqldb 변형 존재)
