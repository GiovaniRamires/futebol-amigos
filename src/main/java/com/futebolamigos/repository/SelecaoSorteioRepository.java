package com.futebolamigos.repository;

import com.futebolamigos.model.SelecaoSorteio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SelecaoSorteioRepository extends JpaRepository<SelecaoSorteio, Long> {

    List<SelecaoSorteio> findAllByPartidaIdOrderByCriadoEmAsc(Long partidaId);

    Optional<SelecaoSorteio> findByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);

    void deleteByPartidaIdAndUsuarioId(Long partidaId, Long usuarioId);
}
