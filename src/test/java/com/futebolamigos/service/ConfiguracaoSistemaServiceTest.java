package com.futebolamigos.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConfiguracaoSistemaServiceTest {

    @Autowired
    private ConfiguracaoSistemaService service;

    @Test
    void geraTokenDeCadastroNaPrimeiraChamadaEReutilizaDepois() {
        String primeiro = service.obterOuGerarTokenCadastro();
        String segundo = service.obterOuGerarTokenCadastro();

        assertThat(primeiro).isNotBlank();
        assertThat(segundo).isEqualTo(primeiro);
    }

    @Test
    void regenerarTokenInvalidaOAnterior() {
        String antigo = service.obterOuGerarTokenCadastro();

        String novo = service.regenerarTokenCadastro();

        assertThat(novo).isNotEqualTo(antigo);
        assertThat(service.tokenCadastroValido(antigo)).isFalse();
        assertThat(service.tokenCadastroValido(novo)).isTrue();
    }

    @Test
    void tokenInvalidoOuNuloNuncaEValido() {
        service.obterOuGerarTokenCadastro();

        assertThat(service.tokenCadastroValido(null)).isFalse();
        assertThat(service.tokenCadastroValido("")).isFalse();
        assertThat(service.tokenCadastroValido("token-qualquer-errado")).isFalse();
    }
}
