/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.faces.component.html.HtmlInputText;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.CotaKm;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKm;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKmHistoricoUltrapassado;
import br.gov.ce.fortaleza.cti.sgf.entity.RegistroVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaKmService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioKmHistoricoService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioKmService;
import br.gov.ce.fortaleza.cti.sgf.service.RegistroVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

/**
 * @author deivid
 *
 */
@Scope("session")
@Component("diarioKmBean")
public class DiarioKmBean extends EntityBean<Integer, RegistroVeiculo>{
	
	@Autowired
	private RegistroVeiculoService service;
	
	@Autowired
	private CotaKmService cotaKmService;
	
	@Autowired
	private DiarioKmHistoricoService historicoService;
	
	@Autowired
	private VeiculoService veiculoService;
	
	private Collection<Veiculo> veiculos;
	
	private List<RegistroVeiculo> viagens;
	
	private Veiculo veiculoPesquisa;
	
	private Veiculo veiculoSelecionado;

	private Double valorInicial;

	private Double valorFinal;

	private Double kmDia;

	private Double cotaKmDisponivel;
	
	private User usuario = SgfUtil.usuarioLogado();

	private boolean isSalvarHistorico;
	
	private UG ugPesquisa;
	private String placaPesquisa;
	private String chassiPesquisa;
	private String renavamPesquisa;
	private List<CotaKm> diarioKms;
	private Integer mes;
	private Integer ano;
	private Date dtInicial;
	private Date dtFinal;
	private RegistroVeiculo viagemSelecionada;
	
	//Componentes JSF
	private HtmlInputText inputValorInicial;
	
