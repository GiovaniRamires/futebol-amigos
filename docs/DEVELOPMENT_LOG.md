# Registro de Desenvolvimento (Development Log)

Registro cronológico das etapas reais de desenvolvimento. Nada aqui é retroativamente inventado — cada entrada reflete o que foi efetivamente executado e verificado na sessão correspondente.

---

## Etapa 1 — Inspeção e planejamento
**Data:** 2026-10-18
**Objetivo:** inspecionar o ambiente e o repositório, decidir a stack técnica final, gerar o esqueleto do projeto e registrar o plano.

**Alterações realizadas:**
- Verificado: repositório `GiovaniRamires/futebol-amigos` no GitHub já existia e estava vazio; clonado localmente.
- Verificado ambiente local: Java 21 instalado (`JAVA_HOME` configurado), Maven e Docker **não** instalados/no PATH, `gh` CLI não disponível. Shell é PowerShell (Windows).
- Tentativa inicial de gerar o projeto com Spring Boot 3.5.16 (última da linha 3.x) falhou: o Spring Initializr público não aceita mais `bootVersion` < 4.0.0 (`compatibility range is >=4.0.0`), pois a linha 3.x do Spring Boot está fora de suporte OSS desde 30/06/2026.
- Usuário autorizou explicitamente, via mensagem direta durante a sessão, o uso de **Spring Boot 4.1.1** em vez da linha 3.x pedida originalmente no briefing.
- Projeto gerado via `start.spring.io` (Maven, Java 21, Spring Boot 4.1.1) com dependências: Web (MVC), Thymeleaf, Spring Data JPA, Validation, Security, MySQL Driver, Flyway, DevTools, Actuator.
- Build de sanidade executado: `./mvnw.cmd -q -DskipTests compile` → sucesso (exit code 0).
- Documentos iniciais criados: `docs/DECISIONS.md`, `docs/ARCHITECTURE.md`, este log.

**Testes executados:** `./mvnw.cmd compile` (apenas compilação, sem testes ainda — não há código de aplicação além do esqueleto gerado).

**Commit relacionado:** pendente (primeiro commit coerente será registrado ao final desta etapa).

**Pendências conhecidas:**
- Docker não disponível neste ambiente de execução dos agentes — `Dockerfile`/`compose.yaml` serão criados mas não puderam ser executados/validados localmente nesta sessão. Isso será declarado com honestidade no README.
- `gh` CLI indisponível — push ao GitHub será feito via `git push` HTTPS padrão; credenciais de push ainda não testadas com conteúdo real.
