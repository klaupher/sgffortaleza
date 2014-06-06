/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.List;

import org.ajax4jsf.component.html.HtmlAjaxCommandLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.gov.ce.fortaleza.cti.sgf.entity.CotaKm;
import br.gov.ce.fortaleza.cti.sgf.entity.Marca;
import br.gov.ce.fortaleza.cti.sgf.entity.Modelo;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaKmService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoServicoVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.TipoServicoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusVeiculo;

/**
 * @author deivid
 *
 */
@Scope("session")
@Component("cotaKmBean")
public class CotaKmBean extends EntityBean<Integer, CotaKm>{
	
	@Autowired
	private CotaKmService cotaKmService;
	
	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private PostoServicoVeiculoService postoServicoVeiculoService;

	@Autowired
	private TipoServicoService servicoService;
	
	private HtmlAjaxCommandLink botaoExcluir;

	private String placa;
	private Integer cota;
	private UG ugPesquisa;
	private String veiculoPesquisa;
	private String placaPesquisa;
	private String marcaPesquisa;
	private Double cotaAtual;
	
	private List<Veiculo> veiculos = new ArrayList<Veiculo>();
	private Veiculo veiculo = new Veiculo();
			
	public String searchPaginaInicial() {
		this.entities = new ArrayList<CotaKm>();
		this.entities.addAll(cotaKmService.findcotasPaginaInicial());
		
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	
	@Override
	public String search() {
		if(StringUtils.hasText(veiculoPesquisa) || StringUtils.hasText(placaPesquisa) || StringUtils.hasText(marcaPesquisa)){
			return pesquisar();
		}
		this.entities = new ArrayList<CotaKm>();
		//this.entities.addAll(cotaKmService.findcotasKmAllVeiculoativos());
		this.entities.addAll(cotaKmService.findcotasKmAllVeiculoativos2(ugPesquisa));
		
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	
	
	public String veiculoSemCotaPorPlaca(){
		if(this.placa != null && this.placa != ""){
			if(cotaKmService.findByPlacaVeiculo(this.placa) == null){
				this.veiculos = veiculoService.findByPlaca(this.placa);
			}
		} else {
			this.veiculos = new ArrayList<Veiculo>();
		}
		return SUCCESS;
	}
	
	public String prepareUpdate(){
		this.cotaAtual = this.entity.getCotaKm();
		return super.prepareUpdate();
	}
	
	public String atualizarCotaVeiculo(){
		if(this.entity.getCotaKm() < this.cotaAtual - (this.entity.getCotaKmDisponivel())){
			JSFUtil.getInstance().addErrorMessage("msg.error.cota.utrapassada");
			return FAIL;
		} else {
			this.entity.setCotaKmDisponivel(this.entity.getCotaKmDisponivel() - (this.cotaAtual - this.entity.getCotaKm()));
		}
		return super.update();
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public String delete(){
		if(this.entity.getVeiculo().getStatus().equals(StatusVeiculo.locado)){
			this.botaoExcluir.setOncomplete("alert('N�o � permitido excluir cotas de ve�culo ativo!')");
			return FAIL;
		} else if(this.entity.getVeiculo().getStatus().equals(StatusVeiculo.disponivel)){
			return super.delete();
		}
		return FAIL;
	}
	
	@Override
	public String prepareSave() {
		veiculos.clear();
		veiculos.addAll(cotaKmService.findVeiculosTerceiros());
		return super.prepareSave();
	}
	
	@Override
	public String save() {
		// TODO Auto-generated method stub
		this.entity.setCotaKmDisponivel(this.entity.getCotaKm());
		return super.save();
	}
	
	
	public String pesquisar(){
		CotaKm cotaKm = new CotaKm();
		
		if(ugPesquisa != null){
			if(cotaKm.getVeiculo() != null){
				cotaKm.setVeiculo(new Veiculo());
			}else{
				cotaKm.getVeiculo();
			}
			//MODIFICACOES 04.06.2014 - PAULO ANDRE
			cotaKm.setVeiculo(new Veiculo());
			//FIM
			cotaKm.getVeiculo().setUa(new UA());
			cotaKm.getVeiculo().getUa().setUg(ugPesquisa);
		}
		if(StringUtils.hasText(this.veiculoPesquisa)){
			cotaKm.setVeiculo(new Veiculo());
			cotaKm.getVeiculo().setModelo(new Modelo());
			cotaKm.getVeiculo().getModelo().setDescricao(this.veiculoPesquisa);
		}
		if(StringUtils.hasText(this.placaPesquisa)){
			if(cotaKm.getVeiculo() != null){
				cotaKm.getVeiculo().setPlaca(this.placaPesquisa);
			} else { 
				cotaKm.setVeiculo(new Veiculo());
				cotaKm.getVeiculo().setPlaca(this.placaPesquisa);
			}
		}

		if(StringUtils.hasText(this.marcaPesquisa)){
			if(cotaKm.getVeiculo() != null){
				if(cotaKm.getVeiculo().getModelo() != null){
					cotaKm.getVeiculo().getModelo().setMarca(new Marca());
					cotaKm.getVeiculo().getModelo().getMarca().setDescricao(this.marcaPesquisa);
				}
			} else {
				cotaKm.setVeiculo(new Veiculo());
				cotaKm.getVeiculo().setModelo(new Modelo());
				cotaKm.getVeiculo().getModelo().setMarca(new Marca());
				cotaKm.getVeiculo().getModelo().getMarca().setDescricao(this.marcaPesquisa);
			}
		}
		this.entities = new ArrayList<CotaKm>();
		this.entities = cotaKmService.pesquisar(cotaKm);
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	
	@Override
	protected BaseService<Integer, CotaKm> retrieveEntityService() {
		// TODO Auto-generated method stub
		return this.cotaKmService;
	}

	@Override
	protected Integer retrieveEntityId(CotaKm entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected CotaKm createNewEntity() {
		// TODO Auto-generated method stub
		CotaKm cotaKm = new CotaKm();
		return cotaKm;
	}

	public String getVeiculoPesquisa() {
		return veiculoPesquisa;
	}

	public void setVeiculoPesquisa(String veiculoPesquisa) {
		this.veiculoPesquisa = veiculoPesquisa;
	}

	public String getPlacaPesquisa() {
		return placaPesquisa;
	}

	public void setPlacaPesquisa(String placaPesquisa) {
		this.placaPesquisa = placaPesquisa;
	}

	public String getMarcaPesquisa() {
		return marcaPesquisa;
	}

	public void setMarcaPesquisa(String marcaPesquisa) {
		this.marcaPesquisa = marcaPesquisa;
	}

	public Double getCotaAtual() {
		return cotaAtual;
	}

	public void setCotaAtual(Double cotaAtual) {
		this.cotaAtual = cotaAtual;
	}

	public CotaKmService getCotaKmService() {
		return cotaKmService;
	}

	public void setCotaKmService(CotaKmService cotaKmService) {
		this.cotaKmService = cotaKmService;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public Integer getCota() {
		return cota;
	}

	public void setCota(Integer cota) {
		this.cota = cota;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public HtmlAjaxCommandLink getBotaoExcluir() {
		return botaoExcluir;
	}

	public void setBotaoExcluir(HtmlAjaxCommandLink botaoExcluir) {
		this.botaoExcluir = botaoExcluir;
	}


	public UG getUgPesquisa() {
		return ugPesquisa;
	}


	public void setUgPesquisa(UG ugPesquisa) {
		this.ugPesquisa = ugPesquisa;
	}

}
