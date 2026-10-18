package com.futebolamigos.service;

import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.MetodoPagamento;
import com.futebolamigos.model.Pagamento;
import com.futebolamigos.model.StatusCobranca;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negocio de pagamentos: parciais, calculo de saldo, bloqueio de
 * pagamento acima do saldo pendente, historico preservado.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final CobrancaService cobrancaService;

    public PagamentoService(PagamentoRepository pagamentoRepository, CobrancaService cobrancaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.cobrancaService = cobrancaService;
    }

    @Transactional
    public Pagamento registrar(Long cobrancaId, BigDecimal valor, LocalDate dataPagamento, MetodoPagamento metodo, Usuario registradoPor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor do pagamento deve ser maior que zero.");
        }
        Cobranca cobranca = cobrancaService.buscarPorId(cobrancaId);

        BigDecimal saldoPendente = calcularSaldoPendente(cobranca);
        if (valor.compareTo(saldoPendente) > 0) {
            throw new RegraNegocioException(
                    "O valor do pagamento (R$ " + valor + ") excede o saldo pendente (R$ " + saldoPendente + ") desta cobranca.");
        }

        Pagamento pagamento = Pagamento.builder()
                .cobranca(cobranca)
                .valor(valor)
                .dataPagamento(dataPagamento)
                .metodo(metodo)
                .registradoPor(registradoPor)
                .build();
        pagamentoRepository.save(pagamento);

        atualizarStatusCobranca(cobranca);

        return pagamento;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPago(Long cobrancaId) {
        return pagamentoRepository.findAllByCobrancaIdOrderByDataPagamentoAsc(cobrancaId).stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPendente(Cobranca cobranca) {
        BigDecimal totalPago = calcularTotalPago(cobranca.getId());
        return cobranca.getValor().subtract(totalPago);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarPorCobranca(Long cobrancaId) {
        return pagamentoRepository.findAllByCobrancaIdOrderByDataPagamentoAsc(cobrancaId);
    }

    private void atualizarStatusCobranca(Cobranca cobranca) {
        BigDecimal saldoPendente = calcularSaldoPendente(cobranca);
        if (saldoPendente.compareTo(BigDecimal.ZERO) <= 0) {
            cobranca.setStatus(StatusCobranca.PAGO);
        } else if (saldoPendente.compareTo(cobranca.getValor()) < 0) {
            cobranca.setStatus(StatusCobranca.PARCIAL);
        } else {
            cobranca.setStatus(StatusCobranca.PENDENTE);
        }
        cobrancaService.salvar(cobranca);
    }
}
