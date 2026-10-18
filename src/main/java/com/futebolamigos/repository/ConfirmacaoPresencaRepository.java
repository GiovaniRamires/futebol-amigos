package com.futebolamigos.repository;

import com.futebolamigos.model.ConfirmacaoPresenca;
import com.futebolamigos.model.StatusConfirmacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConfirmacaoPresencaRepository extends JpaRepository<ConfirmacaoPresenca, Long> {

    Optional<ConfirmacaoPresenca> findByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);

    @Query("SELECT c FROM ConfirmacaoPresenca c JOIN FETCH c.usuario WHERE c.partida.id = :partidaId ORDER BY c.criadoEm ASC")
    List<ConfirmacaoPresenca> findAllByPartidaIdOrderByCriadoEmAsc(@Param("partidaId") Long partidaId);

    long countByPartidaIdAndStatus(Long partidaId, StatusConfirmacao status);

    @Query("SELECT c FROM ConfirmacaoPresenca c JOIN FETCH c.usuario WHERE c.partida.id = :partidaId AND c.status = :status ORDER BY c.criadoEm ASC")
    List<ConfirmacaoPresenca> findAllByPartidaIdAndStatusOrderByCriadoEmAsc(@Param("partidaId") Long partidaId, @Param("status") StatusConfirmacao status);
}
