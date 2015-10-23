package main;

import java.lang.String;
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
	private String City;
	private int Influence;
	public String LastError;

	public boolean isLogin()
	{
		if (GUID !=null) return true;
		return false;
	};
	public void setPos(int lat,int lng){
		this.Lat=lat;
		this.Lng=lng;
	}
	@Override
	public void GetDBData(Connection con, String GUID) {
		// TODO Auto-generated method stub
		PreparedStatement stmt=null;
		
		try {
			stmt=con.prepareStatement("SELECT a.PlayerName, a.USERTOKEN, a.GUID, a.Lat, a.Lng, a.Gold, a.Influence, b.guid city FROM gplayers a LEFT JOIN cities b ON (b.owner = a.guid) WHERE GUID=? LIMIT 0,1");
			stmt.setString(1, GUID);
			ResultSet rs=stmt.executeQuery();
			rs.first();
			UserName=rs.getString("PlayerName");
			Token=rs.getString("USERTOKEN");
			GUID=rs.getString("GUID");
			Lat=rs.getInt("Lat");
			Lng=rs.getInt("Lng");
			Gold=rs.getInt("Gold");
			Influence=rs.getInt("Influence");
			City=rs.getString("city");
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
	@Override
	public String toString(){
		String CityB="N";
		if (City!=null && City.length()>0){
			CityB="Y";
		}
		String result="{"+
				"GUID:"+'"'+GUID+'"'+
				",PlayerName:"+'"'+UserName+'"'+
				",Lat:"+Lat+
				",Lng:"+Lng+
				",Gold:"+Gold+
				",Influence:"+Influence+
				",City:"+'"'+CityB+'"'+
				"}";
		return result;
	}
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
			// TODO Auto-generated catch block
			LastError="Error on commit";
			e.printStackTrace();
		}
        City=newCity.GetGUID();
    }
    public void RemoveCity(Connection con,String Target){
        if (!City.equals(Target)) return;
        //������� ��������
        //�������� �������
        //������� ��������

        //������� �� ������� ��������
        //������� �����
        PreparedStatement pstmt=null;
        try {
            pstmt= con.prepareStatement("DELETE FROM cities WHERE GUID=?");
            pstmt.setString(1, Target);
            pstmt.execute();
            pstmt= con.prepareStatement("DELETE FROM aobject WHERE GUID=?");
            pstmt.setString(1, Target);
            pstmt.execute();
            con.commit();
            pstmt.close();
        } catch (SQLException e) {
            LastError=e.toString();
        }
        SetDBData(con);
        //������� �� ������� ��������

    }
    public String GetLastError(){return LastError;}
}
