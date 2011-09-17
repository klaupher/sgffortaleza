package br.gov.ce.fortaleza.cti.sgf.bean;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.gov.ce.fortaleza.cti.sgf.entity.Transmissao;
import br.gov.ce.fortaleza.cti.sgf.entity.Veiculo;
import br.gov.ce.fortaleza.cti.sgf.service.BaseService;
import br.gov.ce.fortaleza.cti.sgf.service.TransmissaoService;
import br.gov.ce.fortaleza.cti.sgf.service.VeiculoService;
import br.gov.ce.fortaleza.cti.sgf.util.DateUtil;
import br.gov.ce.fortaleza.cti.sgf.util.JSFUtil;
import br.gov.ce.fortaleza.cti.sgf.util.dto.MapDTO;

@Scope("session")
@Component("mapBean")
public class MapBean extends EntityBean<Integer, MapDTO>  {

	public String pontos = new String("");
	private Date horaInicial = DateUtil.adicionarOuDiminuir(new Date(), -DateUtil.HOUR_IN_MILLIS)  ;
	private Date horaFinal = new Date();
	public Date start = new Date();
	public Date end = new Date();
	public Integer tempoConsulta;

	@Autowired
	private VeiculoService veiculoService;

	@Autowired
	private TransmissaoService transmissaoService;

	private Veiculo veiculo;
	private String route = new String("");

	@Override
	protected MapDTO createNewEntity() {
		return new MapDTO();
	}

	@Override
	protected Integer retrieveEntityId(MapDTO entity) {
		throw new IllegalStateException("Não implementado");
	}

	@Override
	protected BaseService<Integer, MapDTO> retrieveEntityService() {
		throw new IllegalStateException("Não implementado");
	}

	public String searchAddress() {
		setCurrentState(MapDTO.SEARCH_ADDRESS);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String veiculosMap() {
		setCurrentState(MapDTO.VEICULOS_MAP);
		setCurrentBean(currentBeanName());
		this.entities = null;
		return SUCCESS;
	}

	public String veiculosRoute() {
		setCurrentState(MapDTO.VEICULOS_ROUTE);
		setCurrentBean(currentBeanName());
		this.entities = null;
		showVehiclesOnMap();
		return SUCCESS;
	}

	public boolean isSearchAddress() {
		return MapDTO.SEARCH_ADDRESS.equals(getCurrentState());
	}

	public boolean isVeiculosMapState(){
		return MapDTO.VEICULOS_MAP.equals(getCurrentState());
	}

	public boolean isVeiculosRouteState(){
		return MapDTO.VEICULOS_ROUTE.equals(getCurrentState());
	}

	public String showVehiclesOnMap(){		
		List<Veiculo> veiculos = veiculoService.veiculosRastreados();
		String line = new String("");

		for (Veiculo v : veiculos) {
			Float odometro = v.getOdometro() != null ? v.getOdometro() : 0F;
			Float dist = v.getDistancia() != null ? v.getDistancia() : 0F;
			Boolean ignicao = v.getIgnicao() == null ? false : v.getIgnicao();
			String ptprox = v.getPontoProximo()==null ? "" : v.getPontoProximo().getDescricao();

			line += ((Point)v.getGeometry()).x + "##" +  ((Point)v.getGeometry()).y + "##" + v.getId() + "##" + v.getPlaca() + "##" + v.getVelocidade() + "##"
					+ odometro  + "##" +  ignicao + "##" + ptprox + "##" + 																																																																																																																																																																	dist + "##" + DateUtil.parseAsString("dd/MM/yyyy HH:mm", v.getDataTransmissao()) + "##$##";
		}
		this.pontos = line;
		return SUCCESS;
	}

	/**
	 * monta uma string de saida para cosntrução da rota de uma veículo
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String showVehiclesRouteOnMap(){
		String line = "";
		this.route = "";
		Calendar cal = Calendar.getInstance();
		if(this.veiculo != null){
			Veiculo v = veiculoService.retrieve(veiculo.getId());

			if(this.start != null){
				cal.setTime(this.start);
				cal.set(Calendar.HOUR_OF_DAY, this.horaInicial.getHours());
				cal.set(Calendar.MINUTE, this.horaInicial.getMinutes());
				cal.set(Calendar.SECOND, this.horaInicial.getSeconds());
				this.start = cal.getTime();
			} else {
				this.start = DateUtil.getDateStartDay(this.start);
			}

			if(this.end != null){
				cal.setTime(this.end);
				cal.set(Calendar.HOUR_OF_DAY, this.horaFinal.getHours());
				cal.set(Calendar.MINUTE, this.horaFinal.getMinutes());
				cal.set(Calendar.SECOND, this.horaFinal.getSeconds());
				this.end = cal.getTime();
			} else {
				this.end = DateUtil.getDateStartDay(this.end);
			}

			List<Transmissao> transmissoes = transmissaoService.retrieveByVeiculo(veiculo.getId(), this.start, this.end);
			if(transmissoes.size() > 0){
				for (Transmissao transmissao : transmissoes) {
					Float vel = transmissao.getVelocidade() == null ? 0F : transmissao.getVelocidade();
					Float odometro = transmissao.getOdometro() == null ? 0F : transmissao.getOdometro();
					Double x = ((Point)transmissao.getGeometry()).x;
					Double y = ((Point)transmissao.getGeometry()).y;
					String placa = v.getPlaca();
					String pprox = transmissao.getPonto() != null ? transmissao.getPonto().getDescricao() : "";
					Float dist = transmissao.getDistancia() == null ? 0F : transmissao.getDistancia();
					line += x + "##" + y + "##" + transmissao.getVeiculoId() + "##" +  placa + "##" + vel + "##" + odometro + "##" +  transmissao.getIgnicao() + "##" + pprox + "##" + dist + "##" + DateUtil.parseAsString("dd/MM/yyyy HH:mm", transmissao.getDataTransmissao())  + "##$##";
				}
			} else {
				JSFUtil.getInstance().addErrorMessage("msg.error.veiculo.sem.rota");
				return FAIL;
			}
		} else {
			return FAIL;
		}
		this.route = line;
		return SUCCESS;
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

	public String getPontos() {
		return pontos;
	}

	public void setPontos(String pontos) {
		this.pontos = pontos;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Date getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(Date horaInicial) {
		this.horaInicial = horaInicial;
	}

	public Date getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(Date horaFinal) {
		this.horaFinal = horaFinal;
	}

	public Integer getTempoConsulta() {
		return tempoConsulta;
	}

	public void setTempoConsulta(Integer tempoConsulta) {
		this.tempoConsulta = tempoConsulta;
	}
}