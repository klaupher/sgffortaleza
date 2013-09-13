/**
 * 
 */
package br.gov.ce.fortaleza.cti.sgf.service;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.fortaleza.cti.sgf.entity.DiarioKmHistoricoUltrapassado;

/**
 * @author deivid
 *
 */
@Repository
@Transactional
public class DiarioKmHistoricoService extends BaseService<Integer, DiarioKmHistoricoUltrapassado>{
	
}
