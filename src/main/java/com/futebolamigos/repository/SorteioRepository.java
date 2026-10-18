package com.futebolamigos.repository;

import com.futebolamigos.model.Sorteio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SorteioRepository extends JpaRepository<Sorteio, Long> {

    List<Sorteio> findAllByPartidaIdOrderByCriadoEmDesc(Long partidaId);

    Optional<Sorteio> findByPartidaIdAndPublicadoTrue(Long partidaId);
}
