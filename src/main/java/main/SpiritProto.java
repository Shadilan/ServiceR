package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class SpiritProto {
	String lastError="";
	
	public SpiritProto(){
		
	}
	public Connection ConnectDB(){
		Context ctx=null;
		DataSource ds=null;
		Connection con =null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			lastError=e.toString();
			return null;
		}
		try {
			ds = (DataSource)ctx.lookup("java:jboss/datasources/MySQLDS");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastError=e.toString(true);
			try{
				ctx.close();
			} catch(NamingException e2){
				
			}
			return null;
		}
		try {
			con = ds.getConnection("adminUuszpdJ","5FKl3fnWFT55");
			con.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastError=e.toString();
			try{
				ctx.close();
				
			} catch(NamingException e2){
				
			}
		}
		
		
		
		return con;
	}
	public String GetToken(String Login,String Password){
		
		Connection con=ConnectDB();
		String Token = UUID.randomUUID().toString();
		int result=0;
		PreparedStatement pstmt=null;
		try {
		pstmt= con.prepareStatement("SELECT count(1) from gplayers WHERE PlayerName=? and Password=?");
		pstmt.setString(1, Login);
		
			pstmt.setString(2, Password);
		
			ResultSet rs=pstmt.executeQuery();
			rs.first();
			if (rs.getInt(1)==0) {
				result=2;
				lastError="User not Found: "+Login+" "+Password;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			result=1;
			lastError=e.toString();
		}
		if (result==0)
		{
			try {
				pstmt= con.prepareStatement("UPDATE gplayers SET USERTOKEN=? WHERE PlayerName=? and Password=?");
				pstmt.setString(1, Token);
				pstmt.setString(2, Login);
			
				pstmt.setString(3, Password);
			
				pstmt.execute();
				con.commit();
				result=0;
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				result=1;
				lastError=e.toString();
			}
		}
		try {
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			lastError=e.toString();
		}
		
		if (result==0) return "{Token:"+'"'+Token+'"'+"}"; else return lastError;
			
	}
	
	public String GetData(String token,int Lat,int Lng){

		Connection con = ConnectDB();
		if (con == null) return "NoConnection";

		PlayerObj player=new PlayerObj();
		player.GetDBDataByToken(con,token);
		if (!player.isLogin()) {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return player.LastError;
		}
		player.setPos(Lat, Lng);
		player.SetDBData(con);
		String result="";
		int cnt=0;
		try {
			PreparedStatement stmt=con.prepareStatement("select GUID,ObjectType from aobject where SQRT(POWER(?-Lat,2)+POWER(?-Lng,2))<1000");
			stmt.setInt(1, Lat);
			stmt.setInt(2, Lng);
			


			ResultSet rs=stmt.executeQuery();
			rs.beforeFirst();
			ArrayList<CityObj> Cities=new ArrayList<CityObj>();
			while (rs.next())
			{
				
				String GUID=rs.getString(1);
				String ObjType=rs.getString(2);
				if (ObjType.equalsIgnoreCase("CITY")){
					CityObj City=new CityObj();
					City.GetDBData(con, GUID);
					Cities.add(City);
				} else
				if (ObjType.equalsIgnoreCase("AMBUSH")){
				};
			}
			result="{Player:"+player.toString();
			String citiesinfo=null;
			for (CityObj city:Cities){
				if (citiesinfo==null) citiesinfo=city.toString(); else citiesinfo+=","+city.toString();
				
			}
			if (citiesinfo!=null) result+=","+"Cities:["+citiesinfo+"]";
			result+="}";

			con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result=e.toString();
		}
		
		try {
			if (!con.isClosed()){
				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		//������������ �����
		
	}

	public String SimpleCommand(String token,int Lat,int Lng,String action,String target){
	    Connection con = ConnectDB();
        PlayerObj player=new PlayerObj();
        player.GetDBDataByToken(con,token);
        if (player.isLogin()==false){
            return "{Result:"+'"'+"Error"+'"'+",Code:"+'"'+"E000001"+'"'+",Message:"+'"'+"User not login in."+'"'+"}";
        }
        player.setPos(Lat,Lng);
        if (action.equals("addCity")){
            player.CreateCity(con);
        } else
        if (action.equals("removeCity")){
            player.RemoveCity(con, target);
        } else
		return "{Result:"+'"'+"Error"+'"'+",Code:"+'"'+"E000000"+'"'+",Message:"+'"'+"Command uknown."+'"'+"}";
        if (player.GetLastError()!=null)
        {
        	try {
				con.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	return "{Result:"+'"'+"Error"+'"'+",Code:"+'"'+"E000003"+'"'+",Message:"+'"'+player.GetLastError()+'"'+"}";
        }
        return "{Result:"+'"'+"Success"+'"'+",Code:"+'"'+"S"+'"'+",Message:"+'"'+"Done"+'"'+"}";


	}
	
}
