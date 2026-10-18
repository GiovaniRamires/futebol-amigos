package com.futebolamigos.repository;

import com.futebolamigos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findAllByCobrancaIdOrderByDataPagamentoAsc(Long cobrancaId);
}
