/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.RegistroVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;

/**
 * @author Deivid
 * @since 20/01/2010
 */
@Transactional
@Repository
public class RegistroVeiculoService extends BaseService<Integer, RegistroVeiculo>{
	
	public List<RegistroVeiculo> pesquisar(Veiculo veiculoPesquisa, Date dtSaida, Date dtRetorno) {
		// TODO Auto-generated method stub
		
		Query query = entityManager.createQuery("select r from RegistroVeiculo r where r.solicitacao.veiculo.id = ? and r.dtRetorno between ? and ? order by r.dtRetorno desc");
		query.setParameter(1, veiculoPesquisa.getId());
		query.setParameter(2, dtSaida);
		query.setParameter(3, dtRetorno);
		
		return query.getResultList();
	}

}
