package com.futebolamigos.service;

import com.futebolamigos.model.ConfiguracaoSistema;
import com.futebolamigos.repository.ConfiguracaoSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acesso e atualizacao da configuracao global do sistema (singleton).
 */
@Service
public class ConfiguracaoSistemaService {

    private final ConfiguracaoSistemaRepository repository;

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
}
