package com.futebolamigos.service;

import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.ConfirmacaoPresenca;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.StatusComparecimento;
import com.futebolamigos.model.StatusConfirmacao;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.ConfirmacaoPresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Regras de negocio de confirmacao de presenca e registro de comparecimento.
 *
 * IMPORTANTE: confirmar presenca aqui NUNCA torna o jogador elegivel para o
 * sorteio por si so. A elegibilidade e sempre uma decisao explicita do
 * administrador, registrada separadamente em {@link SelecaoSorteioService}.
 */
@Service
public class ConfirmacaoPresencaService {

    private final ConfirmacaoPresencaRepository confirmacaoRepository;
    private final PartidaService partidaService;
    private final UsuarioService usuarioService;

    public ConfirmacaoPresencaService(ConfirmacaoPresencaRepository confirmacaoRepository,
                                       PartidaService partidaService,
                                       UsuarioService usuarioService) {
        this.confirmacaoRepository = confirmacaoRepository;
        this.partidaService = partidaService;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public ConfirmacaoPresenca confirmar(Long partidaId, Long usuarioId) {
        Partida partida = partidaService.buscarPorId(partidaId);
        if (partida.isCancelada()) {
            throw new RegraNegocioException("Nao e possivel confirmar presenca em uma partida cancelada.");
        }
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        ConfirmacaoPresenca confirmacao = confirmacaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId)
                .orElseGet(() -> ConfirmacaoPresenca.builder()
                        .partida(partida)
                        .usuario(usuario)
                        .build());

        long confirmados = confirmacaoRepository.countByPartidaIdAndStatus(partidaId, StatusConfirmacao.CONFIRMADO);
        boolean jaEstavaConfirmado = confirmacao.getId() != null && confirmacao.getStatus() == StatusConfirmacao.CONFIRMADO;

        if (!jaEstavaConfirmado && confirmados >= partida.getLimiteParticipantes()) {
            confirmacao.setStatus(StatusConfirmacao.LISTA_ESPERA);
        } else {
            confirmacao.setStatus(StatusConfirmacao.CONFIRMADO);
        }
        confirmacao.setAtualizadoEm(Instant.now());
        return confirmacaoRepository.save(confirmacao);
    }

    @Transactional
    public void cancelar(Long partidaId, Long usuarioId) {
        ConfirmacaoPresenca confirmacao = confirmacaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Confirmacao nao encontrada."));
        confirmacao.setStatus(StatusConfirmacao.CANCELADO);
        confirmacao.setAtualizadoEm(Instant.now());
        confirmacaoRepository.save(confirmacao);
        promoverProximoDaListaDeEspera(partidaId);
    }

    /** Promove o jogador mais antigo da lista de espera quando uma vaga se abre. */
    private void promoverProximoDaListaDeEspera(Long partidaId) {
        Partida partida = partidaService.buscarPorId(partidaId);
        long confirmados = confirmacaoRepository.countByPartidaIdAndStatus(partidaId, StatusConfirmacao.CONFIRMADO);
        if (confirmados >= partida.getLimiteParticipantes()) {
            return;
        }
        confirmacaoRepository.findAllByPartidaIdAndStatusOrderByCriadoEmAsc(partidaId, StatusConfirmacao.LISTA_ESPERA)
                .stream()
                .findFirst()
                .ifPresent(primeiroDaFila -> {
                    primeiroDaFila.setStatus(StatusConfirmacao.CONFIRMADO);
                    primeiroDaFila.setAtualizadoEm(Instant.now());
                    confirmacaoRepository.save(primeiroDaFila);
                });
    }

    @Transactional
    public ConfirmacaoPresenca registrarComparecimento(Long partidaId, Long usuarioId, StatusComparecimento status) {
        Partida partida = partidaService.buscarPorId(partidaId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        ConfirmacaoPresenca confirmacao = confirmacaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId)
                .orElseGet(() -> ConfirmacaoPresenca.builder()
                        .partida(partida)
                        .usuario(usuario)
                        .status(StatusConfirmacao.CANCELADO) // nao havia confirmado; registro e so de comparecimento
                        .build());

        confirmacao.setStatusComparecimento(status);
        confirmacao.setAtualizadoEm(Instant.now());
        return confirmacaoRepository.save(confirmacao);
    }

    @Transactional(readOnly = true)
    public List<ConfirmacaoPresenca> listarPorPartida(Long partidaId) {
        return confirmacaoRepository.findAllByPartidaIdOrderByCriadoEmAsc(partidaId);
    }
}
