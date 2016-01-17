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
    public Player() {

    }

    public static void BuyUpgrade(String PGUID, String CGUID, double PLAT, double PLNG) {
        //range check, coins check, db changes, return result
    }

    public static void GetPlayerInfo(String PGUID) {
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
}
