/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Abastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoCombustivel;
import br.gov.ce.fortaleza.cti.sgf.entity.TipoServico;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.AbastecimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.BombaService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaService;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.service.TipoServicoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.util.StatusAtendimentoAbastecimento;

/**
 * @author Joel
 * @since 11/12/09
 */
@Scope("session")
@Component("abastecimentoBean")
public class AbastecimentoBean extends EntityBean<Integer, Abastecimento> {

	@Autowired
	private AbastecimentoService service;

	@Autowired
	private AtendimentoService atendimentoService;

	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private MotoristaService motoristaService;

	@Autowired
	private TipoServicoService tipoServicoService;

	@Autowired
	private PostoService postoService;

	@Autowired
	private BombaService bombaService;

	@Autowired
	private CotaService cotaService;

	private List<Veiculo> veiculos = new ArrayList<Veiculo>();
	private List<TipoServico> tiposServico = new ArrayList<TipoServico>();
	private List<Motorista> motoristas = new ArrayList<Motorista>();
	private List<Posto> postos = new ArrayList<Posto>();
	private List<Bomba> bombas = new ArrayList<Bomba>();
	private List<TipoCombustivel> combustiveis = new ArrayList<TipoCombustivel>();

	private User usuario;
	private Date dtInicial;
	private Date dtFinal;
	private UG orgaoCadSelecionado;
	private UG orgaoSelecionado;
	private UA uaSelecionada;
	private UG ug;
	private Boolean autorizar;
	private Boolean atendimento;
	private Boolean atender;
	private Boolean negar;
	private Boolean start;
	private Boolean mostrarPosto;
	private Long kmAtendimento;
	private Double quantidadeAbastecida;
	private Long ultimaKilometragem;
	private Bomba bomba;
	private StatusAbastecimento status = StatusAbastecimento.AUTORIZADO;
	private String placa;
	private Double saldoAtual;
	private boolean vasilhame = false;

	@Override
	protected Abastecimento createNewEntity() {
		Abastecimento abastecimento = new Abastecimento();
		abastecimento.setCombustivel(new TipoCombustivel());
		abastecimento.setPosto(new Posto());
		setDtInicial(DateUtil.getDateTime(new Date(), "00:00:00"));
		setDtFinal(DateUtil.getDateTime(new Date(), "23:59:59"));
		this.mostrarPosto = false;
		this.bomba = new Bomba();
		return abastecimento;
	}

	@Override
	protected Integer retrieveEntityId(Abastecimento entity) {
		return entity.getId();
	}

	@Override
	protected AbastecimentoService retrieveEntityService() {
		return this.service;
	}

