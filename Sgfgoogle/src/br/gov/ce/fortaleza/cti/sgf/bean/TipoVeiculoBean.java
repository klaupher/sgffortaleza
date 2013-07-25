/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.TipoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.TipoVeiculoService;

/**
 * @author deivid
 *
 */
@Scope("session")
@Component("tipoVeiculoBean")
public class TipoVeiculoBean extends EntityBean<Integer, TipoVeiculo>{

	@Autowired
	private TipoVeiculoService service;
	
	private List<TipoVeiculo> tipoVeiculos;
	
	@Override
	public String search() {
		
		return super.search();
	}

	@Override
	protected BaseService<Integer, TipoVeiculo> retrieveEntityService() {
		// TODO Auto-generated method stub
		return this.service;
	}

	@Override
	protected Integer retrieveEntityId(TipoVeiculo entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected TipoVeiculo createNewEntity() {
		// TODO Auto-generated method stub
		return new TipoVeiculo();
	}

	public List<TipoVeiculo> getTipoVeiculos() {
		return tipoVeiculos;
	}

	public void setTipoVeiculos(List<TipoVeiculo> tipoVeiculos) {
		this.tipoVeiculos = tipoVeiculos;
	}

	

}
