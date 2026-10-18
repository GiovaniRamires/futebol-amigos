-- V4: confirmacao de presenca (feita pelo jogador) + comparecimento efetivo (registrado pelo admin)

CREATE TABLE confirmacao_presenca (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partida_id              BIGINT          NOT NULL,
    usuario_id              BIGINT          NOT NULL,
    status                  VARCHAR(20)     NOT NULL,
    status_comparecimento  VARCHAR(20)     NOT NULL DEFAULT 'NAO_REGISTRADO',
    criado_em               TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_confirmacao_partida_usuario UNIQUE (partida_id, usuario_id),
    CONSTRAINT fk_confirmacao_partida FOREIGN KEY (partida_id) REFERENCES partida (id),
    CONSTRAINT fk_confirmacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
