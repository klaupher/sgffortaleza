/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.RegistroVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.CotaKmService;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.SolicitacaoVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusRegistroSolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.util.StatusSolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.util.StatusVeiculo;

/**
 * @author Deivid
 * @since 22/12/09
 */
@Component("solicitacaoVeiculoBean")
@Scope("session")
public class SolicitacaoVeiculoBean extends EntityBean<Integer, SolicitacaoVeiculo> {

	private static final Log logger = LogFactory.getLog(SolicitacaoVeiculoBean.class);

	@Autowired
	private SolicitacaoVeiculoService service;

	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private MotoristaService motoristaService;
	
	@Autowired
	private CotaKmService cotaKmService;
	

	//MODIFICADO 29.05.2014 PAULO ANDRE
	//atributo para fazer o calculo da cota de km rodados 
	private double saldo;
	//FIM
	
	private RegistroVeiculo registro;
	private UG orgaoSelecionado;
	private UA uaSelecionada;
	private Veiculo veiculo;
	private List<UG> orgaos;
	private List<UA> uas;
	private List<Veiculo> veiculos;
	private List<Motorista> motoristas;
	private Veiculo veiculoSelecionado;
	private List<SolicitacaoVeiculo> solicitacoesPendentes;
	private List<SolicitacaoVeiculo> ultimosKms;

	private Boolean mostrarSolicitacoes;
	private Boolean flagNegar;
	private Boolean registrar;
	private Boolean autorizar;
	private Boolean retornoVeiculo;
	private Boolean start = true;
	private StatusSolicitacaoVeiculo statusPesquisa;
	private String imagemURL;
	private String placaVeiculo;
	private Date dataSaida;
	private Date dataRetorno;
	private Date horaSaida;
	private Date horaRetorno;
	private Date horaSaidaReal;
	private Date horaRetornoReal;
	private Boolean autorizado;
	private Boolean externo;
	private Boolean desabilita;
	private Boolean veiculoDesignado = true;
	private User usuario = SgfUtil.usuarioLogado();
	private Long ultimaKilometragem;
	private SolicitacaoVeiculo solicitacaoVeiculoMarcado;
	private Boolean marcarTodos;

	@Override
	protected SolicitacaoVeiculo createNewEntity() {
		SolicitacaoVeiculo solicitacao = new SolicitacaoVeiculo();
		solicitacao.setSolicitante(this.usuario);
		this.desabilita = false;
		this.externo = false;
		this.placaVeiculo = null;
		return solicitacao;
	}

	@Override
	protected Integer retrieveEntityId(SolicitacaoVeiculo entity) {
		return entity.getId();
	}

	@Override
	protected SolicitacaoVeiculoService retrieveEntityService() {
		return this.service;
	}

	public void searchSolicitacaoByUser() {
		this.entities.clear();
		this.entities = service.findByUserAndStatus(this.usuario, statusPesquisa);
		if (!this.entities.isEmpty()) {
			for (SolicitacaoVeiculo s : this.entities) {
				if (s.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
					s.setImagemURL("/images/retorno.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.FINALIZADO)) {
					s.setImagemURL("/images/ok.jpg");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.SOLICITADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
					s.setImagemURL("/images/saida.png");
				}
			}
			this.mostrarSolicitacoes = true;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sol.inexistentes");
			this.mostrarSolicitacoes = false;
		}
	}
	
	public void rejeitarSolicitacao() {
		for (SolicitacaoVeiculo s : this.entities) {
			if(s.getMarcado() == null){
				return;	
			}

			if(s.getMarcado()){
				s.setAutorizador(SgfUtil.usuarioLogado());
				s.setStatus(StatusSolicitacaoVeiculo.NEGADO);
				s.setUsuario(SgfUtil.usuarioLogado());
				s.setStatusAtendimento(2);
				service.update(s);
			}
		}
		this.search();
	}

	public void marcarRejeitarAll() {
		int c = 0;
		for (SolicitacaoVeiculo s : this.entities) {
			if(c < 50){
				s.setMarcado(getMarcarTodos());
			}
			c++;
		}
	}
	
	public void searchSolicitacaoByUG() {

		if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				this.entities = this.service.executeResultListQuery("findByStatus",	statusPesquisa);
			}
		} else if (SgfUtil.isChefeTransporte(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				this.entities = service.findByUGAndStatus(this.usuario.getPessoa().getUa().getUg(), statusPesquisa);
			}
		} else if (SgfUtil.isChefeSetor(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				this.entities = service.findByUserAndStatus(this.usuario, statusPesquisa);
			}
		}

