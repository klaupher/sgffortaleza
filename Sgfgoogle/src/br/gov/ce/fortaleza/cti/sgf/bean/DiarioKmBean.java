/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

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
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaKmService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioKmHistoricoService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioKmService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

/**
 * @author deivid
 *
 */
@Scope("session")
@Component("diarioKmBean")
public class DiarioKmBean extends EntityBean<Integer, DiarioKm>{
	
	@Autowired
	private DiarioKmService service;
	
	@Autowired
	private CotaKmService cotaKmService;
	
	@Autowired
	private DiarioKmHistoricoService historicoService;
	
	@Autowired
	private VeiculoService veiculoService;
	
	private Collection<Veiculo> veiculos;
	
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
	
	//Componentes JSF
	private HtmlInputText inputValorInicial;
	
	public void pesquisar(){
		veiculoPesquisa = new Veiculo();
		veiculoPesquisa.setPlaca(placaPesquisa);
		veiculoPesquisa.setRenavam(renavamPesquisa);
		veiculoPesquisa.setChassi(chassiPesquisa);
		if(!SgfUtil.isAdministrador(usuario)){
			ugPesquisa = usuario.getPessoa().getUa().getUg();
			veiculoPesquisa.setUa(new UA());
			veiculoPesquisa.getUa().setUg(ugPesquisa);
		}else if(ugPesquisa != null){
			veiculoPesquisa.setUa(new UA());
			veiculoPesquisa.getUa().setUg(ugPesquisa);
		}
		veiculos = service.pesquisar(veiculoPesquisa);
		populaVeiculoUltimaDiaria(veiculos);
	}
	
	
	@Override
	public String search() {
		// TODO Auto-generated method stub
		pesquisar();
		populaVeiculoUltimaDiaria(veiculos);
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}
	
	private void populaVeiculoUltimaDiaria(Collection<? extends Veiculo> veiculos2){
		for (Veiculo veiculoT : veiculos2) {
			DiarioKm diarioKm = service.findUltimaDiaria(veiculoT);
			if(diarioKm != null){
				veiculoT.setValorInicial(diarioKm.getValorInicial().floatValue());
			}
		}
	}
	
	
	@Override
	public String prepareSave() {
		// TODO Auto-generated method stub
		veiculos = service.findVeiculosComCotaKmAtivos();
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
				if(validaDiario(veiculo)){
					entity.setCotaKm(veiculo.getCotaKm());
					entity.getCotaKm().setCotaKmDisponivel(cotaKmDisponivel - kmDia);
					entity.getCotaKm().setValorFinal(valorFinal);
					entity.setValorFinal(valorFinal);
					entity.setValorInicial(valorInicial);
					try{
						//Atualiza o saldo da cota de quilometragem
						cotaKmService.update(entity.getCotaKm());
						//salva o diario de quilometragem
						entity = service.save(entity);
						retorno = search();
					}catch(Exception e){
						JSFUtil.getInstance().addErrorMessage("diarioKm.falha.salvar.diario");
					}
					//Salva o histórico da cotaKm caso a mesma já tenha sido ultrapassada.
					if(isSalvarHistorico){
						salvarHistorico();
					}
				}
			}
		}
		return retorno;
	}
	
	
	public String saveDiario(){
		veiculos.clear();
		veiculos.add(veiculoSelecionado);
		return save();
	}
	
	
	private void salvarHistorico() {
		// TODO Auto-generated method stub
		DiarioKmHistoricoUltrapassado historicoUltrapassado = new DiarioKmHistoricoUltrapassado();
		historicoUltrapassado.setDataCadastro(new Date());
		historicoUltrapassado.setDataAtualizacao(new Date());
		historicoUltrapassado.setCotaDisponivelMes(entity.getCotaKm().getCotaKm());
		historicoUltrapassado.setCotaKm(entity.getCotaKm());
		historicoUltrapassado.setCotaUltrapassadaMes(kmDia);
		historicoUltrapassado.setAnoMes(recuperarAnoMes());
		historicoService.save(historicoUltrapassado);
	}


	private String recuperarAnoMes() {
		// TODO Auto-generated method stub
		String mes = new DateTime().monthOfYear().getAsString();
		String ano = new DateTime().year().getAsString();
		
		return mes.concat("/"+ano);
	}


	public Boolean validaDiario(Veiculo veiculo){
		if(valorInicial > valorFinal){
			JSFUtil.getInstance().addErrorMessage("diariokm.valorfinal.maior.valorinicial");
			return false;
		}else if(kmDia > cotaKmDisponivel){
//			Perguntar a Verene se é para salvar a Diária e guardar o histórico?
//			Quando ultrapassar jogar na tabela de historico 
//			JSFUtil.getInstance().addErrorMessage("diariokm.cota.ultrapassada");
			setSalvarHistorico(true);
		}else if(existeDiaria(veiculo)){
			JSFUtil.getInstance().addErrorMessage("diariokm.ja.existe.diaria.veiculo");
			return false;
		}
		return true;
	}
	
	private boolean existeDiaria(Veiculo veiculo) {
		// TODO Auto-generated method stub
		return service.findDiariaVeiculoNoDia(veiculo);
	}


	@Override
	protected BaseService<Integer, DiarioKm> retrieveEntityService() {
		// TODO Auto-generated method stub
		return service;
	}

	@Override
	protected Integer retrieveEntityId(DiarioKm entity) {
		// TODO Auto-generated method stub
		return entity.getId();
	}

	@Override
	protected DiarioKm createNewEntity() {
		// TODO Auto-generated method stub
		entity = new DiarioKm();
		entity.setDataAlteracao(new Date());
		entity.setDataCadastro(new Date());
		entity.setUsuarioAlteracao(SgfUtil.usuarioLogado());
		entity.setUsuarioCricao(SgfUtil.usuarioLogado());
		entity.setValorFinal(0D);
		entity.setValorInicial(0D);
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

}
