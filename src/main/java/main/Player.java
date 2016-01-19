package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Well on 17.01.2016.
 */
public class Player {
    String LastError;
    String GUID = "";
    public Player() {

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
     /*   query = con.prepareStatement("select GUID,Lat,Lng,Type from GameObjects where ");
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
        }*/
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
