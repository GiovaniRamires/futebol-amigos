-- V3: partidas

CREATE TABLE partida (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data_hora               TIMESTAMP       NOT NULL,
    local                   VARCHAR(160)    NOT NULL,
    limite_participantes    INT             NOT NULL,
    valor_diaria            DECIMAL(10,2)   NOT NULL,
    cancelada               BOOLEAN         NOT NULL DEFAULT FALSE,
    criado_em               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partida_data_hora ON partida (data_hora);
