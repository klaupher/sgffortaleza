package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable	
public class VeiculoSubstitutoPk implements Serializable {

	private static final long serialVersionUID = 8489339816482874485L;

	@ManyToOne
	@JoinColumn(name = "CODVEICULO")
	protected Veiculo veiculo;
	
	@Column(name = "PLACA_SUBSTITUTO", length = 7)
	private String placa;
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VeiculoSubstitutoPk other = (VeiculoSubstitutoPk) obj;
		return ((veiculo == null && other.veiculo == null) || (veiculo != null && veiculo.equals(other.veiculo)))
		&& ((placa == null && other.placa == null) || (placa != null && placa.equals(other.placa)));
	}

	public int hashCode() {
		int result = 1;
		result = 31 * result + ((veiculo == null) ? 0 : veiculo.hashCode());
		result = 31 * result + ((placa == null) ? 0 : placa.hashCode());
		return result;
	}
	
	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}	
}
