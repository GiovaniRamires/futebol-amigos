# Arquitetura

## Visão geral

Monólito Spring Boot (MVC + Thymeleaf), organizado por responsabilidade em um único módulo Maven. Sem microsserviços, sem cache distribuído, sem filas — desenho adequado a um grupo pequeno de jogadores.

```
com.futebolamigos
├── config        # SecurityConfig, WebConfig, propriedades de configuração
├── model         # entidades JPA
├── repository    # Spring Data JPA repositories
├── service       # regras de negócio e transações
├── dto           # objetos de entrada/saída dos controllers
├── controller    # endpoints MVC (páginas Thymeleaf + ações de formulário)
└── exception     # exceções de negócio + handler global
```

## Camadas e responsabilidades

- **controller**: recebe requisições HTTP, valida entrada (Bean Validation), delega ao service, escolhe a view/redirect. Não contém regra de negócio.
- **service**: contém as regras de negócio (sorteio, cálculo de saldo, geração de cobranças) e as transações (`@Transactional`). É a única camada que pode orquestrar múltiplos repositories.
- **repository**: Spring Data JPA, consultas derivadas e `@Query` quando necessário.
- **model**: entidades JPA puras, sem lógica de negócio complexa (getters/setters via Lombok, validações estruturais via Bean Validation).
- **dto**: formulários de entrada e respostas de view, evitando expor entidades JPA diretamente no Thymeleaf quando há dados sensíveis (ex.: senha).

## Entidades principais e relacionamentos

- `Usuario` (1) ──< `ConfirmacaoPresenca` (N) — um jogador confirma presença em várias partidas.
- `Usuario` (1) ──< `SelecaoSorteio` (N) — um jogador pode ser selecionado para sorteio em várias partidas.
- `Partida` (1) ──< `ConfirmacaoPresenca` (N)
- `Partida` (1) ──< `SelecaoSorteio` (N)
- `Partida` (1) ──< `Sorteio` (N) — histórico: pode haver mais de um sorteio por partida (refazer antes de publicar).
- `Sorteio` (1) ──< `Time` (2) — exatamente dois times por sorteio.
- `Time` (1) ──< `ParticipacaoTime` (N) ── (N) `Usuario`
- `Usuario` (1) ──< `Cobranca` (N) — mensalidades e diárias.
- `Cobranca` (1) ──< `Pagamento` (N) — pagamentos parciais.
- `ConfiguracaoSistema` — singleton de configuração (valor mensalidade, vencimento, chave Pix).

## Fluxo: presença e sorteio (regra central)

1. Administrador cria `Partida`.
2. Jogadores confirmam presença → `ConfirmacaoPresenca` (CONFIRMADO/CANCELADO), com lista de espera se o limite for atingido.
3. Administrador registra comparecimento real (compareceu/faltou/atrasado) — campo na própria confirmação ou registro de presença efetiva.
4. **Separadamente**, o administrador abre a tela de seleção de sorteio: vê todos os jogadores e seus status de presença, e marca explicitamente quem entra no sorteio (`SelecaoSorteio`), podendo incluir atrasados e, com justificativa textual obrigatória, jogadores sem confirmação.
5. Sorteio roda sobre o conjunto de `SelecaoSorteio` da partida — nunca sobre `ConfirmacaoPresenca` diretamente.
6. Sorteio aleatório divide os selecionados em 2 times com diferença máxima de 1 jogador; grava `Sorteio` + `Time` + `ParticipacaoTime`.
7. Administrador pode refazer o sorteio (novo registro de `Sorteio`, histórico preservado) antes de publicar.
8. Ao publicar, jogadores passam a ver os times no painel.

## Fluxo: financeiro

1. Mensalidade: gerada por competência (mês/ano) para jogadores mensalistas ativos, com restrição de unicidade (jogador + tipo MENSALIDADE + competência) para não duplicar.
2. Diária: gerada **somente** quando o administrador registra comparecimento de um jogador avulso numa partida. Faltas não geram diária. Mensalistas não recebem diária.
3. Pagamento: administrador registra pagamento (Pix/dinheiro), valor, data, responsável. Soma de pagamentos de uma cobrança nunca pode exceder o valor da cobrança (validação de serviço + transação).
4. Status da cobrança (PENDENTE/PARCIAL/PAGO) é derivado do saldo (valor - soma de pagamentos).

## Decisões técnicas relevantes

Ver `docs/DECISIONS.md` para o detalhamento e justificativa de cada decisão (versão do Spring Boot, Maven Wrapper, modelagem de Cobranca unificada, etc.).
