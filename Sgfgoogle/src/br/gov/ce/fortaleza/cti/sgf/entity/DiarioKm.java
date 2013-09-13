/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tb_diario_quilometragem" , schema = "SGF")
public class DiarioKm implements Serializable{

	private static final long serialVersionUID = -8847021089636142515L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_diario_km")
	@SequenceGenerator(name = "seq_diario_km", sequenceName = "sgf.coddiariokm_seq", allocationSize = 1)
	@Column(name="coddiariokm", nullable = false)
	private Integer id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_cadastro")
	private Date dataCadastro;
	
	@ManyToOne
	@JoinColumn(name = "codusuariocriacao")
	private User usuarioCricao;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_alteracao")
	private Date dataAlteracao;
	
	@ManyToOne
	@JoinColumn(name = "codusuarioalteracao")
	private User usuarioAlteracao;
	
	@ManyToOne
	@JoinColumn(name = "codcotakm")
	private CotaKm cotaKm;
	
	@Column(name = "vl_inicial")
	private Double valorInicial;
	
	@Column(name = "vl_final")
	private Double valorFinal;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public User getUsuarioCricao() {
		return usuarioCricao;
	}

	public void setUsuarioCricao(User usuarioCricao) {
		this.usuarioCricao = usuarioCricao;
	}

	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public User getUsuarioAlteracao() {
		return usuarioAlteracao;
	}

	public void setUsuarioAlteracao(User usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}

	public Double getValorInicial() {
		return valorInicial;
	}

	public void setValorInicial(Double valorInicial) {
		this.valorInicial = valorInicial;
	}

	public Double getValorFinal() {
		return valorFinal;
	}

	public void setValorFinal(Double valorFinal) {
		this.valorFinal = valorFinal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataAlteracao == null) ? 0 : dataAlteracao.hashCode());
		result = prime * result
				+ ((dataCadastro == null) ? 0 : dataCadastro.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((valorFinal == null) ? 0 : valorFinal.hashCode());
		result = prime * result
				+ ((valorInicial == null) ? 0 : valorInicial.hashCode());
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
		DiarioKm other = (DiarioKm) obj;
		if (dataAlteracao == null) {
			if (other.dataAlteracao != null)
				return false;
		} else if (!dataAlteracao.equals(other.dataAlteracao))
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
		if (valorFinal == null) {
			if (other.valorFinal != null)
				return false;
		} else if (!valorFinal.equals(other.valorFinal))
			return false;
		if (valorInicial == null) {
			if (other.valorInicial != null)
				return false;
		} else if (!valorInicial.equals(other.valorInicial))
			return false;
		return true;
	}

	public CotaKm getCotaKm() {
		return cotaKm;
	}

	public void setCotaKm(CotaKm cotaKm) {
		this.cotaKm = cotaKm;
	}

}
