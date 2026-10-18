package com.futebolamigos.service;

import com.futebolamigos.dto.PartidaFormDto;
import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Partida;
import com.futebolamigos.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio de partidas.
 */
@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;

    public PartidaService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Transactional
    public Partida criar(PartidaFormDto form) {
        Partida partida = Partida.builder()
                .dataHora(form.getDataHora())
                .local(form.getLocal())
                .limiteParticipantes(form.getLimiteParticipantes())
                .valorDiaria(form.getValorDiaria())
                .cancelada(false)
                .build();
        return partidaRepository.save(partida);
    }

    @Transactional
    public Partida editar(Long id, PartidaFormDto form) {
        Partida partida = buscarPorId(id);
        if (partida.isCancelada()) {
            throw new RegraNegocioException("Nao e possivel editar uma partida cancelada.");
        }
        partida.setDataHora(form.getDataHora());
        partida.setLocal(form.getLocal());
        partida.setLimiteParticipantes(form.getLimiteParticipantes());
        partida.setValorDiaria(form.getValorDiaria());
        return partidaRepository.save(partida);
    }

    @Transactional
    public void cancelar(Long id) {
        Partida partida = buscarPorId(id);
        partida.setCancelada(true);
        partidaRepository.save(partida);
    }

    @Transactional(readOnly = true)
    public Partida buscarPorId(Long id) {
        return partidaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Partida nao encontrada: id " + id));
    }

    @Transactional(readOnly = true)
    public List<Partida> listarTodas() {
        return partidaRepository.findAllByOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Partida> proximaPartida() {
        return partidaRepository.findFirstByCanceladaFalseAndDataHoraAfterOrderByDataHoraAsc(LocalDateTime.now());
    }
}
