package com.futebolamigos.repository;

import com.futebolamigos.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findAllByOrderByDataHoraDesc();

    Optional<Partida> findFirstByCanceladaFalseAndDataHoraAfterOrderByDataHoraAsc(LocalDateTime referencia);

    List<Partida> findAllByCanceladaFalseAndDataHoraAfterOrderByDataHoraAsc(LocalDateTime referencia);
}
