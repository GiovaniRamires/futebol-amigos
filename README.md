# Futebol Amigos ⚽

Sistema web para organizar o futebol semanal de um grupo de amigos e familiares: confirmação de presença, seleção e sorteio dos times, mensalidades/diárias e controle de pagamentos — tudo centralizado em um único lugar, no lugar de conversas espalhadas de WhatsApp.

Projeto desenvolvido como demonstração pública de **desenvolvimento assistido por agentes de IA** (Kiro Crew), para portfólio profissional. Ver [`docs/AI_WORKFLOW.md`](docs/AI_WORKFLOW.md) para o relato honesto de como os agentes foram usados.

## Problema que resolve

Grupos pequenos de futebol recreativo costumam perder o controle de quem vai jogar, quem já pagou a mensalidade/diária e como ficaram os times — tudo misturado em grupos de WhatsApp e anotações manuais. O sistema centraliza isso em um painel simples, acessível do celular.

## Público-alvo

Grupos pequenos (dezenas de pessoas, não milhares) de amigos/familiares que jogam futebol regularmente e têm um organizador informal (o "administrador" do grupo).

## Funcionalidades

- **Administrador:** cadastra jogadores (mensalista/avulso), cria partidas, seleciona explicitamente quem participa do sorteio, realiza e publica o sorteio, registra comparecimento, configura mensalidade/Pix, registra pagamentos.
- **Jogador:** confirma/cancela presença (com lista de espera), consulta a próxima partida e os times publicados, consulta suas próprias cobranças e pagamentos.

Regra central: **a confirmação de presença, isoladamente, nunca torna o jogador elegível para o sorteio.** Apenas a seleção explícita do administrador (que pode incluir atrasados e, com justificativa, jogadores sem confirmação prévia) define quem entra no sorteio. Ver [`docs/DECISIONS.md`](docs/DECISIONS.md) (ADR-003).

## Tecnologias

Java 21 · Spring Boot 4.1.1 · Spring MVC · Thymeleaf · Spring Data JPA/Hibernate · PostgreSQL 16 · Flyway · Spring Security · Bean Validation · Lombok · JUnit 5 · Mockito/AssertJ · Maven (com Maven Wrapper) · Docker/Docker Compose · Bootstrap 5.

> **Nota sobre a versão do Spring Boot:** o plano original pedia Spring Boot 3.x. Em outubro de 2026 a linha 3.x já estava fora de suporte OSS e o Spring Initializr público já recusava gerar projetos 3.x. Optou-se por Spring Boot 4.1.1 (decisão autorizada explicitamente durante o desenvolvimento) — ver [`docs/DECISIONS.md`](docs/DECISIONS.md) ADR-001.

## Arquitetura (resumo)

Monólito Spring Boot MVC + Thymeleaf, organizado por camadas (`controller` → `service` → `repository` → `model`), sem microsserviços, sem filas, sem cache distribuído. Detalhes completos em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Como executar localmente

### Opção 1 — Docker Compose (recomendado)

```bash
cp .env.example .env
# edite o .env com uma senha de banco e, opcionalmente, um e-mail/senha de admin
docker compose up --build
```

A aplicação sobe em `http://localhost:8080`, com PostgreSQL 16 em um container próprio (dados persistidos em volume Docker).

> Validado nesta máquina com Docker real: `docker compose up --build` builda a imagem, aplica as 9 migrations Flyway contra o PostgreSQL do container e expõe `/login` com status 200.

### Opção 2 — Maven Wrapper + PostgreSQL local

Pré-requisitos: Java 21, um PostgreSQL 16 acessível (local ou remoto).

```bash
# cria o banco (ajuste usuário/senha conforme seu PostgreSQL)
psql -U postgres -c "CREATE DATABASE futebol_amigos;"

# variáveis de ambiente (ou ajuste direto em application.properties)
export DB_HOST=localhost DB_PORT=5432 DB_NAME=futebol_amigos DB_USER=futebol DB_PASSWORD=sua_senha
export ADMIN_EMAIL=admin@exemplo.com ADMIN_SENHA=uma_senha_forte

./mvnw spring-boot:run      # Linux/macOS
.\mvnw.cmd spring-boot:run  # Windows
```

A aplicação sobe em `http://localhost:8080`.

## Deploy gratuito no Render

O repositório inclui um [`render.yaml`](render.yaml) (Render Blueprint) que provisiona, sem custo e sem cartão de crédito:
- um **Web Service** free, buildado direto do `Dockerfile` deste repositório;
- um **PostgreSQL** gerenciado free.

