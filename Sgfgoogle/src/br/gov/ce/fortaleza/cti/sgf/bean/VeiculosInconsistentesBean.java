package br.gov.ce.fortaleza.cti.sgf.bean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Modelo;
import br.gov.ce.fortaleza.cti.sgf.entity.Parametro;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.ModeloService;
import br.gov.ce.fortaleza.cti.sgf.service.ParametroService;
import br.gov.ce.fortaleza.cti.sgf.service.UAService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.ConnectOracle;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.RelatorioDTO;

/**
 * 
 * Bean que gerencia a verificação dos veículos existentes no patrimônio mas que
 * ainda não estão no frotas
 * 
 * @author Lafitte
 * @since 27/12/2010
 * 
 */
@Component("veiculoInconsistenteBean")
@Scope("session")
public class VeiculosInconsistentesBean extends EntityBean<Integer, RelatorioDTO> {

	/**
	 * Objeto que guarda os dados da conexão e a realiza
	 */
	private ConnectOracle conexaoPatrimonio;
	/**
	 * Serviço de acesso aos veículos na base do SGF
	 */
	@Autowired
	private VeiculoService veiculoService;
	/**
	 * Veículos recuperados do patrimônio
	 */
	private List<Veiculo> veiculos;
	/**
	 * Veículo que será exibido na tela caso não esteja cadastrado no Frotas
	 */
	private Veiculo veiculo;

	private UA ua;
	/**
	 * UG escolhida para que sejam listadas suas UA's e selecionada uma para que
	 * seja feita a sincronizaï¿½ï¿½o dos veï¿½culos da UA escolhida
	 */
	private UG ug;
	/**
	 * Modelo associado ao veï¿½culo que serï¿½ cadastrado
	 */
	private Modelo modelo;