		if (!this.entities.isEmpty()) {
			for (SolicitacaoVeiculo s : this.entities) {
				if (s.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
					s.setImagemURL("/images/retorno.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.FINALIZADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.SOLICITADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
					s.setImagemURL("/images/saida.png");
				}
			}
			this.mostrarSolicitacoes = true;
		} 
		//		else {
		//			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sol.inexistentes");
		//			this.mostrarSolicitacoes = false;
		//		}
		this.placaVeiculo = null;
	}

	@Override
	public String update() {
		this.entity.setDataHoraSaida(DateUtil.addTime(getDataSaida(), getHoraSaida()));
		this.entity.setDataHoraRetorno(DateUtil.addTime(getDataRetorno(), getHoraRetorno()));

		if(this.veiculoDesignado == false && this.veiculo != null){
			Boolean disponivel = service.isVeiculoDisponivel(this.veiculo.getId(), DateUtil.addTime(this.dataSaida, this.horaSaida),
					DateUtil.addTime(this.dataRetorno, this.horaRetorno));
			if(disponivel){
				JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoIndisponivel");
				return FAIL;
			} 
		}
		if(this.veiculoDesignado == false){
			autorizarSolicitacao();
		}

		this.entity.setVeiculo(this.veiculo);
		service.update(this.entity);
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		this.veiculoDesignado = true;
		return search();
	}

	public String populate() {
		return super.populate();
	}
	
	public String carregarUasDaUG() {
		
		this.setUas(new ArrayList<UA>());
		if(SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			if(this.orgaoSelecionado != null) {
				this.setUas(this.orgaoSelecionado.getUas());
			}
		}else {
			this.setUas(this.orgaoSelecionado.getUas());
		}
		carregarVeiculosDisponiveis();
		return SUCCESS;
	}

	/**
	 * carrega os veículos disponíveis para a data e período informado
	 * se o usuário for administrador, checa em todos os orgão da prefeitura, senão
	 * busca no orgão do usuário logado
	 * @return
	 */
	public String carregarVeiculosDisponiveis() {

		this.veiculos = new ArrayList<Veiculo>();
		if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			if(this.orgaoSelecionado != null){
				this.veiculos = service.findVeiculosDisponiveis(this.entity, this.orgaoSelecionado);
			}
		} else if (SgfUtil.isChefeSetor(this.usuario) || SgfUtil.isChefeTransporte(this.usuario)) {
			if (this.placaVeiculo == null) {
				this.veiculos = service.findVeiculosDisponiveis(this.entity, null);
			}
		}

