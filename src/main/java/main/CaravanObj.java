package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Caravan object
 */
public class CaravanObj implements GameObject {

	private String GUID;
	private String Owner;
	private int Lat;
	private int Lng;
    private int ELat;
    private int ELng;
	private String LastError;

    /**
     * Load data from DB
     * @param con Connection to DB
     * @param GUID GUID of object
     */
	@Override
	public void GetDBData(Connection con, String GUID) {
		// TODO:Get data from DB code

	}

    /**
     * Constructor
     * @param route Route for which caravan spawned
     */
    public CaravanObj(RouteObj route){

    }

    /**
     * Create data from DB
     * @param con Connection to DB
     * @param GUID GUID of object
     */
    public CaravanObj(Connection con,String GUID){
        GetDBData(con,GUID);
    }

    /**
     * Write data to DB
     * @param con Connection to DB
     */
	@Override
	public void SetDBData(Connection con) {
        PreparedStatement stmt;

        try {
            stmt = con.prepareStatement("INSERT IGNORE INTO caravan(GUID,Owner,Lat,Lng) VALUES ("
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
            e.printStackTrace();
            LastError = e.toString();

        }
    }

}
