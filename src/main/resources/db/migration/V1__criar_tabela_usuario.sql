-- V1: tabela de usuarios (administradores e jogadores)

CREATE TABLE usuario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(120)    NOT NULL,
    email           VARCHAR(180)    NOT NULL,
    senha_hash      VARCHAR(255)    NOT NULL,
    perfil          VARCHAR(20)     NOT NULL,
    tipo_jogador    VARCHAR(20)     NOT NULL,
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
