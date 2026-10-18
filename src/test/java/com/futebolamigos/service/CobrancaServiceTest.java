package com.futebolamigos.service;

import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.dto.PartidaFormDto;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.StatusComparecimento;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobre as regras obrigatorias de geracao de cobrancas (briefing secao 7.4):
 * mensalidade sem duplicidade, diaria somente apos comparecimento de avulso,
 * mensalistas nunca recebem diaria, faltas nao geram diaria.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CobrancaServiceTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PartidaService partidaService;
    @Autowired
    private ConfirmacaoPresencaService confirmacaoPresencaService;
    @Autowired
    private CobrancaService cobrancaService;

    private Usuario novoJogador(String nome, String email, TipoJogador tipo) {
        return usuarioService.cadastrar(JogadorFormDto.builder()
                .nome(nome)
                .email(email)
                .senha("senha123")
                .tipoJogador(tipo)
                .ativo(true)
                .build());
    }

    private Partida novaPartida() {
        return partidaService.criar(PartidaFormDto.builder()
                .dataHora(LocalDateTime.now().plusDays(1))
                .local("Quadra Financeiro")
                .limiteParticipantes(20)
                .valorDiaria(new BigDecimal("25.00"))
                .build());
    }

    @Test
    void deveGerarMensalidadeApenasUmaVezPorCompetencia() {
        novoJogador("Mensalista Um", "mensalista.um@example.com", TipoJogador.MENSALISTA);
        YearMonth competencia = YearMonth.now();

        int primeiraGeracao = cobrancaService.gerarMensalidadesDaCompetencia(competencia);
        int segundaGeracao = cobrancaService.gerarMensalidadesDaCompetencia(competencia);

        assertThat(primeiraGeracao).isGreaterThanOrEqualTo(1);
        assertThat(segundaGeracao).isZero(); // nao duplica
    }

    @Test
    void naoDeveGerarMensalidadeParaAvulso() {
        Usuario avulso = novoJogador("Avulso Mensalidade", "avulso.mensalidade@example.com", TipoJogador.AVULSO);
        YearMonth competencia = YearMonth.now();

        cobrancaService.gerarMensalidadesDaCompetencia(competencia);

        boolean avulsoTemMensalidade = cobrancaService.listarPorUsuario(avulso.getId()).stream()
                .anyMatch(c -> c.getTipo().name().equals("MENSALIDADE"));
        assertThat(avulsoTemMensalidade).isFalse();
    }

    @Test
    void deveGerarDiariaApenasQuandoAvulsoComparece() {
        Usuario avulso = novoJogador("Avulso Compareceu", "avulso.compareceu@example.com", TipoJogador.AVULSO);
        Partida partida = novaPartida();

        confirmacaoPresencaService.registrarComparecimento(partida.getId(), avulso.getId(), StatusComparecimento.COMPARECEU);

        var cobrancas = cobrancaService.listarPorUsuario(avulso.getId());
        assertThat(cobrancas).hasSize(1);
        assertThat(cobrancas.get(0).getTipo().name()).isEqualTo("DIARIA");
    }

    @Test
    void naoDeveGerarDiariaParaFalta() {
        Usuario avulso = novoJogador("Avulso Faltou", "avulso.faltou@example.com", TipoJogador.AVULSO);
        Partida partida = novaPartida();

        confirmacaoPresencaService.registrarComparecimento(partida.getId(), avulso.getId(), StatusComparecimento.FALTOU);

        var cobrancas = cobrancaService.listarPorUsuario(avulso.getId());
        assertThat(cobrancas).isEmpty();
    }

    @Test
    void naoDeveGerarDiariaParaMensalista() {
        Usuario mensalista = novoJogador("Mensalista Compareceu", "mensalista.compareceu@example.com", TipoJogador.MENSALISTA);
        Partida partida = novaPartida();

        confirmacaoPresencaService.registrarComparecimento(partida.getId(), mensalista.getId(), StatusComparecimento.COMPARECEU);

        var cobrancas = cobrancaService.listarPorUsuario(mensalista.getId());
        assertThat(cobrancas).isEmpty();
    }

    @Test
    void naoDeveDuplicarDiariaAoRegistrarComparecimentoDuasVezes() {
        Usuario avulso = novoJogador("Avulso Duplicado", "avulso.duplicado@example.com", TipoJogador.AVULSO);
        Partida partida = novaPartida();

        confirmacaoPresencaService.registrarComparecimento(partida.getId(), avulso.getId(), StatusComparecimento.COMPARECEU);
        confirmacaoPresencaService.registrarComparecimento(partida.getId(), avulso.getId(), StatusComparecimento.COMPARECEU);

        var cobrancas = cobrancaService.listarPorUsuario(avulso.getId());
        assertThat(cobrancas).hasSize(1);
    }
}
