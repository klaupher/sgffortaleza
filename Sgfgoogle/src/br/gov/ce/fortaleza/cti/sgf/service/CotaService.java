/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoServico;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;

/**
 * @author Deivid
 *
 */
@Repository
@Transactional
public class CotaService extends BaseService<Integer, Cota>{

	public Cota findByVeiculoServico(Veiculo veiculo, TipoServico servico) {

		Cota cota = null;

		Query query = entityManager.createNamedQuery("Cota.findByVeiculoServico");

		query.setParameter(1, veiculo);

		query.setParameter(2, servico);

		cota = (Cota) query.getSingleResult();

		return cota;
	}


	public List<Cota> findCotas(Veiculo veiculo) {

		List<Cota> cotas = executeResultListQuery("findCotasByVeiculo", veiculo);

		return cotas;
	}

	@SuppressWarnings("unchecked")
	public Map<Veiculo, Cota> retrieveMapVeiculoCota(List<Integer> ids) {

		Map<Veiculo, Cota> map = new HashMap<Veiculo, Cota>();
		if (ids != null && ids.size() > 0) {
			String idsList = ids.toString().replaceAll("\\[", "(").replaceAll("\\]", ")");
			Query query = entityManager.createQuery("SELECT c FROM Cota c WHERE c.tipoServico.codTipoServico = 1 and c.veiculo.id IN " + idsList);
			List<Cota> cotas = query.getResultList();
			for (Cota c : cotas) {
				if(map.get(c.getVeiculo()) == null){
					map.put(c.getVeiculo(), c);
				}
			}
			return map;
		} else {
			return map;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Cota> retrieveCotasVeiculosByUG(String codug){

		Query query = entityManager.createQuery("SELECT c FROM Cota c WHERE c.veiculo.ua.ug.id = ? AND c.veiculo.status != -1");
		query.setParameter(1, codug);
		return query.getResultList();
	}


	public Cota retrieveVeiculoCota(Veiculo veiculo) {
		Cota cota = null;
		if (veiculo != null) {
			Query query = entityManager.createQuery("SELECT c FROM Cota c WHERE c.tipoServico.codTipoServico = 1 and c.veiculo.id = " + veiculo.getId());
			cota = (Cota) query.getSingleResult();
		}
		return cota;
	}


	@SuppressWarnings("unchecked")
	public List<Cota> pesquisar(Cota cota) {

		List<Cota> cotas = null;
		Session session = (Session) entityManager.getDelegate();
		Criteria criteria = session.createCriteria(Cota.class);
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
		List<Cota> remove = new ArrayList<Cota>();
		for (Cota c : cotas) {
			if(c.getVeiculo().getStatus().equals(-1)){
				remove.add(c);
			}
		}
		cotas.removeAll(remove);
		return cotas;
	}
	// retorna apenas as cotas de abastecimentos dos veículos que estão ativos
	public List<Cota> cotasVeiculosAtivos(){
		List<Cota> result = executeResultListQuery("findCotasVeiculosAtivos");
		return result;
	}


	public Cota findByPlacaVeiculo(String placa) throws NonUniqueObjectException {
		return executeSingleResultQuery("findByPlacaVeiculo", placa);
	}
}
