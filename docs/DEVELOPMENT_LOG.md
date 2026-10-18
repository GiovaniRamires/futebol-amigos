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
- `gh` CLI indisponível — push ao GitHub foi feito via `git push` HTTPS padrão.
- **Push para `main` bloqueado pela política de segurança do Kiro Crew** (regra `git-publish-push-protected-branch-name`), mesmo com `main` remoto ainda vazio. Os 3 commits iniciais foram enviados para a branch `feature/bootstrap-projeto` (`git push -u origin feature/bootstrap-projeto`, bem-sucedido). **Giovani precisa abrir o Pull Request de `feature/bootstrap-projeto` para `main` no GitHub e mergear manualmente**, ou autorizar explicitamente um push direto a `main` para este caso específico de bootstrap de repositório vazio.

---

## Etapa 3 — Partidas, presença, seleção e sorteio
**Data:** 2026-10-18
**Objetivo:** implementar partidas, confirmação de presença (com lista de espera), a regra central de seleção explícita para o sorteio, e o sorteio aleatório com times equilibrados e histórico.

**Alterações realizadas:**
- `PartidaService`/`PartidaFormDto`/`PartidaAdminController`: CRUD de partidas (criar, editar, cancelar).
- `ConfirmacaoPresencaService`: confirmação/cancelamento de presença pelo jogador, com promoção automática da lista de espera quando uma vaga se abre; registro de comparecimento efetivo (compareceu/faltou/atrasado) pelo administrador, independente da confirmação prévia.
- `SelecaoSorteioService`: seleção explícita do administrador — única fonte de elegibilidade para o sorteio. Exige justificativa obrigatória para incluir jogador sem confirmação prévia.
- `SorteioService`: sorteio aleatório (`Collections.shuffle`) dividindo os selecionados em exatamente 2 times com diferença máxima de 1 jogador; histórico preservado (não apaga sorteios antigos); `refazer()` bloqueado se o último sorteio já foi publicado; `publicar()` despublica qualquer sorteio anterior da mesma partida.
- Controllers e páginas Thymeleaf correspondentes (lista/detalhe/formulário de partida, seleção, sorteio, painel do jogador com próxima partida e times publicados).
- Bug corrigido durante a implementação: `SorteioService` tinha dois construtores (produção + teste) sem `@Autowired` explícito, causando `NoSuchMethodException` no boot do contexto Spring — corrigido anotando o construtor de produção com `@Autowired`.

**Testes executados:** `./mvnw.cmd test` → 12 testes, 0 falhas, 0 erros (`FutebolAmigosApplicationTests`: 1, `UsuarioServiceTest`: 4, `SorteioServiceTest`: 7). `SorteioServiceTest` cobre explicitamente: confirmação isolada não torna elegível; inclusão sem confirmação exige justificativa; sorteio com menos de 2 selecionados é rejeitado; times equilibrados sem duplicados; histórico preservado ao refazer; sorteio publicado não pode ser refeito.

**Commit relacionado:** pendente de registro nesta entrada (ver seção de commits no resumo final).

**Pendências conhecidas:**
- Lista de espera promovida automaticamente ao cancelar presença, mas não há notificação ao jogador promovido (fora do escopo pedido).
- Página de detalhe da partida ainda não lista o status de comparecimento de forma tabular para registro em lote pelo admin (ação simples via formulário por jogador, suficiente para o tamanho do grupo, mas pode ser refinada).
