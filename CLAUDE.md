# conversion-hub — estado do projeto

O plano de negócio completo está em `docs/plano-negocio.md`. Leia antes de sugerir escopo.

Recebe webhook de compra/lead (Hotmart, Shopify, CRMs), normaliza, aplica hash
SHA-256 e envia para Meta Conversions API e Google Ads. Log em PostgreSQL + retry.

## Feito
- Esqueleto Spring Boot 4.2 / Java 25 (Web, Validation, JPA, PostgreSQL, Flyway, Actuator, Testcontainers).
- `compose.yaml` com Postgres local. Teste de contexto passando.

## Próximo passo
Primeira migração Flyway (`V1__...sql`): tabela de log dos eventos recebidos.
Ponto em discussão: **idempotência** — o mesmo webhook pode chegar duas vezes
(retry da plataforma). A deduplicação é pelo id da compra/evento na origem,
não pelo cliente. Isso define uma coluna com restrição UNIQUE.

## Rodar
- `docker compose up -d` (Postgres local)
- `./mvnw test`
