# Deployment Guide — spring-petclinic-rest

> 생성일: 2026-10-04 · 소스: .github/workflows/, pom.xml, docker-compose.yml, README.md

## 인프라 요구사항

- Java 17 런타임 (실행 JAR 또는 Jib 이미지)
- DB: 기본 H2 인메모리(별도 인프라 불필요) / MySQL 8.4 / PostgreSQL 16.3 (프로필 전환)
- 컨테이너 배포 시 Docker (Jib는 데몬 불필요 — 원격 레지스트리로 직접 푸시)

## Docker 이미지 빌드 & 배포 (Jib)

- 이미지명: `springcommunity/spring-petclinic-rest` (tags: `{version}`, `latest`)
- 로컬 빌드(데몬): `mvn compile jib:dockerBuild`
- 레지스트리 푸시:

```sh
mvn compile jib:build -X -DjibSerialize=true \
  -Djib.to.auth.username=xxx -Djib.to.auth.password=xxxxx
```

- 공개 이미지 실행: `docker run -p 9966:9966 springcommunity/spring-petclinic-rest`

## 로컬 DB 컨테이너 (docker-compose.yml)

```sh
docker-compose --profile mysql up      # MySQL 8.4 (petclinic/petclinic, root/root, 3306)
docker-compose --profile postgres up   # PostgreSQL 16.3 (petclinic/petclinic, 5432)
```

기동 후 앱 프로필: `spring.profiles.active=mysql,spring-data-jpa` (또는 `postgres,...`)

## CI/CD — GitHub Actions (4개 워크플로)

| 워크플로 | 트리거 | 내용 |
|---|---|---|
| `maven-build-master.yml` (Java CI on master branch) | push → master | JDK 17(Temurin) + `./mvnw -B verify` + SonarCloud 분석 (`sonar.projectKey=spring-petclinic_spring-petclinic-rest`) |
| `maven-build-pull-request.yml` | PR | Maven 빌드/테스트 |
| `docker-build.yml` (Build to Docker Hub) | push → master | Buildx → Docker Hub 로그인(`DOCKERHUB_USERNAME`/`DOCKERHUB_TOKEN` secrets) → `mvn compile jib:build` |
| `newman-pipeline.yml` | (README 기준 API 회귀) | Newman으로 Postman 컬렉션 실행 |

### 필요 시크릿

- `SONAR_TOKEN` (master CI)
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` (docker-build)

## 환경 구성 요약

| 항목 | 값 |
|---|---|
| 포트 / 컨텍스트 | `9966` / `/petclinic/` |
| 프로필 조합 | `[h2|hsqldb|mysql|postgres],[jdbc|jpa|spring-data-jpa]` |
| 보안 | `petclinic.security.enable` (기본 false — 운영 배포 시 true 권장) |
| DB 초기화 | `spring.sql.init.mode=always` — 기동마다 schema/data 재로드 (운영 영속 DB에서는 재검토 필요) |
| 헬스체크 | `/petclinic/actuator/health` |

## 배포 절차 (표준)

1. master 머지 → CI(빌드+Sonar) 통과 확인
2. docker-build 워크플로가 Jib로 Docker Hub에 `{version}`/`latest` 태그 푸시
3. 대상 환경에서 이미지 실행 + `SPRING_PROFILES_ACTIVE`로 DB/리포지토리 프로필 지정
4. `/actuator/health` 및 `/v3/api-docs`로 기동 검증
