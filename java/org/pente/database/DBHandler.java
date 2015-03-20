
package org.pente.database;

import java.sql.*;

/**
 * @author dweebo
 */
public interface DBHandler {

    /** Get a connection to the database
     *  @return Connection The databae connection
     *  @exception Exception If a connection cannot be made
     */
    public Connection getConnection() throws SQLException;

    /** Free a connection to the database
     *  @param con The database connection to free
     *  @exception If the connection cannot be freed
     */
    public void freeConnection(Connection con) throws SQLException;

    /** Clean up any resources */
    public void destroy();
}