		return SUCCESS;
	}

	public String prepareView() {
		setCurrentBean(currentBeanName());
		setCurrentState(VIEW);
		return SUCCESS;
	}

	public String save() {
		this.solicitacoesPendentes = new ArrayList<SolicitacaoVeiculo>();
		
		try {
			
			// coloca a UA na entidade
			if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
				this.entity.setUaSolicitante(this.uaSelecionada);
			}else {
				this.entity.setUaSolicitante(this.usuario.getPessoa().getUa());
			}
			this.entity.setSolicitante(usuario);
			this.entity.setDataHoraSaida(DateUtil.addTime(DateUtil.getDateStartDay(this.dataSaida), this.horaSaida));
			this.entity.setDataHoraRetorno(DateUtil.addTime(DateUtil.getDateStartDay(this.dataRetorno), this.horaRetorno));
			this.entity.setVeiculo(this.veiculo);

			if (!DateUtil.compareDate(this.entity.getDataHoraSaida(), this.entity.getDataHoraRetorno())) {
				JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
				return FAIL;
			} 
			/*else if (!DateUtil.compareDate(DateUtil.getDateTime(new Date(),"07:59:59"), this.entity.getDataHoraSaida())) {
				JSFUtil.getInstance().addErrorMessage("msg.error.datas.invalida");
				return FAIL;
			}
			 */
			/** verificação da disponibilidade do veículo */
			Boolean disponivel = true;
			if(this.entity.getVeiculo() != null){
				disponivel = !service.isVeiculoDisponivel(this.entity.getVeiculo().getId(), DateUtil.addTime(this.dataSaida, this.horaSaida),
						DateUtil.addTime(this.dataRetorno, this.horaRetorno));
			} else {
				this.veiculoDesignado = false;
			}

			this.pesquisarSolicitacoesPendentes(this.entity); // busca por solicitações pendentes do veículo
			if(this.solicitacoesPendentes.size() > 0){
				JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoComSolicitacoesPendentes");
				return FAIL;
			}

			if(disponivel.equals(false)) {
				JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoIndisponivel");
				return FAIL;
			}

			if(this.entity.getVeiculo() != null ){
				if(this.entity.getVeiculo().getStatus().equals(StatusVeiculo.transfExterna)){
					JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoEmTransferenciaExterna");
					return FAIL;
				}
			}
			this.entity.setStatus(StatusSolicitacaoVeiculo.SOLICITADO);
			super.save();
			setCurrentBean(currentBeanName());
			return search();
		} catch (Exception e) {
			e.printStackTrace();
			super.prepareSave();
			return FAIL;
		}
	}

	@Override
	public String search() {

		if(this.statusPesquisa == null){
			this.statusPesquisa = StatusSolicitacaoVeiculo.SOLICITADO;
		}

		if(SgfUtil.isChefeSetor(this.usuario) || SgfUtil.isChefeTransporte(this.usuario)){
			this.orgaoSelecionado = this.usuario.getPessoa().getUa().getUg();
		}
		this.interval = 2000000;
		this.flagNegar = false;
		this.registrar = false;
		this.autorizar = false;

		if (SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				if(this.orgaoSelecionado != null){
					this.entities = service.findByUGAndStatus(this.orgaoSelecionado, this.statusPesquisa);
				} else {
					this.entities = this.service.executeResultListQuery("findByStatus",	this.statusPesquisa);
				}
			}
		} else if (SgfUtil.isChefeTransporte(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				this.entities = service.findByUGAndStatus(this.usuario.getPessoa().getUa().getUg(), this.statusPesquisa);
			}
		} else if (SgfUtil.isChefeSetor(this.usuario)) {
			if(this.placaVeiculo != null && this.placaVeiculo != ""){
				this.entities = service.findSolicitacoesVeiculos(this.placaVeiculo, this.statusPesquisa);
			} else {
				this.entities = service.findByUserAndStatus(this.usuario, this.statusPesquisa);
			}
		}

		if (!this.entities.isEmpty()) {
			for (SolicitacaoVeiculo s : this.entities) {
				if (s.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
					s.setImagemURL("/images/retorno.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.FINALIZADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.SOLICITADO)) {
					s.setImagemURL("/images/tick.png");
				} else if (s.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
					s.setImagemURL("/images/saida.png");
				}
			}
			this.mostrarSolicitacoes = true;
		} 

		this.placaVeiculo = null;
		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		return SUCCESS;
	}

	public String atualizaStatus(){
		this.entity.setStatus(this.entity.getStatus());
		if(this.entity.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)){
			this.entity.setStatusAtendimento(null);
		}
		
		return super.update();
	}
	
	public String autorizarSolicitacao() {

		if(this.entity.getVeiculo() != null){
			if(this.entity.getVeiculo().getPropriedade().equals("Locado")){
				this.entity.setMotorista(this.entity.getVeiculo().getMotorista());
			}
			this.entity.setAutorizador(SgfUtil.usuarioLogado());
			this.entity.setStatus(StatusSolicitacaoVeiculo.AUTORIZADO);
			this.entity.setUsuario(SgfUtil.usuarioLogado());
			this.flagNegar = false;
			return super.update();
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoNaoSelecionado");
			setCurrentBean(currentBeanName());
			setCurrentState(SAVE);
			return FAIL;
		}
	}

	public void negar() {
		this.flagNegar = true;
		super.prepareEdit();
	}

	public String confirmarNegar() {
		this.flagNegar = false;
		this.entity.setStatus(StatusSolicitacaoVeiculo.NEGADO);
		return super.update();
	}

	/**
	 * Registra a saida do veículo, atualizando sua kilometragem atual informada
	 * @return
	 */

	@SuppressWarnings("deprecation")
	public String registrarSaida() {
		Long kmatual = this.entity.getKmSaida();
		Veiculo veiculo = this.entity.getVeiculo();
		if(veiculo.getKmAtual() != null){
			if(kmatual < veiculo.getKmAtual()){
				JSFUtil.getInstance().addErrorMessage("msg.error.kmatual.retorno.invalido");
				return FAIL;
			}
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.entity.getDataHoraSaida());
		cal.set(Calendar.HOUR_OF_DAY,this.horaSaidaReal.getHours());
		cal.set(Calendar.MINUTE,this.horaSaidaReal.getMinutes());
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		Date dtSaida = cal.getTime();
		
		//this.entity.setDtSaida(DateUtil.addTime(DateUtil.getDateStartDay(),this.horaSaidaReal.get));
		this.entity.setDtSaida(dtSaida);
		if(this.entity.getDataHoraSaida().getTime() > DateUtil.getDateEndDay().getTime()){
			JSFUtil.getInstance().addErrorMessage("msg.error.registro.saida.naopermitida");
			return FAIL;
		}

		this.pesquisarSolicitacoesPendentes(this.entity);
		if(this.solicitacoesPendentes.size() > 0){
			JSFUtil.getInstance().addErrorMessage("msg.error.solicitacao.veiculoComSolicitacoesPendentes");
			return FAIL;
		}

		this.entity.setStatusAtendimento(StatusRegistroSolicitacaoVeiculo.EM_SERVICO);
		this.entity.setUsuario(SgfUtil.usuarioLogado());
		this.entity.setStatus(StatusSolicitacaoVeiculo.EXTERNO);
		veiculo.setKmAtual(kmatual);
		veiculoService.update(veiculo);
		service.update(this.entity);
		this.registrar = false;
		return search();
	}

	/**
	 * Registra o retorno, salvando a kilometragem
	 * @return
	 */
	public String registrarRetorno() {
		
		Long kmatual = this.entity.getKmRetorno();
		Veiculo veiculo = this.entity.getVeiculo();
		if(veiculo.getKmAtual() != null){
			if(this.entity.getKmSaida() != veiculo.getKmAtual()) {
				if(kmatual < this.entity.getKmSaida()){
					JSFUtil.getInstance().addErrorMessage("msg.error.kmatual.retorno.invalido");
					return FAIL;
				}
			} else {
				if(kmatual < veiculo.getKmAtual()){
					JSFUtil.getInstance().addErrorMessage("msg.error.kmatual.retorno.invalido");
					return FAIL;
				}
			}
		}
		if(this.entity.getKmRetorno() < this.entity.getKmSaida()){
			JSFUtil.getInstance().addErrorMessage("msg.error.kmretorno.invalido");
			return FAIL;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.entity.getDataHoraRetorno());
		cal.set(Calendar.HOUR_OF_DAY,this.horaRetornoReal.getHours());
		cal.set(Calendar.MINUTE,this.horaRetornoReal.getMinutes());
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		Date dtRetorno = cal.getTime();
		
		//this.entity.setDtRetorno(DateUtil.addTime(DateUtil.getDateStartDay(), this.horaRetornoReal));
		this.entity.setDtRetorno(dtRetorno);
		
		
		this.entity.setStatusAtendimento(StatusRegistroSolicitacaoVeiculo.FINALIZADO);
		this.entity.setStatus(StatusSolicitacaoVeiculo.FINALIZADO);
		veiculo.setKmAtual(kmatual);
		
		//MODIFICADO 29.05.2014 PAULO ANDRE
		if(veiculo.getCotaKm() != null){
			this.saldo = 0;
			this.saldo = this.entity.getKmRetorno() - this.entity.getKmSaida();
			veiculo.getCotaKm().setCotaKmDisponivel(veiculo.getCotaKm().getCotaKm() - this.saldo);
			cotaKmService.update(veiculo.getCotaKm());
		}//FIM
		
		
		veiculoService.update(veiculo);
		service.update(this.entity);
		this.registrar = false;
		return search();
	}

	public String prepareUpdate() {
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)){
			this.motoristas = motoristaService.retrieveAll();
		} else {
			this.motoristas = motoristaService.findMotoristasServidores(user.getPessoa().getUa().getUg().getId());
		}
		setHoraSaida(DateUtil.getDateAsTimeString(this.entity.getDataHoraSaida()));
		setHoraRetorno(DateUtil.getDateAsTimeString(this.entity.getDataHoraRetorno()));
		return super.prepareUpdate();
	}

	/**
	 * 
	 */
	public String prepareSave() {
		super.prepareSave();
		this.autorizado = false;
		this.externo = false;
		this.dataSaida = null;
		this.dataRetorno = null;
		this.horaSaida = null;
		this.horaRetorno = null;
		this.horaSaidaReal = null;
		this.horaRetornoReal = null;
		this.entity.setSolicitante(this.usuario);
		this.veiculos = new ArrayList<Veiculo>();
		this.veiculo = null;
		this.orgaoSelecionado 	= SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario) ? null : this.usuario.getPessoa().getUa().getUg();
		this.uaSelecionada 		= SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario) ? null : this.usuario.getPessoa().getUa();
		carregarVeiculosDisponiveis();
		carregarUasDaUG();
		this.solicitacoesPendentes = new ArrayList<SolicitacaoVeiculo>();
		return SUCCESS;
	}

	@Override
	public String prepareEdit() {
		this.solicitacoesPendentes = new ArrayList<SolicitacaoVeiculo>();

		this.veiculo = this.entity.getVeiculo();

		if(this.veiculo != null){
			this.orgaoSelecionado = this.veiculo.getUa().getUg();
		} else {
			this.orgaoSelecionado = this.entity.getSolicitante().getPessoa().getUa().getUg();
			this.veiculos = new ArrayList<Veiculo>(veiculoService.veiculosDisponiveis(this.orgaoSelecionado));
			this.veiculoDesignado = false;
		}

		if(SgfUtil.isAdministrador(this.usuario) || SgfUtil.isCoordenador(this.usuario)){
			if(this.orgaoSelecionado != null){
				this.motoristas = motoristaService.findMotoristasServidores(this.orgaoSelecionado.getId());
			}
			this.veiculos = new ArrayList<Veiculo>(veiculoService.veiculosDisponiveis(this.orgaoSelecionado));
		} else if(SgfUtil.isChefeTransporte(this.usuario)){
			this.veiculos = new ArrayList<Veiculo>(veiculoService.veiculosDisponiveis(this.usuario.getPessoa().getUa().getUg()));
			this.motoristas = motoristaService.findMotoristasServidores(this.usuario.getPessoa().getUa().getUg().getId());
		} else {
			this.veiculos = new ArrayList<Veiculo>(veiculoService.veiculosDisponiveis(null));
		}
		if(this.veiculo != null){
			this.veiculos.add(this.veiculo);
		}
		this.veiculos.add(this.veiculo);

		this.desabilita = false;
		if (this.entity.getStatus().equals(StatusSolicitacaoVeiculo.AUTORIZADO)) {
			this.autorizado = true;
			this.externo = false;
			this.desabilita = true;
		}
		if (this.entity.getStatus().equals(StatusSolicitacaoVeiculo.EXTERNO)) {
			this.externo = true;
			this.autorizado = false;
			this.desabilita = true;
		}

		this.dataSaida 			= this.entity.getDataHoraSaida();
		this.dataRetorno		= this.entity.getDataHoraRetorno();
		this.horaSaida 			= this.entity.getDataHoraSaida();
		this.horaRetorno 		= this.entity.getDataHoraRetorno();
		this.horaSaidaReal 		= this.entity.getDtSaida();
		this.horaRetornoReal 	= this.entity.getDtRetorno();


		if (this.entity.getVeiculo() != null) {
			this.ultimaKilometragem = this.entity.getVeiculo().getKmAtual();
			if(!this.veiculos.contains(this.entity.getVeiculo())){
				this.veiculos.add(this.entity.getVeiculo());
			}
			this.veiculoDesignado = true;
		}
		
		this.pesquisaUltimosKms();
		
		//MODIFICADO 29.05.2014 - PAULO ANDRE
		//ATUALIZA O CAMPO VALORCOTAKM DE CADA VEICULO NA LISTAGEM DE VEICULOS
		for (int i = 0; i < veiculos.size() - 1; i++) {
			if(veiculos.get(i).getCotaKm() == null || veiculos.get(i).getCotaKm().getCotaKmDisponivel() == null){
				veiculos.get(i).setValorCotaKm("Nao possui Cota");
			}else {
				veiculos.get(i).setValorCotaKm(veiculos.get(i).getCotaKm().getCotaKmDisponivel().toString());
			}
		}		
		//FIM
		
		setCurrentBean(currentBeanName());
		setCurrentState(EDIT);
		return SUCCESS;
	}	
	
	public String pesquisaUltimosKms() {
		
		this.ultimosKms = new ArrayList<SolicitacaoVeiculo>();
		if(this.veiculo != null) {
			List<SolicitacaoVeiculo> kms = service.findSolicitacoesVeiculo(this.veiculo, StatusSolicitacaoVeiculo.FINALIZADO);
			for(SolicitacaoVeiculo s : kms){
				if(this.ultimosKms.size() <= 5){
					this.ultimosKms.add(s);
				}
			}
		}
		
		return SUCCESS;
	}
	
	/**
	 * busca solicitações pendentes do veículos selecionado
	 * @return
	 */
	public String pesquisarSolicitacoesPendentes(SolicitacaoVeiculo solicitacao){
		this.solicitacoesPendentes = new ArrayList<SolicitacaoVeiculo>();
		if(this.veiculo != null){
			List<SolicitacaoVeiculo> pendentes = new ArrayList<SolicitacaoVeiculo>();
			List<SolicitacaoVeiculo> current = service.findSolicitacoesVeiculo(this.veiculo, StatusSolicitacaoVeiculo.EXTERNO);
			for (SolicitacaoVeiculo s: current) {
				if(s.getDataHoraSaida().getTime() < DateUtil.getDateStartDay().getTime() || 
						(s.getDataHoraSaida().getTime() >= solicitacao.getDataHoraSaida().getTime() && 
								s.getDataHoraRetorno().getTime() <= solicitacao.getDataHoraRetorno().getTime())){
					pendentes.add(s);
				}
			}
			this.solicitacoesPendentes = pendentes;
		}
		return SUCCESS;
	}

	public String pesquisarSolicitacoesPendentes(){
		return pesquisarSolicitacoesPendentes(this.entity);
	}
	
	public String pesquisarNomeMotorista() {
		
		return SUCCESS;
	}

	public List<SolicitacaoVeiculo> getSolicitacoesPendentes() {
		return solicitacoesPendentes;
	}

	public void setSolicitacoesPendentes(
			List<SolicitacaoVeiculo> solicitacoesPendentes) {
		this.solicitacoesPendentes = solicitacoesPendentes;
	}

	public UG getOrgaoSelecionado() {
		return orgaoSelecionado;
	}

	public void setOrgaoSelecionado(UG orgaoSelecionado) {
		this.orgaoSelecionado = orgaoSelecionado;
	}

	public void setOrgaos(List<UG> orgaos) {
		this.orgaos = orgaos;
	}

	public List<UG> getOrgaos() {
		return orgaos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculoSelecionado(Veiculo veiculoSelecionado) {
		this.veiculoSelecionado = veiculoSelecionado;
	}

	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}

	public void setMostrarSolicitacoes(Boolean mostrarSolicitacoes) {
		this.mostrarSolicitacoes = mostrarSolicitacoes;
	}

	public Boolean getMostrarSolicitacoes() {
		return mostrarSolicitacoes;
	}

	public void setFlagNegar(Boolean flagNegar) {
		this.flagNegar = flagNegar;
	}

	public Boolean getFlagNegar() {
		return flagNegar;
	}

	public void setRegistro(RegistroVeiculo registro) {
		this.registro = registro;
	}

	public RegistroVeiculo getRegistro() {
		return registro;
	}

	public void setRegistrar(Boolean registrar) {
		this.registrar = registrar;
	}

	public Boolean getRegistrar() {
		return registrar;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public String getImagemURL() {
		return imagemURL;
	}

	public void setImagemURL(String imagemURL) {
		this.imagemURL = imagemURL;
	}

	public void setRetornoVeiculo(boolean retornoVeiculo) {
		this.retornoVeiculo = retornoVeiculo;
	}

	public boolean isRetornoVeiculo() {
		return retornoVeiculo;
	}

	public void setAutorizar(Boolean autorizar) {
		this.autorizar = autorizar;
	}

	public Boolean getAutorizar() {
		return autorizar;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public boolean isStart() {
		return start;
	}

	public StatusSolicitacaoVeiculo getStatusPesquisa() {
		return statusPesquisa;
	}

	public void setStatusPesquisa(StatusSolicitacaoVeiculo statusPesquisa) {
		this.statusPesquisa = statusPesquisa;
	}

	public String getPlacaVeiculo() {
		return placaVeiculo;
	}

	public void setPlacaVeiculo(String placaVeiculo) {
		this.placaVeiculo = placaVeiculo;
	}

	public Date getDataSaida() {
		return dataSaida;
	}

	public void setDataSaida(Date dataSaida) {
		this.dataSaida = dataSaida;
	}

	public Date getDataRetorno() {
		return dataRetorno;
	}

	public void setDataRetorno(Date dataRetorno) {
		this.dataRetorno = dataRetorno;
	}

	public Date getHoraSaida() {
		return horaSaida;
	}

	public void setHoraSaida(Date horaSaida) {
		this.horaSaida = horaSaida;
	}

	public Date getHoraRetorno() {
		return horaRetorno;
	}

	public void setHoraRetorno(Date horaRetorno) {
		this.horaRetorno = horaRetorno;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public Date getHoraSaidaReal() {
		return horaSaidaReal;
	}

	public void setHoraSaidaReal(Date horaSaidaReal) {
		this.horaSaidaReal = horaSaidaReal;
	}

	public Date getHoraRetornoReal() {
		return horaRetornoReal;
	}

	public void setHoraRetornoReal(Date horaRetornoReal) {
		this.horaRetornoReal = horaRetornoReal;
	}

	public Boolean getAutorizado() {
		return autorizado;
	}

	public void setAutorizado(Boolean autorizado) {
		this.autorizado = autorizado;
	}

	public Boolean getExterno() {
		return externo;
	}

	public void setExterno(Boolean externo) {
		this.externo = externo;
	}

	public Boolean getDesabilita() {
		return desabilita;
	}

	public void setDesabilita(Boolean desabilita) {
		this.desabilita = desabilita;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Boolean getVeiculoDesignado() {
		return veiculoDesignado;
	}

	public void setVeiculoDesignado(Boolean veiculoDesignado) {
		this.veiculoDesignado = veiculoDesignado;
	}

	public Long getUltimaKilometragem() {
		return ultimaKilometragem;
	}

	public void setUltimaKilometragem(Long ultimaKilometragem) {
		this.ultimaKilometragem = ultimaKilometragem;
	}

	public List<UA> getUas() {
		return uas;
	}

	public void setUas(List<UA> uas) {
		this.uas = uas;
	}

	public UA getUaSelecionada() {
		return uaSelecionada;
	}

	public void setUaSelecionada(UA uaSelecionada) {
		this.uaSelecionada = uaSelecionada;
	}

	public List<SolicitacaoVeiculo> getUltimosKms() {
		return ultimosKms;
	}

	public void setUltimosKms(List<SolicitacaoVeiculo> ultimosKms) {
		this.ultimosKms = ultimosKms;
	}

	public SolicitacaoVeiculo getSolicitacaoVeiculoMarcado() {
		return solicitacaoVeiculoMarcado;
	}

	public void setSolicitacaoVeiculoMarcado(SolicitacaoVeiculo solicitacaoVeiculoMarcado) {
		this.solicitacaoVeiculoMarcado = solicitacaoVeiculoMarcado;
	}

	public Boolean getMarcarTodos() {
		return marcarTodos;
	}

	public void setMarcarTodos(Boolean marcarTodos) {
		this.marcarTodos = marcarTodos;
	}
}
