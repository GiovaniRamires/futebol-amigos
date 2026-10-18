-- V2: configuracao global do sistema (singleton, id=1)

CREATE TABLE configuracao_sistema (
    id                              BIGINT PRIMARY KEY,
    valor_mensalidade               DECIMAL(10,2)   NOT NULL,
    dia_vencimento_mensalidade      INT             NOT NULL,
    chave_pix                       VARCHAR(160),
    nome_beneficiario_pix           VARCHAR(160),
    instrucoes_pagamento            VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed inicial com valores neutros; o administrador ajusta depois pela tela de configuracoes.
INSERT INTO configuracao_sistema (id, valor_mensalidade, dia_vencimento_mensalidade)
VALUES (1, 100.00, 10);
