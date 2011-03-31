package br.gov.ce.fortaleza.cti.sgf.job;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.service.ArenaService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;

public class JobArena implements Job {

	public static final Logger log = Logger.getLogger(JobArena.class);

	EntityManagerFactory factory = Persistence.createEntityManagerFactory("sgf");
	EntityManager entityManager = factory.createEntityManager();

	public static Integer CODVEICULO_ARENA = 4774;

	@Override
	@Transactional
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		List<Transmissao> transmissoes;
		EntityTransaction transaction = entityManager.getTransaction();

		try {
			log.info("Iniciando conexão Arena...");
			ArenaService arena = ArenaService.login();
			log.info("Conexão Arena: OK");
			Date fim = DateUtil.getDateNow();
			Date ini = DateUtil.adicionarOuDiminuir(fim, -2*DateUtil.MINUTE_IN_MILLIS);
			transaction.begin();
			transmissoes = arena.retrieveTransmissions(ini, fim, CODVEICULO_ARENA);

			for (Transmissao transmissao : transmissoes) {
				entityManager.persist(transmissao);
			}

			ini = DateUtil.adicionarOuDiminuir(fim, -1L*DateUtil.MINUTE_IN_MILLIS);
			fim = DateUtil.getDateNow();
			transaction.commit();
			log.info("Transmissões retornadas " + transmissoes.size());
			log.info("Execução finalizada...");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			entityManager.close();
		}

	}
}
