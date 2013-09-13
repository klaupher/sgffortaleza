/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author deivid
 *
 */
@Entity
@Table(name = "tb_diario_hist_ultrapassado" , schema = "SGF")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class DiarioKmHistoricoUltrapassado implements Serializable{

	private static final long serialVersionUID = 8513935547719318376L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_diario_km_hist")
	@SequenceGenerator(name = "seq_diario_km_hist", sequenceName = "sgf.diario_km_hist_seq", allocationSize = 1)
	@Column(name = "coddiariohist", nullable = false)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "codcotakm")
	private CotaKm cotaKm;
	
	@Column
	private Double cotaDisponivelMes;
	
	@Column
	private Double cotaUltrapassadaMes;
	
	@Column
	private String anoMes;
	
	@Temporal(TemporalType.DATE)
	private Date dataCadastro;
	
	@Temporal(TemporalType.DATE)
	private Date dataAtualizacao;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CotaKm getCotaKm() {
		return cotaKm;
	}

	public void setCotaKm(CotaKm cotaKm) {
		this.cotaKm = cotaKm;
	}

	public Double getCotaDisponivelMes() {
		return cotaDisponivelMes;
	}

	public void setCotaDisponivelMes(Double cotaDisponivelMes) {
		this.cotaDisponivelMes = cotaDisponivelMes;
	}

	public Double getCotaUltrapassadaMes() {
		return cotaUltrapassadaMes;
	}

	public void setCotaUltrapassadaMes(Double cotaUltrapassadaMes) {
		this.cotaUltrapassadaMes = cotaUltrapassadaMes;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public String getAnoMes() {
		return anoMes;
	}

	public void setAnoMes(String anoMes) {
		this.anoMes = anoMes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anoMes == null) ? 0 : anoMes.hashCode());
		result = prime
				* result
				+ ((cotaDisponivelMes == null) ? 0 : cotaDisponivelMes
						.hashCode());
		result = prime
				* result
				+ ((cotaUltrapassadaMes == null) ? 0 : cotaUltrapassadaMes
						.hashCode());
		result = prime * result
				+ ((dataAtualizacao == null) ? 0 : dataAtualizacao.hashCode());
		result = prime * result
				+ ((dataCadastro == null) ? 0 : dataCadastro.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiarioKmHistoricoUltrapassado other = (DiarioKmHistoricoUltrapassado) obj;
		if (anoMes == null) {
			if (other.anoMes != null)
				return false;
		} else if (!anoMes.equals(other.anoMes))
			return false;
		if (cotaDisponivelMes == null) {
			if (other.cotaDisponivelMes != null)
				return false;
		} else if (!cotaDisponivelMes.equals(other.cotaDisponivelMes))
			return false;
		if (cotaUltrapassadaMes == null) {
			if (other.cotaUltrapassadaMes != null)
				return false;
		} else if (!cotaUltrapassadaMes.equals(other.cotaUltrapassadaMes))
			return false;
		if (dataAtualizacao == null) {
			if (other.dataAtualizacao != null)
				return false;
		} else if (!dataAtualizacao.equals(other.dataAtualizacao))
			return false;
		if (dataCadastro == null) {
			if (other.dataCadastro != null)
				return false;
		} else if (!dataCadastro.equals(other.dataCadastro))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
