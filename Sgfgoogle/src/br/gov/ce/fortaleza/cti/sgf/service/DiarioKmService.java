/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKm;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;

/**
 * @author deivid
 *
 */
@Repository
@Transactional
public class DiarioKmService extends BaseService<Integer, DiarioKm>{

	@SuppressWarnings("unchecked")
	public List<DiarioKm> findVeiculosTerceirosComCota() {
		// TODO Auto-generated method stub
		List<DiarioKm> lista = new ArrayList();
		Query query = entityManager.createQuery("from DiarioKm as dk where "
				+ "exists(from CotaKm as cotaKm where cotaKm.veiculo = dk.cotaKm.veiculo "
				+ "and dk.cotaKm.veiculo.propriedade <> 'PMF')");
		lista = query.getResultList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<? extends Veiculo> findVeiculosComCotaKmAtivos() {
		// TODO Auto-generated method stub
		Query query = entityManager.createQuery("select o from Veiculo o "
				+ "where o.propriedade <> 'PMF' and o in(select c.veiculo from CotaKm c) "
				+ "and o.cotaKm not in(select d.cotaKm from Diario d) "
				+ "and o.status != 6");
		
		List<Veiculo> veiculos = new ArrayList<Veiculo>(query.getResultList());
		return veiculos;
	}

	@SuppressWarnings("unchecked")
	public List<DiarioKm> findVeiculosComDiarioKmAtivos() {
		// TODO Auto-generated method stub
		Query query = entityManager.createQuery("select d from DiarioKm d "
				+ "where d.dataCadastro = (select max(dkm.dataCadastro) from DiarioKm dkm)");
		
		return query.getResultList();
	}
	
}
