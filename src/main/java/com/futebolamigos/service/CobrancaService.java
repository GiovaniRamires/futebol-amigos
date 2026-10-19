package com.futebolamigos.service;

import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.ConfiguracaoSistema;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.StatusCobranca;
import com.futebolamigos.model.StatusComparecimento;
import com.futebolamigos.model.TipoCobranca;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.CobrancaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Regras de negocio de cobrancas (mensalidade e diaria).
 *
 * Mensalidade: gerada por competencia para jogadores mensalistas ativos,
 * sem duplicar cobranca para o mesmo jogador/competencia (ADR-004).
 *
 * Diaria: gerada SOMENTE quando o administrador registra COMPARECEU para
 * um jogador avulso em uma partida (ADR-005). Faltas nao geram diaria.
 * Mensalistas nunca recebem diaria.
 */
@Service
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final UsuarioService usuarioService;
    private final ConfiguracaoSistemaService configuracaoSistemaService;

    public CobrancaService(CobrancaRepository cobrancaRepository,
                            UsuarioService usuarioService,
                            ConfiguracaoSistemaService configuracaoSistemaService) {
        this.cobrancaRepository = cobrancaRepository;
        this.usuarioService = usuarioService;
        this.configuracaoSistemaService = configuracaoSistemaService;
    }

    /**
     * Gera a mensalidade da competencia informada para todos os jogadores
     * mensalistas ativos que ainda nao possuem cobranca para essa competencia.
     *
     * @return quantidade de cobrancas efetivamente criadas (ignora quem ja tinha).
     */
    @Transactional
    public int gerarMensalidadesDaCompetencia(YearMonth competencia) {
        ConfiguracaoSistema config = configuracaoSistemaService.buscar();
        String competenciaStr = Cobranca.competenciaDe(competencia);
        LocalDate vencimento = competencia.atDay(Math.min(config.getDiaVencimentoMensalidade(), competencia.lengthOfMonth()));

        List<Usuario> mensalistasAtivos = usuarioService.listarAtivos().stream()
                .filter(v -> v.getTipoJogador() == TipoJogador.MENSALISTA)
                .map(dto -> usuarioService.buscarPorId(dto.getId()))
                .toList();

        int criadas = 0;
        for (Usuario jogador : mensalistasAtivos) {
            if (cobrancaRepository.existsByUsuarioIdAndTipoAndCompetencia(jogador.getId(), TipoCobranca.MENSALIDADE, competenciaStr)) {
                continue;
            }
            Cobranca cobranca = Cobranca.builder()
                    .usuario(jogador)
                    .tipo(TipoCobranca.MENSALIDADE)
                    .competencia(competenciaStr)
                    .partida(null)
                    .valor(config.getValorMensalidade())
                    .vencimento(vencimento)
                    .status(StatusCobranca.PENDENTE)
                    .build();
            cobrancaRepository.save(cobranca);
            criadas++;
        }
        return criadas;
    }

    /**
     * Gera (se ainda nao existir) a diaria de um jogador avulso para uma
     * partida, mas SOMENTE quando o comparecimento registrado for COMPARECEU.
     * Chamado a partir do registro de comparecimento (ConfirmacaoPresencaService).
     */
    @Transactional
    public void gerarDiariaSeAplicavel(Partida partida, Usuario jogador, StatusComparecimento statusComparecimento) {
        if (jogador.getTipoJogador() != TipoJogador.AVULSO) {
            return; // mensalistas nunca recebem diaria
        }
        if (statusComparecimento != StatusComparecimento.COMPARECEU) {
            return; // faltas (ou atrasados que nao compareceram) nao geram diaria
        }
        String competencia = Cobranca.competenciaDe(YearMonth.from(partida.getDataHora()));
        boolean jaExiste = cobrancaRepository.findByUsuarioIdAndTipoAndPartidaId(jogador.getId(), TipoCobranca.DIARIA, partida.getId()).isPresent();
        if (jaExiste) {
            return;
        }
        Cobranca cobranca = Cobranca.builder()
                .usuario(jogador)
                .tipo(TipoCobranca.DIARIA)
                .competencia(competencia)
                .partida(partida)
                .valor(partida.getValorDiaria())
                .vencimento(partida.getDataHora().toLocalDate().plusDays(7))
                .status(StatusCobranca.PENDENTE)
                .build();
        cobrancaRepository.save(cobranca);
    }

    @Transactional(readOnly = true)
    public Cobranca buscarPorId(Long id) {
        return cobrancaRepository.findByIdComUsuario(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cobranca nao encontrada: id " + id));
    }

    @Transactional
    public Cobranca salvar(Cobranca cobranca) {
        return cobrancaRepository.save(cobranca);
    }

    @Transactional(readOnly = true)
    public List<Cobranca> listarPorUsuario(Long usuarioId) {
        return cobrancaRepository.findAllByUsuarioIdOrderByVencimentoDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Cobranca> listarTodas() {
        return cobrancaRepository.findAllByOrderByVencimentoDesc();
    }
}
