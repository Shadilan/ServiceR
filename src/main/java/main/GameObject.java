package main;

import java.sql.Connection;
import java.sql.SQLException;

public interface GameObject {
	void GetDBData(Connection con, String GUID) throws SQLException;

	void SetDBData(Connection con) throws SQLException;

	String toString();


}
