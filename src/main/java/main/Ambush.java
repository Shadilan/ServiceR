package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Well on 17.01.2016.
 */
public class Ambush {
    public Ambush() {

    }

    public static void Set(String PGUID, double PLAT, double PLNG, double LAT, double LNG, Connection con) {
        String AGUID, result;
        int Radius;
        //do range check, count check for ambushes and insert into tables Ambush and GameObjects
        if (Server.RangeCheck(PLAT, PLNG, LAT, LNG) > 50) {
            if (Player.CheckAmbushesQuantity(PGUID, con)) {
                //insert new ambush into tables
                //insert into Ambushes(GUID,PGUID,Radius) values (?,?,?)
                //? - generate GUID, ? - PGUID, ? - select z1.Effect1 from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='ambushes'
                AGUID = UUID.randomUUID().toString();
                PreparedStatement query;
                try {
                    query = con.prepareStatement("select z1.Effect1 from Upgrades z1, PUpgrades z2 where z2.UGUID=z1.GUID and z2.PGUID=? and z1.TYPE='ambushes'");
                    query.setString(1, PGUID);
                    ResultSet rs = query.executeQuery();
                    rs.first();
                    Radius = rs.getInt(1);

                    query = con.prepareStatement("insert into Ambushes(GUID,PGUID,Radius) values (?,?,?)");
                    query.setString(1, AGUID);
                    query.setString(2, PGUID);
                    query.setInt(3, Radius);
                    query.execute();
                    con.commit();
                    //Server.SendData();
                } catch (SQLException e) {
                    result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
                }
            } else {
                //return FALSE, not enough ambushes
                //Server.SendData();
            }
        } else {
            //return FALSE, too far
            //Server.SendData();
        }
    }

    public static void Destroy(String PGUID, String AGUID, double PLAT, double PLNG) {
        //do range check,
    }
}
