# Project Manager

Este repositorio contem 2 APIs REST (Spring Boot) que trabalham juntas:

- `project-manager-api`: gerencia projetos (CRUD) e disponibiliza relatorio de portifolio em JSON e PDF.
- `members-api`: API externa (mock) responsavel por fornecer dados de membros (inclui o campo `role`/atribuicao).

O `project-manager-api` **nao persiste membros no banco**. Ele armazena apenas `managerId` e `memberIds` no banco de projetos e busca os detalhes do gerente e dos membros sob demanda na `members-api`.

## Modulos

### project-manager-api

- Responsavel por:
  - CRUD completo de `Project`
  - Associacao de membros ao projeto via `memberIds`
  - Validacao de regra de negocio para garantir que `managerId` aponte para um membro com role **`gerente`**
  - Endpoint de relatorio de portifolio:
    - `GET /api/portfolio/report` (JSON)
    - `GET /api/portfolio/report/pdf` (PDF via JasperReports)
- Chamadas externas:
  - Busca membro por id em: `${members.api.base-url}/api/members/{id}`
  - Config (padrao): `members.api.base-url=http://localhost:8081`
- Autenticacao:
  - Spring Security com Basic Auth.
  - Credenciais (in-memory): `admin` / `admin123`

### members-api

- Responsavel por fornecer dados de membros (mock) via endpoint:
  - `GET /api/members/{id}`
- Rodando por padrao na porta `8081`.

## Dependencias principais

### project-manager-api

- `spring-boot-starter-webmvc`: API REST
- `spring-boot-starter-validation`: validacoes via Bean Validation
- `spring-boot-starter-security`: Basic Auth
- `spring-boot-starter-data-jpa`: persistencia JPA no PostgreSQL
- `org.postgresql:postgresql`: driver PostgreSQL (runtime)
- `org.projectlombok:lombok`: reducao de boilerplate (DTOs, etc.)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`: Swagger/OpenAPI
- `net.sf.jasperreports:jasperreports` e `net.sf.jasperreports:jasperreports-pdf`:
  - geracao do PDF do relatorio via JasperReports
- Testes:
  - JUnit 5 (`spring-boot-starter-test`)
  - Mockito (`spring-boot-starter-test`)
  - H2 (profile/testes)
- Cobertura:
  - `jacoco-maven-plugin`

### members-api

- `spring-boot-starter-webmvc`: API REST
- `spring-boot-starter-validation`: validacoes
- `springdoc-openapi-starter-webmvc-ui`: Swagger/OpenAPI
- `lombok`: reducao de boilerplate

## Como executar (Docker Compose)

1. Suba os servicos (PostgreSQL + `members-api`):
   - `docker compose up -d`
2. Rode o `project-manager-api` localmente (exemplo):
   - `cd project-manager-api`
   - `mvn spring-boot:run`

O `docker-compose.yml` expone:

- PostgreSQL em `5432`
- `members-api` em `8081`

## Endpoints do relatorio

- JSON:
  - `GET /api/portfolio/report`
- PDF (attachment):
  - `GET /api/portfolio/report/pdf`

