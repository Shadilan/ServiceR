package main;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Shadilan
 */
public class DBUtils {
    public static Connection ConnectDB() throws NamingException, SQLException {
        Context ctx;
        DataSource ds;
        Connection con;
        ctx = new InitialContext();
        ds = (DataSource) ctx.lookup("java:jboss/datasources/MySQLDS");
        con = ds.getConnection("adminUuszpdJ", "5FKl3fnWFT55");
        con.setAutoCommit(false);

        return con;
    }

}
