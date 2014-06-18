package br.gov.ce.fortaleza.cti.sgf.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.VeiculoSubstituto;
import br.gov.ce.fortaleza.cti.sgf.entity.VeiculoSubstitutoPk;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoSubstitutoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusVeiculo;

@Scope("session")
@Component("veiculoSubstitutoBean")
public class VeiculoSubstitutoBean extends EntityBean<Integer, VeiculoSubstituto>{
	
	@Autowired
	private VeiculoSubstitutoService service;
	
	@Autowired
	private VeiculoService veiculoService;
	
	private User usuario = SgfUtil.usuarioLogado();
	private Veiculo veiculo;
	private Veiculo veiculoSelecionado;
	private String placaSubstituto;
	private Date dataSaida;
	private Date dataRetorno;
	private List<Veiculo> veiculos;
	private UG orgaoSelecionado;
	private Boolean retornar;
	private Integer mes;
	private Integer ano;
	
	@Override
	protected BaseService<Integer, VeiculoSubstituto> retrieveEntityService() {
		return this.service;
	}
	
	@Override
	protected Integer retrieveEntityId(VeiculoSubstituto entity) {
		return entity.getId();
	}
	
	@Override
	protected VeiculoSubstituto createNewEntity() {

		VeiculoSubstituto veiculoSubstituto = new VeiculoSubstituto();
		veiculoSubstituto.setPlaca(null);
		veiculoSubstituto.setVeiculo(new Veiculo());
		veiculoSubstituto.setDataInicio(null);
		veiculoSubstituto.setDataFim(null);
		
		this.atualizaVeiculos();
		return veiculoSubstituto;
	}
	
	public String atualizaVeiculos(){

			if(SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)){
				if(this.orgaoSelecionado != null){
					this.veiculos = new ArrayList<Veiculo>(service.findVeiculosLocados(this.orgaoSelecionado));
				} else {
					this.veiculos = new ArrayList<Veiculo>(service.findVeiculosLocados(this.usuario.getPessoa().getUa().getUg()));
				}
			}else{
				this.veiculos = new ArrayList<Veiculo>(service.findVeiculosLocados(this.usuario.getPessoa().getUa().getUg()));
			}
		return SUCCESS;
	}
	
	public String save(){
		
		this.entity.setVeiculo(this.veiculo);
		this.entity.setPlaca(this.placaSubstituto);
		this.entity.setDataInicio(new Date());
		
		if( veiculoService.verificaSeExistePlaca( this.entity.getPlaca() ) || service.verificaPlacaSubstituto( this.entity.getPlaca() ) ){
			JSFUtil.getInstance().addErrorMessage("msg.error.placa.nomeexistente");
			return FAIL;
		}else{
			return super.save();
		}
	}
	
	/**
	 * Verifica o estado atual do veículo substituto e chama o método prepareUpdate ou prepareSave
	 * @return
	 */
	public String prepareState(){
		String retorno = null;

		if(this.entity.getDataInicio() != null && this.entity.getDataFim() == null){
			retorno = this.prepareUpdate();
		} else if(this.entity.getDataInicio() == null && this.entity.getDataFim() == null){
			retorno = this.prepareSave();
		} else if(this.entity.getDataInicio() != null && this.entity.getDataFim() != null){
			JSFUtil.getInstance().addErrorMessage("Carro substituto devolvido a locadora!");
		}
		return retorno;
	}
	
	public String pesquisar(){
		
		Date dtInicial;
		Date dtFinal;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.set(Calendar.YEAR, this.ano);
		calendar.set(Calendar.MONTH, this.mes);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		dtInicial = DateUtil.getDateStartDay(calendar.getTime());

		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);

		dtFinal = DateUtil.getDateEndDay(calendar.getTime());
		
		entities.clear();
		
		entities = this.orgaoSelecionado != null ?  service.pesquisa(this.orgaoSelecionado, dtInicial, dtFinal) : service.pesquisa(null, dtInicial, dtFinal);
		
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	

	
	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public UG getOrgaoSelecionado() {
		return orgaoSelecionado;
	}

	public void setOrgaoSelecionado(UG orgaoSelecionado) {
		this.orgaoSelecionado = orgaoSelecionado;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public String getPlacaSubstituto() {
		return placaSubstituto;
	}

	public void setPlacaSubstituto(String placaSubstituto) {
		this.placaSubstituto = placaSubstituto;
	}

	public Boolean getRetornar() {
		return retornar;
	}

	public void setRetornar(Boolean retornar) {
		this.retornar = retornar;
	}

	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}

	public void setVeiculoSelecionado(Veiculo veiculoSelecionado) {
		this.veiculoSelecionado = veiculoSelecionado;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

}
