/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.PostPersist;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKm;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKmHistoricoUltrapassado;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
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
	
	private Collection<? extends Veiculo> veiculos;
	
	private Veiculo veiculoSelecionado;

	private Double valorInicial;

	private Double valorFinal;

	private Double kmDia;

	private Double cotaKmDisponivel;

	private boolean isSalvarHistorico;
	
	private UG ugPesquisa;
	private String placaPesquisa;
	private String chassiPesquisa;
	private String renavamPesquisa;
	
	public void pesquisar(){
		Veiculo veiculo = new Veiculo();
		veiculo.setPlaca(placaPesquisa);
		veiculo.setRenavam(renavamPesquisa);
		veiculo.setChassi(chassiPesquisa);
		veiculos = veiculoService.pesquisaVeiculoCotasKm(veiculo, null, null, ugPesquisa);
	}
	
	
	@Override
	public String prepareSave() {
		// TODO Auto-generated method stub
		veiculos = service.findVeiculosComCotaKmAtivos();
		return super.prepareSave();
	}


	@Override
	public String save() {
		// TODO Auto-generated method stub
		createNewEntity();
		valorInicial = entity.getValorInicial();
		valorFinal = entity.getValorFinal();
		kmDia = valorFinal - valorInicial;
		cotaKmDisponivel = veiculoSelecionado.getCotaKm().getCotaKmDisponivel();
		if(validaDiario()){
			entity.setCotaKm(veiculoSelecionado.getCotaKm());
			entity.getCotaKm().setCotaKmDisponivel(cotaKmDisponivel - kmDia);
//			entity.getCotaKm().setValorFinal(valorFinal);
//			entity.setValorFinal(valorFinal);
//			entity.setValorInicial(valorInicial);
			//Atualiza o saldo da cota de quilometragem
			cotaKmService.update(entity.getCotaKm());
			//salva o diario de quilometragem
			String retorno = super.save();
			//Salva o histórico da cotaKm caso a mesma já tenha sido ultrapassada.
			if(isSalvarHistorico){
				salvarHistorico();
			}
			return retorno;
		}
		return FAIL;
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


	public Boolean validaDiario(){
		if(valorInicial > valorFinal){
			JSFUtil.getInstance().addErrorMessage("diariokm.valorfinal.maior.valorinicial");
			return false;
		}else if(kmDia > cotaKmDisponivel){
//			Perguntar a Verene se é para salvar a Diária e guardar o histórico?
//			Quando ultrapassar jogar na tabela de historico 
//			JSFUtil.getInstance().addErrorMessage("diariokm.cota.ultrapassada");
			setSalvarHistorico(true);
		}
		return true;
	}
	
	@Override
	public String search() {
		// TODO Auto-generated method stub
		entities = service.findVeiculosComDiarioKmAtivos();
//		entities.addAll(service.findVeiculosTerceirosComCota());
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
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

	public void setVeiculos(Collection<? extends Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}

	public void setVeiculoSelecionado(Veiculo veiculoSelecionado) {
		this.veiculoSelecionado = veiculoSelecionado;
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

}
