/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Falta;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;

/**
 * @author Arthur
 * @since 10/06/2014
 */
@Repository
@Transactional
public class FaltaService extends BaseService<Integer, Falta> {
	/**
	 * Retorna o total faltas pelo ve�culo no m�s.
	 * 
	 * @param veiculo
	 *            veiculo a ser pesquisado o total abastecido
	 * @return o total j� abastecido
	 */
	public Double pesquisarTotalFaltaMes(Veiculo veiculo) {

		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));

		Query query = entityManager.createNamedQuery("Falta.findCota");
		query.setParameter("id", veiculo.getId());
		query.setParameter("inicio", cal.getTime());

		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		query.setParameter("fim", cal.getTime());

		Object o = query.getSingleResult();

		if (o == null) {
			o = Double.valueOf(0);
		}

		return (Double) o;
	}

	@SuppressWarnings("unchecked")
	public List<Falta> pesquisarFaltaVeiculoPorPlaca(Date dataIni, Date dataFim, UG ug, String placa) {

		List<Falta> result = new ArrayList<Falta>();
		String queryString = "SELECT a FROM Falta a WHERE a.dataFalta <= :currentDate and a.dataFalta BETWEEN :dataIni AND :dataFim ";
		StringBuffer queryBuffer = new StringBuffer(queryString);

		if(ug != null){
			queryBuffer.append(" AND a.veiculo.ua.ug = :ug");
		}
		if(placa != null){
			queryBuffer.append(" AND a.veiculo.placa like :placa");
		}

		Query query = entityManager.createQuery(queryBuffer.toString());
		query.setParameter("currentDate", DateUtil.getDateStartDay(new Date()));
		query.setParameter("dataIni", dataIni);
		query.setParameter("dataFim", dataFim);
		
		queryBuffer.append(" order by a.dataFalta desc");

		if(ug != null){
			query.setParameter("ug", ug);
		}

		if(placa != null){
			query.setParameter("placa", placa);
		}
		queryBuffer.append(" order by a.dataFalta desc");
		result = query.getResultList();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Falta> pesquisarFaltaPorPeriodo(Date dtInicial, Date dtFinal, UG ug) {

		List<Falta> faltas = null;
		String queryString = "select a FROM Falta a where a.dataFalta between :dataInicial and :dataFinal";
		StringBuffer queryBuffer = new StringBuffer(queryString);
		
		if(ug != null){
			queryBuffer.append(" and a.veiculo.ua.ug.id = :orgao");
		}
		
		queryBuffer.append(" order by a.dataFalta desc");
		
		Query query = entityManager.createQuery(queryBuffer.toString());
		query.setParameter("dataInicial", dtInicial);
		query.setParameter("dataFinal", dtFinal);
		
		if(ug != null){
			query.setParameter("orgao", ug.getId());
		}
		
		faltas = query.getResultList();
		return faltas;
	}

	@SuppressWarnings("unchecked")
	public List<Falta> pesquisarFalta(Date dtInicial, Date dtFinal) {

		List<Falta> faltas = null;
		Query query = entityManager.createQuery("select a from Falta a where a.dataFalta between :dataInicial and :dataFinal " +
				" order by a.dataFalta desc");
		//query.setParameter("currentDate", DateUtil.getDateStartDay(new Date()));
		query.setParameter("dataInicial", dtInicial);
		query.setParameter("dataFinal", dtFinal);
		faltas = query.getResultList();
		return faltas;
	}
}
