package br.gov.ce.fortaleza.cti.sgf.job;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.service.ArenaService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;

public class JobArena implements Job {

	private static final Logger log = Logger.getLogger(JobArena.class);

	public static Integer VEICULO_ID_ARENA = 4481;
	public static Integer VEICULO_ID_SGF = 246;

	@Override
	@Transactional
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		EntityManagerFactory factory = Persistence.createEntityManagerFactory("sgf");
		EntityManager entityManager = factory.createEntityManager();
		List<Transmissao> transmissoes;
		EntityTransaction transaction;
		transaction = entityManager.getTransaction();

		try {
			
			transaction.begin();
			
			log.info("Iniciando conexão Arena...");
			ArenaService arena = ArenaService.login();
			log.info("Conexão Arena: OK");
			Date fim = DateUtil.getDateNow();
			Date ini; //DateUtil.adicionarOuDiminuir(fim, -2*DateUtil.MINUTE_IN_MILLIS);
			Query query = entityManager.createQuery("SELECT max(t.dataTransmissao) FROM Transmissao t WHERE t.veiculoId = ?");
			query.setParameter(1, VEICULO_ID_SGF);
			Date dataUltimaTransmissao =  (Date) query.getSingleResult();
			
			if(dataUltimaTransmissao != null){
				ini = DateUtil.adicionarOuDiminuir(dataUltimaTransmissao, DateUtil.SECOND_IN_MILLIS);
			} else {
				ini = DateUtil.adicionarOuDiminuir(fim, -DateUtil.DAY_IN_MILLIS);
			}
			
			transmissoes = arena.retrieveTransmissions(ini, fim, VEICULO_ID_ARENA, VEICULO_ID_SGF);

			for (Transmissao transmissao : transmissoes) {
				entityManager.persist(transmissao);
			}

			transaction.commit();
			log.info("Transmissões retornadas " + transmissoes.size());
			log.info("Execução finalizada...");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			transaction.rollback();
		} finally {
			entityManager.close();
		}

	}
}
