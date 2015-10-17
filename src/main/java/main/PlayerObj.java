package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerObj implements GameObject {
	private String UserName;
	private String Token;
	private String GUID;
	private int Lat;
	private int Lng;
	private int Gold;
	public String LastError;
	public boolean isLogin()
	{
		if (GUID !=null) return true;
		return false;
	};
	public void setPos(int lat,int lng){
		Lat=lat;
		Lng=lng;
	}
	@Override
	public void GetDBData(Connection con, String GUID) {
		// TODO Auto-generated method stub
		PreparedStatement stmt=null;
		
		try {
			stmt=con.prepareStatement("SELECT PlayerName,USERTOKEN,GUID,Lat,Lng,Gold from GPLAYERS WHERE GUID=? LIMIT 0,1");
			stmt.setString(1, GUID);
			ResultSet rs=stmt.executeQuery();
			rs.first();
			UserName=rs.getString("PlayerName");
			Token=rs.getString("USERTOKEN");
			GUID=rs.getString("GUID");
			Lat=rs.getInt("Lat");
			Lng=rs.getInt("Lng");
			Gold=rs.getInt("Gold");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LastError=e.toString();
		}
		
	}
	public void GetDBDataByToken(Connection con, String UserToken) {
		// TODO Auto-generated method stub
		PreparedStatement stmt=null;
		
		try {
			stmt=con.prepareStatement("SELECT PlayerName,USERTOKEN,GUID,Lat,Lng,Gold from GPLAYERS WHERE USERTOKEN=? LIMIT 0,1");
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
			} else
			{
				LastError="NOUSERFOUND "+UserToken;
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LastError=e.toString();
		}
	}

	@Override
	public void SetDBData(Connection con) {
		// TODO Auto-generated method stub
		PreparedStatement stmt=null;
		
		try {
			stmt=con.prepareStatement("UPDATE GPLAYERS set "
					+ "Lat=?,"
					+ "Lng=?,"
					+ "Gold=? WHERE GUID=? LIMIT 0,1");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setInt(3, Gold);
			stmt.setString(4, GUID);
			stmt.execute();
			stmt=con.prepareStatement("UPDATE AOBJECT set "
					+ "Lat=?,"
					+ "Lng=?,"
					+ "WHERE GUID=? LIMIT 0,1");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			stmt.setString(3, GUID);
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Override
	public String toString(){
		String result="Player:{"+
				"GUID:"+'"'+GUID+'"'+
				",PlayerName:"+'"'+UserName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				",Gold:"+Gold+
				"}";
		return result;
	}

}
