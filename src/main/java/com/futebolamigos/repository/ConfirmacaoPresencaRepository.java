package com.futebolamigos.repository;

import com.futebolamigos.model.ConfirmacaoPresenca;
import com.futebolamigos.model.StatusConfirmacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmacaoPresencaRepository extends JpaRepository<ConfirmacaoPresenca, Long> {

    Optional<ConfirmacaoPresenca> findByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);

    List<ConfirmacaoPresenca> findAllByPartidaIdOrderByCriadoEmAsc(Long partidaId);

    long countByPartidaIdAndStatus(Long partidaId, StatusConfirmacao status);

    List<ConfirmacaoPresenca> findAllByPartidaIdAndStatusOrderByCriadoEmAsc(Long partidaId, StatusConfirmacao status);
}