	public void pesquisar(){
				
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.YEAR, this.ano);
		calendar.set(Calendar.MONTH, this.mes);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		this.setDtInicial(DateUtil.getDateStartDay(calendar.getTime()));

		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);

		this.setDtFinal(DateUtil.getDateEndDay(calendar.getTime()));
		
		//veiculoPesquisa = new Veiculo();
		//veiculoPesquisa.setPlaca(placaPesquisa);

		
		viagens = service.pesquisar(this.veiculoPesquisa, this.dtInicial, this.dtFinal);
		//populaVeiculoUltimaDiaria(veiculos);
		
	}
	
	public String carregarVeiculos(){
		
		this.veiculos = veiculoService.findVeiculosComCotakm(this.ugPesquisa);
		
		return SUCCESS;
	}
	
	@Override
	public String search() {

		if(this.veiculoPesquisa != null){
		
			pesquisar();
		}

		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	
	
	@Override
	public String prepareUpdate() {
		
		//Veiculo v = this.entity.getSolicitacao().getVeiculo();
		//v.setKmAtual(this.entity.getKmRetorno());
		//veiculoService.update(v);
		
		// TODO Auto-generated method stub
		return super.prepareUpdate();
	}
	
	@Override
	public String update() {
		
		Veiculo v = this.entity.getSolicitacao().getVeiculo();
		v.setKmAtual(this.entity.getKmRetorno());
		veiculoService.update(v);
		
		// TODO Auto-generated method stub
		return super.update();
	}
	
	@Override
	public String prepareSave() {
		// TODO Auto-generated method stub
		return super.prepareSave();
	}


	@Override
	@Transactional
	public String save() {
		// TODO Auto-generated method stub
		String retorno = FAIL;
		createNewEntity();
		for (Veiculo veiculo : veiculos) {
			if(veiculo.getValorInicial() != null && 
					veiculo.getValorFinal() != null){
				valorInicial = veiculo.getValorInicial().doubleValue();
				valorFinal = veiculo.getValorFinal().doubleValue();
				kmDia = valorFinal - valorInicial;
				cotaKmDisponivel = veiculo.getCotaKm().getCotaKmDisponivel();

					try{
						entity = service.save(entity);
						retorno = search();
											
					}catch(Exception e){
						JSFUtil.getInstance().addErrorMessage("diarioKm.falha.salvar.diario");
					}

			}
		}
		return retorno;
	}
	
	
	public String delete() {
		
		Veiculo v = this.entity.getSolicitacao().getVeiculo();
		v.setKmAtual(this.entity.getKmSaida());
		veiculoService.update(v);

		return super.delete();
	}
	

	private String recuperarAnoMes() {
		// TODO Auto-generated method stub
		String mes = new DateTime().monthOfYear().getAsString();
		String ano = new DateTime().year().getAsString();
		
		return mes.concat("/"+ano);
	}


	@Override
	protected RegistroVeiculoService retrieveEntityService() {
		// TODO Auto-generated method stub
		return service;
	}

	@Override
	protected Integer retrieveEntityId(RegistroVeiculo entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected RegistroVeiculo createNewEntity() {
		// TODO Auto-generated method stub
		entity = new RegistroVeiculo();
		
		return entity;
	}

	public Collection<? extends Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(Collection<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public Veiculo getVeiculoPesquisa() {
		return veiculoPesquisa;
	}

	public void setVeiculoPesquisa(Veiculo veiculoPesquisa) {
		this.veiculoPesquisa = veiculoPesquisa;
	}


	public Double getValorInicial() {
		return valorInicial;
	}


	public void setValorInicial(Double valorInicial) {
		this.valorInicial = valorInicial;
	}


	public Double getValorFinal() {
		return valorFinal;
	}


	public void setValorFinal(Double valorFinal) {
		this.valorFinal = valorFinal;
	}


	public Double getKmDia() {
		return kmDia;
	}


	public void setKmDia(Double kmDia) {
		this.kmDia = kmDia;
	}


	public Double getCotaKmDisponivel() {
		return cotaKmDisponivel;
	}


	public void setCotaKmDisponivel(Double cotaKmDisponivel) {
		this.cotaKmDisponivel = cotaKmDisponivel;
	}


	public boolean isSalvarHistorico() {
		return isSalvarHistorico;
	}


	public void setSalvarHistorico(boolean isSalvarHistorico) {
		this.isSalvarHistorico = isSalvarHistorico;
	}


	public String getPlacaPesquisa() {
		return placaPesquisa;
	}


	public void setPlacaPesquisa(String placaPesquisa) {
		this.placaPesquisa = placaPesquisa;
	}


	public String getChassiPesquisa() {
		return chassiPesquisa;
	}


	public void setChassiPesquisa(String chassiPesquisa) {
		this.chassiPesquisa = chassiPesquisa;
	}


	public String getRenavamPesquisa() {
		return renavamPesquisa;
	}


	public void setRenavamPesquisa(String renavamPesquisa) {
		this.renavamPesquisa = renavamPesquisa;
	}


	public UG getUgPesquisa() {
		return ugPesquisa;
	}


	public void setUgPesquisa(UG ugPesquisa) {
		this.ugPesquisa = ugPesquisa;
	}


	public List<CotaKm> getDiarioKms() {
		return diarioKms;
	}


	public void setDiarioKms(List<CotaKm> diarioKms) {
		this.diarioKms = diarioKms;
	}


	public HtmlInputText getInputValorInicial() {
		return inputValorInicial;
	}


	public void setInputValorInicial(HtmlInputText inputValorInicial) {
		this.inputValorInicial = inputValorInicial;
	}


	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}


	public void setVeiculoSelecionado(Veiculo veiculoSelecionado) {
		this.veiculoSelecionado = veiculoSelecionado;
	}

	public List<RegistroVeiculo> getViagens() {
		return viagens;
	}

	public void setViagens(List<RegistroVeiculo> viagens) {
		this.viagens = viagens;
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

	public Date getDtInicial() {
		return dtInicial;
	}

	public void setDtInicial(Date dtInicial) {
		this.dtInicial = dtInicial;
	}

	public Date getDtFinal() {
		return dtFinal;
	}

	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}

	public RegistroVeiculo getViagemSelecionada() {
		return viagemSelecionada;
	}

	public void setViagemSelecionada(RegistroVeiculo viagemSelecionada) {
		this.viagemSelecionada = viagemSelecionada;
	}

}
