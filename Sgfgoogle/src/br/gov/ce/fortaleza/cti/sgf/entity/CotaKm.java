/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author deivid
 *
 */
@Entity
@Table(name = "tb_cota_quilometragem" , schema = "SGF")
public class CotaKm implements Serializable{
	
	private static final long serialVersionUID = 3034791151106383758L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cota_km")
	@SequenceGenerator(name = "seq_cota_km", sequenceName = "sgf.codcotakm_seq", allocationSize = 1)
	@Column(name = "codcotakm", nullable = false)
	private Integer id;
	
	@Column(name = "COTA_KM")
	private Double cotaKm;
	
	@Column(name = "COTA_KM_DISP_MES")
	private Double cotaKmDisponivel;

	@OneToOne
	@JoinColumn(name = "CODVEICULO")
	private Veiculo veiculo;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getCotaKm() {
		return cotaKm;
	}

	public void setCotaKm(Double cotaKm) {
		this.cotaKm = cotaKm;
	}

	public Double getCotaKmDisponivel() {
		return cotaKmDisponivel;
	}

	public void setCotaKmDisponivel(Double cotaKmDisponivel) {
		this.cotaKmDisponivel = cotaKmDisponivel;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cotaKm == null) ? 0 : cotaKm.hashCode());
		result = prime
				* result
				+ ((cotaKmDisponivel == null) ? 0 : cotaKmDisponivel.hashCode());
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
		CotaKm other = (CotaKm) obj;
		if (cotaKm == null) {
			if (other.cotaKm != null)
				return false;
		} else if (!cotaKm.equals(other.cotaKm))
			return false;
		if (cotaKmDisponivel == null) {
			if (other.cotaKmDisponivel != null)
				return false;
		} else if (!cotaKmDisponivel.equals(other.cotaKmDisponivel))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
}