package br.gov.ce.fortaleza.cti.sgf.relatorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.hibernate.Session;

public class GeradorRelatorio {
	
	@SuppressWarnings("deprecation")
	public static byte[] gerarPdfBD(Map<?, ?> parametros, String jasperPath)
			throws IOException, JRException {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("sgf");
		EntityManager em = factory.createEntityManager();
		Session session = (Session) em.getDelegate();
		Connection con = session.connection();
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperPath);
		byte array[] = JasperRunManager.runReportToPdf(jasperReport, (Map<String, Object>) parametros, con);
		return array;
	}

	public static byte[] gerarPdfCollection(Map<?, ?> parametros, Collection<?> colecao, 
			String jasperPath) throws IOException, JRException{
		
		JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(colecao);
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperPath);
		byte array[] = JasperRunManager.runReportToPdf(jasperReport,(Map<String, Object>) parametros, ds);
		return array;
	}
	
	
	@SuppressWarnings("deprecation")
	public static byte[] gerarExcelCollection(Map<?, ?> parametros, Collection<?> colecao, 
			String jasperPath) throws IOException, JRException{
		
		byte 						array[];
		JRBeanCollectionDataSource 	ds 			= new JRBeanCollectionDataSource(colecao);
		//JasperDesign 				desenho 	= JRXmlLoader.load(jasperPath);
		//JasperReport 				relatorio 	= JasperCompileManager.compileReport( jasperPath );  
	    JasperPrint 				impressao 	= JasperFillManager.fillReport(jasperPath, (Map<String, Object>) parametros, ds);  
	    JRXlsExporter 				exporter 	= new JRXlsExporter();
	    ByteArrayOutputStream 		xlsReport 	= new ByteArrayOutputStream();
	    
	    exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, impressao);    
	    exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, xlsReport);     
	    exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
	    exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
	    exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
	    exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);     
	    exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);     
	    exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, "c:/java/relatorio.xls");
	    exporter.exportReport();     
	    array = xlsReport.toByteArray();
		
		return array;
	}
	
}
