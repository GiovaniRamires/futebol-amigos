
## ADR-010: Link de autocadastro reutilizável, não convite individual por pessoa
**Decisão:** o autocadastro de jogadores usa um único token reutilizável (`ConfiguracaoSistema.tokenCadastro`), compartilhado pelo administrador via WhatsApp, em vez de um convite único por pessoa.
**Contexto:** o objetivo explícito do usuário era reduzir o trabalho manual do administrador; convites individuais exigiriam gerar e enviar um link por pessoa, o que contradiria esse objetivo. O grupo é pequeno e já usa WhatsApp como canal de confiança — o mesmo canal que hoje distribui avisos e cobranças manualmente.
**Consequência:** qualquer pessoa com o link pode se cadastrar enquanto o token não for regenerado; por isso o cadastro via link nunca concede perfil ADMINISTRADOR nem status mensalista automaticamente — o jogador entra sempre como JOGADOR/AVULSO/ativo, e a promoção a mensalista (que implica cobrança recorrente) continua sendo decisão exclusiva do administrador. O administrador pode revogar o link a qualquer momento regenerando o token, invalidando imediatamente o link anterior.

# Decisões Arquiteturais (ADR resumido)

## ADR-001: Versão do Spring Boot
**Decisão:** Spring Boot 4.1.1 (Java 21, Maven).
**Contexto:** O briefing original pedia Spring Boot 3.x. Em 18/10/2026 a linha 3.x (última: 3.5.16) já está fora de suporte OSS desde 30/06/2026, e o Spring Initializr público (`start.spring.io`) já recusa gerar projetos com `bootVersion` 3.x (`compatibility range is >=4.0.0`). O usuário autorizou explicitamente o uso de Spring Boot 4.1.1 durante a Etapa 1.
**Consequência:** nomes de starters mudam (`spring-boot-starter-webmvc` em vez de `-web`), Spring Security 7 (API majoritariamente compatível com 6.x para os casos usados aqui), starters `*-test` dedicados por dependência. Nenhum requisito funcional foi alterado.

## ADR-002: Maven Wrapper em vez de Maven global
**Decisão:** usar `mvnw`/`mvnw.cmd` (gerados pelo Spring Initializr) para build/test.
**Contexto:** o ambiente de execução dos agentes não tem Maven nem Docker instalados globalmente (apenas JDK 21). O wrapper baixa e usa o Maven correto sem exigir instalação prévia.
**Consequência:** qualquer pessoa clona o repo e roda `./mvnw test` sem instalar Maven. Docker continua necessário para rodar MySQL localmente (ver limitações no README).

## ADR-003: Elegibilidade para sorteio é sempre explícita (SelecaoSorteio)
**Decisão:** confirmação de presença (`ConfirmacaoPresenca`) nunca torna um jogador elegível para o sorteio por si só. Existe uma entidade separada `SelecaoSorteio` que registra, para cada partida, quem o administrador selecionou manualmente para participar do sorteio — incluindo atrasados e, com justificativa obrigatória, jogadores sem confirmação prévia.
**Contexto:** regra de negócio obrigatória do briefing (seção 7.3), com atenção especial solicitada.
**Consequência:** o fluxo de sorteio sempre parte de `SelecaoSorteio`, nunca de `ConfirmacaoPresenca` diretamente. Testado explicitamente.

## ADR-004: Cobranca unificada para mensalidade e diária
**Decisão:** uma única entidade `Cobranca` com campo `tipo` (MENSALIDADE | DIARIA), em vez de duas tabelas separadas.
**Contexto:** ambas compartilham o mesmo ciclo de vida (gerar → pagar parcial/total → saldo), e o briefing pede simplicidade.
**Consequência:** restrição de unicidade por (jogador, tipo, competência) evita duplicidade de cobrança para a mesma competência/jogador.

## ADR-005: Diária gerada apenas após registro de comparecimento
**Decisão:** a cobrança do tipo DIARIA só é criada quando o administrador registra "compareceu" na partida para um jogador avulso. Faltas não geram diária. Mensalistas nunca recebem diária.
**Contexto:** regra de negócio explícita do briefing (seção 7.4).

## ADR-006: Sem integração bancária ou WhatsApp automática
**Decisão:** Pix é apenas uma chave configurada + QR estático opcional; WhatsApp é só um link `wa.me` com mensagem pré-formatada que o administrador envia manualmente.
**Contexto:** exigência explícita do briefing (seções 7.5 e 7.6) para manter o escopo pequeno e evitar dependências externas pagas/sensíveis.

## ADR-007: Monólito único, sem microsserviços
**Decisão:** uma aplicação Spring Boot MVC + Thymeleaf server-side rendering, MySQL único, sem filas, sem cache distribuído.
**Contexto:** exigência explícita do briefing (seção 3) e adequação ao tamanho real do grupo de usuários (um grupo pequeno de amigos).

## ADR-008: Ambiente de testes usa H2 em modo MySQL (nao Testcontainers)
**Decisão:** os testes de integracao usam H2 em memoria com `MODE=MySQL`, em vez de Testcontainers com um MySQL real.
**Contexto:** o ambiente de execucao dos agentes de IA (esta maquina) nao possui Docker instalado, o que torna Testcontainers inviavel aqui. A aplicacao de producao continua configurada para MySQL 8 real (`application.properties`); apenas o profile `test` (`application-test.properties`) usa H2.
**Consequência e limitação conhecida:** H2 em modo MySQL cobre a grande maioria dos casos (tipos, constraints, Flyway), mas nao e MySQL real — diferencas finas de dialeto SQL podem nao ser detectadas pelos testes. Isso e uma limitação conhecida e documentada, não uma validação completa contra MySQL. Recomenda-se, antes de publicar/produzir, rodar `./mvnw verify` uma vez com Testcontainers (ou MySQL local via Docker) em uma maquina que tenha Docker disponivel — ver README.

## ADR-009: Papéis de "agentes" dentro do Kiro Crew
**Decisão:** uma única sessão de agente conduz o desenvolvimento ponta a ponta (planejamento, implementação, revisão), documentando explicitamente qual "chapéu" está sendo usado em cada etapa, em vez de instanciar 5 subagentes persistentes e paralelos.
**Contexto:** o Kiro Crew oferece subagentes (`spawn_run`) para tarefas paralelas e independentes — mas desenvolvimento sequencial de um monólito pequeno com arquivos compartilhados (entidades, services, controllers interdependentes) não se beneficia de paralelismo real; paralelizar aqui criaria conflitos de edição simultânea nos mesmos arquivos. Subagentes serão usados pontualmente para tarefas genuinamente independentes (ex.: revisão de um conjunto de testes já implementado, ou geração de documentação a partir de código já finalizado), e isso será registrado com honestidade em `docs/AI_WORKFLOW.md`.
