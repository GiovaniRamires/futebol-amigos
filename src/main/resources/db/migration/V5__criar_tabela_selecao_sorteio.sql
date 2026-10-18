-- V5: selecao explicita do administrador para o sorteio (fonte unica de elegibilidade)

CREATE TABLE selecao_sorteio (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partida_id                  BIGINT          NOT NULL,
    usuario_id                  BIGINT          NOT NULL,
    justificativa_excecao       VARCHAR(255),
    criado_em                   TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_selecao_partida_usuario UNIQUE (partida_id, usuario_id),
    CONSTRAINT fk_selecao_partida FOREIGN KEY (partida_id) REFERENCES partida (id),
    CONSTRAINT fk_selecao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
