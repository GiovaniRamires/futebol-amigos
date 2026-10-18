package com.futebolamigos.service;

import com.futebolamigos.model.ConfiguracaoSistema;
import com.futebolamigos.repository.ConfiguracaoSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Acesso e atualizacao da configuracao global do sistema (singleton).
 */
@Service
public class ConfiguracaoSistemaService {

    private final ConfiguracaoSistemaRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ConfiguracaoSistemaService(ConfiguracaoSistemaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoSistema buscar() {
        return repository.findById(ConfiguracaoSistema.ID_UNICO)
                .orElseThrow(() -> new IllegalStateException(
                        "Configuracao do sistema nao encontrada - a migration de seed (V2) nao foi aplicada corretamente."));
    }

    @Transactional
    public ConfiguracaoSistema atualizar(ConfiguracaoSistema novaConfiguracao) {
        ConfiguracaoSistema atual = buscar();
        atual.setValorMensalidade(novaConfiguracao.getValorMensalidade());
        atual.setDiaVencimentoMensalidade(novaConfiguracao.getDiaVencimentoMensalidade());
        atual.setChavePix(novaConfiguracao.getChavePix());
        atual.setNomeBeneficiarioPix(novaConfiguracao.getNomeBeneficiarioPix());
        atual.setInstrucoesPagamento(novaConfiguracao.getInstrucoesPagamento());
        return repository.save(atual);
    }

    /**
     * Retorna o token vigente do link de autocadastro, gerando um na primeira
     * chamada caso ainda nao exista (instalacoes criadas antes da V9).
     */
    @Transactional
    public String obterOuGerarTokenCadastro() {
        ConfiguracaoSistema atual = buscar();
        if (atual.getTokenCadastro() == null || atual.getTokenCadastro().isBlank()) {
            atual.setTokenCadastro(gerarToken());
            repository.save(atual);
        }
        return atual.getTokenCadastro();
    }

    /** Invalida o link atual e gera um novo, para revogar acesso ao link anterior. */
    @Transactional
    public String regenerarTokenCadastro() {
        ConfiguracaoSistema atual = buscar();
        atual.setTokenCadastro(gerarToken());
        repository.save(atual);
        return atual.getTokenCadastro();
    }

    /** Verifica se o token informado corresponde ao token vigente (comparacao simples; token e de uso publico por link, nao secreto de alta sensibilidade). */
    @Transactional(readOnly = true)
    public boolean tokenCadastroValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String atual = buscar().getTokenCadastro();
        return atual != null && atual.equals(token);
    }

    private String gerarToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
