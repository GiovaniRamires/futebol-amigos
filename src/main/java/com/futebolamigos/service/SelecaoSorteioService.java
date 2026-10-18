package com.futebolamigos.service;

import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.ConfirmacaoPresenca;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.SelecaoSorteio;
import com.futebolamigos.model.StatusConfirmacao;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.ConfirmacaoPresencaRepository;
import com.futebolamigos.repository.SelecaoSorteioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Selecao EXPLICITA de jogadores para o sorteio de uma partida.
 *
 * Esta e a UNICA fonte de elegibilidade para o sorteio (ver docs/DECISIONS.md
 * ADR-003). A confirmacao de presenca, isoladamente, nunca e suficiente:
 * o administrador deve selecionar explicitamente cada participante aqui,
 * podendo incluir atrasados e, com justificativa obrigatoria, jogadores
 * sem confirmacao previa.
 */
@Service
public class SelecaoSorteioService {

    private final SelecaoSorteioRepository selecaoRepository;
    private final ConfirmacaoPresencaRepository confirmacaoRepository;
    private final PartidaService partidaService;
    private final UsuarioService usuarioService;

    public SelecaoSorteioService(SelecaoSorteioRepository selecaoRepository,
                                  ConfirmacaoPresencaRepository confirmacaoRepository,
                                  PartidaService partidaService,
                                  UsuarioService usuarioService) {
        this.selecaoRepository = selecaoRepository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.partidaService = partidaService;
        this.usuarioService = usuarioService;
    }

    /**
     * Inclui um jogador na selecao do sorteio.
     *
     * @param justificativaExcecao obrigatoria apenas quando o jogador nao tem
     *                              confirmacao de presenca (CONFIRMADO) registrada
     *                              para esta partida; pode ser nula/vazia caso
     *                              contrario.
     */
    @Transactional
    public SelecaoSorteio incluir(Long partidaId, Long usuarioId, String justificativaExcecao) {
        Partida partida = partidaService.buscarPorId(partidaId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        if (selecaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId).isPresent()) {
            throw new RegraNegocioException("Este jogador ja esta selecionado para o sorteio desta partida.");
        }

        boolean confirmadoOuAtrasado = confirmacaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId)
                .map(c -> c.getStatus() == StatusConfirmacao.CONFIRMADO)
                .orElse(false);

        if (!confirmadoOuAtrasado && (justificativaExcecao == null || justificativaExcecao.isBlank())) {
            throw new RegraNegocioException(
                    "Jogador sem confirmacao de presenca: informe uma justificativa para incluir excepcionalmente no sorteio.");
        }

        SelecaoSorteio selecao = SelecaoSorteio.builder()
                .partida(partida)
                .usuario(usuario)
                .justificativaExcecao(confirmadoOuAtrasado ? null : justificativaExcecao)
                .build();
        return selecaoRepository.save(selecao);
    }

    @Transactional
    public void remover(Long partidaId, Long usuarioId) {
        Optional<SelecaoSorteio> selecao = selecaoRepository.findByPartidaIdAndUsuarioId(partidaId, usuarioId);
        if (selecao.isEmpty()) {
            throw new RecursoNaoEncontradoException("Selecao nao encontrada para este jogador nesta partida.");
        }
        selecaoRepository.deleteByPartidaIdAndUsuarioId(partidaId, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<SelecaoSorteio> listarPorPartida(Long partidaId) {
        return selecaoRepository.findAllByPartidaIdOrderByCriadoEmAsc(partidaId);
    }

    /** Status de presenca de todos os jogadores ativos, para a tela de selecao do admin. */
    @Transactional(readOnly = true)
    public List<ConfirmacaoPresenca> statusPresencaParaSelecao(Long partidaId) {
        return confirmacaoRepository.findAllByPartidaIdOrderByCriadoEmAsc(partidaId);
    }
}
