/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;

/**
 * @author Deivid
 * @since 28/01/2010	
 */
@Scope("session")
@Component("atendimentoAbastecimentoBean")
public class AtendimentoAbastecimentoBean extends EntityBean<Integer, AtendimentoAbastecimento>{
	
	@Autowired
	private AtendimentoService service;

	@Override
	protected AtendimentoAbastecimento createNewEntity() {
		return new AtendimentoAbastecimento();
	}

	@Override
	protected Integer retrieveEntityId(AtendimentoAbastecimento entity) {
		return entity.getId();
	}


	@Override
	protected AtendimentoService retrieveEntityService() {
		return this.service;
	}
}
