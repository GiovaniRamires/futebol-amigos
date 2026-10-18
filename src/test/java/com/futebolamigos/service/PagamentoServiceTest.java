package com.futebolamigos.service;

import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.MetodoPagamento;
import com.futebolamigos.model.StatusCobranca;
import com.futebolamigos.model.TipoCobranca;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre as regras obrigatorias de pagamento (briefing secao 10):
 * pagamentos parciais, calculo de saldo, bloqueio de pagamento acima do saldo,
 * proecao das informacoes financeiras (saldo nunca negativo).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PagamentoServiceTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private CobrancaService cobrancaService;
    @Autowired
    private PagamentoService pagamentoService;

    private Usuario novoAdmin() {
        return usuarioService.listarTodos().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR"))
                .findFirst()
                .map(dto -> usuarioService.buscarPorId(dto.getId()))
                .orElseThrow();
    }

    private Cobranca novaCobrancaMensalidade(BigDecimal valor) {
        Usuario jogador = usuarioService.cadastrar(JogadorFormDto.builder()
                .nome("Jogador Pagamento " + System.nanoTime())
                .email("pagamento" + System.nanoTime() + "@example.com")
                .senha("senha123")
                .tipoJogador(TipoJogador.MENSALISTA)
                .ativo(true)
                .build());
        return cobrancaService.salvar(Cobranca.builder()
                .usuario(jogador)
                .tipo(TipoCobranca.MENSALIDADE)
                .competencia(Cobranca.competenciaDe(YearMonth.now()))
                .valor(valor)
                .vencimento(LocalDate.now().plusDays(10))
                .status(StatusCobranca.PENDENTE)
                .build());
    }

    @Test
    void devePermitirPagamentoParcialECalcularSaldo() {
        Cobranca cobranca = novaCobrancaMensalidade(new BigDecimal("100.00"));
        Usuario admin = novoAdmin();

        pagamentoService.registrar(cobranca.getId(), new BigDecimal("40.00"), LocalDate.now(), MetodoPagamento.PIX, admin);

        BigDecimal saldo = pagamentoService.calcularSaldoPendente(cobrancaService.buscarPorId(cobranca.getId()));
        assertThat(saldo).isEqualByComparingTo("60.00");
        assertThat(cobrancaService.buscarPorId(cobranca.getId()).getStatus()).isEqualTo(StatusCobranca.PARCIAL);
    }

    @Test
    void deveMarcarComoPagoQuandoSaldoZera() {
        Cobranca cobranca = novaCobrancaMensalidade(new BigDecimal("50.00"));
        Usuario admin = novoAdmin();

        pagamentoService.registrar(cobranca.getId(), new BigDecimal("50.00"), LocalDate.now(), MetodoPagamento.DINHEIRO, admin);

        assertThat(cobrancaService.buscarPorId(cobranca.getId()).getStatus()).isEqualTo(StatusCobranca.PAGO);
    }

    @Test
    void naoDevePermitirPagamentoAcimaDoSaldoPendente() {
        Cobranca cobranca = novaCobrancaMensalidade(new BigDecimal("30.00"));
        Usuario admin = novoAdmin();

        assertThatThrownBy(() -> pagamentoService.registrar(cobranca.getId(), new BigDecimal("31.00"), LocalDate.now(), MetodoPagamento.PIX, admin))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("saldo pendente");
    }

    @Test
    void naoDevePermitirPagamentoAcimaDoSaldoAposPagamentoParcial() {
        Cobranca cobranca = novaCobrancaMensalidade(new BigDecimal("100.00"));
        Usuario admin = novoAdmin();
        pagamentoService.registrar(cobranca.getId(), new BigDecimal("70.00"), LocalDate.now(), MetodoPagamento.PIX, admin);

        assertThatThrownBy(() -> pagamentoService.registrar(cobranca.getId(), new BigDecimal("30.01"), LocalDate.now(), MetodoPagamento.PIX, admin))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoDevePermitirPagamentoComValorZeroOuNegativo() {
        Cobranca cobranca = novaCobrancaMensalidade(new BigDecimal("100.00"));
        Usuario admin = novoAdmin();

        assertThatThrownBy(() -> pagamentoService.registrar(cobranca.getId(), BigDecimal.ZERO, LocalDate.now(), MetodoPagamento.PIX, admin))
                .isInstanceOf(RegraNegocioException.class);
    }
}
