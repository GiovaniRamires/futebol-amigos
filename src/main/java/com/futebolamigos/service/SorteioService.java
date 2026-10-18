package com.futebolamigos.service;

import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Partida;
import com.futebolamigos.model.ParticipacaoTime;
import com.futebolamigos.model.SelecaoSorteio;
import com.futebolamigos.model.Sorteio;
import com.futebolamigos.model.Time;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.ParticipacaoTimeRepository;
import com.futebolamigos.repository.SorteioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Sorteio aleatorio dos times, a partir EXCLUSIVAMENTE da selecao explicita
 * do administrador ({@link SelecaoSorteioService}). Preserva o historico de
 * sorteios por partida e permite refazer antes da publicacao.
 */
@Service
public class SorteioService {

    private static final String NOME_TIME_A = "Time A";
    private static final String NOME_TIME_B = "Time B";

    private final SorteioRepository sorteioRepository;
    private final ParticipacaoTimeRepository participacaoTimeRepository;
    private final SelecaoSorteioService selecaoSorteioService;
    private final PartidaService partidaService;
    private final Random random;

    @Autowired
    public SorteioService(SorteioRepository sorteioRepository,
                           ParticipacaoTimeRepository participacaoTimeRepository,
                           SelecaoSorteioService selecaoSorteioService,
                           PartidaService partidaService) {
        this(sorteioRepository, participacaoTimeRepository, selecaoSorteioService, partidaService, new Random());
    }

    /** Construtor usado em testes para injetar um Random com seed deterministico. */
    SorteioService(SorteioRepository sorteioRepository,
                    ParticipacaoTimeRepository participacaoTimeRepository,
                    SelecaoSorteioService selecaoSorteioService,
                    PartidaService partidaService,
                    Random random) {
        this.sorteioRepository = sorteioRepository;
        this.participacaoTimeRepository = participacaoTimeRepository;
        this.selecaoSorteioService = selecaoSorteioService;
        this.partidaService = partidaService;
        this.random = random;
    }

    /**
     * Realiza um novo sorteio para a partida, a partir da selecao explicita
     * vigente. Nao apaga sorteios anteriores (historico preservado) - apenas
     * marca o novo como o mais recente nao publicado.
     */
    @Transactional
    public Sorteio sortear(Long partidaId) {
        Partida partida = partidaService.buscarPorId(partidaId);

        List<SelecaoSorteio> selecionados = selecaoSorteioService.listarPorPartida(partidaId);
        if (selecionados.size() < 2) {
            throw new RegraNegocioException("Selecione ao menos 2 jogadores para realizar o sorteio.");
        }

        List<Usuario> usuarios = new ArrayList<>();
        for (SelecaoSorteio selecao : selecionados) {
            if (usuarios.contains(selecao.getUsuario())) {
                throw new RegraNegocioException("Jogador duplicado na selecao: " + selecao.getUsuario().getNome());
            }
            usuarios.add(selecao.getUsuario());
        }

        Collections.shuffle(usuarios, random);

        int tamanhoTimeA = (usuarios.size() + 1) / 2; // diferenca maxima de 1 jogador
        List<Usuario> timeAJogadores = usuarios.subList(0, tamanhoTimeA);
        List<Usuario> timeBJogadores = usuarios.subList(tamanhoTimeA, usuarios.size());

        Sorteio sorteio = Sorteio.builder()
                .partida(partida)
                .publicado(false)
                .build();

        Time timeA = Time.builder().sorteio(sorteio).nome(NOME_TIME_A).build();
        timeA.setParticipacoes(criarParticipacoes(timeA, timeAJogadores));

        Time timeB = Time.builder().sorteio(sorteio).nome(NOME_TIME_B).build();
        timeB.setParticipacoes(criarParticipacoes(timeB, timeBJogadores));

        sorteio.setTimes(new ArrayList<>(List.of(timeA, timeB)));

        return sorteioRepository.save(sorteio);
    }

    private List<ParticipacaoTime> criarParticipacoes(Time time, List<Usuario> jogadores) {
        List<ParticipacaoTime> participacoes = new ArrayList<>();
        for (Usuario jogador : jogadores) {
            participacoes.add(ParticipacaoTime.builder().time(time).usuario(jogador).build());
        }
        return participacoes;
    }

    /** Refaz o sorteio: so permitido se o ultimo sorteio da partida ainda nao foi publicado. */
    @Transactional
    public Sorteio refazer(Long partidaId) {
        Optional<Sorteio> ultimo = sorteioRepository.findAllByPartidaIdOrderByCriadoEmDesc(partidaId)
                .stream().findFirst();
        if (ultimo.isPresent() && ultimo.get().isPublicado()) {
            throw new RegraNegocioException("O sorteio desta partida ja foi publicado e nao pode ser refeito. Publique um novo sorteio primeiro.");
        }
        return sortear(partidaId);
    }

    @Transactional
    public Sorteio publicar(Long sorteioId) {
        Sorteio sorteio = sorteioRepository.findById(sorteioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sorteio nao encontrado: id " + sorteioId));
        // despublica qualquer outro sorteio publicado anteriormente para a mesma partida
        sorteioRepository.findByPartidaIdAndPublicadoTrue(sorteio.getPartida().getId())
                .filter(outro -> !outro.getId().equals(sorteio.getId()))
                .ifPresent(outro -> {
                    outro.setPublicado(false);
                    sorteioRepository.save(outro);
                });
        sorteio.setPublicado(true);
        return sorteioRepository.save(sorteio);
    }

    /**
     * Busca o sorteio com times e participantes (usuario) completamente
     * carregados, para exibicao na view fora da sessao Hibernate. Carrega
     * times com JOIN FETCH e, em seguida, as participacoes de todos os
     * times (com usuario) em uma segunda consulta - uma unica query com
     * JOIN FETCH de duas colecoes (times e participacoes) e rejeitada pelo
     * Hibernate (MultipleBagFetchException).
     */
    @Transactional(readOnly = true)
    public Sorteio buscarPorId(Long id) {
        Sorteio sorteio = sorteioRepository.findByIdComTimes(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sorteio nao encontrado: id " + id));
        popularParticipacoes(sorteio);
        return sorteio;
    }

    @Transactional(readOnly = true)
    public List<Sorteio> historicoPorPartida(Long partidaId) {
        return sorteioRepository.findAllByPartidaIdOrderByCriadoEmDesc(partidaId);
    }

    @Transactional(readOnly = true)
    public Optional<Sorteio> sorteioPublicado(Long partidaId) {
        Optional<Sorteio> sorteio = sorteioRepository.findPublicadoComTimesByPartidaId(partidaId);
        sorteio.ifPresent(this::popularParticipacoes);
        return sorteio;
    }

    /** Preenche Time.participacoes (com usuario ja carregado) para cada time do sorteio, numa unica consulta. */
    private void popularParticipacoes(Sorteio sorteio) {
        List<Long> timeIds = sorteio.getTimes().stream().map(Time::getId).toList();
        if (timeIds.isEmpty()) {
            return;
        }
        Map<Long, List<ParticipacaoTime>> porTimeId = participacaoTimeRepository.findAllByTimeIdInComUsuario(timeIds)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getTime().getId()));
        for (Time time : sorteio.getTimes()) {
            time.setParticipacoes(porTimeId.getOrDefault(time.getId(), new ArrayList<>()));
        }
    }
}
