package com.futebolamigos.repository;

import com.futebolamigos.model.SelecaoSorteio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SelecaoSorteioRepository extends JpaRepository<SelecaoSorteio, Long> {

    @Query("SELECT s FROM SelecaoSorteio s JOIN FETCH s.usuario WHERE s.partida.id = :partidaId ORDER BY s.criadoEm ASC")
    List<SelecaoSorteio> findAllByPartidaIdOrderByCriadoEmAsc(@Param("partidaId") Long partidaId);

    Optional<SelecaoSorteio> findByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);

    void deleteByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);
}
