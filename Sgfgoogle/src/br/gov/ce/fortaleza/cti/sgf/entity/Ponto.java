package br.gov.ce.fortaleza.cti.sgf.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import org.postgis.Geometry;

@Entity
@Table(name = "TB_PONTO", schema = "SGF")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Ponto implements Serializable {

	private static final long serialVersionUID = 1313916916086964373L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="CODPONTO", nullable = false)
	private Integer id;

	@Column(name="DESCPONTO", length =255)
	private String descricao;

	@Type(type="br.gov.ce.fortaleza.cti.sgf.conversores.GeometryType")
	@Column(name = "GEOMPONTO", nullable = false)
	private Geometry geometry;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int hashCode() {
		int result = 1;
		result = 31 * result + ((id == null) ? 0 : id.hashCode());
		result = 31 * result + ((geometry == null) ? 0 : geometry.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ponto other = (Ponto) obj;
		return ((id == null && other.id == null) || (id != null && id.equals(other.id))) && 
		((geometry == null && other.geometry == null) || (geometry != null && geometry.equals(other.geometry)));
	}
}