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

/**
 * Main object for Server
 */
public class SpiritProto {
	String lastError="";

	/**
	 * Default Constructor
	 */
	public SpiritProto(){
		
	}
	public Connection ConnectDB(){
		Context ctx;
		DataSource ds;
		Connection con =null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			lastError=e.toString();
			return null;
		}
		try {
			ds = (DataSource)ctx.lookup("java:jboss/datasources/MySQLDS");
		} catch (NamingException e) {
			e.printStackTrace();
			lastError=e.toString(true);
			try{
				ctx.close();
			} catch(NamingException e2){
				lastError=e2.toString();
			}
			return null;
		}
		try {
			con = ds.getConnection("adminUuszpdJ","5FKl3fnWFT55");
			con.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			lastError=e.toString();
			try{
				ctx.close();
				
			} catch(NamingException e2){
				lastError=e2.toString();
			}
		}
		
		
		
		return con;
	}

	/**
	 * Get Token
	 * @param Login Login of user
	 * @param Password Password of user
	 * @return Generated Token
	 */
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
            if (pstmt!=null) {
                pstmt.close();
            }
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			lastError=e.toString();
		}
		
		if (result==0) return "{Token:"+'"'+Token+'"'+"}"; else return lastError;
			
	}

	/**
	 * Get info of objects around coord
	 * @param token Secure Token
	 * @param Lat Latitude of point
	 * @param Lng Longtitude of point
	 * @return JSON String
	 */
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
		String result;
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
                    //For future
				}else
                if (ObjType.equalsIgnoreCase("CARAVAN")){
                    //For future
                }

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
				con.rollback();
				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * SimpleActions
	 * @param token  Secure Token
	 * @param Lat Latitude of command
	 * @param Lng Longtitude of command
	 * @param action Action to do
	 * @param target Target of Action
	 * @return JSON String of result
	 */
	public String SimpleCommand(String token,int Lat,int Lng,String action,String target){
	    Connection con = ConnectDB();
        PlayerObj player=new PlayerObj();
        player.GetDBDataByToken(con,token);
		//Check if player have correct Token
        if (!player.isLogin()){
            return "{Result:"+'"'+"Error"+'"'+",Code:"+'"'+"E000001"+'"'+",Message:"+'"'+"User not login in."+'"'+"}";
        }
		//Set new player position
        player.setPos(Lat,Lng);

		//Check Actions (add new action here
        if (action.equals("addCity")){
            player.CreateCity(con);
			lastError=player.GetLastError();
        } else
        if (action.equals("removeCity")){
            player.RemoveCity(con, target);
			lastError=player.GetLastError();
        } else
		//Create route
		if (action.equals("addroute")){
			CityObj targetCity=new CityObj(con,target);
			CityObj homeCity=new CityObj(con,player.GetCity());
			//Check if all city founded;
			RouteObj route=new RouteObj(player,homeCity,targetCity);
			if (route.GetLastError().equals("")) route.SetDBData(con);
			else lastError=route.GetLastError();
		}
		else
		//Return defaul error for unknown command
		lastError="Command unknown.";

		//TODO change to exception
		//Check LastError
        if (lastError!=null)
        {
        	try {
				con.rollback();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	return "{Result:"+'"'+"Error"+'"'+",Code:"+'"'+"E000003"+'"'+",Message:"+'"'+lastError+'"'+"}";
        }
        
        try {
        	con.commit();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return "{Result:"+'"'+"Success"+'"'+",Code:"+'"'+"S"+'"'+",Message:"+'"'+"Done"+'"'+"}";
	}
    public void MoveCaravans(){
        //Update all Caravans

        //Check Cross with Cities

        //Check Cross with Ambush
    }
    public void CreateCaravans(){
        //Check All Routes with timeout
        //For each cooldown
            //CreateCaravan
            //UpdateCooldown

    }

	
}
