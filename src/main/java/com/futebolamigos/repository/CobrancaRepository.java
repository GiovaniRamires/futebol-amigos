package com.futebolamigos.repository;

import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.TipoCobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    boolean existsByUsuarioIdAndTipoAndCompetencia(Long usuarioId, TipoCobranca tipo, String competencia);

    Optional<Cobranca> findByUsuarioIdAndTipoAndPartidaId(Long usuarioId, TipoCobranca tipo, Long partidaId);

    List<Cobranca> findAllByUsuarioIdOrderByVencimentoDesc(Long usuarioId);

    List<Cobranca> findAllByOrderByVencimentoDesc();
}
