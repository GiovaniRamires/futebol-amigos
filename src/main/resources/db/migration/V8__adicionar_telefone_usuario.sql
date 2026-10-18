-- V8: adiciona telefone ao usuario (usado para montar o link do WhatsApp)

ALTER TABLE usuario ADD COLUMN telefone VARCHAR(20) NULL;
