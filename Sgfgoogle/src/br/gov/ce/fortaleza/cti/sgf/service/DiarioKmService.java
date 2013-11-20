/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.CotaKm;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKm;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
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
	
	public List<DiarioKm> findVeiculos(){
		List<DiarioKm> diarioKms = new ArrayList<DiarioKm>();
		
		return diarioKms;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Veiculo> findVeiculosComCotaKmAtivos() {
		// TODO Auto-generated method stub
		
		Query query = entityManager.createQuery("select o from Veiculo o "
				+ "where o.propriedade <> 'PMF' and o in(select c.veiculo from CotaKm c) "
				+ "and o.cotaKm not in(select d.cotaKm from DiarioKm d) "
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

	public boolean findDiariaVeiculoNoDia(Veiculo veiculo) {
		// TODO Auto-generated method stub
		DateTime dataHoje = new DateTime();
		
		Query query = entityManager.createQuery("select d from DiarioKm d where d.cotaKm.veiculo.id = ? "
				+ "and day(d.dataCadastro) = ? "
				+ "and month(d.dataCadastro) = ? "
				+ "and year(d.dataCadastro) = ?");
		
		query.setParameter(1, veiculo.getId());
		query.setParameter(2, dataHoje.getDayOfMonth());
		query.setParameter(3, dataHoje.getMonthOfYear());
		query.setParameter(4, dataHoje.getYear());
		return query.getResultList().size() > 0 ? true : false; 
	}

	public DiarioKm findUltimaDiaria(Veiculo veiculo) {
		// TODO Auto-generated method stub
		try{
			Query query = entityManager.createQuery("from DiarioKm d where d.dataCadastro = (select max(d2.dataCadastro) from DiarioKm d2 where d2.cotaKm.veiculo.id = ?)");
			query.setParameter(1, veiculo.getId());
			return (DiarioKm) query.getSingleResult();
		}catch(NoResultException noResult){
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<Veiculo> pesquisar(Veiculo veiculoPesquisa) {
		// TODO Auto-generated method stub
		
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Veiculo.class, "veiculo")
				.add(Example.create(veiculoPesquisa).enableLike(MatchMode.ANYWHERE).ignoreCase());
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Veiculo.class, "veiculo2");
		detachedCriteria.createAlias("veiculo2.cotaKm", "cotaKm");
		detachedCriteria.add(Restrictions.eqProperty("veiculo.id", "veiculo2.id"))
			.setProjection(Projections.id());
		
		if(veiculoPesquisa.getUa() != null){
			criteria.createCriteria("ua.ug").add(Example.create(veiculoPesquisa.getUa().getUg()));
		}
		criteria.add(Subqueries.exists(detachedCriteria));
		return criteria.list();
	}
	
}
