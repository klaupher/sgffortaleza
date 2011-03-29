package br.gov.ce.fortaleza.cti.sgf.util;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.service.ArenaService;
import br.gov.ce.fortaleza.cti.sgf.service.TransmissaoService;

public class Main {
	
	public static final Logger log = Logger.getLogger(Main.class);
	
	@Autowired
	private static TransmissaoService transmissaoService;
	
	@PersistenceContext(unitName="sgf")
	protected static EntityManager entityManager;
	
	public static void main(String[] args) throws Exception {
		
		Connection conn = null;
		
		while (true) {
			try {
				log.info("Iniciando conexão Mysql...");
				conn = Connetions.connectPostgres();
				log.info("Conexão Mysql: OK");
				log.info("Iniciando conexão Arena...");
				Integer id = 4774;
				ArenaService arena = ArenaService.login();
				log.info("Conexão Arena: OK");
				Date fim = DateUtil.getDateNow();
				Date ini = DateUtil.adicionarOuDiminuir(fim, -7*DateUtil.DAY_IN_MILLIS);
				while (true) {
					List<Transmissao> transmissoes = arena.retrieveTransmissions(ini, fim, id);
					for (Transmissao transmissao : transmissoes) {
						entityManager.persist(transmissao);
					}
					sleep(DateUtil.MINUTE_IN_MILLIS);
					
					ini = DateUtil.adicionarOuDiminuir(fim, -1L*DateUtil.MINUTE_IN_MILLIS);
					fim = DateUtil.getDateNow();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
				System.out.println(e.getMessage());
			} finally {
				conn.close();
			}
			log.info("Tentando se recuperar do erro...");
			log.info("Reiniciando conexões...");
		}
	}
	
	private static void sleep(long time) {

		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

}
