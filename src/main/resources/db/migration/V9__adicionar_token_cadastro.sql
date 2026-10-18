-- V9: token de autocadastro de jogadores (link publico reutilizavel).
-- Gerado automaticamente no boot (ver ConfiguracaoSistemaService) para instalacoes
-- existentes; novas instalacoes recebem um token aleatorio já no seed.

ALTER TABLE configuracao_sistema
    ADD COLUMN token_cadastro VARCHAR(40) NULL;
