package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.Ponto;
import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.PontoService;
import br.gov.ce.fortaleza.cti.sgf.service.TransmissaoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.MapUtil;
import br.gov.ce.fortaleza.cti.sgf.util.SgfUtil;
import br.gov.ce.fortaleza.cti.sgf.util.dto.PontoDTO;


@Scope("session")
@Component("pontoBean")
public class PontoBean extends EntityBean<Integer, Ponto>{

	private static final Log logger = LogFactory.getLog(PontoBean.class);

	@Autowired
	private PontoService service;

	@Autowired
	private TransmissaoService transmissaoService;

	@Autowired
	private VeiculoService veiculoService;

	private Boolean reload = true;
	private Boolean ignicao = true;
	private Float velocidade;
	private Float odometro;

	private Double lat;
	private Double lng;
	private String ponto;
	private List<PontoDTO> pontos;
	private List<Veiculo> veiculos;
	private String geoms = new String("");
	private String veiculosPontos;
	private boolean show = false;
	private Veiculo veiculo;
	private String route = new String("");
	private Integer timeReload = 1000;

	@Override
	protected Integer retrieveEntityId(Ponto entity) {
		return entity.getId();
	}

	@Override
	protected PontoService retrieveEntityService() {
		return this.service;
	}

	@Override
	protected Ponto createNewEntity() {
		Ponto ponto = new Ponto();
		return ponto;
	}

	/**
	 * monta uma string de saida para cosntrução da rota de uma veículo
	 * @return
	 */
	public String mostrarRotaVeiculo(){
		this.reload = false;
		String tmp = "";
		if(this.veiculo != null){
			Veiculo v = veiculoService.retrieve(veiculo.getId());
			Date current = new Date();
			Date initialDate = DateUtil.adicionarOuDiminuir(current, -8*DateUtil.HOUR_IN_MILLIS); // dimuinui 4 horas
			List<Transmissao> transmissoes = transmissaoService.findByVeiculo(veiculo.getId(), initialDate, current);
			if(transmissoes.size() > 0){
				for (Transmissao transmissao : transmissoes) {
					Float vel = transmissao.getVelocidade() == null ? 0F : transmissao.getVelocidade();
					Float odometro = transmissao.getOdometro() == null ? 0F : transmissao.getOdometro();
					Double x = ((Point)transmissao.getGeometry()).x;
					Double y = ((Point)transmissao.getGeometry()).y;
					String placa = v.getPlaca();
					String pprox = transmissao.getPonto() != null ? transmissao.getPonto().getDescricao() : "";
					Float dist = transmissao.getDistancia() == null ? 0F : transmissao.getDistancia();
					tmp += y + "#%" + x + "#%" + transmissao.getVeiculoId() + "#%" +  placa + "#%" + vel + "#%" + odometro + 
					"#%" + pprox + "#%" + dist + "#%" + DateUtil.parseAsString("dd/MM/yyyy HH:mm", transmissao.getDataTransmissao())  + "$####";
				}
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sem.rota");
				return FAIL;
			}
		} else {
			return FAIL;
		}
		this.route = tmp;
		return SUCCESS;
	}

	/**
	 * construi uma string de saída com o posicionamentos dos veículos para visualização dos veículos no mapa
	 * @return
	 */
	public String mostrarVeiculosMapa(){
		this.veiculos = new ArrayList<Veiculo>();
		this.geoms = new String("");
		if(SgfUtil.isAdministrador(SgfUtil.usuarioLogado())){
			this.veiculos = veiculoService.veiculosRastreados();
			this.geoms = MapUtil.parseVeiculosMap(this.veiculos);   
		} else if(this.veiculo == null){
			this.veiculos = veiculoService.findByUG(SgfUtil.usuarioLogado().getPessoa().getUa().getUg());
			this.geoms = MapUtil.parseVeiculosMap(this.veiculos);
		} else {
			this.veiculos.add(this.veiculo);
			this.geoms = MapUtil.parseVeiculosMap(this.veiculos);
		}
		return SUCCESS;
	}

	@Override
	public String prepareSave() {
		this.lat = null;
		this.lng = null;
		return super.prepareSave();
	}

	@Override
	public String prepareUpdate() {
		this.lat = ((Point)this.entity.getGeometry()).x;
		this.lng = ((Point)this.entity.getGeometry()).y;
		return super.prepareUpdate();
	}

	@Override
	public String update() {
		if(this.lat != null && this.lat != null){
			this.entity.setGeometry(new Point(this.lat, this.lng));
			return super.update();
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
			return FAIL;
		}
	}

	@Transactional
	public String salvarLocalizacaoVeiculo(){
		this.reload = true;
		if(this.veiculo != null){
			if(this.lat != null && this.lng != null){
				Transmissao transmissao = new Transmissao();
				transmissao.setVeiculoId(this.veiculo.getId());
				transmissao.setDataTransmissao(new Date());
				transmissao.setGeometry(new Point(this.lat, this.lng));
				transmissao.setVelocidade(this.velocidade);
				transmissao.setIgnicao(this.ignicao);
				transmissao.setOdometro(this.odometro);
				this.transmissaoService.save(transmissao);
				setCurrentBean(currentBeanName());
				setCurrentState(SEARCH);
				return SUCCESS;
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
				return FAIL;
			}
		} else {
			JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.localizacao.nao.encontrada");
			return FAIL;
		}
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public String getPonto() {
		return ponto;
	}

	public void setPonto(String ponto) {
		this.ponto = ponto;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public List<PontoDTO> getPontos() {
		return pontos;
	}

	public void setPontos(List<PontoDTO> pontos) {
		this.pontos = pontos;
	}

	public String getGeoms() {
		return geoms;
	}

	public void setGeoms(String geoms) {
		this.geoms = geoms;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public Boolean getReload() {
		return reload;
	}

	public void setReload(Boolean reload) {
		this.reload = reload;
	}

	public Integer getTimeReload() {
		return timeReload;
	}

	public void setTimeReload(Integer timeReload) {
		this.timeReload = timeReload;
	}

	public Boolean getIgnicao() {
		return ignicao;
	}

	public void setIgnicao(Boolean ignicao) {
		this.ignicao = ignicao;
	}

	public Float getVelocidade() {
		return velocidade;
	}

	public void setVelocidade(Float velocidade) {
		this.velocidade = velocidade;
	}

	public Float getOdometro() {
		return odometro;
	}

	public void setOdometro(Float odometro) {
		this.odometro = odometro;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(List<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}

	public String getVeiculosPontos() {
		return veiculosPontos;
	}

	public void setVeiculosPontos(String veiculosPontos) {
		this.veiculosPontos = veiculosPontos;
	}
	
}
