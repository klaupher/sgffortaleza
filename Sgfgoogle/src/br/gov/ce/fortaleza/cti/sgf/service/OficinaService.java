package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Oficina;

@Repository
@Transactional
public class OficinaService  extends BaseService<Integer, Oficina>{

	@SuppressWarnings("unchecked")
	public List<Oficina> findOficinasAtivas() {
		// TODO Auto-generated method stub
		Query query = entityManager.createQuery("SELECT o FROM Oficina o WHERE  o.dtBloqueio is null");
		return query.getResultList(); 
	}

}
