package main;

import java.lang.String;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Player info
 * @author Shadilan
 */
public class PlayerObj implements GameObject {
	private String UserName;
	private String Token;
	private String GUID;public String GetGUID(){return GUID;}
	private int Lat;
	private int Lng;
	private int Gold;public int GetGold(){return Gold;} public void SetGold(int Gold){this.Gold=Gold;}
	private String City;public String GetCity(){return City;}
	private int Influence;
	//TODO:Change to exception
	public String LastError;public String GetLastError(){return LastError;}

	public PlayerObj(){

	}
	/**
	 * Load data from DB by GUID
	 * @param con DB Connection
	 * @param GUID GUID
	 */
	public PlayerObj(Connection con,String GUID){
		GetDBData(con,GUID);
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
	public void GetDBData(Connection con, String GUID) {

		PreparedStatement stmt;

		try {
			stmt=con.prepareStatement("SELECT a.PlayerName, a.USERTOKEN, a.GUID, a.Lat, a.Lng, a.Gold, a.Influence, b.guid city FROM gplayers a LEFT JOIN cities b ON (b.owner = a.guid) WHERE GUID=? LIMIT 0,1");
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
			City=rs.getString("city");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LastError=e.toString();
		}
		
	}

	/**
	 * Load data from DB by Secure Token
	 * @param con Connection to DB
	 * @param UserToken Secure Token
	 */
	public void GetDBDataByToken(Connection con, String UserToken) {
		PreparedStatement stmt;
		
		try {
			stmt=con.prepareStatement("SELECT a.PlayerName, a.USERTOKEN, a.GUID, a.Lat, a.Lng, a.Gold, a.Influence, b.guid city FROM gplayers a  LEFT JOIN cities b ON (b.owner = a.guid) WHERE USERTOKEN=? LIMIT 0,1");
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
				City=rs.getString("city");
			} else
			{
				LastError="NOUSERFOUND "+UserToken;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LastError=e.toString();
		}
	}

	/**
	 * Write data to DB
	 * @param con Connection to db
	 */
	@Override
	public void SetDBData(Connection con) {

		PreparedStatement stmt;
		
		try {
			stmt=con.prepareStatement("UPDATE gplayers set "
					+ "Lat=?,"
					+ "Lng=?,"
					+ "Gold=?,Influence=? WHERE GUID=?");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setInt(3, Gold);
			stmt.setInt(4, Influence);
			stmt.setString(5, GUID);
			stmt.execute();
			stmt=con.prepareStatement("UPDATE aobject set "
					+ "Lat=?,"
					+ "Lng=?"
					+ "WHERE GUID=?");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setString(3, GUID);
			stmt.execute();
			con.commit();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Generate JSON String
	 * @return JSON String
	 */
	@Override
	public String toString(){
		String CityB="N";
		if (City!=null && City.length()>0){
			CityB="Y";
		}
		return"{"+
				"GUID:"+'"'+GUID+'"'+
				",PlayerName:"+'"'+UserName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				",Gold:"+Gold+
				",Influence:"+Influence+
				",City:"+'"'+CityB+'"'+
				"}";

	}

	/**
	 * Create City with player as owner
	 * @param con Connection to DB
	 */
    public void CreateCity(Connection con){
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
	 */
    public void RemoveCity(Connection con,String Target){
        if (!City.equals(Target)) return;
        PreparedStatement pstmt;
        try {
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
        } catch (SQLException e) {
            LastError=e.toString();
        }
        SetDBData(con);
    }

}
