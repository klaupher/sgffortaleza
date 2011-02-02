package br.gov.ce.fortaleza.cti.sgf.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.activation.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Classe utilizada para realizar a conexão com o banco de dados do
 * patrimônio, em Oracle
 * 
 * @author lafitte
 * @since 23/04/2010
 *
 */
public class ConnectOracle {

	/**
	 * Retorna a conexão do pool de conexões do container. As configurações da conexão estão no 
	 * arquivo de contexto
	 * @return
	 * @throws NamingException
	 * @throws SQLException
	 */
	public static Connection connection() throws NamingException, SQLException {

		java.sql.Connection conection = null;
		try {
			Context initContext = new InitialContext();
			javax.sql.DataSource ds = (javax.sql.DataSource)   initContext.lookup("java:comp/env/jdbc/patrimonio");
			conection = ds.getConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conection;
	}
}