	@Autowired
	public ParametroService parametroService;
	/**
	 * Serviï¿½o de acesso às UAS na base do SGF
	 */
	@Autowired
	private UAService uaService;
	/**
	 * Serviï¿½o de acesso aos modelos na base do SGF
	 */
	@Autowired
	private ModeloService modeloService;
	/**
	 * Busca os veículos no patrimônio e verifica quais não estão cadastrados
	 * na base de dados do frotas, aqueles que não estiverem serão exibidos
	 * na tela do sistema
	 * 
	 * @throws SQLException
	 */
	public void buscaVeiculosPatrimonio() throws SQLException {

		String codUAPat;
		String codUASgf;
		/*
		 * Verifica o código da UG selecionada, caso seja uma das DTE's,
		 * Zoonoses, NUCEM ou SAMU, faz o tratamento pois essas UG's são UA's no
		 * Patrimônio.
		 */
		Map<String, String> mapParam = new HashMap<String, String>();
		List<Parametro> parametros = parametroService.findByTipo("ID_UG");

		for (Parametro p : parametros) {
			mapParam.put(p.getNome(), p.getValor());
		}
		
		
//		codUAPat = this.ug.getId();
//		codUASgf = mapParam.get(this.ug.getId());
//		
//
//		if (codUASgf == null) {
//			codUAPat = this.ua.getId();
//			codUASgf = this.ua.getId();
//		}
		/*
		 * Consulta utilizada para buscar os veículos do patrimônio associados à
		 * ua selecionada
		 */
		String query = "select * from ("
				+ "select cd_ua_atual,cd_bem_perm,"
				+ "(select ds_car_char  from BP_ENT_CAR_BEM bb"
				+ "   where cd_caracteristica = '038'"
				+ " and bb.sq_bem_perm = v.sq_bem_perm) placa,"
				+ "(select ds_car_char"
				+ " from BP_ENT_CAR_BEM bb"
				+ " where cd_caracteristica = '039' "
				+ "and bb.sq_bem_perm = v.sq_bem_perm) chassi,"
				+ " (select ds_car_char"
				+ " from BP_ENT_CAR_BEM bb "
				+ "where cd_caracteristica = '040' "
				+ "and bb.sq_bem_perm = v.sq_bem_perm) renavam, "
				+ "(select ct.nm_caract_tab"
				+ " from BP_ENT_CAR_GEN bg, cr_caract_tab ct, BP_BEM B"
				+ " where ct.cd_caracteristica = '035'"
				+ " and bg.cd_entrada = v.cd_entrada"
				+ " and b.sq_bem_perm = v.sq_bem_perm"
				+ " and ct.cd_caracteristica = bg.cd_caracteristica"
				+ " and ct.sq_caract_tab = bg.ds_car_char"
				+ " and bg.sq_item = b.sq_item"
				+ " and bg.cd_ug = b.cd_ug) combustivel,"
				+ " (select ct.nm_caract_tab"
				+ " from BP_ENT_CAR_GEN bg, cr_caract_tab ct, BP_BEM B"
				+ " where ct.cd_caracteristica = '033'"
				+ " and bg.cd_entrada = v.cd_entrada"
				+ " and b.sq_bem_perm = v.sq_bem_perm"
				+ " and ct.cd_caracteristica = bg.cd_caracteristica"
				+ " and ct.sq_caract_tab = bg.ds_car_char"
				+ " and bg.sq_item = b.sq_item"
				+ " and bg.cd_ug = b.cd_ug) modelo,"
				+ " (select ct.nm_caract_tab"
				+ " from BP_ENT_CAR_GEN bg, cr_caract_tab ct, BP_BEM B"
				+ " where ct.cd_caracteristica = '036'"
				+ " and bg.cd_entrada = v.cd_entrada"
				+ " and b.sq_bem_perm = v.sq_bem_perm"
				+ " and ct.cd_caracteristica = bg.cd_caracteristica"
				+ " and ct.sq_caract_tab = bg.ds_car_char"
				+ " and bg.sq_item = b.sq_item"
				+ " and bg.cd_ug = b.cd_ug) ANOFABR,"
				+ " (select ct.nm_caract_tab"
				+ " from BP_ENT_CAR_GEN bg, cr_caract_tab ct, BP_BEM B"
				+ " where ct.cd_caracteristica = '037'"
				+ " and bg.cd_entrada = v.cd_entrada"
				+ " and b.sq_bem_perm = v.sq_bem_perm"
				+ " and ct.cd_caracteristica = bg.cd_caracteristica"
				+ " and ct.sq_caract_tab = bg.ds_car_char"
				+ " and bg.sq_item = b.sq_item"
				+ " and bg.cd_ug = b.cd_ug) ANOMODELO,"
				+ " (select ct.nm_caract_tab"
				+ " from BP_ENT_CAR_GEN bg, cr_caract_tab ct, BP_BEM B"
				+ " where ct.cd_caracteristica = '009'"
				+ " and bg.cd_entrada = v.cd_entrada"
				+ " and b.sq_bem_perm = v.sq_bem_perm"
				+ " and ct.cd_caracteristica = bg.cd_caracteristica"
				+ " and ct.sq_caract_tab = bg.ds_car_char"
				+ " and bg.sq_item = b.sq_item"
				+ " and bg.cd_ug = b.cd_ug) cor"
				+ " from (SELECT C.CD_ORGAO, C.SQ_BEM_PERM,"
				+ " MAX(C.CD_PATR_ANTIGO) AS CD_PATR_ANTIGO,"
				+ " MAX(C.CD_BEM_PERM) AS CD_BEM_PERM,"
				+ " MAX(C.VL_UNITARIO) AS VL_UNITARIO,"
				+ " MAX(C.VL_UFIR) AS VL_UFIR,"
				+ " MAX(C.DT_GARANTIA_INI) AS DT_GARANTIA_INI,"
				+ " MAX(C.DT_GARANTIA_FIM) AS DT_GARANTIA_FIM,"
				+ " MAX(C.CD_TP_SIT_FISICA) AS CD_TP_SIT_FISICA,"
				+ " MAX(C.CD_UG_COMPRA) AS CD_UG_COMPRA,"
				+ " MAX(C.CD_UL_ATUAL) AS CD_UL_ATUAL,"
				+ " MAX(C.CD_UA_ATUAL) AS CD_UA_ATUAL,"
				+ " MAX(C.CD_TP_STATUS) AS CD_TP_STATUS,"
				+ " MAX(C.CD_ORG_DETENTOR) AS CD_ORG_DETENTOR,"
				+ " MAX(C.CD_DETENTOR) AS CD_DETENTOR,"
				+ " MAX(C.CD_CONTA) AS CD_CONTA,"
				+ " MAX(C.CD_BEM_SERVICO) AS CD_BEM_SERVICO,"
				+ " MAX(C.CD_ENTRADA) AS CD_ENTRADA,"
				+ " MAX(C.DT_INCLUSAO) AS DT_INCLUSAO,"
				+ " MAX(C.DT_AQUISICAO) AS DT_AQUISICAO,"
				+ " MAX(C.DT_BAIXA) AS DT_BAIXA,"
				+ " MAX(C.DT_CONTABIL) AS DT_CONTABIL,"
				+ " MAX(C.CD_AGENTE) AS CD_AGENTE,"
				+ " MAX(C.CD_TP_OPERACAO) AS CD_TP_OPERACAO,"
				+ " MAX(C.CD_TP_FORMA_OP) AS CD_TP_FORMA_OP,"
				+ " MAX(C.CD_TP_BEM) AS CD_TP_BEM,"
				+ " MAX(C.CD_ORGAO_SUP) AS CD_ORGAO_SUP,"
				+ " MAX(C.SQ_BEM_PERM_SUP) AS SQ_BEM_PERM_SUP,"
				+ " MAX(C.CD_UG_ATUAL) AS CD_UG_ATUAL,"
				+ " MAX(C.CD_CENTRO_CUSTO) AS CD_CENTRO_CUSTO,"
				+ " MAX(C.DS_COMPLETA) AS DS_COMPLETA,"
				+ " MAX(C.CD_SIMOV) AS CD_SIMOV, MAX(C.VL_MERCADO) AS VL_MERCADO,"
				+ " MAX(C.NR_PROCESSO_COMODATO) AS NR_PROCESSO_COMODATO"
				+ " FROM BP_V_CONSULTA_GERAL C"
				+ " WHERE (((C.CD_BEM_SERVICO = '100001175' AND C.CD_ORGAO = '001') OR"
				+ " (C.CD_BEM_SERVICO = '100001374' AND C.CD_ORGAO = '001') OR"
				+ " (C.CD_BEM_SERVICO = '100000968' AND C.CD_ORGAO = '001') OR"
				+ " (C.CD_BEM_SERVICO = '100000255' AND C.CD_ORGAO = '001') OR"
				+ " (C.CD_BEM_SERVICO = '100001071' AND C.CD_ORGAO = '001') OR"
				+ " (C.CD_BEM_SERVICO = '100000848' AND C.CD_ORGAO = '001')))"
				+ " GROUP BY C.CD_ORGAO, C.SQ_BEM_PERM"
				+ " ORDER BY 4 ASC) v)";

		Connection connection = null;
		Statement stmt = null;

		try {
			this.veiculos = new ArrayList<Veiculo>();
			String placaValidada;
			this.conexaoPatrimonio = new ConnectOracle();
			/**
			 * Conexão utilizada para o acesso à base do Patrimï¿½nio
			 */
			connection = conexaoPatrimonio.conectaOracle();
			stmt = connection.createStatement();
			// Executa a consulta do parâmetro query
			ResultSet rs = stmt.executeQuery(query);

			/*
			 * Itera pelo resultset que contém os veículos cadastrados no
			 * patrimônio e verifica se o veículo já está cadastrado na base de
			 * dados do SGF, caso não esteja exibe o veículo
			 */
			while (rs.next()) {
				if (rs.getString("placa") != null) {
					if (rs.getString("placa").contains("-")) {
						placaValidada = rs.getString("placa").replace("-", "");

					} else {
						placaValidada = rs.getString("placa");
					}
				} else {
					placaValidada = "";
				}
				

				this.veiculo = veiculoService.findByPlacaSingle(placaValidada
						.toUpperCase());

				//this.ua = uaService.retrieve(codUASgf);
				if (rs.getString("modelo") != null
						&& rs.getString("modelo") != "") {
					try {
						this.modelo = modeloService.findByDesc(rs
								.getString("modelo"));
					} catch (Exception e) {
						this.modelo = null;
					}
				}
				if (this.veiculo == null) {
					this.veiculo = new Veiculo();
					//this.veiculo.setUa(ua);
					this.veiculo.setNumeroPatrimonio(rs
							.getString("cd_bem_perm"));
					this.veiculo.setPlaca(placaValidada);
					this.veiculo.setNumeroPatrimonio(rs.getString("cd_bem_perm"));
					this.veiculo.setChassi(rs.getString("chassi"));
					this.veiculo.setRenavam(rs.getString("renavam"));
					this.veiculo.setCombustivel(rs.getString("combustivel"));
					this.veiculo.setModelo(modelo);
					this.veiculo.setStatus(0);
					this.veiculo.setTemSeguro(0);
					if (rs.getString("anofabr") != null
							&& rs.getString("anofabr") != "") {
						this.veiculo.setAnoFabricacao(Integer.parseInt(rs
								.getString("anofabr")));
					}
					if (rs.getString("anomodelo") != null
							&& rs.getString("anomodelo") != "") {
						this.veiculo.setAnoModelo(Integer.parseInt(rs
								.getString("anomodelo")));
					}
					this.veiculo.setCor(rs.getString("cor"));
					this.veiculos.add(this.veiculo);
					this.veiculo = null;
				} 
			}

			if (this.veiculos.isEmpty()) {
				JSFUtil.getInstance().addErrorMessage("msg.sinc.vazia");
			}

		} catch (SQLException e) {
			StackTraceElement[] stack = e.getStackTrace();
			String message = "Class:" + stack[0].getClassName() + "\nMethod:"
					+ stack[0].getMethodName() + "\nLine:"
					+ stack[0].getLineNumber() + "\n";
			// Mail.sendMailSsl(Mail.FROM, Mail.TO, "Error: " + e.getCause(),
			// message);
			e.printStackTrace();
			JSFUtil.getInstance().addErrorMessage("msg.error.sincronizacao");
		} catch (Exception e2) {
			StackTraceElement[] stack = e2.getStackTrace();
			String message = "Class:" + stack[0].getClassName() + "\nMethod:"
					+ stack[0].getMethodName() + "\nLine:"
					+ stack[0].getLineNumber() + "\n";
			// Mail.sendMailSsl(Mail.FROM, Mail.TO, "Error: " + e2.getCause(),
			// message);
			e2.printStackTrace();
			JSFUtil.getInstance().addErrorMessage("msg.error.sincronizacao");
		} finally {
			if (connection != null) {
				connection.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	/**
	 * Método utilizado para enviar o sistema para a página
	 * de exibição dos resultados
	 * 
	 * @return String
	 */
	public String exibeInconsistencias() {
		setCurrentBean(currentBeanName());
		try {
			buscaVeiculosPatrimonio();
			return SUCCESS;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return FAIL;
		}
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

	@Override
	protected BaseService<Integer, RelatorioDTO> retrieveEntityService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Integer retrieveEntityId(RelatorioDTO entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RelatorioDTO createNewEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}
