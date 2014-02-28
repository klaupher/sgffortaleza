package br.gov.ce.fortaleza.cti.sgf.bean;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.mapping.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Bomba;
import br.gov.ce.fortaleza.cti.sgf.entity.Cota;
import br.gov.ce.fortaleza.cti.sgf.entity.DiarioBomba;
import br.gov.ce.fortaleza.cti.sgf.entity.ItemRequisicao;
import br.gov.ce.fortaleza.cti.sgf.entity.Motorista;
import br.gov.ce.fortaleza.cti.sgf.entity.Multa;
import br.gov.ce.fortaleza.cti.sgf.entity.Posto;
import br.gov.ce.fortaleza.cti.sgf.entity.RequisicaoManutencao;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoLubrificante;
import br.gov.ce.fortaleza.cti.sgf.entity.SolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.relatorio.GeradorRelatorio;
import br.gov.ce.fortaleza.cti.sgf.service.AbastecimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.AtendimentoService;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.CotaService;
import br.gov.ce.fortaleza.cti.sgf.service.DiarioBombaService;
import br.gov.ce.fortaleza.cti.sgf.service.ItemRequisicaoService;
import br.gov.ce.fortaleza.cti.sgf.service.MotoristaService;
import br.gov.ce.fortaleza.cti.sgf.service.MultaService;
import br.gov.ce.fortaleza.cti.sgf.service.PostoService;
import br.gov.ce.fortaleza.cti.sgf.service.RequisicaoManutencaoService;
import br.gov.ce.fortaleza.cti.sgf.service.SolicitacaoLubrificanteService;
import br.gov.ce.fortaleza.cti.sgf.service.SolicitacaoVeiculoService;
import br.gov.ce.fortaleza.cti.sgf.service.UGService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.Constants;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.RelatorioUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.StatusSolicitacaoVeiculo;
import br.gov.ce.fortaleza.cti.sgf.util.dto.RelatorioDTO;

@Scope("session")
@Component("relatorioBean")
public class RelatorioBean extends EntityBean<Integer, RelatorioDTO> {

	private static final Log logger = LogFactory.getLog(RelatorioBean.class);

	@Autowired
	public MultaService multaService;

	@Autowired
	public MotoristaService motoristaService;

	@Autowired
	public UGService ugService;

	@Autowired
	public SolicitacaoVeiculoService solicitacaoService;

	@Autowired
	public PostoService postoService;

	@Autowired
	public DiarioBombaService diarioBombaService;

	@Autowired
	public VeiculoService veiculoService;

	@Autowired
	public AbastecimentoService abastecimentoService;

	@Autowired
	public AtendimentoService atendimentoService;

	@Autowired
	public CotaService cotaService;

	@Autowired
	public RequisicaoManutencaoService manutencaoService;

	@Autowired
	public ItemRequisicaoService itemManutencaoService;

	@Autowired
	public SolicitacaoLubrificanteService solicitacaoLubrificanteService;

	private UG orgao 					= new UG();
	private List<UG> orgaos 			= new ArrayList<UG>();
	private Posto posto;
	private List<Posto> postos;
	private Motorista motorista 		= new Motorista();
	private Veiculo veiculo;
	private String propriedadeVeiculo;
	private DiarioBomba diarioBomba;
	private List<Veiculo> veiculos 		= new ArrayList<Veiculo>();
	private List<Motorista> motoristas 	= new ArrayList<Motorista>();
	private List<Multa> multas 			= new ArrayList<Multa>();
	private List<RelatorioDTO> result 	= new ArrayList<RelatorioDTO>();
	List<SelectItem> meses 				= DateUtil.getSelectItemMeses();
	List<SelectItem> years 				= DateUtil.getSelectItemAnos();

	private boolean generate 			= false;
	private boolean allugs 				= false;
	private boolean mostrarVeiculos 	= false;
	private boolean mostrarMotoristas 	= false;
	private Integer searchId 			= 1;
	private Integer mes;
	private Integer ano;
	private String searchFlag 			= "SEARCH_UG";
	private String nomeRelatorio;
	private Date dtInicial;
	private Date dtFinal;
	private String statusAbastecimento;
	private String statusVeiculo;
	private String propriedade;
	private Double saldo;
	private String contrato;

	private final String relMotoristaPontuacao 				= "relat.motorista.pontuacao";
	private final String relDiarioBombas 					= "relat.diario.bomba";
	private final String relVeiculoMulta 					= "relat.veiculo.multa";
	private final String relConferenciaAbastecimento 		= "relat.conferencia.abastecimento";
	private final String relAbastecimentoPosto 				= "relat.abastecimento.posto";
	private final String relConsolidadoMensal 				= "relat.consolidado.mensal";
	private final String relVeiculosEmManutencao 			= "relat.veiculos.em.manutencao";
	private final String relHistoricoVeiculoManutencao 		= "relat.historico.veiculo.manutencao";
	private final String relHistoricoTrocaPneus 			= "relat.historico.troca.pneus";
	private final String relVeiculosSemRetornoManutencao 	= "relat.veiculos.sem.retorno.manutencao";
	private final String relMultasVeiculoByUG 				= "relat.multas.veiculo.ug";
	private final String relMultasVeiculos 					= "relat.multas.veiculo";
	private final String relMultasVeiculoByMotorista 		= "relat.multas.veiculo.motorista";
	private final String relSolicitacaoVeiculo 				= "relat.solicitacao.veiculo";

	private final String relInformacoesVeiculo 				= "relat.informacoes.veiculo";
	private final String relInformacoesKmsRodadosVeiculo 	= "relat.informacoes.kms.rodados.veiculo";
	private final String relTrocasLubrificantes 			= "relat.trocas.lubrificante.veiculo";
	private final String relCotasVeiculos 					= "relat.cotas.veiculos";

	private List<SelectItem> listProprietario;

	@Override
	protected RelatorioDTO createNewEntity() {
		return new RelatorioDTO();
	}

	@Override
	protected Integer retrieveEntityId(RelatorioDTO entity) {
		throw new IllegalStateException("Não implementado");
	}

	@Override
	protected BaseService<Integer, RelatorioDTO> retrieveEntityService() {
		throw new IllegalStateException("Não implementado");
	}

	public List<SelectItem> getMesList(){
		return DateUtil.getSelectItemMeses();
	}

	public List<SelectItem> getAnoList(){
		return DateUtil.getSelectItemAnos();
	}

