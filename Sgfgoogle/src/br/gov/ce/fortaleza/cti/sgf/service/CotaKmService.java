/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.CotaKm;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.StatusVeiculo;

/**
 * @author deivid
 *
 */
@Repository
@Transactional
public class CotaKmService extends BaseService<Integer, CotaKm>{

	@SuppressWarnings("unchecked")
	public List<CotaKm> pesquisar(CotaKm cota) {
		List<CotaKm> cotas = null;
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(CotaKm.class);
		if(cota.getVeiculo() != null){
			criteria.createCriteria("veiculo").add(Example.create(cota.getVeiculo()).enableLike(MatchMode.ANYWHERE).ignoreCase());
			if(cota.getVeiculo().getModelo() != null){
				criteria.createCriteria("veiculo.modelo").add(Example.create(cota.getVeiculo().getModelo()).enableLike(MatchMode.ANYWHERE).ignoreCase());
				if(cota.getVeiculo().getModelo().getMarca() != null){
					criteria.createCriteria("veiculo.modelo.marca").add(Example.create(cota.getVeiculo().getModelo().getMarca()).enableLike(MatchMode.ANYWHERE).ignoreCase());
				}
			}
		}
		cotas = criteria.list();
		List<CotaKm> remove = new ArrayList<CotaKm>();
		for (CotaKm c : cotas) {
			if(c.getVeiculo().getStatus().equals(StatusVeiculo.baixado)){
				remove.add(c);
			}
		}
		cotas.removeAll(remove);
		remove = new ArrayList<CotaKm>();
		return cotas;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends CotaKm> findcotasKmAllVeiculoativos() {
		// TODO Auto-generated method stub
		Query query = entityManager.createQuery("SELECT c FROM CotaKm c WHERE c.veiculo.status <> ?");
		query.setParameter(1, StatusVeiculo.baixado);
		List<CotaKm> result = new ArrayList<CotaKm>(query.getResultList());
		return result;
	}

	public Object findByPlacaVeiculo(String placa) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends Veiculo> findVeiculosTerceiros() {
		// TODO Auto-generated method stub
		Query query = entityManager.createQuery("select o from Veiculo o where o.propriedade <> 'PMF' and o not in(select c.veiculo from CotaKm c) and o.status != 6");
		
		List<Veiculo> veiculos = new ArrayList<Veiculo>(query.getResultList());
		return veiculos;
	}

}
