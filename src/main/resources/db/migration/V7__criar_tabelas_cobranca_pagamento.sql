-- V7: cobrancas (mensalidade/diaria) e pagamentos

CREATE TABLE cobranca (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      BIGINT          NOT NULL,
    tipo            VARCHAR(20)     NOT NULL,
    competencia     VARCHAR(7)      NOT NULL,
    partida_id      BIGINT          NULL,
    valor           DECIMAL(10,2)   NOT NULL,
    vencimento      DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    criado_em       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cobranca_usuario_tipo_competencia_partida
        UNIQUE (usuario_id, tipo, competencia, partida_id),
    CONSTRAINT fk_cobranca_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_cobranca_partida FOREIGN KEY (partida_id) REFERENCES partida (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pagamento (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cobranca_id         BIGINT          NOT NULL,
    valor               DECIMAL(10,2)   NOT NULL,
    data_pagamento      DATE            NOT NULL,
    metodo              VARCHAR(20)     NOT NULL,
    registrado_por_id   BIGINT          NOT NULL,
    criado_em           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pagamento_cobranca FOREIGN KEY (cobranca_id) REFERENCES cobranca (id),
    CONSTRAINT fk_pagamento_registrado_por FOREIGN KEY (registrado_por_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_cobranca_usuario ON cobranca (usuario_id);
CREATE INDEX idx_pagamento_cobranca ON pagamento (cobranca_id);
