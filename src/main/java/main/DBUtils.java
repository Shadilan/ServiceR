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
    private static DBUtils instance;
    private Connection con;
    public static Connection ConnectDB() throws NamingException, SQLException {
        if (instance ==null) {
            Context ctx;
            DataSource ds;
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:jboss/datasources/MySQLDS");
            instance.con = ds.getConnection("adminUuszpdJ", "5FKl3fnWFT55");
            instance.con.setAutoCommit(false);
        }
        return instance.con;
    }

}
