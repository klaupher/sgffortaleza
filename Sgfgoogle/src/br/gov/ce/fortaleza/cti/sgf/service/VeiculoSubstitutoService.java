package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.VeiculoSubstituto;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

@Repository
@Transactional
public class VeiculoSubstitutoService extends BaseService<Integer, VeiculoSubstituto> {
	
	private static final Log logger = LogFactory.getLog(VeiculoSubstitutoService.class);
	
	public List<VeiculoSubstituto> findAll(){
		List<VeiculoSubstituto> result = new ArrayList<VeiculoSubstituto>();
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)  || SgfUtil.isCoordenador(user)){
			result = retrieveAll();
		} else {
			UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();
			if(ua != null){
				result = retrieveByUG(ua.getUg().getId());
			}
		}
		Collections.sort(result, new Comparator<VeiculoSubstituto>() {
			public int compare(VeiculoSubstituto o1, VeiculoSubstituto o2) {
				return o1.getPlaca().compareTo(o2.getPlaca());
			}
		});
		return result;
	}
	
	public List<VeiculoSubstituto> retrieveByUG(String ug){
		Query query = entityManager.createQuery("Select s from VeiculoSubstituto s where s.veiculo.ua.ug.id = ? and s.veiculo.status != 6");
		query.setParameter(1, ug);
		List<VeiculoSubstituto> result =  query.getResultList();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosLocados(UG ug){
		
		StringBuilder sqlv = new StringBuilder("select o from Veiculo o where o.propriedade = 'Locado' and o.status != 6 \n");
		StringBuilder sqls = new StringBuilder("select distinct s.veiculo.id from VeiculoSubstituto s where s.dataFim is null");
		if(ug != null){
			sqlv.append("and o.ua.ug.id = '"+ug.getId()+"' \n");
		}
		
		Query querys = entityManager.createQuery(sqls.toString());
				
		if(querys.getResultList().size() > 0){
			sqlv.append("and o.id not in (:ids)");
		}
		
		Query queryv = entityManager.createQuery(sqlv.toString());
		
		if(querys.getResultList().size() > 0){
			queryv.setParameter("ids", querys.getResultList());
		}
				
		return queryv.getResultList();
	}
	
	public Boolean verificaPlacaSubstituto(String placa){
		String consulta = "select v from VeiculoSubstituto v where upper(trim(v.placa)) like :placa AND dataFim is null";
		StringBuffer hql = new StringBuffer(consulta);
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("placa",  placa);
		return query.getResultList().size() > 0;
	}
	
	public List<VeiculoSubstituto> pesquisa(UG ug, Date dtInicial, Date dtFinal){
		
		StringBuilder sql = new StringBuilder("select s from VeiculoSubstituto s \n");
		
		sql.append("where 1 = 1 \n");
		
		
		if(ug != null){
			sql.append("and s.veiculo.ua.ug.id = '"+ug.getId()+"' \n");
		}
		
		if(dtInicial != null) {
			sql.append("and s.dataInicio between :dtInicial and :dtFinal \n");
		}
		
		Query query = entityManager.createQuery(sql.toString());
		
		if(dtInicial != null) {
			query.setParameter("dtInicial", dtInicial);
			query.setParameter("dtFinal", dtFinal);
		}
		return query.getResultList();
		
	}


}