Passos:
1. No [Render Dashboard](https://dashboard.render.com), **New → Blueprint** e aponte para este repositório (branch `main`).
2. Render lê o `render.yaml`, cria o banco e o serviço, e já liga as variáveis `DB_*` automaticamente ao banco criado.
3. Defina manualmente `ADMIN_EMAIL` e `ADMIN_SENHA` nas env vars do Web Service (ficam marcadas `sync: false` no blueprint — não vão para o Git).
4. Aguarde o build/deploy; a URL pública aparece no painel do serviço.

Limitações do plano free (declaradas com honestidade): o Web Service "dorme" após 15 min sem tráfego (primeira requisição subsequente demora ~30-60s); o banco free do Render expira após 90 dias de existência e precisa ser recriado (não é por inatividade de uso, é por idade).

## Configuração do PostgreSQL

A aplicação espera um banco PostgreSQL 16 vazio (as tabelas são criadas automaticamente pelo Flyway no primeiro start). Variáveis de ambiente aceitas (ver `application.properties`):

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `futebol_amigos` | Nome do banco |
| `DB_USER` / `DB_PASSWORD` | `futebol` / `futebol` | Credenciais |

## Como criar o primeiro administrador

Não é necessário nenhum passo manual: no primeiro start, se **nenhum** usuário com perfil `ADMINISTRADOR` existir no banco, a aplicação cria automaticamente um administrador com:

- E-mail: variável `ADMIN_EMAIL` (padrão `admin@futebolamigos.local`)
- Senha: variável `ADMIN_SENHA` — se deixada em branco, uma senha aleatória é **gerada e impressa uma única vez** no log de inicialização (procure por `Administrador inicial criado` no console). Anote-a imediatamente; ela não é exibida novamente.

Depois do primeiro login, use a tela **Jogadores** para cadastrar os demais administradores/jogadores.

## Como executar os testes

```bash
./mvnw test       # Linux/macOS
.\mvnw.cmd test   # Windows
```

Os testes usam um perfil `test` com H2 em memória em modo de compatibilidade PostgreSQL (não Testcontainers) — ver [`docs/DECISIONS.md`](docs/DECISIONS.md) ADR-008 para a justificativa e a limitação conhecida dessa escolha.

Estado real na última execução registrada: **27 testes, 0 falhas, 0 erros** (ver [`docs/DEVELOPMENT_LOG.md`](docs/DEVELOPMENT_LOG.md) para o detalhamento por etapa).

## Como acessar a aplicação

Depois de iniciada (qualquer uma das opções acima), acesse `http://localhost:8080/login` no navegador (celular ou computador) e entre com o e-mail/senha do administrador inicial (ou de um jogador cadastrado por ele).

## Capturas de tela

Ainda não capturadas nesta fase de desenvolvimento — pendente.

## Status real do desenvolvimento

- ✅ Autenticação, cadastro/gestão de jogadores, controle de acesso por perfil.
- ✅ Partidas, confirmação de presença com lista de espera, registro de comparecimento.
- ✅ Seleção explícita para o sorteio, sorteio aleatório com times equilibrados, histórico, publicação.
- ✅ Mensalidades, diárias (geradas apenas após comparecimento de avulso), pagamentos parciais com saldo, link do WhatsApp.
- ✅ Suíte de testes automatizados cobrindo as regras de negócio obrigatórias (ver seção de testes).
- ⚠️ Dockerfile/Compose escritos, **não executados** neste ambiente (sem Docker disponível) — validação pendente em máquina com Docker.
- ⚠️ QR Code estático do Pix (opcional no briefing) não implementado.
- ❌ Aplicação **não está publicada** em nenhuma hospedagem até o momento desta entrega.

## Link da aplicação publicada

Não disponível. A aplicação não foi publicada em nenhuma hospedagem externa nesta entrega — ver seção "Próximos passos" no resumo final da sessão de desenvolvimento.

## Uso do Kiro Crew e de agentes de IA

Este projeto foi desenvolvido com o Kiro Crew conduzindo o ciclo completo (planejamento, implementação, revisão, documentação) em uma única sessão de agente contínua, documentando explicitamente cada etapa. O relato detalhado e honesto — incluindo limitações reais e decisões que exigiram intervenção humana — está em [`docs/AI_WORKFLOW.md`](docs/AI_WORKFLOW.md).
