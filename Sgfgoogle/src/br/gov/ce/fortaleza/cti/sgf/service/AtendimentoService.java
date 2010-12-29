package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.mapping.Array;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Abastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;

/**
 * 
 * @author Deivid
 * @since 17/01/2010
 *
 */
@Transactional
@Repository
public class AtendimentoService extends BaseService<Integer, AtendimentoAbastecimento>{


	/**
	 * Consulta abastecimento por UG, Veículos, período(data inicial - data final)
	 * @param ug
	 * @param veiculo
	 * @param dataInicio
	 * @param dataFim
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<AtendimentoAbastecimento> findByPeriodo(String ug, String veiculo, Date dataInicio, Date dataFim){

		StringBuffer str = new StringBuffer("select o from AtendimentoAbastecimento o where o.data between ? and ?");
		if(ug != null){
			str.append(" and o.abastecimento.veiculo.ua.ug.id = :ug");
		}
		if(veiculo != null){
			str.append(" and o.abastecimento.veiculo.id = :veiculo");
		}
		str.append(" order by o.data asc");
		Query query = entityManager.createQuery(str.toString());
		query.setParameter(1, dataInicio);
		query.setParameter(2, dataFim);
		if(ug != null){
			query.setParameter("ug", ug);
		}
		if(veiculo != null){
			query.setParameter("veiculo", veiculo);
		}
		return query.getResultList();
	}
	

	@SuppressWarnings("unchecked")
	public Map<UG, List<AtendimentoAbastecimento>> findByPeriodoHashMap(String ug, String veiculo, Date dataInicio, Date dataFim){

		StringBuffer str = new StringBuffer("select o from AtendimentoAbastecimento o where o.data between ? and ?");
		if(ug != null){
			str.append(" and o.abastecimento.veiculo.ua.ug.id = :ug");
		}
		if(veiculo != null){
			str.append(" and o.abastecimento.veiculo.id = :veiculo");
		}
		str.append(" order by o.data asc");
		Query query = entityManager.createQuery(str.toString());
		query.setParameter(1, dataInicio);
		query.setParameter(2, dataFim);
		if(ug != null){
			query.setParameter("ug", ug);
		}
		if(veiculo != null){
			query.setParameter("veiculo", veiculo);
		}

		Map<UG, List<AtendimentoAbastecimento>> map = new HashMap<UG, List<AtendimentoAbastecimento>>();
		
		List<AtendimentoAbastecimento> result = query.getResultList();
		
		for (AtendimentoAbastecimento atend : result) {
			UG uga = atend.getAbastecimento().getVeiculo().getUa().getUg();
			if(map.get(uga) == null){
				List<AtendimentoAbastecimento> novo = new ArrayList<AtendimentoAbastecimento>();
				novo.add(atend);
				map.put(uga,novo);
			} else {
				map.get(uga).add(atend);
			}
		}
		return map;
	}

	/**
	 * retorna um hashmap dos atendimento por UG
	 * @param dataInicio
	 * @param dataFim
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<UG, List<AtendimentoAbastecimento>> findByPeriodoHashMap(Date dataInicio, Date dataFim){

		StringBuffer str = new StringBuffer("select o from AtendimentoAbastecimento o where o.data between ? and ?");
		str.append(" order by o.data asc");
		Query query = entityManager.createQuery(str.toString());
		query.setParameter(1, dataInicio);
		query.setParameter(2, dataFim);

		Map<UG, List<AtendimentoAbastecimento>> map = new HashMap<UG, List<AtendimentoAbastecimento>>();
		
		List<AtendimentoAbastecimento> result = query.getResultList();
		
		for (AtendimentoAbastecimento atend : result) {
			UG ug = atend.getAbastecimento().getVeiculo().getUa().getUg();
			if(map.get(ug) == null){
				List<AtendimentoAbastecimento> novo = new ArrayList<AtendimentoAbastecimento>();
				novo.add(atend);
				map.put(ug,novo);
			} else {
				map.get(ug).add(atend);
			}
		}
		return map;
	}

	/**
	 * Busca a quantidade abastecida no mês pelo veículo
	 * @param veiculo
	 * @return
	 */
	public Double findQtdAbastecida(Veiculo veiculo) {
		Double result = 0.0;
		Query query = entityManager.createNamedQuery("AtendimentoAbastecimento.findCota");
		query.setParameter("veiculo", veiculo);
		query.setParameter("mes", new Date().getMonth());
		result = (Double) query.getSingleResult();
		return result;
	}
}