	@PostConstruct
	public void init() {
		//this.orgaoSelecionado = SgfUtil.usuarioLogado().getPessoa().getUa().getUg();
		//this.dtInicial = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "00:00:00");
		//this.dtFinal = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "23:59:59");
		this.entities = new ArrayList<Abastecimento>();
		this.dtInicial = DateUtil.getDateTime(DateUtil.getDateTime(new Date()));
		this.dtFinal = DateUtil.getDateTime(DateUtil.getDateTime(new Date()));
		this.status = StatusAbastecimento.AUTORIZADO;

		if(SgfUtil.isChefeTransporte(SgfUtil.usuarioLogado()) || SgfUtil.isChefeSetor(SgfUtil.usuarioLogado())){

			this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, 
					SgfUtil.usuarioLogado().getPessoa().getUa().getUg(), this.status);
		} else if(SgfUtil.isAdministrador(SgfUtil.usuarioLogado())){

			if(this.orgaoSelecionado != null){
				this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.status);
			} else {
				this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, 
						SgfUtil.usuarioLogado().getPessoa().getUa().getUg(), this.status);
			}
		}
	}

	@Override
	public String prepareSave() {
		this.autorizar = false;
		this.atender = false;
		this.atendimento = false;
		this.mostrarPosto = false;
		refreshLists();
		return super.prepareSave();
	}

	@Override
	public String prepareUpdate() {
		this.postos = new ArrayList<Posto>();
		this.orgaoSelecionado = this.entity.getVeiculo().getUa().getUg();
		if(this.orgaoSelecionado != null){
			this.veiculos.addAll(veiculoService.findByUG(this.orgaoSelecionado));
			this.motoristas = motoristaService.findByUG(this.orgaoSelecionado.getId());
		}
		postoPorCombustivel();
		//refreshLists();
		return super.prepareUpdate();
	}

	public String postoPorCombustivel(){
		this.postos = new ArrayList<Posto>();
		if (this.entity.getCombustivel() != null) {
			this.postos = postoService.findByCombustivel(this.entity.getCombustivel());
			this.mostrarPosto = true;
		} else {
			this.mostrarPosto = false;
		}
		return SUCCESS;
	}

	/**
	 * pesquisa os abastecimentos efetuados no período por perfil de usuario, período e orgão
	 * @return
	 */
	public String pesquisarAbastecimentosPorPeriodoPorOrgao() {

		User usuarioLogado = SgfUtil.usuarioLogado();
		this.entities = new ArrayList<Abastecimento>();
		//this.dtInicial = DateUtil.getDateTime(this.dtInicial, "00:00:00");
		//this.dtFinal = DateUtil.getDateTime(this.dtFinal, "23:59:59");
		this.dtInicial = DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal = DateUtil.getDateEndDay(this.dtFinal);

		if (DateUtil.compareDate(this.dtInicial, this.dtFinal)) {

			if (SgfUtil.isOperador(usuarioLogado)) {
				if (this.placa != null && this.placa != "") {
					this.entities = service.pesquisarAbastecimentoVeiculoPorPlaca(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.placa,this.status);
				} else {
					this.entities = service.findByPeriodoAndPosto(usuarioLogado.getPosto().getCodPosto(), this.dtInicial,this.dtFinal, status);
				}
			} else if(SgfUtil.isChefeTransporte(usuarioLogado) || SgfUtil.isChefeSetor(usuarioLogado)){
				if (this.placa != null && this.placa != "") {
					this.entities = service.pesquisarAbastecimentoVeiculoPorPlaca(this.dtInicial,this.dtFinal, 
							usuarioLogado.getPessoa().getUa().getUg(), this.placa,this.status);
				} else {
					this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, 
							usuarioLogado.getPessoa().getUa().getUg(), this.status);
				}
			} else if (SgfUtil.isAdministrador(usuarioLogado) || SgfUtil.isCoordenador(usuarioLogado)) {

				if(this.orgaoSelecionado != null && this.orgaoSelecionado.getId() != null){
					if (this.placa != null && this.placa != "") {
						this.entities = service.pesquisarAbastecimentoVeiculoPorPlaca(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.placa,this.status);
					} else {
						this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, this.orgaoSelecionado, this.status);
					}
				} else {
					//this.entities = service.pesquisarAbastecimentos(this.dtInicial,this.dtFinal, this.status);
					this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial,this.dtFinal, null, this.status);
				}
			}
			return SUCCESS;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
	}

	public void loadVeiculos() {
		this.veiculos = new ArrayList<Veiculo>();
		Veiculo vasilhame = veiculoService.findByPlacaSingle("VASILHA");
		this.veiculos.add(vasilhame);
		if (this.orgaoSelecionado != null) {
			this.veiculos.addAll(veiculoService.findByUG(this.orgaoSelecionado));
		}
		loadMotoristas();
	}

	public void loadMotoristas() {
		this.motoristas = new ArrayList<Motorista>();
		if (this.orgaoSelecionado != null) {
			this.motoristas = motoristaService.findByUG(this.orgaoSelecionado.getId());
		}
	}

	private void refreshLists() {
		this.veiculos.clear();
		this.motoristas.clear();
		this.tiposServico.clear();
		if (this.entity != null) {
			if (entity.getQuilometragem() != null) {
				this.ultimaKilometragem = entity.getQuilometragem();
			}
		}
		Veiculo vasilhame = veiculoService.findByPlacaSingle("VASILHA");
		this.veiculos.add(vasilhame);
		if (this.orgaoSelecionado != null) {
			this.veiculos.addAll(veiculoService.findByUG(this.orgaoSelecionado));
			this.motoristas.addAll(motoristaService.findByUG(this.orgaoSelecionado.getId()));
		} else {
			this.veiculos.addAll(veiculoService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg()));
			this.motoristas.addAll(motoristaService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId()));
		}
		this.tiposServico.add(tipoServicoService.retrieve(1));
		if (this.entity.getPosto() != null) {
			if(this.entity.getPosto().getListaBomba() != null){
				setBombas(this.entity.getPosto().getListaBomba());
			}
		}
	}

	private boolean validaQuilometragem() {

		if (this.atendimento) {
			Abastecimento last = service.executeSingleResultQuery("findLast", this.entity.getVeiculo().getId());
			if (last != null) {
				this.ultimaKilometragem = last.getQuilometragem();
				if (this.ultimaKilometragem != null) {
					if (this.ultimaKilometragem > this.kmAtendimento) {
						JSFUtil.getInstance().addErrorMessage("msg.error.quilometragem.inconsistente");
						return false;
					}
				}
			}
		}

		if (validaCota()) {
			return true;
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.cota.utrapassada");
			return false;
		}
	}
	/**
	 * Valida a cota dispon�vel para o ve�culo durante o atendimento
	 * do abastecimento
	 * 
	 * @return Boolean
	 */
	private Boolean validaCota() {
		return this.entity.getVeiculo().getCota().getCotaDisponivel() > 0 ? true : false;
	}

	/**
	 * faz validações de autorizaçao existente, se veículo é vasilhame
	 * se veículo possui cota
	 * se veículo já possui autorizacao de abastecimento na data solicitada
	 * @return
	 */
	public boolean validarAutorizacao() {

		Veiculo veiculo = this.entity.getVeiculo();
		boolean vasilhame = false;
		boolean existeAutorizacao = false;
		if(veiculo != null){
			if(this.entity.getDataAutorizacao() == null){
				existeAutorizacao = service.validarAutorizacaoVeiculo(veiculo, DateUtil.getDateTime(new Date()));
			} else {
				existeAutorizacao = service.validarAutorizacaoVeiculo(veiculo, this.entity.getDataAutorizacao());
			}
			if(existeAutorizacao){
				JSFUtil.getInstance().addErrorMessage("msg.error.abastecimento.autoriazacaoExistente");
				return false;
			}
		}

		if(veiculo.getModelo() != null){
			vasilhame = veiculo.getModelo().getId() == 75;
		}

		if (veiculo.getCota() != null || vasilhame) {
			if(vasilhame){
				return true;
			} else if (veiculo.getCota().getCotaDisponivel() > 0) {
				return true;
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.cota.utrapassada");
				return false;
			}
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.cota.indisponivel");
			return false;
		}
	}

	public String populate() {
		this.placa = null;
		return SUCCESS;
	}


	@Override
	public String search() {
		User usuarioLogado = SgfUtil.usuarioLogado();
		Set<Abastecimento> filtro = new HashSet<Abastecimento>(0);
		this.entities = new ArrayList<Abastecimento>();
		this.tiposServico.add(tipoServicoService.retrieve(1));
		if (SgfUtil.isChefeTransporte(usuarioLogado)  || SgfUtil.isChefeSetor(usuarioLogado)) {
			this.orgaoSelecionado = usuarioLogado.getPessoa().getUa().getUg();
		}
		this.autorizar = false;
		this.atender = false;
		this.atendimento = false;
		this.status = StatusAbastecimento.AUTORIZADO;

		//String result = pesquisarAbastecimentosPorPeriodoPorOrgao();

		this.dtInicial = DateUtil.getDateTime(this.dtInicial);
		this.dtFinal = DateUtil.getDateTime(this.dtFinal);

		if (SgfUtil.isAdministrador(usuarioLogado) || SgfUtil.isCoordenador(usuarioLogado)) {
			this.entities = service.pesquisarAbastecimentos(this.dtInicial, this.dtFinal, this.status);
		} else if (SgfUtil.isOperador(SgfUtil.usuarioLogado())) {
			this.dtInicial = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "00:00:00");
			this.dtFinal = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "23:59:59");
			this.entities = service.findByPeriodoAndPosto(usuarioLogado.getPosto().getCodPosto(), this.dtInicial, this.dtFinal, this.status);

		} else if (SgfUtil.isChefeTransporte(usuarioLogado)) {
			this.dtInicial = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "00:00:00");
			this.dtFinal = DateUtil.getDateTime(DateUtil.getDateTime(new Date()), "23:59:59");
			this.entities = service.pesquisarAbastecimentosPorPeriodo(this.dtInicial, this.dtFinal, usuarioLogado.getPessoa().getUa().getUg(),this.status);
			//loadVeiculos();
		}

		for (Abastecimento abastecimento : this.entities) {

			if (SgfUtil.isAdministrador(usuarioLogado) || SgfUtil.isCoordenador(usuarioLogado)) {
				this.autorizar = true;
			} else if (usuarioLogado.getAutoriza() != null) {
				this.autorizar = usuarioLogado.getAutoriza();
			}

			if (!this.atendimento) {
				this.atendimento = SgfUtil.isOperador(usuarioLogado) && (abastecimento.getStatus().equals(StatusAbastecimento.AUTORIZADO));
			}

			if (this.autorizar || this.atendimento) {
				this.negar = false;
				this.atender = false;
				filtro.add(abastecimento);
			}
		}

		setCurrentBean(currentBeanName());
		setCurrentState(SEARCH);
		this.interval = 2000000;
		return SUCCESS;
	}

	@Override
	public String save() {
		if (validarAutorizacao()) {
			if(this.entity.getDataAutorizacao() == null){
				this.entity.setDataAutorizacao(DateUtil.getDateTime(DateUtil.getDateStartDay(new Date())));
			}
			this.entity.setAutorizador(SgfUtil.usuarioLogado());
			super.save();
			return search();
		}
		return FAIL;
	}

	/**
	 * Atualiza o cadastro do  atendimento, veificando se o abastecimento não se refere ao abastecimento 
	 * de um vasilhame(veículo de modelo com código = 75)
	 */
	@Override
	public String update() {
		boolean vasilhame = false;
		if (getCurrentState().equals(VIEW) && this.entity.getStatus().equals(StatusAbastecimento.AUTORIZADO)) {
			//StatusAbastecimento status = entity.getStatus();
			if(this.entity.getVeiculo().getModelo() != null){
				vasilhame = this.entity.getVeiculo().getModelo().getId() == 75;
			}
			if(vasilhame){
				this.entity.setQuilometragem(0L);
				this.entity.setStatus(StatusAbastecimento.ATENDIDO);
				Date now = new Date();
				AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();
				atendimento.setBomba(this.bomba);
				atendimento.setData(now);
				atendimento.setHora(now);
				atendimento.setQuantidadeAbastecida(quantidadeAbastecida);
				atendimento.setQuilometragem(0L);
				atendimento.setUsuario(SgfUtil.usuarioLogado());
				atendimento.setStatus(StatusAtendimentoAbastecimento.ATENDIDO);
				atendimento.setAbastecimento(this.entity);
				atendimentoService.save(atendimento);
			} else if (validaQuilometragem()) {
				Double cotaAtualizada = 0.0;
				this.entity.setQuilometragem(kmAtendimento);
				this.entity.setStatus(StatusAbastecimento.ATENDIDO);
				Date now = new Date();
				AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();
				atendimento.setBomba(this.bomba);
				atendimento.setData(now);
				atendimento.setHora(now);
				atendimento.setQuantidadeAbastecida(quantidadeAbastecida);
				cotaAtualizada = this.entity.getVeiculo().getCota().getCotaDisponivel() - this.quantidadeAbastecida;
				this.entity.getVeiculo().getCota().setCotaDisponivel(cotaAtualizada);
				atendimento.setQuilometragem(kmAtendimento);
				atendimento.setUsuario(SgfUtil.usuarioLogado());
				atendimento.setStatus(StatusAtendimentoAbastecimento.ATENDIDO);
				atendimento.setAbastecimento(this.entity);
				this.cotaService.update(this.entity.getVeiculo().getCota());
				atendimentoService.save(atendimento);
			} else {
				return FAIL;
			}

		}
		return super.update();
	}

	/**
	 * Negação do abastecimento
	 * @return
	 */
	public String negarAbastecimento() {
		Date now = new Date();
		AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();
		atendimento.setData(now);
		atendimento.setHora(now);
		atendimento.setUsuario(SgfUtil.usuarioLogado());
		atendimento.setStatus(StatusAtendimentoAbastecimento.NEGADO);
		atendimento.setAbastecimento(this.entity);
		atendimentoService.save(atendimento);
		this.entity.setStatus(StatusAbastecimento.NEGADO);
		return SUCCESS;
	}

	public String confirmarNegacaoAbastecimento(){
		service.update(this.entity);
		return search();
	}

	/**
	 * Prepara tela para efetuar o atendimento do abastecimento
	 * @return
	 */
	public String atenderAbastecimento() {
		this.vasilhame = false;
		if(this.entity.getVeiculo().getModelo() != null){
			if(this.entity.getVeiculo().getModelo().getId() != 75){
				Cota cota = this.entity.getVeiculo().getCota();
				this.saldoAtual = cota.getCotaDisponivel();
				Abastecimento last = service.executeSingleResultQuery("findLast", this.entity.getVeiculo().getId());
				this.ultimaKilometragem = last.getQuilometragem();
			} else {
				this.vasilhame = true;
			}
		}

		this.bombas = new ArrayList<Bomba>();
		this.kmAtendimento = null;
		this.quantidadeAbastecida = null;
		this.bomba = null;
		this.veiculos = new ArrayList<Veiculo>();
		this.tiposServico = new ArrayList<TipoServico>();
		this.tiposServico.add(tipoServicoService.retrieve(1));

		for (Abastecimento e : this.entities) {
			this.veiculos.add(e.getVeiculo());
			this.motoristas.add(e.getMotorista());
		}

		if (SgfUtil.usuarioLogado().getPosto() != null) {
			this.bombas.addAll(bombaService.findBombaByPosto(SgfUtil.usuarioLogado().getPosto().getCodPosto()));
		}
		this.atendimento = true;
		return super.prepareView();
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<TipoServico> getTiposServico() {
		return tiposServico;
	}

	public void setTiposServico(List<TipoServico> tiposServico) {
		this.tiposServico = tiposServico;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public List<Posto> getPostos() {
		return postos;
	}

	public void setPostos(List<Posto> postos) {
		this.postos = postos;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public boolean isAutorizar() {
		return autorizar;
	}

	public void setAutorizar(boolean autorizar) {
		this.autorizar = autorizar;
	}

	public void setAtendimento(Boolean atendimento) {
		this.atendimento = atendimento;
	}

	public boolean isAtendimento() {
		return atendimento;
	}

	public boolean isAtender() {
		return atender;
	}

	public void setAtender(boolean atender) {
		this.atender = atender;
	}

	public void setBombas(List<Bomba> bombas) {
		this.bombas = bombas;
	}

	public List<Bomba> getBombas() {
		return bombas;
	}

	public Long getKmAtendimento() {
		return kmAtendimento;
	}

	public void setKmAtendimento(Long kmAtendimento) {
		this.kmAtendimento = kmAtendimento;
	}

	public Double getQuantidadeAbastecida() {
		return quantidadeAbastecida;
	}

	public void setQuantidadeAbastecida(Double quantidadeAbastecida) {
		this.quantidadeAbastecida = quantidadeAbastecida;
	}

	public Bomba getBomba() {
		return bomba;
	}

	public void setBomba(Bomba bomba) {
		this.bomba = bomba;
	}

	public void setNegar(Boolean negar) {
		this.negar = negar;
	}

	public Boolean getNegar() {
		return negar;
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

	public void setCombustiveis(List<TipoCombustivel> combustiveis) {
		this.combustiveis = combustiveis;
	}

	public List<TipoCombustivel> getCombustiveis() {
		return combustiveis;
	}

	public Long getUltimaKilometragem() {
		return ultimaKilometragem;
	}

	public void setUltimaKilometragem(Long ultimaKilometragem) {
		this.ultimaKilometragem = ultimaKilometragem;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public boolean isStart() {
		return start;
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

	public void setMostrarPosto(Boolean mostrarPosto) {
		this.mostrarPosto = mostrarPosto;
	}

	public Boolean getMostrarPosto() {
		return mostrarPosto;
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

	public StatusAbastecimento getStatus() {
		return status;
	}

	public void setStatus(StatusAbastecimento status) {
		this.status = status;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public Double getSaldoAtual() {
		return saldoAtual;
	}

	public void setSaldoAtual(Double saldoAtual) {
		this.saldoAtual = saldoAtual;
	}

	public boolean isVasilhame() {
		return vasilhame;
	}

	public void setVasilhame(boolean vasilhame) {
		this.vasilhame = vasilhame;
	}
}
