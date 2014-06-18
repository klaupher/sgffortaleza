package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "TB_VEICULO_SUBSTITUTO", schema = "SGF")
public class VeiculoSubstituto implements Serializable {

	private static final long serialVersionUID = 3596795955187165026L;


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_substituto")
	@SequenceGenerator(name = "seq_substituto", sequenceName = "sgf.codsubstituto_seq", allocationSize = 1)
	@Column(name = "COD_SUBSTITUTO")
	private Integer id;
	
	
	@Column(name = "PLACA_SUBSTITUTO", length = 7)
	private String placa;
	
	@ManyToOne
	@JoinColumn(name = "CODVEICULO")
	protected Veiculo veiculo;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATAINI")
	private Date dataInicio;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATAFIM")
	private Date dataFim;

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
