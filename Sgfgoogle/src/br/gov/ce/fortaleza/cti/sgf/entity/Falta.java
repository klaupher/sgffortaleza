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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "TB_FALTA", schema = "SGF")
@NamedQueries( {
	
	@NamedQuery(name = "Falta.findCota", query = "select sum(1) from " +
	"Falta o where o.veiculo.id = :id and (o.dataFalta >= :inicio and o.dataFalta <= :fim)"
	),
	
	// select * from tb_abastecimento a, tb_cadveiculo v where
	// a.codveiculo = v.codveiculo and a.codsolabastecimento = 
	//(select max(codsolabastecimento) from tb_abastecimento where v.codveiculo = 175)
	
	//@NamedQuery(name = "Falta.findLast", query = "select a from Falta a where a.id = (select max(a1.id) from Abastecimento a1 where a1.veiculo.id = ? and a1.status = 2)"),
	//@NamedQuery(name = "Falta.findByPosto", query = "select a from Abastecimento a where a.posto.codPosto = ? and a.status = ? order by a.dataAutorizacao desc"),
	//@NamedQuery(name = "Abastecimento.findByPeriodoAndPosto", query = "select a from Abastecimento a where " +
	//		"a.posto.codPosto = ? and a.dataAutorizacao BETWEEN ? and ? and a.status = ?  order by a.dataAutorizacao desc"),
	//@NamedQuery(name = "Abastecimento.findByPeriodoAndPostoUG", query = "select a from Abastecimento a where a.veiculo.ua.ug.id = ? and " +
	//		"a.posto.codPosto = ? and a.dataAutorizacao BETWEEN ? and ? and a.status = ?  order by a.dataAutorizacao desc") 
})
public class Falta implements Serializable {

	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = -5061858557705018194L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_falta")
	@SequenceGenerator(name = "seq_falta", sequenceName = "sgf.tb_falta_codfalta_seq", allocationSize = 1)
	@Column(name = "CODFALTA", nullable = false)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "CODVEICULO", nullable = false)
	private Veiculo veiculo;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DTFALTA", nullable = false)
	private Date dataFalta;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((veiculo == null) ? 0 : veiculo.hashCode());
		result = prime * result + ((dataFalta == null) ? 0 : dataFalta.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) return false;
		Falta other = (Falta) obj;

		if (getId() == null) { if (other.getId() != null) return false;
		} else if (!getId().equals(other.getId())) return false;
		if (getDataFalta() == null) { if (other.getDataFalta() != null) return false;
		} else if (!getDataFalta().equals(other.getDataFalta())) return false;
		if (getVeiculo() == null) { if (other.getVeiculo() != null) return false;
		} else if (!getVeiculo().equals(other.getVeiculo())) return false;
		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Date getDataFalta() {
		return dataFalta != null ? (Date) dataFalta.clone() : null;
	}

	public void setDataFalta(Date dataFalta) {
		this.dataFalta = dataFalta;
	}

}
