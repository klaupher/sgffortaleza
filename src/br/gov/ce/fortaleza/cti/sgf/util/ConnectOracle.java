package br.gov.ce.fortaleza.cti.sgf.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
	 * Conexão que será utilizada para se comunicar com o banco de dados
	 */
	private Connection con = null;
	
	/**
	 * Realiza a conexão com o banco de dados do Patrimônio
	 * @return
	 */
	public Connection conectaOracle() {
		String driverName = "oracle.jdbc.driver.OracleDriver";
		try {
			// Carrega o driver JDBC do Oracle
			Class.forName(driverName);
			String url = "jdbc:oracle:thin:@172.31.2.9:1521:pmf";
			String username = "asiweb";
			String password = "asiweb";
			con = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			StackTraceElement[] stack = e.getStackTrace();
			String message = "Class:" + stack[0].getClassName() + "\nMethod:" + stack[0].getMethodName() + "\nLine:" + stack[0].getLineNumber() + "\n";
			//Mail.sendMailSsl(Mail.FROM, Mail.TO, "Error: " + e.getCause(), message);
			JSFUtil.getInstance().addErrorMessage("msg.error.acess.database");
		} catch (SQLException e) {
			StackTraceElement[] stack = e.getStackTrace();
			String message = "Class:" + stack[0].getClassName() + "\nMethod:" + stack[0].getMethodName() + "\nLine:" + stack[0].getLineNumber() + "\n";
			//Mail.sendMailSsl(Mail.FROM, Mail.TO, "Error: " + e.getCause(), message);
			JSFUtil.getInstance().addErrorMessage("msg.error.acess.database");
		}
		return this.con;
	}
}
