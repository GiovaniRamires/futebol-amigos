-- V6: sorteios, times e participacoes (historico preservado)

CREATE TABLE sorteio (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    partida_id      BIGINT          NOT NULL,
    publicado       BOOLEAN         NOT NULL DEFAULT FALSE,
    criado_em       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sorteio_partida FOREIGN KEY (partida_id) REFERENCES partida (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE time (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sorteio_id      BIGINT          NOT NULL,
    nome            VARCHAR(40)     NOT NULL,
    CONSTRAINT fk_time_sorteio FOREIGN KEY (sorteio_id) REFERENCES sorteio (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE participacao_time (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    time_id         BIGINT          NOT NULL,
    usuario_id      BIGINT          NOT NULL,
    CONSTRAINT uk_participacao_time_usuario UNIQUE (time_id, usuario_id),
    CONSTRAINT fk_participacao_time FOREIGN KEY (time_id) REFERENCES time (id),
    CONSTRAINT fk_participacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_sorteio_partida ON sorteio (partida_id);
