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
	private String CityName;
	private int Lat;
	private int Lng;
	private String LastError = null;

	/**
	 * Creator of object
	 * @param owner Owner of City (Player GUID)
	 * @param lat Latitude of City
	 * @param lng Longtitude of City
	 */
	public CityObj(String owner, int lat, int lng) {
		GUID=UUID.randomUUID().toString();
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
	public CityObj(Connection con, String GUID) throws SQLException {
		GetDBData(con,GUID);
	}

	public String GetGUID() {
		return GUID;
	}


	public int GetLat() {
		return Lat;
	}

	public int GetLng() {
		return Lng;
	}

	public String GetLastError() {
		return LastError;
	}

	/**
	 * Load data from DB
	 * @param con Connection to DB
	 * @param GUID GUID of City
	 */
	@Override
	public void GetDBData(Connection con, String GUID) throws SQLException {
		PreparedStatement stmt;

		stmt = con.prepareStatement("SELECT GUID,CITYNAME,Lat,Lng from cities WHERE GUID=?");
			stmt.setString(1, GUID);
			ResultSet rs=stmt.executeQuery();
		rs.beforeFirst();
		if (rs.isBeforeFirst()) {
			rs.first();

			this.GUID = rs.getString("GUID");
			CityName = rs.getString("CITYNAME");
			Lat = rs.getInt("Lat");
			Lng = rs.getInt("Lng");
			stmt.close();
		} else this.GUID = GUID;

	}

	/**
	 * Write data to DB
	 * @param con Connection to DB
	 */
	@Override
	public void SetDBData(Connection con) throws SQLException {
		PreparedStatement stmt;

		stmt = con.prepareStatement("INSERT IGNORE INTO cities(GUID,CITYNAME,Lat,Lng) VALUES ("
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?"
					+ ")");
			stmt.setString(1, GUID);
			stmt.setString(2, CityName);
		stmt.setInt(3, Lat);
		stmt.setInt(4, Lng);
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


	}


	/**
	 * Generate JSON
	 * @return JSON string of Object
	 */
	@Override
	public String toString(){
		return"{"+
				"GUID:"+'"'+GUID+'"'+
				",CityName:"+'"'+CityName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				"}";

	}



}
