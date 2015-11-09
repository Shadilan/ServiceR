package main;


import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
                result = MyUtils.getJSONError("AccessDenied", "User not found.");
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
            return MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));

        } catch (NamingException e) {
            return MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));

		}
        if (result.equals("")) return "{Token:" + '"' + Token + '"' + "}";
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
            return MyUtils.getJSONError("Resource", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (SQLException e) {
            try {
                if (con != null && !con.isClosed()) {
                    con.rollback();
                    con.close();
                }
            } catch (SQLException el) {
                return MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            }
            MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
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
                result = "AccessDenied";
            } else {
                //Set new player position
                player.setPos(Lat, Lng);
                //Check Actions (add new action here
                switch (action) {
                    case "addCity":
                        player.CreateCity(con);
                        result = player.GetLastError();
                        break;
                    case "removeCity":
                        player.RemoveCity(con, target);
                        result = player.GetLastError();
                        break;
                    //Create route
                    case "addroute":
                        CityObj targetCity = new CityObj(con, target);
                        CityObj homeCity = new CityObj(con, player.GetCity());
                        //Check if all city founded;
                        RouteObj route = new RouteObj(player, homeCity, targetCity);
                        if (route.GetLastError().equals("")) route.SetDBData(con);
                        else result = route.GetLastError();
                        break;
                    default:
                        //Return defaul error for unknown command
                        result = "UnknownCommand";
                        break;
                }
            }
            if (!result.equals("")) {
                try {
                    con.rollback();
                    con.close();
                } catch (SQLException e) {
                    MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
                }
                return "{Result:" + '"' + "Error" + '"' + ",Code:" + '"' + result + '"' + ",Message:" + '"' + result + '"' + "}";
            } else {
                con.commit();
                con.close();
            }
        } catch (NamingException e) {
            MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (SQLException e) {
            MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
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
        String result;
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
                result = "NoInviteCode";
                return MyUtils.getJSONError(result, result);
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
                result = "UserExists";
                return MyUtils.getJSONError(result, result);
            }
            if (Password.length()<6){
				stmt.close();
				con.close();
                result = "EasyPassword";
                return MyUtils.getJSONError(result, result);
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
            return MyUtils.getJSONError("DBError", e.toString() + Arrays.toString(e.getStackTrace()));
        } catch (NamingException e) {
            return MyUtils.getJSONError("DBError", e.toString() + Arrays.toString(e.getStackTrace()));
        }

        return "{Result:\"Success\",Message:\"User created\"";
    }


	
}
