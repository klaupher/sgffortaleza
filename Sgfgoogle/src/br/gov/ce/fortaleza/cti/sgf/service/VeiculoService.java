package br.gov.ce.fortaleza.cti.sgf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.gov.ce.fortaleza.cti.sgf.entity.Abastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.AtendimentoAbastecimento;
import br.gov.ce.fortaleza.cti.sgf.entity.Falta;
import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.entity.UA;
import br.gov.ce.fortaleza.cti.sgf.entity.UG;
import br.gov.ce.fortaleza.cti.sgf.entity.User;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.dto.PontoDTO;

@Repository
@Transactional
public class VeiculoService extends BaseService<Integer, Veiculo>{

	private static final Log logger = LogFactory.getLog(VeiculoService.class);

	@Autowired
	private TransmissaoService transmissaoService;

	@Autowired
	public ParametroService parametroService;


	@Transactional
	public Boolean verificaSeExistePlaca(String placa){

		String consulta = "select v from Veiculo v where upper(trim(v.placa)) like :placa";
		StringBuffer hql = new StringBuffer(consulta);
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("placa",  placa);
		return query.getResultList().size() > 0;
	}

	@Transactional
	public Boolean verificaSeExisteChassi(String chassi){

		String consulta = "select v from Veiculo v where upper(trim(v.chassi)) like :chassi";
		StringBuffer hql = new StringBuffer(consulta);
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("chassi",  chassi);
		return query.getResultList().size() > 0;
	}

	@Transactional
	public Boolean verificaSeExisteRenavam(String renavam){

		String consulta = "select v from Veiculo v where upper(trim(v.renavam)) like :renavam";
		StringBuffer hql = new StringBuffer(consulta);
		Query query = entityManager.createQuery(hql.toString());
		query.setParameter("renavam",  renavam);
		return query.getResultList().size() > 0;
	}

	//Modificado 21.05.2014 -- Paulo Andre
	@Transactional
	public List<String> verificaNumeroDeContrato(){
		Query query = entityManager.createQuery("SELECT distinct(v.numeroContrato) FROM Veiculo v WHERE  v.numeroContrato <> ''");
		return query.getResultList();
	}
	//Fim
	
