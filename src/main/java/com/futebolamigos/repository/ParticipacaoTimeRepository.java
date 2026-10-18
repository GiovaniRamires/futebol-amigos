package com.futebolamigos.repository;

import com.futebolamigos.model.ParticipacaoTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ParticipacaoTimeRepository extends JpaRepository<ParticipacaoTime, Long> {

    /**
     * Busca as participacoes de varios times de uma vez, com o usuario ja
     * carregado (JOIN FETCH). Usado por SorteioService para popular
     * Time.participacoes apos buscar o sorteio com os times, evitando o
     * MultipleBagFetchException de buscar duas colecoes (times e
     * participacoes) no mesmo JOIN FETCH, e evitando LazyInitializationException
     * na view ao exibir o nome do jogador.
     */
    @Query("SELECT p FROM ParticipacaoTime p JOIN FETCH p.usuario WHERE p.time.id IN :timeIds")
    List<ParticipacaoTime> findAllByTimeIdInComUsuario(@Param("timeIds") Collection<Long> timeIds);
}
