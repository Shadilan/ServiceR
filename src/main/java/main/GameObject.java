package main;

import java.sql.Connection;

public interface GameObject {
	public void GetDBData(Connection con,String GUID);
	public void SetDBData(Connection con);
	public String toString();
}
