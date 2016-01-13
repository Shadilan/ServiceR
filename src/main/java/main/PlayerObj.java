package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Player info
 * @author Shadilan
 */
public class PlayerObj implements GameObject {
	//TODO:Change to exception
	public String LastError;
	private String UserName;
	private String Token;
	private String GUID;
	private int Lat;
	private int Lng;
	private int Gold;
	private String City;
	private String CityName;
	private int Influence;

	public PlayerObj() {

	}
	/**
	 * Load data from DB by GUID
	 * @param con DB Connection
	 * @param GUID GUID
	 */
	public PlayerObj(Connection con, String GUID) throws SQLException {
        if (GUID.substring(1, 1).equals("T")) {
            GetDBDataByToken(con, GUID);
        } else
            GetDBData(con, GUID);
    }

	public void addGold(int Gold) {
		this.Gold += Gold;
	}

    public String GetGUID() {
        return GUID;
	}

	public int GetGold() {
		return Gold;
	}

	public void SetGold(int Gold) {
		this.Gold = Gold;
	}

	public String GetCity() {
		return City;
	}

	public String GetLastError() {
		return LastError;
	}

	/**
	 *
	 * @return True when player Loaded
	 */
	public boolean isLogin()
	{
		return GUID != null;
	}

	/**
	 * Set Position of Player
	 * @param lat Latitude
	 * @param lng Longtitude
	 */
	public void setPos(int lat,int lng){
		this.Lat=lat;
		this.Lng=lng;
	}

	/**
	 * Load data from DB by GUID
	 * @param con Connetion to DB
	 * @param GUID GUID of Player
	 */
	@Override
	public void GetDBData(Connection con, String GUID) throws SQLException {

		PreparedStatement stmt;

		stmt = con.prepareStatement("SELECT a.PlayerName, a.USERTOKEN, a.GUID, a.Lat, a.Lng, a.Gold, a.Influence,HomeCity,(select cityname from cities c where c.guid=a.guid) cityname FROM gplayers a WHERE GUID=? LIMIT 0,1");
			stmt.setString(1, GUID);
			ResultSet rs=stmt.executeQuery();
			rs.first();
			UserName=rs.getString("PlayerName");
			Token=rs.getString("USERTOKEN");
			this.GUID=rs.getString("GUID");
			Lat=rs.getInt("Lat");
			Lng=rs.getInt("Lng");
			Gold=rs.getInt("Gold");
			Influence=rs.getInt("Influence");
		City = rs.getString("HomeCity");
		CityName = rs.getString("cityname");
			stmt.close();

		
	}

	/**
	 * Load data from DB by Secure Token
	 * @param con Connection to DB
	 * @param UserToken Secure Token
	 */
	public void GetDBDataByToken(Connection con, String UserToken) throws SQLException {
		PreparedStatement stmt;


        stmt = con.prepareStatement("SELECT a.PlayerName, a.USERTOKEN, a.GUID, a.Lat, a.Lng, a.Gold, a.Influence,HomeCity,(select cityname from cities c where c.guid=a.HomeCity) cityname FROM gplayers a  WHERE USERTOKEN=? LIMIT 0,1");
        stmt.setString(1, UserToken);
			ResultSet rs=stmt.executeQuery();
			if (rs.isBeforeFirst()){
				rs.first();
				UserName=rs.getString("PlayerName");
				Token=rs.getString("USERTOKEN");
				GUID=rs.getString("GUID");
				Lat=rs.getInt("Lat");
				Lng=rs.getInt("Lng");
				Gold=rs.getInt("Gold");
				Influence=rs.getInt("Influence");
                City = rs.getString("HomeCity");
                CityName = rs.getString("cityname");
            } else
			{
				LastError="NOUSERFOUND "+UserToken;
			}
			stmt.close();


	}

	/**
	 * Write data to DB
	 * @param con Connection to db
	 */
	@Override
	public void SetDBData(Connection con) throws SQLException {

		PreparedStatement stmt;

			stmt=con.prepareStatement("UPDATE gplayers set "
					+ "Lat=?,"
					+ " Lng=?,"
					+ " Gold=? WHERE GUID=?");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setInt(3, Gold);
		stmt.setString(4, GUID);
			stmt.execute();
			stmt=con.prepareStatement("UPDATE aobject set "
					+ "Lat=?,"
					+ " Lng=?"
					+ " WHERE GUID=?");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setString(3, GUID);
			stmt.execute();
			con.commit();
			stmt.close();


	}

	/**
	 * Generate JSON String
	 * @return JSON String
	 */
	@Override
	public String toString(){

		return"{"+
				"GUID:"+'"'+GUID+'"'+
				",PlayerName:"+'"'+UserName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				",Gold:"+Gold+
				",Influence:"+Influence+
				",City:" + '"' + CityName + '"' +
				"}";

	}



	/* Removed by new conception. Zlodiak
	/**
	 * Create City with player as owner
	 * @param con Connection to DB

	public void CreateCity(Connection con) throws SQLException {
		if (City!=null)
        	{
        	LastError="You already have city";
        	return;
        	}
        CityObj newCity=new CityObj(GUID,Lat,Lng);
        
        newCity.SetDBData(con);
        if (newCity.GetLastError()!=null){
        	LastError=newCity.GetLastError();
        	return;
        }
        try {
			con.commit();
		} catch (SQLException e) {
			LastError="Error on commit";
			e.printStackTrace();
		}
        City=newCity.GetGUID();
    }

	/**
	 * Remove city (with check if Player is Owner)
	 * @param con Connection to DB
	 * @param Target City to remove

	public void RemoveCity(Connection con, String Target) throws SQLException {
		if (!City.equals(Target)) return;
        PreparedStatement pstmt;

		pstmt= con.prepareStatement("DELETE FROM cities WHERE GUID=?");
            pstmt.setString(1, Target);
            pstmt.execute();
            pstmt= con.prepareStatement("DELETE FROM aobject WHERE GUID=?");
            pstmt.setString(1, Target);
            pstmt.execute();
            pstmt= con.prepareStatement("DELETE FROM route WHERE RSTART=? or REND=?");
            pstmt.setString(1, Target);
            pstmt.setString(2, Target);
            pstmt.execute();
            //TODO:Remove all caravans
            
            con.commit();
            pstmt.close();

		SetDBData(con);
    }
	*/

	public String setHome(String Player, String City) {
		PreparedStatement stmt;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("update gplayers set HomeCity=? where GUID=?");
			stmt.setString(1, City);
			stmt.setString(2, Player);
			stmt.execute();
			con.commit();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
		return MyUtils.getJSONSuccess("Home city changed.");
	}

}