	public List<Veiculo> findAll(){
		List<Veiculo> result = new ArrayList<Veiculo>();
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)  || SgfUtil.isCoordenador(user)){
			result = retrieveAll();
		} else {
			UA ua = SgfUtil.usuarioLogado().getPessoa().getUa();
			if(ua != null){
				result = retrieveByUG(ua.getUg().getId());
			}
		}
		Collections.sort(result, new Comparator<Veiculo>() {
			public int compare(Veiculo o1, Veiculo o2) {
				return o1.getPlaca().compareTo(o2.getPlaca());
			}
		});
		return result;
	}
	
	public List<AtendimentoAbastecimento> informacoesVeiculos(UG ug, String propriedade, String placa, Boolean status, String numeroContrato){
		
		List<Object> abastecimento = new ArrayList<Object>();
		List<AtendimentoAbastecimento> result = new ArrayList<AtendimentoAbastecimento>();
		
		StringBuilder sql = new StringBuilder("SELECT * FROM ( ");
		sql.append("SELECT MAX(at.hora_atendimento) OVER (PARTITION BY v.codveiculo) AS ultabast, at.codatendabastecimento, hora_atendimento, v.codveiculo ");
		sql.append("FROM tb_cadveiculo AS v ");
		sql.append("LEFT JOIN tb_abastecimento AS ab ON (v.codveiculo = ab.codveiculo) ");
		sql.append("LEFT JOIN tb_atendabastecimento AS at ON (at.codsolabastecimento = ab.codsolabastecimento) ");
		sql.append("INNER JOIN tb_ua AS ua ON(v.cod_ua_asi = ua.cod_ua) ");
		sql.append("INNER JOIN tb_ug as ug ON(ua.cod_ug = ug.cod_ug) ");
		
		if(ug != null){
			sql.append("WHERE ug.cod_ug = '"+ ug.getId()+"' ");
		}else{
			sql.append("WHERE 1 = 1");
		}
		//Modificacoes 22.05.2014 --Paulo Andre
		if(!numeroContrato.isEmpty()) {
			sql.append(" AND v.contrato = '"+ numeroContrato +"'");
		}
		//FIM
		if(!placa.isEmpty()) {
			sql.append(" AND v.placa LIKE '%"+ placa +"%'");
		}else{
			if(propriedade != null){
				sql.append(" AND v.propriedade = '"+ propriedade+"'");
			}
			
			if(status != null) {
				if(status){
				sql.append(" AND v.status != 6 ");
				}else{
					sql.append(" AND v.status = 6 ");
				}
			}else{
				sql.append(" AND v.status != 6 ");
			}
		}

		sql.append("GROUP BY v.codveiculo, at.codatendabastecimento ");
		sql.append(") x ");
		sql.append("WHERE COALESCE(ultabast,to_date('19000101','yyyymmdd')) = COALESCE(hora_atendimento,to_date('19000101','yyyymmdd'))");
		
		Query query = entityManager.createNativeQuery(sql.toString());
		
		abastecimento = query.getResultList();
		for (int i = 0; i < abastecimento.size(); i++) {
			Object[] array = (Object[]) abastecimento.get(i);
			
			if(array[1] != null) {
				AtendimentoAbastecimento atend = (AtendimentoAbastecimento) entityManager.createNamedQuery("AtendimentoAbastecimento.findById")
						.setParameter(1, array[1])
						.getSingleResult();
				result.add(atend);
				
			} else {
				
				Veiculo veiculo = (Veiculo) entityManager.createNamedQuery("Veiculo.findById")
						.setParameter(1, array[3])
						.getSingleResult();
				
				AtendimentoAbastecimento atendimento = new AtendimentoAbastecimento();
				atendimento.setAbastecimento(new Abastecimento());
				atendimento.getAbastecimento().setVeiculo(veiculo);
				
				result.add(atendimento);
			}
		}
		return result;
	}
	
	/**
	 * retorna os veículos com status disponíveis. Se o usuário é administrador, a lista virá com todos os veículos, senão
	 * lista virá com os veículos do ôrgãos do usuário logado.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Veiculo> veiculosDisponiveis(UG ug){
		
		List<Veiculo> veiculos		= new ArrayList<Veiculo>();
		List<Veiculo> faltaVeiculos = new ArrayList<Veiculo>();
		
		StringBuffer fsql = new StringBuffer("SELECT f.veiculo FROM Falta f WHERE f.dataFalta BETWEEN :inicio AND :fim ");
		
		Calendar ini = Calendar.getInstance();  
		ini.setTime(new Date());  
		ini.set(Calendar.HOUR_OF_DAY, 0);  
		ini.set(Calendar.MINUTE, 0);  
		ini.set(Calendar.SECOND, 0);  
        Date dtIni = ini.getTime();
        
        Calendar fim = Calendar.getInstance();  
        fim.setTime(new Date());  
        fim.set(Calendar.HOUR_OF_DAY, 23);  
        fim.set(Calendar.MINUTE, 59);  
        fim.set(Calendar.SECOND, 59);  
        Date dtFim = fim.getTime();

		Query query = null;
		Query faltosos = null;
		
		if(SgfUtil.isAdministrador(SgfUtil.usuarioLogado())){
			if(ug != null){
				query = entityManager.createQuery("SELECT v FROM Veiculo v WHERE v.status = 0 and v.ua.ug.id = :ug AND (v.dataDut > NOW() OR v.dataDut IS NULL) order by v.cotaKm");
				query.setParameter("ug", ug.getId());
				
				
				
				fsql.append("and f.veiculo.ua.ug.id = :ug ");
				faltosos = entityManager.createQuery(fsql.toString());
				faltosos.setParameter("ug", ug.getId());
				
				
			} else {
				query = entityManager.createQuery("SELECT v FROM Veiculo v WHERE v.status = 0 order by v.cotaKm");
			}
			//return query.getResultList();
		} else {
			query = entityManager.createQuery("SELECT v FROM Veiculo v WHERE v.ua.ug.id = :ug and v.status = 0 AND (v.dataDut > NOW() OR v.dataDut is NULL) order by v.cotaKm");
			query.setParameter("ug", SgfUtil.usuarioLogado().getPessoa().getUa().getUg().getId());
			
			fsql.append("and f.veiculo.ua.ug.id = :ug ");
			faltosos = entityManager.createQuery(fsql.toString());
			faltosos.setParameter("ug", ug.getId());
			
			//return query.getResultList();
		}
		
		faltosos.setParameter("inicio", dtIni);
		faltosos.setParameter("fim", dtFim);
		
		faltaVeiculos 	= faltosos.getResultList();
		veiculos		= query.getResultList();
		veiculos.removeAll(faltaVeiculos);

		return veiculos;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculobyModelo(String filter) {
		List<Veiculo> listaVeiculo = new ArrayList<Veiculo>();
		Query query = entityManager.createQuery("Select object(v) from Veiculo v where v.status != 6 and lower(v.modelo.descricao) like lower(:busca)");
		query.setParameter("busca", "%"+filter+"%");
		listaVeiculo = query.getResultList();
		return listaVeiculo;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> retrieveByUG(String ug){
		Query query = entityManager.createQuery("Select v from Veiculo v where v.ua.ug.id = ? and v.status != 6");
		query.setParameter(1, ug);
		List<Veiculo> result =  query.getResultList();
		return result;
	}

	//	@SuppressWarnings("unchecked")
	//	public List<Veiculo> findByPCR(String p, String c, String r){
	//		List<Veiculo> result = executeResultListGenericQuery("findByPCR", p, c, r);
	//		return result;
	//	}
	//
	@SuppressWarnings("unchecked")
	public List<Integer> retrieveIdsVeiculos(){
		Query query = entityManager.createQuery("SELECT v.id FROM Veiculo v WHERE v.dataTransmissao != null");
		List<Integer> result = query.getResultList();
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findByOrgaoPlacaChassiRenavam(String orgaoId, String placa, String chassi, String renavam){

		List<Veiculo> result = null;

		try {

			StringBuilder sql = new StringBuilder("SELECT v.id FROM Veiculo v WHERE v.status != 6 ");
			
			if(orgaoId != null){
				sql.append(" and v.ua.ug.id = :id");
			}
			if(placa != null){
				sql.append(" and v.placa = :placa");
			}
			if(chassi != null){
				sql.append(" and v.chassi = :chassi");
			}
			if(renavam != null){
				sql.append(" and v.renavam = :renavam");
			}

			Query query = entityManager.createQuery(sql.toString());

			if(orgaoId != null){
				query.setParameter("id", orgaoId);
			}
			if(placa != null){
				query.setParameter("placa", placa);
			}
			if(chassi != null){
				query.setParameter("chassi", chassi);
			}
			if(renavam != null){
				query.setParameter("renavam", renavam);
			}

			result = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findByUG(UG ug) {
		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		String ugid = ug.getId();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id and o.status != 6");
			query.setParameter("id", ugid);
			veiculos = query.getResultList();
		}
		return veiculos;
	}

	public List<Veiculo> veiculosAtivoscomcota(UG ug){

		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		String ugid = ug.getId();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id and o.cota.cotaDisponivel > 0 and o.status != 6");
			query.setParameter("id", ugid);
			veiculos = query.getResultList();
		}
		return veiculos;		
	}

	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosAtivosByUG(UG ug) {
		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		String ugid = ug.getId();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id and o.status = 0 order by o.placa");
			query.setParameter("id", ugid);
			veiculos = query.getResultList();
		}
		return veiculos;
	}
	
	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosInativosByUG(UG ug) {
		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		String ugid = ug.getId();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id and o.status = 6 order by o.placa");
			query.setParameter("id", ugid);
			veiculos = query.getResultList();
		}
		return veiculos;
	}

	public List<Veiculo> findByPlaca(String placa){
		List<Veiculo> result = executeResultListQuery("findByPlaca", placa.toUpperCase());
		return result;
	}

	public Veiculo findByPlacaSingle(String placa){
		Veiculo veiculo = null;
		try {
			veiculo = executeSingleResultQuery("findByPlaca", placa);
		} catch (NoResultException e) {
			//e.printStackTrace();
			return null;
		}
		return veiculo;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Map<Integer, Veiculo> retrieveMapVeiculo() {

		Map<Integer, Veiculo> map = new HashMap<Integer, Veiculo>();
		Query query = entityManager.createQuery("SELECT distinct(v) FROM Veiculo v WHERE  v.status != 6");
		List<Veiculo> veiculos = query.getResultList();
		for (Veiculo v : veiculos) {
			if(!map.containsKey(v.getId())){
				map.put(v.getId(), v);
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public List<String> veiculosAusentes(List<String> placas){

		List<String> sql = new ArrayList<String>();
		for (String s : placas) {
			s = "\'" + s + "\'";
			sql.add(s);
		}
		String str = sql.toString();
		str = str.replace("[", "(").replaceAll("]", ")");
		String sqlq = "SELECT v.placa FROM Veiculo v WHERE v.status != 6 and v.placa IN " + str;
		Query query = entityManager.createQuery(sqlq);
		List<String> result = query.getResultList();
		placas.removeAll(result);
		return placas;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> veiculoIds(String ugId){
		StringBuilder sql = new StringBuilder("SELECT v.id FROM Veiculo v WHERE v.status != 6");
		if(ugId != null){
			sql.append(" and v.ua.ug.id = :id");
		}
		Query query = entityManager.createQuery(sql.toString());
		if(ugId != null){
			query.setParameter("id", ugId);
		}
		return query.getResultList();
	}
	/**
	 * Busca a quantidade abastecida no m�s para o ve�culo informado
	 * 
	 * @param veiculo
	 * @return Double
	 */
	public Double findQtdAbastecida(Veiculo veiculo) {
		Double result = 0.0;
		Date data = new Date();
		Query query = entityManager.createNamedQuery("AtendimentoAbastecimento.findCota");
		query.setParameter("veiculo", veiculo);
		query.setParameter("mes", data.getMonth());
		result = (Double) query.getSingleResult();
		return result;
	}


	@SuppressWarnings("unchecked")
	@Transactional
	public List<Veiculo> veiculos(){
		Query query = null;
		List<Veiculo> result = null;
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)){
			query = entityManager.createQuery("select v from Veiculo v where  v.status != 6");
			result = query.getResultList();
		} else {
			query = entityManager.createQuery("select v from Veiculo v where v.ua.ug.id = :ug and v.status != 6");
			query.setParameter("ug", user.getPessoa().getUa().getUg().getId());
			result = query.getResultList();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Veiculo> veiculosRastreados(){
		Query query = null;
		List<Veiculo> result = null;
		User user = SgfUtil.usuarioLogado();
		if(SgfUtil.isAdministrador(user)){
			query = entityManager.createQuery("select v from Veiculo v where v.dataTransmissao != null and  v.status != 6");
			result = query.getResultList();
		} else {
			query = entityManager.createQuery("select v from Veiculo v where v.dataTransmissao != null and v.ua.ug.id = :ug and v.status != 6");
			query.setParameter("ug", user.getPessoa().getUa().getUg().getId());
			result = query.getResultList();
		}
		return result;
	}

	/**
	 * Método quer gera as últimas transmissões dos veículos que possuem rastreamento. As informações serão
	 * mostradad na tela de monitoramento
	 * @param veiculos
	 * @param exibirPontos
	 * @param velocidadeMaxima
	 * @param init
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<PontoDTO> searchPontosMonitoramento(List<Veiculo> veiculos, boolean exibirPontos, boolean exibirRastro, Float velocidadeMaxima, Date init) {

		Map<Integer, List<Transmissao>> mapTransmissoes = null;
		List<Integer> veiculoIds = new ArrayList<Integer>();
		for (Veiculo veiculo : veiculos) {
			veiculoIds.add(veiculo.getId());
		}

		if (exibirPontos) {
			Date end = DateUtil.getDateNow();
			mapTransmissoes  = transmissaoService.findByVeiculoList(veiculoIds, init, end);
		}

		int k = 0;
		List<PontoDTO> pontos = new ArrayList<PontoDTO>();

		try {
			for (Veiculo veiculo : veiculos) {
				if (veiculo.getDataTransmissao() != null) {
					PontoDTO m = new PontoDTO();
					try {
						String nomeVeiculo = veiculo.getModelo() != null ? veiculo.getModelo().getDescricao().replace("|", ":") : "";
						m.setId(veiculo.getId());
						m.setNome(nomeVeiculo);
						m.setPlaca(veiculo.getPlaca());
						m.setVelocidade(veiculo.getVelocidade());
						m.setOdometro(veiculo.getOdometro() != null ? veiculo.getOdometro() : 0);

						Float kmAtual = veiculo.getOdometro() == null ? 0f: veiculo.getOdometro();
						m.setKmAtual(kmAtual);
						m.setDataTransmissao(veiculo.getDataTransmissao());

						String pontoNome = veiculo.getPontoProximo() == null ? "" : veiculo.getPontoProximo().getDescricao();
						m.setPontoProximo(pontoNome);
						m.setDistPontoProximo(veiculo.getDistancia() != null ? veiculo.getDistancia() : 0);

						String marcaDescricao = veiculo.getModelo() == null ? "" : veiculo.getModelo().getMarca().getDescricao();
						m.setMarca(marcaDescricao);
						m.setCor(veiculo.getCor());

						String modeloDescricao = veiculo.getModelo() == null ? "" : veiculo.getModelo().getDescricao();
						m.setModelo(modeloDescricao);
						m.setAno(veiculo.getAnoFabricacao());
						if(veiculo.getIgnicao() != null){
							m.setIgnicao(veiculo.getIgnicao());
						}
						if (veiculo.getGeometry() != null) {
							m.setLng((float) ((Point)veiculo.getGeometry()).x);
							m.setLat((float) ((Point)veiculo.getGeometry()).y);
						}

						if (exibirPontos) {

							m.setVelocidadeMaxima(velocidadeMaxima);

							List<PontoDTO> rastro = new ArrayList<PontoDTO>();
							List<Transmissao> historicoTransmissoes = mapTransmissoes.get(veiculo.getId());

							if (historicoTransmissoes != null && historicoTransmissoes.size() > 0) {

								for (Transmissao t : historicoTransmissoes) {

									try {
										PontoDTO pdto = new PontoDTO();
										pdto.setNome("" + veiculo.getId());
										pdto.setPlaca(veiculo.getPlaca());
										pdto.setPontoProximo(t.getPonto().getDescricao());
										pdto.setDistPontoProximo(t.getDistancia());
										pdto.setVelocidade(t.getVelocidade());
										pdto.setDataTransmissao(t.getDataTransmissao());
										pdto.setLng((float) ((Point)veiculo.getGeometry()).x);
										pdto.setLat((float) ((Point)veiculo.getGeometry()).y);
										pdto.setVelocidadeMaxima(velocidadeMaxima);
										rastro.add(pdto);
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
									}
								}
							}

							m.setRastro(rastro);
						}

						m.setIndex(k++);
						pontos.add(m);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return classificar(pontos);
	}

	@Transactional(readOnly = true)
	private List<PontoDTO> classificar(List<PontoDTO> list) {
		Collections.sort(list, new Comparator<PontoDTO>() {
			public int compare(PontoDTO m1, PontoDTO m2) {
				return m1.getDataTransmissao().before(m2.getDataTransmissao()) ? 1 : -1;
			}
		});
		return list;
	}

	
	@SuppressWarnings("unchecked")
	public List<Veiculo> pesquisa(Veiculo veiculo, Date dtInicial, Date dtFinal, UG ugPesquisa, String abastecimento) {
		// TODO Auto-generated method stub
		
		StringBuilder sql = new StringBuilder("select distinct(v) from Veiculo v  \n");
		boolean flag;
		
		if(dtInicial != null) {
			flag = true;
	
			sql.append("left join v.abastecimentos as a \n");
			sql.append("with a.dataAutorizacao between :dtInicial and :dtFinal \n");
			
			
			if(abastecimento.equals("true")){
				sql.append("and a.veiculo is not null \n");
			}else {
				sql.append("and a.veiculo is null \n");
			}
			
		} else {
			flag = false;
		}
		sql.append("where 1=1 \n");
		
		if(ugPesquisa != null){
			sql.append("and v.ua.ug.id = '"+ugPesquisa.getId()+"' \n");
		}
		
		if(StringUtils.hasText(veiculo.getPropriedade())){
			sql.append("and v.propriedade = '"+veiculo.getPropriedade()+"' \n");
		}
		if(StringUtils.hasText(veiculo.getPlaca())){
			sql.append("and v.placa like UPPER('%"+veiculo.getPlaca()+"%') \n");
		}
		if(StringUtils.hasText(veiculo.getChassi())){
			sql.append("and v.chassi like '%"+veiculo.getChassi()+"%' \n");
		}
		if(StringUtils.hasText(veiculo.getRenavam())){
			sql.append("and v.renavam like '%"+veiculo.getRenavam()+"%' \n");
		}
		if(veiculo.getStatus() != null){
			sql.append("and v.status = "+veiculo.getStatus().getValor()+" \n");
		}
		
		Query query = entityManager.createQuery(sql.toString());
		
		if(flag){
			if(dtInicial != null)
				query.setParameter("dtInicial", dtInicial);
			if(dtFinal != null)
				query.setParameter("dtFinal", dtFinal);
		}
		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends Veiculo> pesquisaVeiculoCotasKm(Veiculo veiculo, Object object, Object object2, UG ugPesquisa) {
		// TODO Auto-generated method stub
		StringBuilder sql = new StringBuilder("select o from Veiculo o where o.propriedade <> 'PMF' and o in(select c.veiculo from CotaKm c) and o.status != 6 \n");
		if(ugPesquisa != null){
			sql.append("and o.ua.ug.id = '"+ugPesquisa.getId()+"' \n");
		}
		
		if(StringUtils.hasText(veiculo.getPlaca())){
			sql.append("and o.placa like '%"+veiculo.getPlaca()+"%' \n");
		}
		if(StringUtils.hasText(veiculo.getChassi())){
			sql.append("and o.chassi like '%"+veiculo.getChassi()+"%' \n");
		}
		if(StringUtils.hasText(veiculo.getRenavam())){
			sql.append("and o.renavam like '%"+veiculo.getRenavam()+"%' \n");
		}
		Query query = entityManager.createQuery(sql.toString());
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosLocados(UG ug){
		
		StringBuilder sql = new StringBuilder("select o from Veiculo o where o.propriedade = 'Locado' and o.status != 6 \n");
		if(ug != null){
			sql.append("and o.ua.ug.id = '"+ug.getId()+"' \n");
		}
		
		Query query = entityManager.createQuery(sql.toString());
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Veiculo> findVeiculosComCotakm(UG ug){
		
		List<Veiculo> veiculos = new ArrayList<Veiculo>();
		String ugid = ug.getId();
		if(ug != null){
			Query query = entityManager.createQuery("select o from Veiculo o where o.ua.ug.id = :id and o.status != 6");
			query.setParameter("id", ugid);
			veiculos = query.getResultList();
		}
		return veiculos;
	}
	

}