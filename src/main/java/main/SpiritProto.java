package main;


import javax.naming.NamingException;
import javax.swing.text.html.HTMLDocument;
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
            if (!player.isLogin()){
                con.close();
                MyUtils.getJSONError("NotLogin","We dont know you.");
            }
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
            result= MyUtils.getJSONError("Resource", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (SQLException e) {

            result=MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.rollback();
                    con.close();
                }
            } catch (SQLException el) {
                result= MyUtils.getJSONError("DBError", el.toString() + "\n" + Arrays.toString(el.getStackTrace()));
            }
        }

		return result;
	}


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
    public String action(String Token, String PLat, String PLng, String TargetGUID, String Action){
        int Lat=Integer.parseInt(PLat);
        int Lng=Integer.parseInt(PLng);
        return action(Token,Lat,Lng,TargetGUID,Action);
    }
    public String action(String Token, int PLat, int PLng, String TargetGUID, String Action) {
        Connection con = null;
        String result;
        try {
            con = DBUtils.ConnectDB();
            PlayerObj player= new PlayerObj();
            player.GetDBDataByToken(con,Token);
            if (player.isLogin())  {
                switch (Action) {
                    case "createRoute":
                        RouteObj route = new RouteObj(player.GetGUID(), TargetGUID);
                        result=route.checkCreateRoute(player.GetGUID()); //Так делать нельзя
                        if (result.equalsIgnoreCase("Ok")) {
                            result="!OK!";
                            result= route.createRoute(player.GetGUID(), TargetGUID);
                        }
                        break;
                    case "createAmbush":
                        PlayerObj ambush = new PlayerObj();
                        result=ambush.checkCreateAmbush(PLat, PLng);
                        if (result.equalsIgnoreCase("Оk")) {
                            result= ambush.createAmbush(player.GetGUID(), PLat, PLng);
                        }
                        break;
                    default:
                        result = MyUtils.getJSONError("ActtionNotFound", "Действие не определено");
                }
            } else
            {
                result = MyUtils.getJSONError("AccessDenied", "PlayerNotLoginIn " + Token);
            }
        } catch (SQLException | NamingException e) {
            result=MyUtils.getJSONError("DBError",e.toString());
        }
        return result;

    }
    public String getRouteList(String token,String city){
        Connection con = null;
        String result ;
        try {
             con=DBUtils.ConnectDB();
            PreparedStatement stmt;
            PlayerObj player=new PlayerObj();
            player.GetDBDataByToken(con,token);
            if (player.isLogin()) {
                if (city.equalsIgnoreCase("")) {
                    stmt = con.prepareStatement("select GUID from routes where owner=?");
                    stmt.setString(1,player.GetGUID());
                } else {
                    stmt=con.prepareStatement("select GUID from routes where onwer=? and (start=? or finish=?)");
                    stmt.setString(1,player.GetGUID());
                    stmt.setString(2,city);
                    stmt.setString(3,city);
                }
                ResultSet rs = stmt.executeQuery();
                rs.beforeFirst();
                result="{Routes:[";
                if (rs.isBeforeFirst()){

                    while (rs.next()){
                        RouteObj route=new RouteObj(con,rs.getString("GUID"));

                        if (rs.isFirst()) result+=route.toString();
                        else result+=','+route.toString();
                    }

                }
                result+="]}";
            } else
            {
                result=MyUtils.getJSONError("AccessDenied","PlayerNotLoginIn");
            }

        } catch (NamingException | SQLException e) {
            result=MyUtils.getJSONError("DBError",e.toString());
        } finally {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                result=MyUtils.getJSONError("DBError",e.toString());
            }
        }
        return result;
    }
}
