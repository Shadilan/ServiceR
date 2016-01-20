package main;

import javax.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

/**
 * Created by Well on 17.01.2016.
 */
public class Player {
    String LastError;
    String GUID = "";
    String Name = "";
    int Level = 0;
    int Exp = 0;
    int Gold = 0;
    double Lat = 100;
    double Lng = 200;
    int AmbLeft = 0;
    public Player() {

    }

    public Player(String Token, Connection con) {
        PreparedStatement query;
        ResultSet rs;
        try {
            query = con.prepareStatement("select z1.GUID, z1.Name, z1.Level, z1.Exp, z1.Gold, z2.Lat, z2.Lng from Connection z0, Players z1, GameObjects z2 where z0.Token=? and z0.PGUID=z1.GUID and z2.GUID=z1.GUID");
            query.setString(1, Token);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                GUID = rs.getString(1);
                Name = rs.getString(2);
                Level = rs.getInt(3);
                Exp = rs.getInt(4);
                Gold = rs.getInt(5);
                Lat = rs.getDouble(6);
                Lng = rs.getDouble(7);
            } else {
                LastError = MyUtils.getJSONError("NOUSERFOUND", "(" + Token + ")");
            }
            query.close();
        } catch (SQLException e) {
            LastError = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void BuyUpgrade(String PGUID, String CGUID, double PLAT, double PLNG, Connection con) {
        //range check, coins check, db changes, return result
    }

    public static void GetPlayerInfo(String PGUID, Connection con) {
        //
    }

    public static boolean CheckAmbushesQuantity(String PGUID, Connection con) {
        //select z1.Effect1-(select count(1) from Ambushes where PGUID=?) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='set_ambushes'
        //if (result>0) return true
        //else return false
        PreparedStatement query;
        ResultSet rs;
        int AmbLeft;
        String result;
        boolean ret;
        try {
            query = con.prepareStatement("select z1.Effect1-(select count(1) from Ambushes where PGUID=?) from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='set_ambushes'");
            query.setString(1, PGUID);
            query.setString(2, PGUID);
            rs = query.executeQuery();
            rs.first();
            AmbLeft = rs.getInt(1);
            rs.close();
            ret = (AmbLeft > 0);
        } catch (SQLException e) {
            result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            ret = false;
        }
        return ret;
    }

    public static void ScanRange(String PGUID, double PLAT, double PLNG, Connection con) {
        PreparedStatement query;
        String GUID, Type, Lat, Lng, Result;
        JSONObject obj = new JSONObject();

        try {
            query = con.prepareStatement("select GUID,Lat,Lng,Type from GameObjects where abs(Lat-?)<10000 and abs(Lng-?)<10000");
            query.setString(1, Token);
            ResultSet rs = query.executeQuery();

            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    GUID = rs.getString("GUID");
                    Type = rs.getString("Type");
                    Lat = rs.getString("Lat");
                    Lng = rs.getString("Lng");
                    Result = Result.concat()
                }
                query.close();

            } else {
                query.close();
                LastError = "Error: NOUSERFOUND (" + Token + ")";
                return LastError;
            }
        } catch (SQLException e) {
            LastError = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public String GetGUIDByToken(Connection con, String Token) throws SQLException {
        PreparedStatement query;
        query = con.prepareStatement("select PGUID from Connection where Token=? limit 1");
        query.setString(1, Token);
        ResultSet rs = query.executeQuery();
        if (rs.isBeforeFirst()) {
            rs.first();
            GUID = rs.getString("PGUID");
            query.close();
            return GUID;
        } else {
            query.close();
            LastError = "Error: NOUSERFOUND (" + Token + ")";
            return LastError;
        }
    }

    public String GetGUID() {
        return GUID;
    }

    public String getLastError() {
        return LastError;
    }

}
