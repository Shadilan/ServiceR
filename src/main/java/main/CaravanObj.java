package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CaravanObj implements GameObject {

	private String GUID;
	private String Owner;
	private int Lat;
	private int Lng;
	private String LastError;

	@Override
	public void GetDBData(Connection con, String GUID) {
		// TODO Auto-generated method stub

	}
    public CaravanObj(RouteObj route){

    }
    public CaravanObj(Connection con,String GUID){
        GetDBData(con,GUID);
    }
	@Override
	public void SetDBData(Connection con) {
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement("INSERT IGNORE INTO cities(GUID,Owner,Lat,Lng) VALUES ("
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?"
                    + ")");
            stmt.setString(1, GUID);
            stmt.setString(2, Owner);
            stmt.setInt(3, Lat);
            stmt.setInt(4, Lng);
            stmt.execute();
            stmt = con.prepareStatement("INSERT IGNORE INTO aobject(GUID,ObjectType,Lat,Lng) VALUES("
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?"
                    + ")");
            stmt.setString(1, GUID);
            stmt.setString(2, "CITY");
            stmt.setInt(3, Lat);
            stmt.setInt(4, Lng);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LastError = e.toString();

        }
    }

}
