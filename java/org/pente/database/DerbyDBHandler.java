package org.pente.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author dweebo
 */
public class DerbyDBHandler implements DBHandler {

	private String path;

	public DerbyDBHandler(String path) throws Exception {
		this.path = path;
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
	}

	public void destroy() {
		boolean gotSQLExc = false;
		   try {
		      DriverManager.getConnection("jdbc:derby:;shutdown=true");
		   } catch (SQLException se)  {
		      if ( se.getSQLState().equals("XJ015") ) {
		         gotSQLExc = true;
		      }
		   }
		   if (!gotSQLExc) {
		      System.out.println("Database did not shut down normally");
		   }  else  {
		      System.out.println("Database shut down normally");
		   }
	}

	public void freeConnection(Connection con) throws SQLException {
		con.close();
	}

	public Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(
			"jdbc:derby:" + path);
		//con.createStatement().execute("CALL SYSCS_UTIL.SYSCS_SET_STATISTICS_TIMING(1)");
		return con;
	}

}
