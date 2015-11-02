package main;

import javax.naming.NamingException;
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
	public CityObj(String owner,int lat,int lng){
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
			rs.first();
			this.GUID=rs.getString("GUID");
			CityName=rs.getString("CITYNAME");
			Lat=rs.getInt("Lat");
			Lng=rs.getInt("Lng");
			stmt.close();

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

	//проверяем возможность создания маршрута. если есть незавершенный маршрут, то нельзя
	public String checkCreateRoute(String Owner) {
		PreparedStatement stmt;
		try {
			Connection con = DBUtils.ConnectDB();
			ResultSet rs;
			stmt = con.prepareStatement("select count(1) cnt from routes where Owner=? and finish is null");
			stmt.setString(1, Owner);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt") > 0) {
				return "Сначала завершите текущий маршрут";
			} else {
				return "ОК";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	//создаем незавершенный (из одного города) маршрут, проверок внутри нет - все проверки отдельно (ну или надо обсуждать)
	public String createRoute(String Owner, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		int c_lat;
		int c_lng;
		try {
			Connection con = DBUtils.ConnectDB();
			String GUID_ROUTE = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into routes (guid,owner,start) values(?,?,?)");
			stmt.setString(1, GUID_ROUTE);
			stmt.setString(2, Owner);
			stmt.setString(3, City);
			stmt.execute();

			stmt = con.prepareStatement("select lat, lng from cities where GUID=?");
			stmt.setString(1, City);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			c_lat = rs.getInt("lat");
			c_lng = rs.getInt("lng");

			String GUID = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into waypoints values(?,?,'1',?,?)");
			stmt.setString(1, GUID);
			stmt.setString(2, GUID_ROUTE);
			stmt.setInt(3, c_lat);
			stmt.setInt(4, c_lng);

			stmt.execute();

			return "ОК";
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	//проверяем возможность завершения маршрута в этом городе. если уже есть маршрут между двумя этими городами, то отказываем
	public String checkFinishRoute(String Owner, String Route, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("select count(1) cnt from routes where (start,finish) in (select start,? from routes where guid=?) or (finish,start) in (select start,? from routes where guid=?)");
			stmt.setString(1, City);
			stmt.setString(2, Route);
			stmt.setString(3, City);
			stmt.setString(4, Route);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt") > 0) {
				return "Такой маршрут уже существует";
			} else {
				return "ОК";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	//завершаем маршрут в указанном городе
	public String finishRoute(String Owner, String Route, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		int c_lat;
		int c_lng;
		try {
			Connection con = DBUtils.ConnectDB();
			//добавим конечный город в вэйпойнты
			stmt = con.prepareStatement("select lat, lng from cities where GUID=?");
			stmt.setString(1, City);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			c_lat = rs.getInt("lat");
			c_lng = rs.getInt("lng");
			String GUID = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into waypoints values(?,?,'1',?,?)");
			stmt.setString(1, GUID);
			stmt.setString(2, Route);
			stmt.setInt(3, c_lat);
			stmt.setInt(4, c_lng);
			stmt.execute();

			//незавершенный маршрут сделаем завершенным
			stmt = con.prepareStatement("update routes set finish=? where guid=?");
			stmt.setString(1, City);
			stmt.setString(2, Route);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
		return "ОК";
	}
}
