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
     * Carrega o sorteio com os times em uma unica consulta. As participacoes
     * de cada time (e o usuario de cada participacao) sao inicializadas
     * explicitamente pelo SorteioService, ainda dentro da mesma transacao -
     * nao e feito aqui num unico JOIN FETCH porque o Hibernate rejeita
     * (MultipleBagFetchException) buscar duas colecoes (times e
     * participacoes) ao mesmo tempo quando ambas sao List.
     */
    @Query("SELECT DISTINCT s FROM Sorteio s LEFT JOIN FETCH s.times WHERE s.id = :id")
    Optional<Sorteio> findByIdComTimes(@Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Sorteio s LEFT JOIN FETCH s.times WHERE s.partida.id = :partidaId AND s.publicado = true")
    Optional<Sorteio> findPublicadoComTimesByPartidaId(@Param("partidaId") Long partidaId);
}
