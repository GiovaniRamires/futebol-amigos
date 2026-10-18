package com.futebolamigos.repository;

import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.TipoCobranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    boolean existsByUsuarioIdAndTipoAndCompetencia(Long usuarioId, TipoCobranca tipo, String competencia);

    Optional<Cobranca> findByUsuarioIdAndTipoAndPartidaId(Long usuarioId, TipoCobranca tipo, Long partidaId);

    @Query("SELECT c FROM Cobranca c JOIN FETCH c.usuario WHERE c.usuario.id = :usuarioId ORDER BY c.vencimento DESC")
    List<Cobranca> findAllByUsuarioIdOrderByVencimentoDesc(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Cobranca c JOIN FETCH c.usuario ORDER BY c.vencimento DESC")
    List<Cobranca> findAllByOrderByVencimentoDesc();
}
