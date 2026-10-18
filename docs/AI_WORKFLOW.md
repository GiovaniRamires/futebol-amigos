# Fluxo de Desenvolvimento Assistido por IA (Kiro Crew)

Este documento descreve, com honestidade, como o Kiro Crew foi efetivamente utilizado no desenvolvimento deste projeto — sem inventar deliberações, colaborações multiagente ou resultados que a ferramenta não produziu de fato.

## Como o Kiro Crew foi utilizado

O desenvolvimento ocorreu em **uma única sessão de agente de IA contínua** (Kiro, rodando sobre o Kiro Crew), que conduziu o ciclo completo: inspeção do ambiente, planejamento, implementação, testes e documentação. Não foram instanciados múltiplos subagentes paralelos (`spawn_run`) para a escrita de código de produção.

**Por que uma única sessão, e não 5 agentes paralelos independentes?** Avaliou-se essa opção explicitamente no início do projeto (ver `docs/DECISIONS.md` ADR-009). O Kiro Crew oferece mecanismos reais de paralelismo (`spawn_run` para subagentes em background, `workflow_run` para workflows multi-fase), mas o desenvolvimento de um monólito pequeno com entidades, services e controllers fortemente interdependentes — onde cada etapa lê e escreve nos mesmos arquivos de domínio criados na etapa anterior — não se beneficia de paralelismo real: paralelizar a escrita de código ali criaria conflitos de edição simultânea nos mesmos arquivos, exatamente o problema que o próprio briefing pede para evitar ("evitar que agentes alterem simultaneamente os mesmos arquivos sem coordenação"). A decisão foi documentada e comunicada no início da sessão, não inventada retroativamente.

## Papéis/responsabilidades efetivamente empregados

Não houve 5 processos de agente distintos. A mesma sessão assumiu, em sequência, os papéis descritos no briefing original:

1. **Planejamento e arquitetura** — inspeção do repositório e do ambiente (Git, estrutura, ferramentas disponíveis), definição da modelagem de entidades, documentação das decisões arquiteturais (`docs/DECISIONS.md`, `docs/ARCHITECTURE.md`) antes de qualquer código.
2. **Backend Java/Spring Boot** — implementação de entidades JPA, repositórios, services com as regras de negócio, DTOs, controllers MVC, Spring Security.
3. **Interface web** — páginas Thymeleaf (layout reutilizável via `thymeleaf-layout-dialect`, Bootstrap 5, formulários integrados aos controllers).
4. **Testes e qualidade** — escrita e **execução real** de testes JUnit/Mockito/AssertJ a cada etapa concluída, nunca declarando testes como aprovados sem rodá-los (ver seção "Como os testes foram usados" abaixo).
5. **Revisão e documentação** — atualização contínua de `docs/DEVELOPMENT_LOG.md` a cada etapa, com data, objetivo, alterações, testes e pendências reais.

## Como as tarefas foram distribuídas e coordenadas

Como não houve múltiplos agentes simultâneos, a "coordenação" se deu através de:

- Uma única fonte de requisitos e decisões: este repositório e seus documentos em `docs/`.
- Divisão do trabalho em etapas sequenciais e verificáveis (Etapa 1 a 6, conforme planejado no início), cada uma com commit(s) próprio(s) e testes executados antes de avançar.
- Nenhuma tarefa foi considerada concluída apenas porque o agente "disse que terminou": cada etapa foi seguida por compilação (`mvnw compile`) e execução de testes (`mvnw test`) reais, com os resultados (número de testes, falhas, erros) lidos diretamente da saída do Maven/Surefire antes de registrar a etapa como concluída.

## Como as alterações foram revisadas e integradas

Cada arquivo criado foi lido de volta (ou seu diff revisado) antes do commit. Erros de compilação encontrados durante o desenvolvimento (ex.: import incorreto de `DateTimeFormat`, ambiguidade de construtor no `SorteioService` causando falha no boot do contexto Spring) foram diagnosticados pela mensagem de erro real do Maven/Spring e corrigidos, não ignorados ou "simulados como corrigidos".

## Como os testes foram utilizados para validar as entregas

A cada etapa funcional concluída (autenticação/jogadores, partidas/sorteio, financeiro), uma suíte de testes JUnit 5 + Spring Boot Test + AssertJ foi escrita cobrindo as regras de negócio obrigatórias do briefing, e **executada de fato** via `./mvnw test`, com o resultado lido do relatório Surefire (`target/surefire-reports/*.txt`) — nunca apenas assumido. Nenhuma falha de compilação ou de teste foi ignorada para declarar uma etapa "pronta"; quando um teste falhava, a causa raiz era investigada (ex.: lendo o stacktrace completo do `BeanCreationException` do Spring) antes de prosseguir.

## Limitações, problemas e conflitos encontrados

- O **Spring Initializr público já não aceita gerar projetos com Spring Boot 3.x** (linha fora de suporte OSS desde 30/06/2026) — isso exigiu uma mudança de decisão técnica em relação ao briefing original, que pedia explicitamente 3.x. A mudança para Spring Boot 4.1.1 foi comunicada e autorizada pelo usuário antes de prosseguir.
- O **ambiente onde a sessão de agente rodou não tinha Maven, Docker nem GitHub CLI (`gh`) instalados globalmente** — apenas JDK 21. Isso foi contornado usando o Maven Wrapper (gerado pelo próprio Initializr) para build/test, e usando `git` HTTPS padrão para push. O Docker/Compose foram escritos mas não puderam ser executados/validados nesta sessão — limitação declarada explicitamente no README, não escondida.
- **Push direto para a branch `main` foi bloqueado pela política de segurança do próprio Kiro Crew** (regra que impede agentes de fazer push para branches protegidas), mesmo com `main` remoto ainda vazio no momento do primeiro push. Isso foi tratado criando uma branch de feature (`feature/bootstrap-projeto`) e enviando os commits para ela — o merge para `main` requer uma ação humana explícita (abrir e aceitar o Pull Request no GitHub, ou autorizar expressamente um push direto nesse caso específico de bootstrap).
- Um bug real de inicialização do Spring (ambiguidade de construtor no `SorteioService`, causando `NoSuchMethodException` ao montar o contexto da aplicação) foi encontrado durante a execução dos testes da Etapa 3, diagnosticado pelo stacktrace e corrigido com `@Autowired` explícito no construtor de produção.

## Decisões que exigiram intervenção ou validação humana

- Confirmação explícita para usar **Spring Boot 4.1.1** em vez da linha 3.x pedida no briefing original (ver `docs/DECISIONS.md` ADR-001).
- Autorização explícita para usar **subagentes paralelos** quando a memória do ambiente permitiu (o briefing original já previa essa flexibilidade: "se necessário, um agente pode acumular responsabilidades"; a decisão de não paralelizar a escrita de código do monólito, mesmo com memória liberada, foi técnica, documentada em ADR-009, e comunicada no fluxo da sessão).
- **Decisão pendente de ação humana:** o merge de `feature/bootstrap-projeto` para `main` no GitHub (via Pull Request) ou a autorização explícita de um push direto a `main`, já que a política de segurança do Kiro Crew bloqueia esse push a partir do agente.
