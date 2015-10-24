package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Shadilan
 */
public class CityObj implements GameObject {
	private String GUID;
	private String Owner;
	private String CityName;
	private int Lat;
	private int Lng;

	private String LastError=null;

	/**
	 * Creator of object
	 * @param owner Owner of City (Player GUID)
	 * @param lat Latitude of City
	 * @param lng Longtitude of City
	 */
	public CityObj(String owner,int lat,int lng){
		GUID=UUID.randomUUID().toString();
		Owner=owner;
		Lat=lat;
		Lng=lng;
	}

	//Default constructor
	public CityObj() {

	}

	/**
	 * Constructor with loading from DB
	 * @param con Connection to DB
	 * @param GUID GUID of City
	 */
	public CityObj(Connection con,String GUID) {
		GetDBData(con,GUID);
	}

	/**
	 * Load data from DB
	 * @param con Connection to DB
	 * @param GUID GUID of City
	 */
	@Override
	public void GetDBData(Connection con, String GUID) {
		PreparedStatement stmt;
		
		try {
			stmt=con.prepareStatement("SELECT GUID,Owner,CITYNAME,Lat,Lng from cities WHERE GUID=? LIMIT 0,1");
			stmt.setString(1, GUID);
			ResultSet rs=stmt.executeQuery();
			rs.first();
			this.GUID=rs.getString("GUID");
			Owner=rs.getString("Owner");
			CityName=rs.getString("CITYNAME");
			Lat=rs.getInt("Lat");
			Lng=rs.getInt("Lng");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LastError=e.toString();
		}
	}

	/**
	 * Write data to DB
	 * @param con Connection to DB
	 */
	@Override
	public void SetDBData(Connection con) {
		PreparedStatement stmt;
		try {
			stmt=con.prepareStatement("INSERT IGNORE INTO cities(GUID,CITYNAME,Owner,Lat,Lng) VALUES ("
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?"
					+ ")");
			stmt.setString(1, GUID);
			stmt.setString(2, CityName);
			stmt.setString(3, Owner);
			stmt.setInt(4, Lat);
			stmt.setInt(5, Lng);
			stmt.execute();
			stmt=con.prepareStatement("INSERT IGNORE INTO aobject(GUID,ObjectType,Lat,Lng) VALUES("
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
			LastError=e.toString();
		}

	}

	/**
	 * Geter
	 * @return GUID of object
	 */
	public String GetGUID(){
		return GUID;
	}

	/**
	 * Generate JSON
	 * @return JSON string of Object
	 */
	@Override
	public String toString(){
		return"{"+
				"GUID:"+'"'+GUID+'"'+
				",Owner:"+'"'+Owner+'"'+
				",CityName:"+'"'+CityName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				"}";

	}

	/**
	 * Getter
	 * @return return LastError
	 */
	public String GetLastError(){return LastError;}

}
