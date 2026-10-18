package com.futebolamigos.repository;

import com.futebolamigos.model.Sorteio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SorteioRepository extends JpaRepository<Sorteio, Long> {

    List<Sorteio> findAllByPartidaIdOrderByCriadoEmDesc(Long partidaId);

    Optional<Sorteio> findByPartidaIdAndPublicadoTrue(Long partidaId);

    /**
     * Carrega o sorteio com times e participantes (usuario) em uma unica
     * consulta, evitando LazyInitializationException na view ao navegar
     * sorteio -> times -> participacoes -> usuario fora da sessao Hibernate.
     */
    @Query("SELECT DISTINCT s FROM Sorteio s " +
            "LEFT JOIN FETCH s.times t " +
            "LEFT JOIN FETCH t.participacoes p " +
            "LEFT JOIN FETCH p.usuario " +
            "WHERE s.id = :id")
    Optional<Sorteio> findByIdComTimesEParticipantes(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Sorteio s " +
            "LEFT JOIN FETCH s.times t " +
            "LEFT JOIN FETCH t.participacoes p " +
            "LEFT JOIN FETCH p.usuario " +
            "WHERE s.partida.id = :partidaId AND s.publicado = true")
    Optional<Sorteio> findPublicadoComTimesEParticipantesByPartidaId(@Param("partidaId") Long partidaId);
}