	public String relatorioCotasVeiculos() {
		this.nomeRelatorio = this.relCotasVeiculos;
		setCurrentState(RelatorioDTO.COTAS_VEICULOS);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioTrocasLubrificante() {
		this.nomeRelatorio = this.relTrocasLubrificantes;
		setCurrentState(RelatorioDTO.TROCAS_LUBRIFICANTE);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioInformacoesVeiculos() {
		this.nomeRelatorio = this.relInformacoesVeiculo;
		setCurrentState(RelatorioDTO.INFORMACOES_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		this.orgao = null;
		VeiculoBean proprietario = new VeiculoBean();
		this.setListProprietario(proprietario.getProprietarioList());
		return SUCCESS;
	}

	public String relatorioMotoristaPontuacao() {
		this.nomeRelatorio = this.relMotoristaPontuacao;
		setCurrentState(RelatorioDTO.PONTUACAO_MOTORISTA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.generate = false;
		return SUCCESS;
	}

	public String relatorioVeiculoMulta() {
		this.nomeRelatorio = this.relVeiculoMulta;
		setCurrentState(RelatorioDTO.VEICULO_MULTA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioMultasOrgao() {
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioMultasVeiculo() {
		this.nomeRelatorio = this.relMultasVeiculos;
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioMultasMotorista() {
		this.nomeRelatorio = this.relMultasVeiculoByMotorista;
		setCurrentState(RelatorioDTO.MULTAS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioConsumoCombustivel() {
		this.nomeRelatorio = this.relConferenciaAbastecimento;
		setCurrentState(RelatorioDTO.CONFERENCIA_ABASTECIMENTO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		this.statusAbastecimento = "com";
		return SUCCESS;
	}
	
	public String relatorioConsumoPosto() {
		this.nomeRelatorio = this.relAbastecimentoPosto;
		setCurrentState(RelatorioDTO.ABASTECIMENTO_POSTO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioConsolidadoMensal() {
		this.nomeRelatorio = this.relConsolidadoMensal;
		setCurrentState(RelatorioDTO.CONSOLIDADO_MENSAL);
		setCurrentBean(currentBeanName());
		this.orgao = null;
		this.entities = null;
		return SUCCESS;
	}	

	public String relatorioSolicitacaoVeiculo(){
		this.nomeRelatorio = relSolicitacaoVeiculo;
		setCurrentState(RelatorioDTO.SOLICITACAO_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioDiarioBomba() {
		nomeRelatorio = relDiarioBombas;
		setCurrentState(RelatorioDTO.DIARIO_BOMBA);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String relatorioVeiculosEmManutencao() {
		this.nomeRelatorio = this.relVeiculosEmManutencao;
		setCurrentState(RelatorioDTO.VEICULOS_EM_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioVeiculosSemRetorno() {
		this.nomeRelatorio = this.relVeiculosSemRetornoManutencao;
		setCurrentState(RelatorioDTO.VEICULOS_SEM_RETORNO_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioHistoricoTrocaPneus() {
		this.nomeRelatorio = this.relHistoricoTrocaPneus;
		setCurrentState(RelatorioDTO.HISTORICO_TROCA_PNEUS);
		setCurrentBean(currentBeanName());
		this.entities = null;
		this.orgao = null;
		return SUCCESS;
	}

	public String relatorioHistoricoVeiculoManutencao() {
		this.veiculos = new ArrayList<Veiculo>();
		this.orgao = null;
		this.nomeRelatorio = this.relHistoricoVeiculoManutencao;
		setCurrentState(RelatorioDTO.HISTORICO_VEICULO_MANUTENCAO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}
	/**
	 * m�todos de chamada do relat�rio de kilometros rodados
	 * @return
	 */
	public String relatorioKilometrosRodadosVeiculo() {
		this.veiculos = new ArrayList<Veiculo>();
		this.orgao = null;
		this.nomeRelatorio = this.relInformacoesKmsRodadosVeiculo;
		setCurrentState(RelatorioDTO.KILOMETROS_RODADOS_VEICULO);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public boolean isRelatorioCotasVeiculosState() {
		return RelatorioDTO.COTAS_VEICULOS.equals(getCurrentState());
	}
	public boolean isRelatorioTrocasLubrificanteState() {
		return RelatorioDTO.TROCAS_LUBRIFICANTE.equals(getCurrentState());
	}

	public boolean isRelatorioInformacoesVeiculoState() {
		return RelatorioDTO.INFORMACOES_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioSolicitacaoVeiculoState() {
		return RelatorioDTO.SOLICITACAO_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioConsumoCombustivelState() {
		return RelatorioDTO.CONFERENCIA_ABASTECIMENTO.equals(getCurrentState());
	}
	
	public boolean isRelatorioConsumoPostoState() {
		return RelatorioDTO.ABASTECIMENTO_POSTO.equals(getCurrentState());
	}

	public boolean isRelatorioConsolidadoMensalState() {
		return RelatorioDTO.CONSOLIDADO_MENSAL.equals(getCurrentState());
	}

	public boolean isRelatorioDiarioBombaState() {
		return RelatorioDTO.DIARIO_BOMBA.equals(getCurrentState());
	}

	public boolean isRelatorioMotoristaPontuacaoState() {
		return RelatorioDTO.PONTUACAO_MOTORISTA.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculoMultaState() {
		return RelatorioDTO.VEICULO_MULTA.equals(getCurrentState());
	}

	public boolean isRelatorioMultasOrgaoState() {
		return RelatorioDTO.MULTAS_VEICULO.equals(getCurrentState());
	}

	public boolean isRelatorioHistoricoVeiculoManutencaoState() {
		return RelatorioDTO.HISTORICO_VEICULO_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculosSemRetornoState() {
		return RelatorioDTO.VEICULOS_SEM_RETORNO_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioVeiculosEmManutencaoState() {
		return RelatorioDTO.VEICULOS_EM_MANUTENCAO.equals(getCurrentState());
	}

	public boolean isRelatorioHistoricoTrocaPneusState() {
		return RelatorioDTO.HISTORICO_TROCA_PNEUS.equals(getCurrentState());
	}

	public boolean isRelatorioKilometrosRodadosState() {
		return RelatorioDTO.KILOMETROS_RODADOS_VEICULO.equals(getCurrentState());
	}

	public String populate() {
		if (this.searchId == 1) {
			this.motoristas = new ArrayList<Motorista>();
			this.veiculos = new ArrayList<Veiculo>();
			this.mostrarMotoristas = false;
			this.mostrarVeiculos = false;
		} else if (this.searchId == 2) {
			populateMotoristas();
			this.mostrarMotoristas = true;
			this.mostrarVeiculos = false;
		} else if (this.searchId == 3) {
			populateVeiculos();
			this.mostrarVeiculos = true;
			this.mostrarMotoristas = false;
		} else {
			this.motoristas = new ArrayList<Motorista>();
			this.veiculos = new ArrayList<Veiculo>();
			this.mostrarMotoristas = false;
			this.mostrarVeiculos = false;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		return SUCCESS;
	}

	public String populateMultas() {
		if (this.searchId == 2) {
			populateMotoristas();
		} else if (this.searchId == 3) {
			populateVeiculos();
		}
		return SUCCESS;
	}

	public String populateOption(){
		this.entities = null;
		if(this.searchFlag.equals("SEARCH_UG")){
			this.mostrarVeiculos = false;
			this.veiculos = null;
		} else {
			this.mostrarVeiculos = true;
			this.veiculos = veiculoService.findAll();
		}
		return SUCCESS;
	}

	public String populateMotoristas() {
		User user = SgfUtil.usuarioLogado();
		if (this.orgao != null) {
			this.motoristas = motoristaService.findByUG(orgao.getId());
		} else {
			if(SgfUtil.isAdministrador(user)){
				this.motoristas = motoristaService.retrieveAll();
			}
		}
		return SUCCESS;
	}

	public String populateVeiculos() {
		this.entities = new ArrayList<RelatorioDTO>();
		User user = SgfUtil.usuarioLogado();
		this.veiculos = new ArrayList<Veiculo>();
		if (this.orgao != null) {
			this.veiculos = veiculoService.findByUG(this.orgao);
		} else {
			if(SgfUtil.isAdministrador(user)){
				this.veiculos = veiculoService.retrieveAll();
			}
		}
		Collections.sort(this.veiculos, new Comparator<Veiculo>() {
			public int compare(Veiculo o1, Veiculo o2) {
				return o1.getPlaca().compareTo(o2.getPlaca());
			}
		});
		return SUCCESS;
	}

	public String consultarCotasVeiculos(){

		this.entities = new ArrayList<RelatorioDTO>();
		List<UG> ugs = new ArrayList<UG>();
		List<Cota> cotas;
		if(this.orgao != null){
			ugs.add(this.orgao);
		} else {
			ugs.addAll(ugService.retrieveAll());
		}

		Double total = 0D;
		for (UG ug : ugs) {
			cotas = cotaService.retrieveCotasVeiculosByUG(ug.getId());
			if(cotas != null && cotas.size() > 0){
				RelatorioDTO relatorio = new RelatorioDTO();
				Double somaCotas = 0D;
				relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
				for (Cota cota : cotas) {
					RelatorioDTO rel = new RelatorioDTO();
					rel.setCotaVeiculo(cota);
					rel.setOrgao(ug);
					somaCotas += cota.getCota();
					relatorio.getRelatorios().add(rel);
				}
				relatorio.setOrgao(ug);
				relatorio.setCotaSoma(somaCotas);
				total += somaCotas;
				this.entities.add(relatorio);
			}
		}
		return SUCCESS;
	}


	public String consultarTrocasLubrificanteVeiculos(){

		this.entities = new ArrayList<RelatorioDTO>();
		List<RelatorioDTO> result = new ArrayList<RelatorioDTO>();
		Map<String, List<SolicitacaoLubrificante>> solicitacoesLubrificanteMap = null;

		// status = 1 => solicitado
		// status = 2 => atendido

		Date begin = DateUtil.getDateStartDay( this.dtInicial);
		Date end = DateUtil.getDateEndDay(this.dtFinal);

		if(this.orgao != null){
			solicitacoesLubrificanteMap = solicitacaoLubrificanteService.retrieveSolicLubrificanteMap(2, null, null, begin, end, this.orgao);
		} else {
			solicitacoesLubrificanteMap = solicitacaoLubrificanteService.retrieveSolicLubrificanteMap(2, null, null, begin, end, null);
		}

		for (String key : solicitacoesLubrificanteMap.keySet()) {
			List<SolicitacaoLubrificante> solicitacoes = solicitacoesLubrificanteMap.get(key);
			UG orgao = ugService.retrieve(key);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setOrgao(orgao);
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			List<RelatorioDTO> relatorios = new ArrayList<RelatorioDTO>();
			for (SolicitacaoLubrificante sol : solicitacoes) {
				RelatorioDTO rel = new RelatorioDTO();
				rel.setOrgao(orgao);
				rel.setVeiculo(sol.getVeiculo());
				rel.setSolicitacaoLubrificante(sol);
				rel.setDtInicial(this.dtInicial);
				rel.setDtFinal(this.dtFinal);
				relatorios.add(rel);
			}
			relatorio.getRelatorios().addAll(relatorios);
			result.add(relatorio);
		}
		this.entities.addAll(result);
		return SUCCESS;
	}

	public String consultarSolicitacaoVeiculo(){
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		Date begin = DateUtil.getDateStartDay(this.dtInicial);
		Date end = DateUtil.getDateEndDay(this.dtFinal);
		this.entities = new ArrayList<RelatorioDTO>();
		List<SolicitacaoVeiculo> solicitacoes;
		if(this.orgao != null){
			solicitacoes = solicitacaoService.findSolVeiculoOrgao(begin, end, this.orgao, StatusSolicitacaoVeiculo.FINALIZADO);
		} else {
			solicitacoes = solicitacaoService.findSolVeiculoOrgao(begin, end, null, StatusSolicitacaoVeiculo.FINALIZADO);
		}
		Map<UG, Map<Veiculo, List<SolicitacaoVeiculo>>> map = new HashMap<UG, Map<Veiculo,List<SolicitacaoVeiculo>>>();
		if(solicitacoes.size() > 0){
			for (SolicitacaoVeiculo solicitacao : solicitacoes) {
				UG ug = solicitacao.getVeiculo().getUa().getUg();
				if(map.containsKey(ug)){
					Map<Veiculo, List<SolicitacaoVeiculo>>  mapVeiculo = map.get(ug);
					Veiculo veiculo = solicitacao.getVeiculo();
					if(mapVeiculo.containsKey(veiculo)){
						mapVeiculo.get(veiculo).add(solicitacao);
					} else {
						List<SolicitacaoVeiculo> list = new ArrayList<SolicitacaoVeiculo>();
						list.add(solicitacao);
						mapVeiculo.put(veiculo, list);
					}
				} else {
					Map<Veiculo, List<SolicitacaoVeiculo>>  mapVeiculo = new HashMap<Veiculo, List<SolicitacaoVeiculo>>();
					Veiculo veiculo = solicitacao.getVeiculo();
					List<SolicitacaoVeiculo> list = new ArrayList<SolicitacaoVeiculo>();
					list.add(solicitacao);
					mapVeiculo.put(veiculo, list);
					map.put(ug, mapVeiculo);
				}
			}
			for (UG ug : map.keySet()) {
				RelatorioDTO relatorio = new RelatorioDTO();
				relatorio.setOrgao(ug);
				relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
				Map<Veiculo, List<SolicitacaoVeiculo>> mapVeiculo = map.get(ug);
				for (Veiculo veiculo : mapVeiculo.keySet()) {
					RelatorioDTO rel = new RelatorioDTO();
					rel.setVeiculo(veiculo);
					rel.setOrgao(ug);
					rel.setRelatorios(new ArrayList<RelatorioDTO>());
					List<SolicitacaoVeiculo> result = mapVeiculo.get(veiculo);
					for (SolicitacaoVeiculo s : result) {
						RelatorioDTO dto = new RelatorioDTO();
						dto.setSolicitacaoVeiculo(s);
						dto.setVeiculo(veiculo);
						dto.setOrgao(ug);
						dto.setDtInicial(begin);
						dto.setDtFinal(end);
						rel.getRelatorios().add(dto);
						this.result.add(dto);
					}
					relatorio.getRelatorios().add(rel);
				}
				this.entities.add(relatorio);
			}
		}
		return SUCCESS;
	}

	public String consultarDiarios() {
		this.entities = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.dtInicial = DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal = DateUtil.getDateEndDay(this.dtFinal);
		this.postos = new ArrayList<Posto>();
		if(this.posto != null){
			this.postos.add(this.posto);
		} else {
			this.postos = diarioBombaService.findDiariosPeriodoPosto(this.dtInicial, this.dtFinal);
		}

		for (Posto posto : postos) {
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setPosto(this.posto);
			List<RelatorioDTO> relatorios = new ArrayList<RelatorioDTO>();
			List<DiarioBomba> diarioBombas = diarioBombaService.findByPeriodoPosto(this.dtInicial, this.dtFinal, posto);
			relatorio.getPosto().setDiarioBombas(diarioBombas);

			Float totalLitrosAbastecidos = 0F;
			for (DiarioBomba diaria : diarioBombas) {
				RelatorioDTO dto = new RelatorioDTO();
				//Float litrosAbastecidos = 0F;
				if(diaria.getValorFinal() != null && diaria.getValorInicial() != null){
					//					if(diaria.getValorFinal() < diaria.getValorInicial()){
					//						if(diaria.getValorFinal() < Constants.LIMITE_INFERIOR_BOMBACOMBUSTIVEL && diaria.getValorInicial() > Constants.LIMITE_SUPERIOR_BOMBACOMBUSTIVEL){
					//							litrosAbastecidos =  ((Constants.VALOR_MAXIMO_BOMBACOMBUSTIVEL - diaria.getValorInicial()) + diaria.getValorFinal());
					//							diaria.setLitrosAbastecidos(litrosAbastecidos);
					//							totalLitrosAbastecidos += litrosAbastecidos;
					//						}
					//					} else {
					//						litrosAbastecidos = diaria.getValorFinal() - diaria.getValorInicial();
					//						diaria.setLitrosAbastecidos(litrosAbastecidos);
					//						totalLitrosAbastecidos += litrosAbastecidos;
					//					}
					diaria.setLitrosAbastecidos(diaria.getQuantidadeSaida());
					//litrosAbastecidos += diaria.getQuantidadeSaida();
					System.out.println("quant. = " + diaria.getQuantidadeSaida());
					if(diaria.getQuantidadeSaida() == null){
						System.out.println("erro");
					}else {
						totalLitrosAbastecidos += diaria.getQuantidadeSaida();
					}
				}
				dto.setDiarioBomba(diaria);
				dto.setPosto(posto);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				relatorios.add(dto);
			}
			relatorio.getRelatorios().addAll(relatorios);
			relatorio.setConsumoPosto(totalLitrosAbastecidos);
			this.entities.add(relatorio);
		}
		return SUCCESS;
	}

	public String consultarMultasVeiculo() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		if (this.searchFlag.equals("SEARCH_VEICULO")) {
			consultarMultasPorVeiculo();
		} else if (this.searchFlag.equals("SEARCH_MOTORISTA")) {
			consultarMultasPorMotorista();
		} else if (this.searchFlag.equals("SEARCH_UG")) {
			consultarMultasPorOrgao();
		}
		return SUCCESS;
	}

	public String consultarMultasPorOrgao() {
		this.nomeRelatorio = this.relMultasVeiculoByUG;
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<Multa> multas =  multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		if(multas != null && multas.size() > 0){
			Map<UG, List<Multa>> map = new HashMap<UG, List<Multa>>();
			for (Multa m : multas) {
				UG ug = null;  
				if(m.getVeiculo().getUa() != null){
					ug = m.getVeiculo().getUa().getUg();
				}
				if(map.containsKey(ug) == false){
					List<Multa> list = new ArrayList<Multa>();
					list.add(m);
					map.put(ug, list);
				} else {
					map.get(ug).add(m);
				}
			}
			for (UG ug : map.keySet()) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setOrgao(ug);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setRelatorios(new ArrayList<RelatorioDTO>());
				List<Multa> result = map.get(ug);
				for(Multa multa: result){
					RelatorioDTO rel = new RelatorioDTO();
					rel.setMulta(multa);
					rel.setOrgao(ug);
					rel.setDtInicial(this.dtInicial);
					rel.setDtFinal(this.dtFinal);
					dto.getRelatorios().add(rel);
				}
				this.entities.add(dto);
				this.result.addAll(dto.getRelatorios());
			}
		}
		return SUCCESS;
	}


	public String consultarMultasPorVeiculo() {
		this.nomeRelatorio = this.relMultasVeiculos;
		List<Multa> multas = null;
		this.result = new ArrayList<RelatorioDTO>();
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.veiculo != null){
			multas = multaService.findByVeiculo(this.veiculo, null, this.dtInicial, this.dtFinal);
		} else {
			multas = multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		}
		Map<Veiculo, List<Multa>> map = new HashMap<Veiculo, List<Multa>>();
		for (Multa m : multas) {
			Veiculo v = m.getVeiculo();
			if(map.containsKey(v) == false){
				List<Multa> list = new ArrayList<Multa>();
				list.add(m);
				map.put(v, list);
			} else {
				map.get(v).add(m);
			}
		}

		for (Veiculo v : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setVeiculo(v);
			dto.setDtInicial(this.dtInicial);
			dto.setDtFinal(this.dtFinal);
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Multa> result = map.get(v);
			for(Multa multa: result){
				RelatorioDTO rel = new RelatorioDTO();
				rel.setMulta(multa);
				rel.setVeiculo(v);
				rel.setPeriodo(this.dtInicial);
				rel.setDtInicial(this.dtInicial);
				rel.setDtFinal(this.dtFinal);
				dto.getRelatorios().add(rel);
			}
			this.result.addAll(dto.getRelatorios());
			this.entities.add(dto);
		}
		return SUCCESS;
	}

	public String consultarMultasPorMotorista() {
		this.nomeRelatorio = this.relMultasVeiculoByMotorista;
		List<Multa> multas = null;
		this.result = new ArrayList<RelatorioDTO>();
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.motorista != null){
			multas = multaService.findByMotorista(this.motorista, null, this.dtInicial, this.dtFinal);
		} else {
			multas = multaService.findByUG(this.orgao, this.dtInicial, this.dtFinal);
		}
		Map<Motorista, List<Multa>> map = new HashMap<Motorista, List<Multa>>();
		for (Multa m : multas) {
			Motorista motorista = m.getMotorista();
			if(motorista != null){
				if(map.containsKey(motorista) == false){
					List<Multa> list = new ArrayList<Multa>();
					list.add(m);
					map.put(motorista, list);
				} else {
					map.get(motorista).add(m);
				}
			}
		}

		for (Motorista m : map.keySet()) {
			RelatorioDTO dto = new RelatorioDTO();
			dto.setMotorista(m);
			dto.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Multa> result = map.get(m);
			for(Multa multa: result){
				RelatorioDTO rel = new RelatorioDTO();
				rel.setMulta(multa);
				rel.setPeriodo(this.dtInicial);
				rel.setMotorista(multa.getMotorista());
				dto.getRelatorios().add(rel);
			}
			this.result.addAll(dto.getRelatorios());
			this.entities.add(dto);
		}
		return SUCCESS;
	}
	
	/**
	 * Consulta os consumos de combustível de veículos por orgão por período
	 * Caso o mês selecionado esteja nulo(TODOS), será consultado o ano inteiro
	 * Caso o orgão selecionado seja nulo(TODOS), será consultados todos os ôrgãos
	 * @return
	 */
	public String consultarConsumoCombustivel() {

		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}

		this.dtInicial 	= DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal 	= DateUtil.getDateEndDay(this.dtFinal);

		Map<UG, List<AtendimentoAbastecimento>> hashAtendimentosUg = new HashMap<UG, List<AtendimentoAbastecimento>>();
		
		if(this.statusAbastecimento.equals("com")) {
			hashAtendimentosUg = atendimentoService.findHashAbastecimentosVeiculo(this.orgao, this.posto, null, this.dtInicial, this.dtFinal);
		}else {
			hashAtendimentosUg = atendimentoService.findHashVeiculoSemAbastecimento(this.orgao, this.posto, null, this.dtInicial, this.dtFinal);
		}
		

		this.entities 				= new ArrayList<RelatorioDTO>();
		Float consumoTotalUg 		= 0F;
		Float consumoTotalGasolina 	= 0F;
		Float consumoTotalEtanol 	= 0F;
		Float consumoTotalDiesel 	= 0F;
		
		for (UG ug : hashAtendimentosUg.keySet()) {

			List<AtendimentoAbastecimento> abastecimentosUg = hashAtendimentosUg.get(ug);
			RelatorioDTO relatorioUg = new RelatorioDTO();
			relatorioUg.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorioUg.setOrgao(ug);
			consumoTotalUg 			= 0F;
			consumoTotalGasolina 	= 0F;
			consumoTotalEtanol 		= 0F;
			
			Map<Veiculo, List<AtendimentoAbastecimento>> atendimentosVeiculo 	= new HashMap<Veiculo, List<AtendimentoAbastecimento>>();
			
			for (AtendimentoAbastecimento atendimento : abastecimentosUg) {

				Veiculo veiculo = atendimento.getAbastecimento().getVeiculo();
				
				if(atendimentosVeiculo.containsKey(veiculo)){
					atendimentosVeiculo.get(veiculo).add(atendimento);
				} else {
					List<AtendimentoAbastecimento> newlist = new ArrayList<AtendimentoAbastecimento>();
					newlist.add(atendimento);
					atendimentosVeiculo.put(veiculo, newlist);
				}
			}

			for (Veiculo veiculo : atendimentosVeiculo.keySet()) {

				Float cota = (veiculo.getCota() == null) ? 0F : veiculo.getCota().getCota().floatValue();
				
				RelatorioDTO relatorioVeiculo = new RelatorioDTO();
				relatorioVeiculo.setOrgao(ug);
				relatorioVeiculo.setVeiculo(veiculo);
				relatorioVeiculo.setCota(cota);
				relatorioVeiculo.setRelatorios(new ArrayList<RelatorioDTO>());
				Float total = 0F;
				Float totalgas = 0F;
				Float totaletan = 0F;
				Float totaldiesel = 0F;

				List<AtendimentoAbastecimento> abastecimentosVeiculos = atendimentosVeiculo.get(veiculo);
				for (AtendimentoAbastecimento atendimento : abastecimentosVeiculos) {
					RelatorioDTO item = new RelatorioDTO();
					total += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0;

					if(atendimento.getAbastecimento().getCombustivel().getId() == 1){
						totalgas += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 2){
						totaletan += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 3){
						totaldiesel += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}

					item.setOrgao(ug);
					item.setVeiculo(veiculo);
					item.setAtendimento(atendimento);
					item.setAbastecimento(atendimento.getAbastecimento());
					item.setMotorista(atendimento.getAbastecimento().getMotorista());
					item.setConsumo(atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() :  0F);
					item.setStatus(atendimento.getStatus().toString());
					item.setCota(cota);
					item.setSaldoCota(cota - total);
					item.setSaldoFinal(cota - total);
					item.setDataAtendimento(DateUtil.parseAsString("dd/MM/yyyy", atendimento.getData()));
					item.setHoraAtendimento(DateUtil.parseAsString("HH:mm", atendimento.getHoraAtendimento()));
					item.setKmAtual(atendimento.getQuilometragem() != null ? atendimento.getQuilometragem().intValue() : 0L);
					relatorioVeiculo.setConsumoTotal(relatorioVeiculo.getConsumoTotal() + item.getConsumo());
					relatorioVeiculo.getRelatorios().add(item);
				}

				relatorioVeiculo.setNumeroAbastecimentos(abastecimentosVeiculos.size());
				relatorioUg.setConsumoTotal(total);
				relatorioUg.getRelatorios().add(relatorioVeiculo);

				consumoTotalUg += total;
				consumoTotalGasolina += totalgas;
				consumoTotalEtanol += totaletan;
				consumoTotalDiesel += totaldiesel;
				total = 0F;
			}

			relatorioUg.setConsumoCombustivelOrgao(consumoTotalUg);
			relatorioUg.setConsumoGasolina(consumoTotalGasolina);
			relatorioUg.setConsumoEtanol(consumoTotalEtanol);
			this.entities.add(relatorioUg);

		}
		return SUCCESS;
	}
	
	/**
	 * Consulta os consumos de combustível de veículos por orgão por período
	 * Caso o mês selecionado esteja nulo(TODOS), será consultado o ano inteiro
	 * Caso o orgão selecionado seja nulo(TODOS), será consultados todos os ôrgãos
	 * @return
	 */
	public String consultarConsumoPosto() {

		// Validando o intervalo de datas
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}

		// Atribuindo os valores de datas
		this.dtInicial 	= DateUtil.getDateStartDay(this.dtInicial);
		this.dtFinal 	= DateUtil.getDateEndDay(this.dtFinal);
		
		this.entities 				= new ArrayList<RelatorioDTO>();
		Float consumoTotalPosto		= 0F;
		Float consumoTotalUg 		= 0F;
		Float consumoTotalGasolina 	= 0F;
		Float consumoTotalEtanol 	= 0F;
		Float consumoTotalDiesel 	= 0F;
		
		/*
		 * Postos
		 */
		Map<Posto, List<AtendimentoAbastecimento>> hashAtendimentosPosto 	= new HashMap<Posto, List<AtendimentoAbastecimento>>();
		hashAtendimentosPosto 												= atendimentoService.findHashAbastecimentosPosto(this.orgao, this.posto, null, this.dtInicial, this.dtFinal);

		for(Posto posto : hashAtendimentosPosto.keySet()) {
			
			List<AtendimentoAbastecimento> abastecimentosPosto 	= hashAtendimentosPosto.get(posto);
			RelatorioDTO relatorioPosto 						= new RelatorioDTO();
			relatorioPosto.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorioPosto.setPosto(posto);
			relatorioPosto.setConsumo(0F);
			
			/*
			 * Órgãos
			 */
			Map<UG,RelatorioDTO> atendimentosUg 	= new HashMap<UG, RelatorioDTO>();
			// Verifica os Órgão que abasteceram no posto
			for(AtendimentoAbastecimento atendimento : abastecimentosPosto) {
				
				UG ugAtendimento = atendimento.getAbastecimento().getVeiculo().getUa().getUg();

				if(!(atendimentosUg.containsKey(ugAtendimento))) {
					
					RelatorioDTO relatorioUg = new RelatorioDTO();
					relatorioUg.setOrgao(ugAtendimento);
					relatorioUg.setRelatorios(new ArrayList<RelatorioDTO>());
					relatorioUg.setConsumoCombustivelOrgao(0F);
					atendimentosUg.put(ugAtendimento, relatorioUg);
				}
			}
			
			Map<Veiculo, List<AtendimentoAbastecimento>> atendimentosVeiculo 	= new HashMap<Veiculo, List<AtendimentoAbastecimento>>();
			for(AtendimentoAbastecimento atendimento : abastecimentosPosto) {
				
				Veiculo veiculo = atendimento.getAbastecimento().getVeiculo();
				if(atendimentosVeiculo.containsKey(veiculo)) {
					atendimentosVeiculo.get(veiculo).add(atendimento);
				} else {
					List<AtendimentoAbastecimento> novaLista = new ArrayList<AtendimentoAbastecimento>();
					novaLista.add(atendimento);
					atendimentosVeiculo.put(veiculo, novaLista);
				}
			}
			
			for(Veiculo veiculo : atendimentosVeiculo.keySet()) {
				
				RelatorioDTO relatorioVeiculo = new RelatorioDTO();
				relatorioVeiculo.setOrgao(veiculo.getUa().getUg());
				relatorioVeiculo.setVeiculo(veiculo);
				relatorioVeiculo.setCota(veiculo.getCota().getCota().floatValue());
				relatorioVeiculo.setRelatorios(new ArrayList<RelatorioDTO>());
				// limpa as variaveis
				Float total = 0F;
				Float totalgas = 0F;
				Float totaletan = 0F;
				Float totaldiesel = 0F;
				
				List<AtendimentoAbastecimento> abastecimentosVeiculos = atendimentosVeiculo.get(veiculo);
				for(AtendimentoAbastecimento atendimento : abastecimentosVeiculos) {
					
					RelatorioDTO item = new RelatorioDTO();
					total += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0;
					
					if(atendimento.getAbastecimento().getCombustivel().getId() == 1){
						totalgas += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 2){
						totaletan += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 3){
						totaldiesel += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0; 
					}
					
					Float cota = veiculo.getCota() != null ? veiculo.getCota().getCota().floatValue() : 0F;
					item.setOrgao(veiculo.getUa().getUg());
					item.setVeiculo(veiculo);
					item.setAtendimento(atendimento);
					item.setAbastecimento(atendimento.getAbastecimento());
					item.setMotorista(atendimento.getAbastecimento().getMotorista());
					item.setConsumo(atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() :  0F);
					item.setStatus(atendimento.getStatus().toString());
					item.setCota(cota);
					item.setSaldoCota(cota - total);
					item.setSaldoFinal(cota - total);
					item.setDataAtendimento(DateUtil.parseAsString("dd/MM/yyyy", atendimento.getData()));
					item.setHoraAtendimento(DateUtil.parseAsString("HH:mm", atendimento.getHoraAtendimento()));
					item.setKmAtual(atendimento.getQuilometragem() != null ? atendimento.getQuilometragem().intValue() : 0L);
					relatorioVeiculo.setConsumoTotal(relatorioVeiculo.getConsumoTotal() + item.getConsumo());
					relatorioVeiculo.getRelatorios().add(item);
				}
				
				relatorioVeiculo.setNumeroAbastecimentos(abastecimentosVeiculos.size());
				
				if(atendimentosUg.containsKey(veiculo.getUa().getUg())) {
					
					Float totalUg = atendimentosUg.get(veiculo.getUa().getUg()).getConsumoCombustivelOrgao();
					Float totalgasUg = atendimentosUg.get(veiculo.getUa().getUg()).getConsumoGasolina();
					Float totaletanUg = atendimentosUg.get(veiculo.getUa().getUg()).getConsumoEtanol();
					Float totaldieselUg = atendimentosUg.get(veiculo.getUa().getUg()).getConsumoDiesel();
					
					atendimentosUg.get(veiculo.getUa().getUg()).setConsumoCombustivelOrgao(totalUg + total);
					atendimentosUg.get(veiculo.getUa().getUg()).setConsumoGasolina(totalgasUg + totalgas);
					atendimentosUg.get(veiculo.getUa().getUg()).setConsumoEtanol(totaletanUg + totaletan);
					atendimentosUg.get(veiculo.getUa().getUg()).setConsumoDiesel(totaldieselUg + totaldiesel);
					atendimentosUg.get(veiculo.getUa().getUg()).getRelatorios().add(relatorioVeiculo);
					
				}
			}

			Iterator it = atendimentosUg.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				RelatorioDTO rel = (RelatorioDTO) pairs.getValue();
				
				relatorioPosto.setConsumo(relatorioPosto.getConsumo() + rel.getConsumoCombustivelOrgao());
				relatorioPosto.getRelatorios().add(rel);
			}

			this.entities.add(relatorioPosto);
		}
		return SUCCESS;
	}

	/**
	 * Realiza a consulta dos abastecimentos, fazendo o resumo do mensal
	 * Caso seja selecionado todos os meses, será feita a consulta em todos os meses
	 * Caso seja selecionado todos os órgãos, será feita a consulta para todos os órgãos
	 * @return
	 */

	@SuppressWarnings("static-access")
	public String consultarConsolidadoMensal() {

		
		if(this.mes != null){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());

			calendar.set(Calendar.YEAR, this.ano);
			calendar.set(Calendar.MONTH, this.mes);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			this.dtInicial = DateUtil.getDateStartDay(calendar.getTime());

			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 0);

			this.dtFinal = DateUtil.getDateEndDay(calendar.getTime());

			if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
				JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
				return FAIL;
			}

		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, 0); // JANEIRO
			this.dtInicial = DateUtil.getDateStartDay(calendar.getTime());

			calendar.set(Calendar.MONTH, 11); // DEZEMBRO
			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
			this.dtFinal = DateUtil.getDateEndDay(calendar.getTime());
		}

		this.entities = new ArrayList<RelatorioDTO>();

		/*
		 * Constroi um hash dos abastecimentos do orgão ou órgão
		 */
		Map<UG, List<AtendimentoAbastecimento>> hashAtendimentosUg = new HashMap<UG, List<AtendimentoAbastecimento>>();
		
		UG orgao 			= this.orgao != null ? this.orgao : null;
		Posto posto			= this.posto != null ? this.posto : null;
		hashAtendimentosUg 	= atendimentoService.findHashAbastecimentosVeiculo(orgao, null, null, this.dtInicial, this.dtFinal);


		for (UG ug : hashAtendimentosUg.keySet()) {
			Float consumoOrgao = 0F;
			Float consumoEtanol = 0F;
			Float consumoGasolina = 0F;
			Float consumoDiesel = 0F;
			RelatorioDTO novo = new RelatorioDTO();
			novo.setRelatorios(new ArrayList<RelatorioDTO>());
			novo.setOrgao(ug);
			List<AtendimentoAbastecimento> abastecimentosUg = hashAtendimentosUg.get(ug);

			/*
			 * Constroi hashs de abastecimento por veículo 
			 */
			Map<Veiculo, List<AtendimentoAbastecimento>> atendimentosVeiculo = new HashMap<Veiculo, List<AtendimentoAbastecimento>>();
			for (AtendimentoAbastecimento atendimento : abastecimentosUg) {

				Veiculo veiculo = atendimento.getAbastecimento().getVeiculo();
				if(atendimentosVeiculo.containsKey(veiculo)){
					atendimentosVeiculo.get(veiculo).add(atendimento);
				} else {
					List<AtendimentoAbastecimento> newlist = new ArrayList<AtendimentoAbastecimento>();
					newlist.add(atendimento);
					atendimentosVeiculo.put(veiculo, newlist);
				}
			}

			/*
			 * Constroi saída para relatório dos abastecimentos do veículos
			 */
			for (Veiculo veiculo : atendimentosVeiculo.keySet()) {

				Float total = 0F;
				Float totalGasolina = 0F;
				Float totalEtanol = 0F;
				Float totalDiesel = 0F;
				Long kmInicial = 0L;
				Long kmFinal = 0L;
				int nrAbastecimentos = 0;
				RelatorioDTO dto = new RelatorioDTO();
				dto.setOrgao(ug);
				dto.setRelatorios(new ArrayList<RelatorioDTO>());
				dto.setVeiculo(veiculo);
				/*
				 * lista de atendimentos do veículo corrente
				 */
				List<AtendimentoAbastecimento> atendimentos =  atendimentosVeiculo.get(veiculo);
				//atendimentoService.findAtendimentoByVeiculoAbastecimento(ug.getId(), veiculo.getId().toString(), this.dtInicial, this.dtFinal);
				for (AtendimentoAbastecimento atendimento : atendimentos) {


					if(atendimento.getAbastecimento().getCombustivel().getId() == 1){
						totalGasolina += atendimento.getQuantidadeAbastecida().floatValue();
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 2){
						totalEtanol += atendimento.getQuantidadeAbastecida().floatValue();
					}
					if(atendimento.getAbastecimento().getCombustivel().getId() == 3){
						totalDiesel += atendimento.getQuantidadeAbastecida().floatValue();
					}

					total += atendimento.getQuantidadeAbastecida() != null ? atendimento.getQuantidadeAbastecida().floatValue() : 0F;
					Long aux = (atendimento.getQuilometragem() == null)? 0L : atendimento.getQuilometragem();
					if(kmInicial == 0F){
						kmInicial = aux;
					}
					if(atendimento.getQuilometragem() != null){
						kmFinal = atendimento.getQuilometragem();
					}
					nrAbastecimentos++;
					dto.setCombustivel(atendimento.getAbastecimento().getCombustivel().getDescricao());
				}

				/*
				 * consulta cota do veículo	
				 */
				Cota cota = cotaService.retrieveVeiculoCota(veiculo);
				if(cota != null){
					dto.setCota(cota.getCota().floatValue());
					dto.setSaldoCota(cota.getCota().floatValue() - total);
				} else {
					dto.setCota(0F);
				}
				Float consumo = 0F;
				dto.setConsumo(total);
				dto.setKmRodados(kmFinal - kmInicial);

				if(total == 0){
					consumo = 0F;
				} else {
					
					consumo = (kmFinal - kmInicial)/total;
					if(consumo < 0)
						System.out.println("km ini = " +kmInicial + "  km fim  = " +  kmFinal + " consumo = " + (kmFinal - kmInicial));
				}

				dto.setKmPorLitro(Math.abs(consumo));
				dto.setNumeroAbastecimentos(nrAbastecimentos);
				consumoOrgao += total;
				consumoGasolina += totalGasolina;
				consumoEtanol += totalEtanol;
				consumoDiesel += totalDiesel;

				novo.getRelatorios().add(dto);
			}
			novo.setConsumoCombustivelOrgao(consumoOrgao);
			novo.setConsumoGasolina(consumoGasolina);
			novo.setConsumoEtanol(consumoEtanol);
			novo.setConsumoDiesel(consumoDiesel);

			this.entities.add(novo);
		}
		return SUCCESS;
	}

	public String consultarPontuacaoMotorista() {
		List<Motorista> motoristas = null;
		this.entities = new ArrayList<RelatorioDTO>();
		if(this.orgao != null){
			this.orgaos = new ArrayList<UG>();
			motoristas = motoristaService.findByUG(orgao.getId());
			for (Motorista motorista : motoristas) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setOrgao(this.orgao);
				motorista.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
				dto.setMotorista(motorista);
				this.entities.add(dto);
			}
			this.orgao.setMotoristas(motoristas);
			this.orgaos.add(this.orgao);
		}else{
			orgaos = motoristaService.findMotoristaOrgao();
			if(orgaos.size() > 0){

				for (UG ug : this.orgaos) {
					motoristas = motoristaService.findByOrgao(ug);
					for (Motorista motorista : motoristas) {
						RelatorioDTO dto = new RelatorioDTO();
						dto.setOrgao(ug);
						motorista.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
						dto.setMotorista(motorista);
						dto.setStatus(motorista.getPontosCnh() == 20 ? "Atingiu 20pt" : motorista.getPontosCnh() > 20 ? "Suspensa" : "OK");
						ug.setMotoristas(motoristas);
						this.entities.add(dto);
					}
				}
			} else {
				this.entities = new ArrayList<RelatorioDTO>();
			}
		}
		return SUCCESS;
	}


	public String consultarHistoricoVeiculoManutencao() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();

		List<UG> orgaos = null;
		if(this.orgao == null && this.veiculo == null){
			orgaos = manutencaoService.findUgsByManutencao(null, null, this.dtInicial, this.dtFinal);
		}
		if(this.orgao != null && this.veiculo == null){
			this.searchId = 2;
			orgaos = manutencaoService.findUgsByManutencao(this.orgao.getId(), null, DateUtil.getDateStartDay(this.dtInicial), DateUtil.getDateEndDay(this.dtFinal));
		} else if(this.orgao != null && this.veiculo != null){
			this.searchId = 1;
			orgaos = manutencaoService.findUgsByManutencao(this.orgao.getId(), this.veiculo.getId(), this.dtInicial, this.dtFinal);
		}

		for (UG ug : orgaos) {
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(ug);
			List<Veiculo> veiculos = manutencaoService.findVeiculosUGByManutencao(ug.getId(), null, this.dtInicial, this.dtFinal);
			ug.setVeiculos(veiculos);
			for (Veiculo veiculo : veiculos) {
				relatorio.setVeiculo(veiculo);
				List<RequisicaoManutencao> manutencoes = manutencaoService.findManutencaoVeiculos(ug.getId(), veiculo.getId(), this.dtInicial, this.dtFinal);
				veiculo.setManutencoes(manutencoes);
				for (RequisicaoManutencao manutencao : manutencoes) {
					RelatorioDTO dto = new RelatorioDTO();
					dto.setOrgao(ug);
					dto.setVeiculo(veiculo);
					dto.setManutencao(manutencao);
					if(manutencao.getDataRetorno() != null && manutencao.getDataSaida() != null){
						dto.setDuracaoManutencao(DateUtil.tempoEntreDatas(manutencao.getDataRetorno(), manutencao.getDataSaida()));
					}
					List<ItemRequisicao> itens = manutencaoService.findItensRequisicao(manutencao.getId());
					manutencao.setItensRequisicao(itens);
					relatorio.setItensManutencao(itens);
					Float manuValorTotal = 0F;
					for (ItemRequisicao item : itens) {
						manuValorTotal += item.getValorTotal();
					}
					veiculo.setValorTotal(manuValorTotal);
					relatorio.getRelatorios().add(dto);
				}				
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}

		return SUCCESS;
	}

	public String consultarVeiculosSemRetorno() {
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<RequisicaoManutencao> result = null;
		if(this.orgao != null){
			result = manutencaoService.findManutencaoSemRetorno(this.orgao, this.dtInicial, this.dtFinal);
		} else {
			result = manutencaoService.findManutencaoSemRetorno(this.orgao, DateUtil.getDateStartDay(this.dtInicial), 
					DateUtil.getDateEndDay(this.dtFinal));
		}
		Map<UG, List<RequisicaoManutencao>> map = new HashMap<UG, List<RequisicaoManutencao>>();
		for (RequisicaoManutencao req : result) {
			UG ug  = req.getVeiculo().getUa().getUg();
			if(!map.containsKey(ug)){
				List<RequisicaoManutencao> list = new ArrayList<RequisicaoManutencao>();
				list.add(req);
				map.put(ug, list);
			} else {
				map.get(ug).add(req);
			}
		}
		for (UG ug : map.keySet()) {
			List<RequisicaoManutencao> list = map.get(ug);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(ug);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			Date dtAtual = new Date();
			for (RequisicaoManutencao m : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(m);
				dto.setOrgao(ug);
				dto.setAtraso(DateUtil.tempoEntreDatas(dtAtual, m.getDataFim()));
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	public String consultarVeiculosEmManutencao() {
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		List<RequisicaoManutencao> result = null;
		if(this.orgao != null){
			result = manutencaoService.findByUgStatus(this.orgao, this.dtInicial, this.dtFinal);
		} else {
			result = manutencaoService.findByUgStatus(this.orgao, DateUtil.getDateStartDay(this.dtInicial), DateUtil.getDateEndDay(this.dtFinal));
		}

		Map<UG, List<RequisicaoManutencao>> map = new HashMap<UG, List<RequisicaoManutencao>>();
		for (RequisicaoManutencao req : result) {
			UG ug  = req.getVeiculo().getUa().getUg();
			if(!map.containsKey(ug)){
				List<RequisicaoManutencao> list = new ArrayList<RequisicaoManutencao>();
				list.add(req);
				map.put(ug, list);
			} else {
				map.get(ug).add(req);
			}
		}

		for (UG ug : map.keySet()) {
			List<RequisicaoManutencao> list = map.get(ug);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setOrgao(ug);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			relatorio.setNumeroManutencoes(list.size());
			for (RequisicaoManutencao m : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(m);
				for (ItemRequisicao item : m.getItensRequisicao()) {
					RelatorioDTO toReport = new RelatorioDTO();
					toReport.setDtInicial(this.dtInicial);
					toReport.setDtFinal(this.dtFinal);
					toReport.setOrgao(ug);
					toReport.setVeiculo(m.getVeiculo());
					toReport.setManutencao(m);
					toReport.setItemManutencao(item);
					toReport.setNumeroManutencoes(list.size());
					this.result.add(toReport);
				}
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
		}
		return SUCCESS;
	}

	public String consultarHistoricoTrocaPneus() {
		if (!DateUtil.compareDate(this.dtInicial, this.dtFinal)) {
			JSFUtil.getInstance().addErrorMessage("msg.error.datas.inconsistentes");
			return FAIL;
		}
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<ItemRequisicao> itens;

		if(this.veiculo == null){
			List<Integer> ids = new ArrayList<Integer>();
			if(this.veiculos.size() > 0){
				for (Veiculo v : this.veiculos) {
					ids.add(v.getId());
				}
			}
			Integer tipoServicoId = 40;
			itens = itemManutencaoService.findByVeiculosTipoServico(ids, tipoServicoId);
		} else {
			Integer tipoServicoId = 40;
			List<Integer> ids = new ArrayList<Integer>();
			ids.add(this.veiculo.getId());
			itens = itemManutencaoService.findByVeiculosTipoServico(ids, tipoServicoId);
		}
		Map<Veiculo, List<ItemRequisicao>> map = new HashMap<Veiculo, List<ItemRequisicao>>();
		for (ItemRequisicao item : itens) {
			if(map.containsKey(item.getRequisicaoManutencao().getVeiculo()) == false){
				List<ItemRequisicao> list = new ArrayList<ItemRequisicao>();
				list.add(item);
				map.put(item.getRequisicaoManutencao().getVeiculo(), list);
			} else {
				map.get(item.getRequisicaoManutencao().getVeiculo()).add(item);
			}
		}
		for (Veiculo v : map.keySet()) {
			List<ItemRequisicao> list = map.get(v);
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			relatorio.setVeiculo(v);
			relatorio.setOrgao(this.orgao);
			relatorio.setDtInicial(this.dtInicial);
			relatorio.setDtFinal(this.dtFinal);
			Integer total = 0;
			for (ItemRequisicao i : list) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setVeiculo(v);
				dto.setDtInicial(this.dtInicial);
				dto.setDtFinal(this.dtFinal);
				dto.setManutencao(i.getRequisicaoManutencao());
				dto.setItemManutencao(i);
				dto.setQuantTrocaPneus(i.getQuantidade());
				total += i.getQuantidade();
				relatorio.getRelatorios().add(dto);
			}
			relatorio.setQuantTrocaPneus(total);
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}
		return SUCCESS;
	}

	public String consultarInformacoesVeiculo(){
		this.entities = new ArrayList<RelatorioDTO>();
		this.result = new ArrayList<RelatorioDTO>();
		List<AtendimentoAbastecimento> abastecimentos = null;
		Boolean statusVeiculo = Boolean.parseBoolean(this.statusVeiculo);
		
		List<AtendimentoAbastecimento> result = new ArrayList<AtendimentoAbastecimento>();
		
		abastecimentos = veiculoService.informacoesVeiculos(this.orgao, this.propriedade, statusVeiculo);
		
		Map<UG, List<RelatorioDTO>> map = new HashMap<UG, List<RelatorioDTO>>();
		for(AtendimentoAbastecimento abastecimento : abastecimentos) {
			
			Veiculo v 				= abastecimento.getAbastecimento().getVeiculo();
			RelatorioDTO relatorio 	= new RelatorioDTO();
			
			relatorio.setDataAbastecimento(abastecimento.getHora());
			relatorio.setOrgao(v.getUa().getUg());
			relatorio.setVeiculo(v);
			
			if(map.containsKey(v.getUa().getUg())) {
				map.get(v.getUa().getUg()).add(relatorio);
			} else {
				
				List<RelatorioDTO> rlist = new ArrayList<RelatorioDTO>();
				
				rlist.add(relatorio);
				map.put(v.getUa().getUg(), rlist);
			}
		}

		for (UG ug : map.keySet()) {
			RelatorioDTO ugRelat = new RelatorioDTO();
			ugRelat.setOrgao(ug);
			ugRelat.setRelatorios(new ArrayList<RelatorioDTO>());
			List<RelatorioDTO> vlist = map.get(ug);
			ugRelat.setRelatorios(vlist);
			
			this.entities.add(ugRelat);
			this.result.addAll(ugRelat.getRelatorios());
		}
		/*for (AtendimentoAbastecimento veiculo : veiculos) {
			
						
			if(this.propriedadeVeiculo != null && !this.propriedadeVeiculo.equals("Todos")) {
				
				if( this.propriedadeVeiculo.equals( veiculo.getPropriedade() ) ) {
					if(map.containsKey(veiculo.getUa().getUg())){
						map.get(veiculo.getUa().getUg()).add(veiculo);
					} else {
						List<Veiculo> vlist = new ArrayList<Veiculo>();
						
						vlist.add(veiculo);
						map.put(veiculo.getUa().getUg(), vlist);
					}
				}
			}else {
				if(map.containsKey(veiculo.getUa().getUg())){
					map.get(veiculo.getUa().getUg()).add(veiculo);
				} else {
					List<Veiculo> vlist = new ArrayList<Veiculo>();
					
					vlist.add(veiculo);
					map.put(veiculo.getUa().getUg(), vlist);
				}
			}
		}
		
		for (UG ug : map.keySet()) {
			RelatorioDTO relatorio = new RelatorioDTO();
			relatorio.setOrgao(ug);
			relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
			List<Veiculo> vlist = map.get(ug);
			for (Veiculo veiculo : vlist) {
				RelatorioDTO dto = new RelatorioDTO();
				dto.setVeiculo(veiculo);
				dto.setOrgao(ug);
				relatorio.getRelatorios().add(dto);
			}
			this.entities.add(relatorio);
			this.result.addAll(relatorio.getRelatorios());
		}*/
		return SUCCESS;
	}

	/**
	 * 
	 * gera consulta dos kilomentros rodados dos ve�culos do org�os
	 * @return
	 */

	public String consultarKilometrosRodados(){

		this.entities 			= new ArrayList<RelatorioDTO>();
		this.result 			= new ArrayList<RelatorioDTO>();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		
		calendar.set(Calendar.YEAR, this.ano);
		calendar.set(Calendar.MONTH, this.mes);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		this.dtInicial = DateUtil.getDateStartDay(calendar.getTime());

		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
		this.dtFinal = DateUtil.getDateEndDay(calendar.getTime());
		
		Map<UG, Map<Veiculo, Object[]>> mapsKm 	= new HashMap<UG, Map<Veiculo, Object[]>>();
		this.saldo				= 0D;
		
		if(this.orgao != null) {

			Map<Veiculo, Object[]> mapKilometragem = solicitacaoService.mapKilometragem(this.orgao, dtInicial, dtFinal);
			if(mapKilometragem.size() > 0){
				mapsKm.put(this.orgao, mapKilometragem);
			}

		} else {
			
			this.orgaos = ugService.retrieveAll();
			if(this.orgaos.size() > 0) {
				for(UG ug : this.orgaos) {

					Map<Veiculo, Object[]> mapKilometragem = solicitacaoService.mapKilometragem(ug, dtInicial, dtFinal);
					if(mapKilometragem.size() > 0){
						mapsKm.put(ug, mapKilometragem);
					}
				}
			}
		}
			
		if(mapsKm.size() > 0) {
			
			for (UG orgao  : mapsKm.keySet()) {
				this.saldo				= 0D;
				RelatorioDTO relatorio 	= new RelatorioDTO();
				relatorio.setRelatorios(new ArrayList<RelatorioDTO>());
				relatorio.setOrgao(orgao);
				
				Map<Veiculo, Object[]> mapKmRodados = mapsKm.get(orgao);
				if(mapKmRodados.size() > 0) {
					
					for (Veiculo v  : mapKmRodados.keySet()) {
						
						if(v.getPropriedade().equals(this.propriedade)) {
							
							RelatorioDTO dto = new RelatorioDTO();
							dto.setVeiculo(v);
							dto.setOrgao(orgao);
							
							Object[] array 	= (Object[]) mapKmRodados.get(v);
							Long kmMax 		= (Long) array[1];
							Long kmMin 		= (Long) array[2];
							Long kmRodados 	= kmMax - kmMin;
							
							dto.setKmInicial(kmMin);
							dto.setKmAtual(kmMax);
							dto.setKmRodados(kmRodados);
							
							if(v.getCotaKm() != null) {
								Double cotaSoma = v.getCotaKm().getCotaKm() - kmRodados;
								dto.setCotaSoma(cotaSoma);
								
								if(cotaSoma < 0) {
									this.saldo += cotaSoma;
								}
							}
							dto.setSaldoCotaKm(this.saldo);
							if(!this.contrato.isEmpty()){
								if(this.contrato.equals(v.getNumeroContrato())){
									relatorio.setSaldoCotaKm(this.saldo);
									this.result.add(dto);
									relatorio.getRelatorios().add(dto);
								}
							}else {
								relatorio.setSaldoCotaKm(this.saldo);
								this.result.add(dto);
								relatorio.getRelatorios().add(dto);
							}
						}
					}
					
					for (RelatorioDTO repairSaldoKm : relatorio.getRelatorios()) {
						repairSaldoKm.setSaldoCotaKm(this.saldo);
					}
					
					this.entities.add(relatorio);
				}
			}
		}

		return SUCCESS;
	}

	private Map<String, Object> montarParametrosRelat() {
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IMAGEM_URI", RelatorioUtil.getInstance().retornarImagensDir());
		return parametros;
	}
	
	public void gerarExcel() {
		Map<String, Object> parametros = null;
		parametros = montarParametrosRelat();
		HashMap<String, Integer> relHash = new HashMap<String, Integer>();
		
		relHash.put(this.relInformacoesVeiculo, 1);
		relHash.put(this.relHistoricoVeiculoManutencao, 2);
		relHash.put(this.relMotoristaPontuacao, 3);
		relHash.put(this.relCotasVeiculos, 4);
		relHash.put(this.relTrocasLubrificantes, 5);
		relHash.put(this.relDiarioBombas, 6);
		relHash.put(this.relVeiculoMulta, 7);
		relHash.put(this.relMultasVeiculoByUG, 8);
		relHash.put(this.relMultasVeiculos, 9);
		relHash.put(this.relMultasVeiculoByMotorista, 10);
		relHash.put(this.relConferenciaAbastecimento, 11);
		relHash.put(this.relAbastecimentoPosto, 12);
		relHash.put(this.relConsolidadoMensal, 13);
		relHash.put(this.relVeiculosEmManutencao, 14);
		relHash.put(this.relHistoricoTrocaPneus, 15);
		relHash.put(this.relVeiculosSemRetornoManutencao, 16);
		relHash.put(this.relSolicitacaoVeiculo, 17);
		relHash.put(this.relInformacoesKmsRodadosVeiculo, 18);
		
		try{
			
			switch( relHash.containsKey(this.nomeRelatorio) ? relHash.get(this.nomeRelatorio) : -1 ) {
				case 1:
					gerarRelatorioExcel(parametros, this.result, this.nomeRelatorio);
				break;
				
				case 11: {
					List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();

					for (RelatorioDTO r : this.entities) {

						for (RelatorioDTO rr : r.getRelatorios()) {

							//rr.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
							//rr.setConsumoTotal(rr.getConsumoTotal() + r.getConsumoTotal());

							for (RelatorioDTO r3 : rr.getRelatorios()) {
								r3.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
								r3.setConsumoGasolina(r.getConsumoGasolina());
								r3.setConsumoEtanol(r.getConsumoEtanol());
								r3.setConsumoTotal(rr.getConsumoTotal());
							}
							list.addAll(rr.getRelatorios());
						}
					}
					gerarRelatorioExcel(parametros, list, this.nomeRelatorio);
					break;
				}
				case 12: {
					List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
					for (RelatorioDTO r : this.entities) { // posto

						for (RelatorioDTO rr : r.getRelatorios()) { //orgaos
							for(RelatorioDTO r3 : rr.getRelatorios()) { // veiculos
								for(RelatorioDTO r4 : r3.getRelatorios()) {
									r4.setPosto(r.getPosto());
									r4.setConsumoPosto(r.getConsumo());
									r4.setConsumoCombustivelOrgao(rr.getConsumoCombustivelOrgao());
									r4.setConsumoGasolina(rr.getConsumoGasolina());
									r4.setConsumoEtanol(rr.getConsumoEtanol());
									r4.setConsumoDiesel(rr.getConsumoDiesel());
								}
								list.addAll(r3.getRelatorios());
							}
						}
						//list.addAll(r.getRelatorios());
					}
					gerarRelatorioExcel(parametros, list, this.nomeRelatorio);
					break;
				}
				
				case 13:
					List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
					for (RelatorioDTO r : this.entities) {
						for (RelatorioDTO rr : r.getRelatorios()) {
							rr.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
							rr.setConsumoGasolina(r.getConsumoGasolina());
							rr.setConsumoEtanol(r.getConsumoEtanol());
							rr.setConsumoDiesel(r.getConsumoDiesel());
						}
						list.addAll(r.getRelatorios());
					}
					gerarRelatorioExcel(parametros, list, this.nomeRelatorio);
				break;
				
				case 18: {
					
					gerarRelatorioExcel(parametros, this.result, this.nomeRelatorio);
					break;
				}
			}
			
		}catch (IOException e) {

			e.printStackTrace();
		} catch (JRException e) {

			e.printStackTrace();
		}
	}

	public void gerarRelatorio() {

		Map<String, Object> parametros = null;
		parametros = montarParametrosRelat();

		try {

			if (this.nomeRelatorio.equals(this.relInformacoesVeiculo)) {
				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relHistoricoVeiculoManutencao)) {

				ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
				//				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);
				parametros.put("dtInicial", this.dtInicial);
				parametros.put("dtFinal", this.dtFinal);
				parametros.put("SUBREPORT_DIR", servletContext.getRealPath("/relatorios/jasper/"));
				gerarRelatorioBD(parametros, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relMotoristaPontuacao)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relCotasVeiculos)) {

				List<RelatorioDTO> result = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO rel : this.entities) {
					for (RelatorioDTO sub : rel.getRelatorios()) {
						sub.setCotaSoma(rel.getCotaSoma());
						result.add(sub);
					}
				}
				gerarRelatorioCollection(parametros, result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relTrocasLubrificantes)) {

				List<RelatorioDTO> result = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO rel : this.entities) {
					result.addAll(rel.getRelatorios());
				}
				gerarRelatorioCollection(parametros, result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relDiarioBombas)) {

				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO r : this.entities) {
					for (RelatorioDTO sub : r.getRelatorios()) {
						sub.setConsumoPosto(r.getConsumoPosto());
						list.add(sub);
					}
				}
				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relVeiculoMulta)) {

				gerarRelatorioCollection(parametros, this.entities, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relMultasVeiculoByUG) || 
					this.nomeRelatorio.equals(this.relMultasVeiculos) || 
					this.nomeRelatorio.equals(this.relMultasVeiculoByMotorista)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relConferenciaAbastecimento)) { // gera pdf do relatório consolidado mensal

				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();

				for (RelatorioDTO r : this.entities) {

					for (RelatorioDTO rr : r.getRelatorios()) {

						//rr.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
						//rr.setConsumoTotal(rr.getConsumoTotal() + r.getConsumoTotal());

						for (RelatorioDTO r3 : rr.getRelatorios()) {
							r3.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
							r3.setConsumoGasolina(r.getConsumoGasolina());
							r3.setConsumoEtanol(r.getConsumoEtanol());
							r3.setConsumoTotal(rr.getConsumoTotal());
						}
						list.addAll(rr.getRelatorios());
					}
				}
				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);

			 } else if(this.nomeRelatorio.equals(this.relAbastecimentoPosto)) {
	
				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO r : this.entities) { // posto

					for (RelatorioDTO rr : r.getRelatorios()) { //orgaos
						for(RelatorioDTO r3 : rr.getRelatorios()) { // veiculos
							for(RelatorioDTO r4 : r3.getRelatorios()) {
								r4.setPosto(r.getPosto());
								r4.setConsumoPosto(r.getConsumo());
								r4.setConsumoCombustivelOrgao(rr.getConsumoCombustivelOrgao());
								r4.setConsumoGasolina(rr.getConsumoGasolina());
								r4.setConsumoEtanol(rr.getConsumoEtanol());
								r4.setConsumoDiesel(rr.getConsumoDiesel());
							}
							list.addAll(r3.getRelatorios());
						}
					}
					//list.addAll(r.getRelatorios());
				}
				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);
				
			} else if (this.nomeRelatorio.equals(this.relConsolidadoMensal)) { // gera pdf do relatório consolidado mensal

				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO r : this.entities) {
					for (RelatorioDTO rr : r.getRelatorios()) {
						rr.setConsumoCombustivelOrgao(r.getConsumoCombustivelOrgao());
						rr.setConsumoGasolina(r.getConsumoGasolina());
						rr.setConsumoEtanol(r.getConsumoEtanol());
						rr.setConsumoDiesel(r.getConsumoDiesel());
					}
					list.addAll(r.getRelatorios());
				}
				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relVeiculosEmManutencao)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relHistoricoTrocaPneus)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			}  else if (this.nomeRelatorio.equals(this.relVeiculosSemRetornoManutencao)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if (this.nomeRelatorio.equals(this.relSolicitacaoVeiculo)) {

				gerarRelatorioCollection(parametros, this.result, this.nomeRelatorio);

			} else if(this.nomeRelatorio.equals(this.relInformacoesKmsRodadosVeiculo)){

				List<RelatorioDTO> list = new ArrayList<RelatorioDTO>();
				for (RelatorioDTO r : this.entities) {
					list.addAll(r.getRelatorios());
				}
				
				gerarRelatorioCollection(parametros, list, this.nomeRelatorio);
			}

		} catch (IOException e) {

			e.printStackTrace();
		} catch (JRException e) {

			e.printStackTrace();
		}
	}

	public void gerarRelatorioExcel(Map<String, Object> parametros, Collection<?> colecao, String filePropertie) 
			throws IOException, JRException {
		
		String 				jasperPath 	= RelatorioUtil.getInstance().retornarJasperPathExcel(filePropertie);
		byte[] 				array 		= GeradorRelatorio.gerarExcelCollection(parametros, colecao, jasperPath);
		HttpServletResponse res 		= JSFUtil.getInstance().getResponse(FacesContext.getCurrentInstance());
		
		res.setContentType("application/vnd.ms-excel");     
	    res.setContentLength(array.length);
	    res.setHeader("Pragma", "public");
		res.setHeader("Cache-control", "must-revalidate");
		res.setHeader("Content-Disposition", "attachment;filename=" + nomeRelatorio + ".xls");

		// Enviando o pdf para o navegador
		ServletOutputStream servletOutputStream = res.getOutputStream();
		servletOutputStream.write(array);
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	public void gerarRelatorioCollection(Map<String, Object> parametros, Collection<?> colecao, String filePropertie) 
			throws IOException, JRException {

		// Gerando relatorio
		// Montando jasper path
		String jasperPath = RelatorioUtil.getInstance().retornarJasperPath(filePropertie);
		byte[] array = GeradorRelatorio.gerarPdfCollection(parametros, colecao, jasperPath);

		// Resgatando response
		HttpServletResponse res = JSFUtil.getInstance().getResponse(FacesContext.getCurrentInstance());

		// Configurando cabeçalho
		res.setContentType("application/pdf");
		res.setHeader("Pragma", "public");
		res.setHeader("Cache-control", "must-revalidate");
		res.setHeader("Content-Disposition", "attachment;filename=" + nomeRelatorio + ".pdf");

		// Enviando o pdf para o navegador
		ServletOutputStream servletOutputStream = res.getOutputStream();
		servletOutputStream.write(array);
		FacesContext.getCurrentInstance().responseComplete();
	}

	public void gerarRelatorioBD(Map<String, Object> parametros, String filePropertie) throws IOException, JRException{
		// Gerando relatorio
		// Montando jasper path
		String jasperPath = RelatorioUtil.getInstance().retornarJasperPath(filePropertie);
		//		byte[] array = GeradorRelatorio.gerarPdfCollection(parametros, colecao, jasperPath);
		byte[] array = GeradorRelatorio.gerarPdfBD(parametros, jasperPath);

		// Resgatando response
		HttpServletResponse res = JSFUtil.getInstance().getResponse(FacesContext.getCurrentInstance());

		// Configurando cabeçalho
		res.setContentType("application/pdf");
		res.setHeader("Pragma", "public");
		res.setHeader("Cache-control", "must-revalidate");
		res.setHeader("Content-Disposition", "attachment;filename=" + nomeRelatorio + ".pdf");

		// Enviando o pdf para o navegador
		ServletOutputStream servletOutputStream = res.getOutputStream();
		servletOutputStream.write(array);
		FacesContext.getCurrentInstance().responseComplete();

	}

	public UG getOrgao() {
		return orgao;
	}

	public void setOrgao(UG orgao) {
		this.orgao = orgao;
	}

	public Motorista getMotorista() {
		return motorista;
	}

	public void setMotorista(Motorista motorista) {
		this.motorista = motorista;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Integer getSearchId() {
		return searchId;
	}

	public void setSearchId(Integer searchId) {
		this.searchId = searchId;
	}

	public boolean isMostrarVeiculos() {
		return mostrarVeiculos;
	}

	public void setMostrarVeiculos(boolean mostrarVeiculos) {
		this.mostrarVeiculos = mostrarVeiculos;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public List<Motorista> getMotoristas() {
		return motoristas;
	}

	public void setMotoristas(List<Motorista> motoristas) {
		this.motoristas = motoristas;
	}

	public boolean isMostrarMotoristas() {
		return mostrarMotoristas;
	}

	public void setMostrarMotoristas(boolean mostrarMotoristas) {
		this.mostrarMotoristas = mostrarMotoristas;
	}

	public void setNomeRelatorio(String nomeRelatorio) {
		this.nomeRelatorio = nomeRelatorio;
	}

	public String getNomeRelatorio() {
		return nomeRelatorio;
	}

	public DiarioBomba getDiarioBomba() {
		return diarioBomba;
	}

	public void setDiarioBomba(DiarioBomba diarioBomba) {
		this.diarioBomba = diarioBomba;
	}

	public Posto getPosto() {
		return posto;
	}

	public void setPosto(Posto posto) {
		this.posto = posto;
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

	public List<Multa> getMultas() {
		return multas;
	}

	public void setMultas(List<Multa> multas) {
		this.multas = multas;
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

	public boolean isGenerate() {
		return generate;
	}

	public void setGenerate(boolean generate) {
		this.generate = generate;
	}

	public boolean isAllugs() {
		return allugs;
	}

	public void setAllugs(boolean allugs) {
		this.allugs = allugs;
	}

	public List<Posto> getPostos() {
		return postos;
	}

	public void setPostos(List<Posto> postos) {
		this.postos = postos;
	}

	public void setOrgaos(List<UG> orgaos) {
		this.orgaos = orgaos;
	}

	public List<UG> getOrgaos() {
		return orgaos;
	}

	public List<RelatorioDTO> getListToPDF() {
		return result;
	}

	public void setListToPDF(List<RelatorioDTO> listToPDF) {
		this.result = listToPDF;
	}

	public String getSearchFlag() {
		return searchFlag;
	}

	public void setSearchFlag(String searchFlag) {
		this.searchFlag = searchFlag;
	}

	public List<SelectItem> getMeses() {
		return meses;
	}

	public void setMeses(List<SelectItem> meses) {
		this.meses = meses;
	}

	public List<SelectItem> getYears() {
		return years;
	}

	public void setYears(List<SelectItem> years) {
		this.years = years;
	}

	public List<SelectItem> getListProprietario() {
		return listProprietario;
	}

	public void setListProprietario(List<SelectItem> listProprietario) {
		this.listProprietario = listProprietario;
	}

	public String getPropriedadeVeiculo() {
		return propriedadeVeiculo;
	}

	public void setPropriedadeVeiculo(String propriedadeVeiculo) {
		this.propriedadeVeiculo = propriedadeVeiculo;
	}

	public String getStatusAbastecimento() {
		return statusAbastecimento;
	}

	public void setStatusAbastecimento(String statusAbastecimento) {
		this.statusAbastecimento = statusAbastecimento;
	}

	public String getStatusVeiculo() {
		return statusVeiculo;
	}

	public void setStatusVeiculo(String statusVeiculo) {
		this.statusVeiculo = statusVeiculo;
	}

	public String getPropriedade() {
		return propriedade;
	}

	public void setPropriedade(String propriedade) {
		this.propriedade = propriedade;
	}

	public Double getSaldo() {
		return saldo;
	}

	public void setSaldo(Double saldo) {
		this.saldo = saldo;
	}

	public String getContrato() {
		return contrato;
	}

	public void setContrato(String contrato) {
		this.contrato = contrato;
	}
}