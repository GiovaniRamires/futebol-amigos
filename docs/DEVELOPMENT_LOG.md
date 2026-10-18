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

---

## Etapa 4 — Financeiro
**Data:** 2026-10-18
**Objetivo:** mensalidades e diárias, pagamentos parciais com saldo, configuração de Pix, link de WhatsApp pré-formatado.

**Alterações realizadas:**
- `ConfiguracaoSistemaService`/`ConfiguracaoFormDto`/`ConfiguracaoAdminController`: tela de configuração de mensalidade, dia de vencimento e dados de Pix.
- `CobrancaService`: geração de mensalidade por competência para mensalistas ativos (sem duplicar — unicidade por jogador+tipo+competência, ADR-004); geração de diária acoplada ao registro de comparecimento (`ConfirmacaoPresencaService.registrarComparecimento` agora chama `CobrancaService.gerarDiariaSeAplicavel`), só para avulsos com status COMPARECEU (ADR-005).
- `PagamentoService`: pagamentos parciais, cálculo de saldo pendente, bloqueio de pagamento acima do saldo (`RegraNegocioException`), status da cobrança (PENDENTE/PARCIAL/PAGO) derivado automaticamente do saldo.
- `WhatsAppLinkService`: monta link `wa.me` com mensagem pré-formatada (nome, tipo de cobrança, valor, vencimento, Pix) e `URLEncoder` para codificação correta. Não envia nada automaticamente — administrador revisa e clica manualmente.
- Campo `telefone` adicionado a `Usuario` (migration V8) para viabilizar o link do WhatsApp.
- `FinanceiroAdminController`/`FinanceiroJogadorController` e páginas Thymeleaf: lista financeira, detalhe de cobrança com histórico de pagamentos e registro de novo pagamento, painel "minhas cobranças" do jogador.

**Testes executados:** `./mvnw.cmd test` → 27 testes, 0 falhas, 0 erros. Novos: `CobrancaServiceTest` (6 casos: mensalidade sem duplicidade, não gera para avulso, diária só com comparecimento, não gera para falta, não gera para mensalista, não duplica diária em registro repetido), `PagamentoServiceTest` (5 casos: pagamento parcial e saldo, pago ao zerar saldo, bloqueio acima do saldo total, bloqueio acima do saldo após parcial, bloqueio de valor zero/negativo), `ControleAcessoTest` (4 casos: redirect sem autenticação, bloqueio de página admin sem autenticação, 403 para jogador comum em página admin, 403 para jogador comum no financeiro admin).

**Commit relacionado:** pendente de registro nesta entrada (ver seção de commits no resumo final).

**Pendências conhecidas:**
- QR Code estático do Pix (mencionado como opcional no briefing, seção 7.5) não foi implementado nesta etapa — a tela de configuração aceita apenas a chave Pix em texto. Pode ser adicionado depois sem mudança estrutural.
- Teste de "proteção das informações financeiras" cobre controle de acesso por perfil (admin vs jogador), mas não cobre explicitamente que um jogador não acessa a cobrança de outro jogador via URL direta (`/admin/financeiro/{id}`) — essa rota já está protegida por `hasRole("ADMINISTRADOR")`, então um jogador comum não acessa nenhuma cobrança por ali; falta apenas um teste explícito de isolamento entre dois jogadores nas rotas `/jogador/financeiro` (a query já é escopada por `usuario.getId()` do principal autenticado, mas sem teste automatizado dedicado).

---

## Etapa 5 — Integração, Docker e documentação final
**Data:** 2026-10-18
**Objetivo:** ligar a navegação entre os módulos, preparar execução via Docker, escrever README e documentação de uso de agentes de IA.

**Alterações realizadas:**
- Dashboard administrativo e painel do jogador atualizados: cards "em desenvolvimento" substituídos por links reais para Partidas, Financeiro, Configurações (admin) e Próxima partida/Financeiro (jogador).
- `Dockerfile` (build multi-stage com Maven Wrapper, runtime `eclipse-temurin:21-jre`), `compose.yaml` (app + MySQL 8 com healthcheck), `.env.example` sem credenciais reais.
- `.gitignore` atualizado para excluir `.env`/`.env.local`/chaves privadas.
- `README.md` completo: problema, público-alvo, funcionalidades, tecnologias, arquitetura resumida, instruções de execução (Docker e Maven Wrapper local), configuração do MySQL, criação do primeiro administrador, execução de testes, status real do desenvolvimento, limitações declaradas.
- `docs/AI_WORKFLOW.md`: relato honesto do uso do Kiro Crew, incluindo a decisão de não paralelizar a escrita de código do monólito, os problemas reais encontrados (Spring Initializr recusando Boot 3.x, ambiente sem Docker/Maven/gh, bug de ambiguidade de construtor, bloqueio de push a `main`) e as decisões que exigiram validação humana.

**Testes executados:** `./mvnw.cmd test` → 27 testes, 0 falhas, 0 erros (suíte completa inalterada nesta etapa, apenas documentação/infra/navegação).

**Commit relacionado:** pendente de registro nesta entrada (ver seção de commits no resumo final).

**Pendências conhecidas:**
- `Dockerfile`/`compose.yaml` **não executados** nesta sessão (ambiente sem Docker) — validação em máquina com Docker é um próximo passo necessário antes de declarar a via Docker como comprovadamente funcional.
- Capturas de tela da aplicação ainda não foram produzidas (dependem de execução visual da aplicação, não realizada nesta sessão).
- Aplicação não publicada em nenhuma hospedagem externa.
