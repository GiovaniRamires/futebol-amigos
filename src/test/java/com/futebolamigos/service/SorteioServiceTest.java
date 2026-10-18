package com.futebolamigos.service;

import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.dto.PartidaFormDto;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.Sorteio;
import com.futebolamigos.model.StatusConfirmacao;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre as regras obrigatorias de selecao e sorteio (briefing secao 7.3):
 * elegibilidade explicita, sem duplicados, times equilibrados, historico preservado.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SorteioServiceTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PartidaService partidaService;
    @Autowired
    private ConfirmacaoPresencaService confirmacaoPresencaService;
    @Autowired
    private SelecaoSorteioService selecaoSorteioService;
    @Autowired
    private SorteioService sorteioService;

    private Usuario novoJogador(String nome, String email) {
        return usuarioService.cadastrar(JogadorFormDto.builder()
                .nome(nome)
                .email(email)
                .senha("senha123")
                .tipoJogador(TipoJogador.MENSALISTA)
                .ativo(true)
                .build());
    }

    private Partida novaPartida() {
        return partidaService.criar(PartidaFormDto.builder()
                .dataHora(LocalDateTime.now().plusDays(1))
                .local("Quadra Teste")
                .limiteParticipantes(20)
                .valorDiaria(new BigDecimal("20.00"))
                .build());
    }

    @Test
    void confirmacaoDePresencaIsoladaNaoTornaJogadorElegivelParaSorteio() {
        Usuario jogador = novoJogador("Confirmado Sem Selecao", "confirmado.sem.selecao@example.com");
        Partida partida = novaPartida();

        confirmacaoPresencaService.confirmar(partida.getId(), jogador.getId());

        List<?> selecionados = selecaoSorteioService.listarPorPartida(partida.getId());
        assertThat(selecionados).isEmpty();
    }

    @Test
    void naoDeveIncluirSemConfirmacaoENaoJustificado() {
        Usuario jogador = novoJogador("Sem Confirmacao", "sem.confirmacao@example.com");
        Partida partida = novaPartida();

        assertThatThrownBy(() -> selecaoSorteioService.incluir(partida.getId(), jogador.getId(), null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("justificativa");
    }

    @Test
    void deveIncluirSemConfirmacaoComJustificativa() {
        Usuario jogador = novoJogador("Excecao Justificada", "excecao.justificada@example.com");
        Partida partida = novaPartida();

        var selecao = selecaoSorteioService.incluir(partida.getId(), jogador.getId(), "Chegou atrasado sem confirmar antes");

        assertThat(selecao.getJustificativaExcecao()).isNotBlank();
    }

    @Test
    void naoDeveSortearComMenosDeDoisSelecionados() {
        Usuario jogador = novoJogador("Unico Selecionado", "unico.selecionado@example.com");
        Partida partida = novaPartida();
        confirmacaoPresencaService.confirmar(partida.getId(), jogador.getId());
        selecaoSorteioService.incluir(partida.getId(), jogador.getId(), null);

        assertThatThrownBy(() -> sorteioService.sortear(partida.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveSortearDoisTimesEquilibradosSemDuplicados() {
        Partida partida = novaPartida();
        for (int i = 1; i <= 11; i++) {
            Usuario jogador = novoJogador("Jogador " + i, "jogador" + i + ".sorteio@example.com");
            confirmacaoPresencaService.confirmar(partida.getId(), jogador.getId());
            selecaoSorteioService.incluir(partida.getId(), jogador.getId(), null);
        }

        Sorteio sorteio = sorteioService.sortear(partida.getId());

        assertThat(sorteio.getTimes()).hasSize(2);
        int tamanhoA = sorteio.getTimes().get(0).getParticipacoes().size();
        int tamanhoB = sorteio.getTimes().get(1).getParticipacoes().size();
        assertThat(tamanhoA + tamanhoB).isEqualTo(11);
        assertThat(Math.abs(tamanhoA - tamanhoB)).isLessThanOrEqualTo(1);

        List<Long> idsTimeA = sorteio.getTimes().get(0).getParticipacoes().stream()
                .map(p -> p.getUsuario().getId()).toList();
        List<Long> idsTimeB = sorteio.getTimes().get(1).getParticipacoes().stream()
                .map(p -> p.getUsuario().getId()).toList();
        assertThat(idsTimeA).doesNotContainAnyElementsOf(idsTimeB);
    }

    @Test
    void devePreservarHistoricoDeSorteiosAoRefazer() {
        Partida partida = novaPartida();
        for (int i = 1; i <= 4; i++) {
            Usuario jogador = novoJogador("Historico " + i, "historico" + i + "@example.com");
            confirmacaoPresencaService.confirmar(partida.getId(), jogador.getId());
            selecaoSorteioService.incluir(partida.getId(), jogador.getId(), null);
        }

        sorteioService.sortear(partida.getId());
        sorteioService.refazer(partida.getId());

        List<Sorteio> historico = sorteioService.historicoPorPartida(partida.getId());
        assertThat(historico).hasSize(2);

        // Simula o acesso feito pela view (admin/partidas/sorteio.html):
        // sorteio.times -> time.participacoes -> p.usuario.nome, fora de
        // qualquer transacao aberta pelo controller. Sem o fetch correto em
        // historicoPorPartida, isso lanca LazyInitializationException.
        for (Sorteio sorteio : historico) {
            assertThat(sorteio.getTimes()).isNotEmpty();
            for (var time : sorteio.getTimes()) {
                assertThat(time.getParticipacoes()).isNotEmpty();
                for (var participacao : time.getParticipacoes()) {
                    assertThat(participacao.getUsuario().getNome()).isNotBlank();
                }
            }
        }
    }

    @Test
    void naoDeveRefazerSorteioJaPublicado() {
        Partida partida = novaPartida();
        for (int i = 1; i <= 4; i++) {
            Usuario jogador = novoJogador("Publicado " + i, "publicado" + i + "@example.com");
            confirmacaoPresencaService.confirmar(partida.getId(), jogador.getId());
            selecaoSorteioService.incluir(partida.getId(), jogador.getId(), null);
        }

        Sorteio sorteio = sorteioService.sortear(partida.getId());
        sorteioService.publicar(sorteio.getId());

        assertThatThrownBy(() -> sorteioService.refazer(partida.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("publicado");
    }
}
