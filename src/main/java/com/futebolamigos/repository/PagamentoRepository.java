package com.futebolamigos.repository;

import com.futebolamigos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    @Query("SELECT p FROM Pagamento p JOIN FETCH p.registradoPor WHERE p.cobranca.id = :cobrancaId ORDER BY p.dataPagamento ASC")
    List<Pagamento> findAllByCobrancaIdOrderByDataPagamentoAsc(@Param("cobrancaId") Long cobrancaId);
}
