package main;


import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Main object for Server
 */
public class SpiritProto {
    /**
     * Default Constructor
	 */
	public SpiritProto(){

	}
	/**
	 * Get Token
	 * @param Login Login of user
	 * @param Password Password of user
	 * @return Generated Token
	 */
    //TODO Move to Player Class;
    public String GetToken(String Login,String Password){

        Connection con;
        PreparedStatement pstmt;
        String Token = "T" + UUID.randomUUID().toString();
        String result = "";
        try {
            con = DBUtils.ConnectDB();
            pstmt = con.prepareStatement("SELECT count(1) from gplayers WHERE PlayerName=? and Password=?");
            pstmt.setString(1, Login);
            pstmt.setString(2, Password);
            ResultSet rs = pstmt.executeQuery();
            rs.first();
			if (rs.getInt(1)==0) {
                result = "User not Found: " + Login;
            } else {
                pstmt = con.prepareStatement("UPDATE gplayers SET USERTOKEN=? WHERE PlayerName=? and Password=?");
                pstmt.setString(1, Token);
                pstmt.setString(2, Login);
                pstmt.setString(3, Password);
                pstmt.execute();
                con.commit();
                con.close();
            }

        } catch (SQLException e) {

            result = e.toString();

        } catch (NamingException e) {
            e.printStackTrace();
            result = e.toString();

		}
        if (result.equals("")) return "{Token:" + '"' + Token + '"' + "}";
            //TODO JSON Format of error
        else return result;

    }

	/**
	 * Get info of objects around coord
	 * @param token Secure Token
	 * @param Lat Latitude of point
	 * @param Lng Longtitude of point
	 * @return JSON String
	 */
	public String GetData(String token,int Lat,int Lng){
        String result = "";
        Connection con = null;
        try {
            con = DBUtils.ConnectDB();
            PlayerObj player = new PlayerObj();
            player.GetDBDataByToken(con, token);
            if (!player.isLogin()) con.close();
            else {
                player.setPos(Lat, Lng);
                player.SetDBData(con);
                PreparedStatement stmt = con.prepareStatement("select GUID,ObjectType from aobject where SQRT(POWER(?-Lat,2)+POWER(?-Lng,2))<1000");
                stmt.setInt(1, Lat);
                stmt.setInt(2, Lng);


                ResultSet rs = stmt.executeQuery();
                rs.beforeFirst();
                ArrayList<CityObj> Cities = new ArrayList<>();
                while (rs.next()) {

                    String GUID = rs.getString(1);
                    String ObjType = rs.getString(2);
                    if (ObjType.equalsIgnoreCase("CITY")) {
                        CityObj City = new CityObj();
                        City.GetDBData(con, GUID);
                        Cities.add(City);
                    } else if (ObjType.equalsIgnoreCase("AMBUSH")) {
                        //For future
                    } else if (ObjType.equalsIgnoreCase("CARAVAN")) {
                        //For future
                    }

                }
                result = "{Player:" + player.toString();
                String citiesinfo = null;
                for (CityObj city : Cities) {
                    if (citiesinfo == null) citiesinfo = city.toString();
                    else citiesinfo += "," + city.toString();

                }
                if (citiesinfo != null) result += "," + "Cities:[" + citiesinfo + "]";
                result += "}";

                con.commit();
            }

        } catch (NamingException e) {
            //TODO JSON Format of error
            result = e.toString();
        } catch (SQLException e) {
            try {
                if (con != null && !con.isClosed()) {
                    con.rollback();
                    con.close();
                }
            } catch (SQLException el) {
                el.printStackTrace();
            }
            //TODO JSON Format of error
            result = e.toString();
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
    /*public String SimpleCommand(String token,int Lat,int Lng,String action,String target){
        Connection con;
        String result = "";
        try {
            con = DBUtils.ConnectDB();
            PlayerObj player = new PlayerObj();
            player.GetDBDataByToken(con, token);
            //Check if player have correct Token
            if (!player.isLogin()) {
                result = "User not login in.";
            } else {
                //Set new player position
                player.setPos(Lat, Lng);
                //Check Actions (add new action here
                if (action.equals("addCity")) {
                    player.CreateCity(con);
                    result = player.GetLastError();
                } else if (action.equals("removeCity")) {
                    player.RemoveCity(con, target);
                    result = player.GetLastError();
                } else
                    //Create route
                    if (action.equals("addroute")) {
                        CityObj targetCity = new CityObj(con, target);
                        CityObj homeCity = new CityObj(con, player.GetCity());
                        //Check if all city founded;
                        RouteObj route = new RouteObj(player, homeCity, targetCity);
                        if (route.GetLastError().equals("")) route.SetDBData(con);
                        else result = route.GetLastError();
                    } else
                        //Return defaul error for unknown command
                        result = "Command unknown.";
            }
            if (!result.equals("")) {
                try {
                    con.rollback();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return "{Result:" + '"' + "Error" + '"' + ",Code:" + '"' + "E" + '"' + ",Message:" + '"' + result + '"' + "}";
            } else {
                con.commit();
                con.close();
            }
        } catch (NamingException e) {
            return "{Result:" + '"' + "Error" + '"' + ",Code:" + '"' + "E" + '"' + ",Message:" + '"' + e.toString() + '"' + "}";
        } catch (SQLException e) {
            return "{Result:" + '"' + "Error" + '"' + ",Code:" + '"' + "E" + '"' + ",Message:" + '"' + e.toString() + '"' + "}";
        }
        return "{Result:" + '"' + "Success" + '"' + ",Code:" + '"' + "S" + '"' + ",Message:" + '"' + "Done" + '"' + "}";
    }*/





    /**
     * Create New Player
     *
     * @param Login Player Login
     * @param Password Player Password
     * @param email Player Email
     */
    public String NewPlayer(String Login,String Password,String email,String InviteCode){

		PreparedStatement stmt;
		try {
            Connection con = DBUtils.ConnectDB();
            ResultSet rs;
			//Check inviteCode
            stmt = con.prepareStatement("select count(1) cnt from invites where inviteCode=? and Invited=''");
            stmt.setString(1, InviteCode);
            rs=stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt")==0){
				stmt.close();
				con.close();
				return "No Invite Code";
			}
			//Check Name Available
            stmt = con.prepareStatement("select count(1) cnt from gplayers where PlayerName=? or email=?");
            stmt.setString(1,Login);
            stmt.setString(2,email);
			rs=stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt")>0){
				stmt.close();
				con.close();
				return "Name or Mail already Exists";
			}
			if (Password.length()<6){
				stmt.close();
				con.close();
				return "Name or Mail already Exists";
			}
			//Write InviteCode
			String GUID=UUID.randomUUID().toString();
			stmt=con.prepareStatement("update invites set Invited=? where inviteCode=?");
            stmt.setString(1, GUID);
            stmt.setString(2, InviteCode);
            stmt.execute();
            //Write Player Info
			stmt=con.prepareStatement("insert into gplayers(GUID,PlayerName,Password,email) VALUES(?,?,?,?)");
			stmt.setString(1,GUID);
			stmt.setString(2,Login);
			stmt.setString(3,Password);
            stmt.setString(4, email);
            stmt.execute();
            stmt=con.prepareStatement("insert into aobject(GUID) VALUES(?)");
			stmt.setString(1, GUID);
			stmt.execute();
			stmt.close();
			con.commit();
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }

		return "User Created";
	}


	
}
