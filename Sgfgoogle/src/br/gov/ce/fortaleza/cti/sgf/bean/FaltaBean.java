/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Falta;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.FaltaService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;

/**
 * @author Arthur
 * @since 10/06/2014
 */
@Scope("session")
@Component("faltaBean")
public class FaltaBean extends EntityBean<Integer, Falta> {

	@Autowired
	private FaltaService service;

	@Autowired
	private VeiculoService veiculoService;

	private List<Veiculo> veiculos = new ArrayList<Veiculo>();

	private User usuario;
	private Date dtInicial;
	private Date dtFinal;
	private UG orgaoCadSelecionado;
	private UG orgaoSelecionado;
	private UA uaSelecionada;
	private UG ug;
	private String placa;
	private boolean confirmAtender = false;

	@Override
	protected Falta createNewEntity() {
		Falta falta = new Falta();
		setDtInicial(DateUtil.getDateTime(new Date(), "00:00:00"));
		setDtFinal(DateUtil.getDateTime(new Date(), "23:59:59"));
		return falta;
	}

	@Override
	protected Integer retrieveEntityId(Falta entity) {
		return entity.getId();
	}

	@Override
	protected FaltaService retrieveEntityService() {
		return this.service;
	}

	@PostConstruct
	public void init() {

		this.entities = new ArrayList<Falta>();
		this.dtInicial = DateUtil.getDateTime(DateUtil.getDateTime(new Date()));
		this.dtFinal = DateUtil.getDateTime(DateUtil.getDateTime(new Date()));

		if(SgfUtil.isChefeTransporte(SgfUtil.usuarioLogado()) || SgfUtil.isChefeSetor(SgfUtil.usuarioLogado())){

			this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, 
					SgfUtil.usuarioLogado().getPessoa().getUa().getUg() );
			
		} else if(SgfUtil.isAdministrador(SgfUtil.usuarioLogado())){

			if(this.orgaoSelecionado != null){
				this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, this.orgaoSelecionado);
			} else {
				this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, 
						SgfUtil.usuarioLogado().getPessoa().getUa().getUg() );
			}
		}
	}

	@Override
	public String prepareSave() {
		refreshLists();
		return super.prepareSave();
	}

	@Override
	public String prepareUpdate() {
		this.orgaoSelecionado = this.entity.getVeiculo().getUa().getUg();
		loadVeiculos();
		return super.prepareUpdate();
	}

	/**
	 * pesquisa os abastecimentos efetuados no período por perfil de usuario, período e orgão
	 * @return
	 */
	public String pesquisarFaltasPorPeriodoPorOrgao() {

		User usuarioLogado = SgfUtil.usuarioLogado();
		this.entities = new ArrayList<Falta>();
		this.dtInicial = DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal = DateUtil.getDateEndDay(this.dtFinal);

		if (DateUtil.compareDate(this.dtInicial, this.dtFinal)) {

			this.placa = this.placa.toUpperCase();

			if (SgfUtil.isOperador(usuarioLogado)) {
				if (this.placa != null && this.placa != "") {
					this.entities = service.pesquisarFaltaVeiculoPorPlaca(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.placa);
				} else {
					this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, this.orgaoSelecionado);
				}
			} else if(SgfUtil.isChefeTransporte(usuarioLogado) || SgfUtil.isChefeSetor(usuarioLogado)){
				if (this.placa != null && this.placa != "") {
					this.entities = service.pesquisarFaltaVeiculoPorPlaca(this.dtInicial,this.dtFinal, 
							usuarioLogado.getPessoa().getUa().getUg(), this.placa);
				} else {
					this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, 
							usuarioLogado.getPessoa().getUa().getUg() );
				}
			} else if (SgfUtil.isAdministrador(usuarioLogado) || SgfUtil.isCoordenador(usuarioLogado)) {

				if(this.orgaoSelecionado != null && this.orgaoSelecionado.getId() != null){
					if (this.placa != null && this.placa != "") {
						this.entities = service.pesquisarFaltaVeiculoPorPlaca(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.placa );
					} else {
						this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, this.orgaoSelecionado );
					}
				} else {
					//this.entities = service.pesquisarAbastecimentos(this.dtInicial,this.dtFinal, this.status);
					this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial,this.dtFinal, null);
				}
			}
			return SUCCESS;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
	}

	/**
	 * O m�todo loadVeiculos, povoa a lista de ve�culos do �rg�o selecionado e chama o m�todo para 
	 * povoar a lista de motoristas.
	 */
	public void loadVeiculos() {
		this.veiculos = new ArrayList<Veiculo>();

		if (this.orgaoSelecionado != null) {
			this.veiculos.addAll(veiculoService.veiculosAtivoscomcota(this.orgaoSelecionado));
		}
		Collections.sort(this.veiculos, new Comparator<Veiculo>() {
			public int compare(Veiculo p1, Veiculo p2) {
				return p1.getPlaca().compareTo(p2.getPlaca());
			}
		});
	}

	private void refreshLists() {
		this.veiculos.clear();
		
		if (this.orgaoSelecionado != null) {
			this.veiculos.addAll(veiculoService.findByUG(this.orgaoSelecionado));
		} else {
			this.veiculos.addAll(veiculoService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg()));
		}
	}

	public String populate() {
		this.placa = null;
		return SUCCESS;
	}


	@Override
	public String search() {
		User usuarioLogado = SgfUtil.usuarioLogado();
		Set<Falta> filtro = new HashSet<Falta>(0);
		this.entities = new ArrayList<Falta>();
		
		this.dtInicial = DateUtil.getDateStartDay(this.dtInicial);//DateUtil.getDateTime(this.dtInicial);
		this.dtInicial = DateUtil.setMilisecondsIndate(this.dtInicial);

		this.dtFinal = DateUtil.getDateEndDay(this.dtFinal);
		this.dtFinal = DateUtil.setMilisecondsIndate(this.dtFinal);

		if (SgfUtil.isAdministrador(usuarioLogado) || SgfUtil.isCoordenador(usuarioLogado)) {

			if(this.orgaoSelecionado == null){
				this.entities = service.pesquisarFalta(this.dtInicial, this.dtFinal);
			} else {
				this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial, this.dtFinal, this.orgaoSelecionado);
			}

		} else if (SgfUtil.isChefeTransporte(usuarioLogado)) {
			this.entities = service.pesquisarFaltaPorPeriodo(this.dtInicial, this.dtFinal, usuarioLogado.getPessoa().getUa().getUg() );
		}

		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		this.interval = 2000000;
		return SUCCESS;
	}
	

	@SuppressWarnings("finally")
	@Override
	public String save() {
		
		if(this.entity.getDataFalta() == null){
			this.entity.setDataFalta(DateUtil.getDateTime(DateUtil.getDateStartDay(new Date())));
		} else {
			this.entity.setDataFalta(DateUtil.getDateTime(DateUtil.getDateStartDay(this.entity.getDataFalta())));
			this.entity.setDataFalta(DateUtil.adicionarOuDiminuir(this.entity.getDataFalta(), DateUtil.SECOND_IN_MILLIS));
		}

		try{
			 retrieveEntityService().save(this.entity);
			 return search();
		} catch (Exception e) {
			JSFUtil.getInstance().addErrorMessage("msg.error.falta.autoriazacaoExistente");
			return FAIL;
		} finally{
			return FAIL;
		}
			

	}

	/**pages/home.jsf
	 * Atualiza o cadastro do  atendimento, veificando se o abastecimento não se refere ao abastecimento 
	 * de um vasilhame(veículo de modelo com código = 75)
	 */
	@Override
	public String update() {

		try {
			
			this.service.update(this.entity);
			return search();
		} catch (Exception e) {
			//e.printStackTrace();
			JSFUtil.getInstance().addErrorMessage("msg.error.falta.autoriazacaoExistente");
			setCurrentBean(currentBeanName());
			setCurrentState(SEARCH);
			return FAIL;
		}

		//setCurrentBean(currentBeanName());
		//setCurrentState(SEARCH);
		//return SUCCESS;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
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

	public void setOrgaoSelecionado(UG orgaoSelecionado) {
		this.orgaoSelecionado = orgaoSelecionado;
	}

	public UG getOrgaoSelecionado() {
		return orgaoSelecionado;
	}

	public UG getUg() {
		return ug;
	}

	public void setUg(UG ug) {
		this.ug = ug;
	}

	public UG getOrgaoCadSelecionado() {
		return orgaoCadSelecionado;
	}

	public void setOrgaoCadSelecionado(UG orgaoCadSelecionado) {
		this.orgaoCadSelecionado = orgaoCadSelecionado;
	}

	public UA getUaSelecionada() {
		return uaSelecionada;
	}

	public void setUaSelecionada(UA uaSelecionada) {
		this.uaSelecionada = uaSelecionada;
	}

	public String getPlaca() {
		return placa;
	}


	public boolean isConfirmAtender() {
		return confirmAtender;
	}
	
	public void setPlaca(String placa) {
		this.placa = placa;
	}

}
