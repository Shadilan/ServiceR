package main;

import javax.naming.NamingException;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Shadilan
 * @author Zlodiak
 */
public class AdminTools {
    final Runnable task = new Runnable() {
        public void run() {
            try {
                Thread.sleep(60000);
                Connection con;
                con = DBUtils.ConnectDB();
                PreparedStatement stmt;
                stmt = con.prepareStatement("select count(1) as cnt from service.PROCESS_CONTROL where PROCESS_NAME='MAIN' and STOP_FLAG='Y'");
                ResultSet rs = stmt.executeQuery();
                rs.first();
                if (rs.getInt("cnt") == 0) new Thread(task).start();
                stmt.close();
                CreateCaravans(con);
                MoveCaravans(con);
                stmt = con.prepareStatement("update service.PROCESS_CONTROL set LAST_RUN=NOW() where PROCESS_NAME='MAIN'");
                stmt.execute();
                con.commit();
                stmt.close();
                con.close();
            } catch (SQLException | NamingException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Start server task
     *
     * @return result
     */
    public String StartTask() {
        try {
            Connection con = DBUtils.ConnectDB();
            PreparedStatement stmt = con.prepareStatement("update service.PROCESS_CONTROL set STOP_FLAG='N' where PROCESS_NAME='MAIN'");
            stmt.execute();
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
        new Thread(task).start();
        return "Started";
    }

    /**
     * Stop current server task
     *
     * @return result
     */
    public String StopTask() {

        try {
            Connection con = DBUtils.ConnectDB();
            PreparedStatement stmt = con.prepareStatement("update service.PROCESS_CONTROL set STOP_FLAG='Y' where PROCESS_NAME='MAIN'");
            stmt.execute();
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "Stoped";
    }

    /**
     * Move caravans to new positions
     *
     * @param con DB Connection
     */
    public void MoveCaravans(Connection con) {
        //Update all Caravans
        PreparedStatement stmt;
        try {
            stmt = con.prepareStatement("UPDATE caravan a, aobject b SET b.Lat = a.Lat + a.SpdLat, b.Lng = a.Lng + a.SpdLng, a.Lat = a.Lat + a.SpdLat, a.Lng = a.Lng + a.SpdLng");
            stmt.execute();
            //Maybe do it in
            stmt = con.prepareStatement("SELECT a.GUID " +
                    "FROM caravan a, cities b " +
                    "WHERE a.endpoint = b.guid " +
                    "AND FLOOR( a.lat / ABS( a.SpdLat ) ) = FLOOR( b.lat / ABS( a.SpdLat ) )  " +
                    "AND FLOOR( a.lng / ABS( a.SpdLng ) ) = FLOOR( b.lng / ABS( a.SpdLng ) ) ");
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    CaravanObj caravan = new CaravanObj(con, rs.getString("GUID"));
                    PlayerObj player = new PlayerObj(con, caravan.GetOwner());
                    player.SetGold(player.GetGold() + caravan.GetGold(con));
                    player.SetDBData(con);
                }
            }
            stmt.close();
            //Check Cross with Ambush
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Check Cross with Cities


    }

    /**
     * Create new Caravans
     *
     * @param con Connection DB
     */
    public void CreateCaravans(Connection con) {
        //Check All Routes with timeout
        PreparedStatement stmt;
        try {
            stmt = con.prepareStatement("SELECT guid, owner\n" +
                    "FROM route " +
                    "WHERE NEXT < NOW( ) ");
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst()) {
                while (rs.next()) {
                    RouteObj route = new RouteObj(con, rs.getString("GUID"));
                    CaravanObj caravan = new CaravanObj(route);
                    caravan.SetDBData(con);
                    route.SetNext();
                    route.SetDBData(con);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public String GenMap() {
        String result = "";
        try {
            Connection con;
            con = DBUtils.ConnectDB();
            PreparedStatement stmt;
            stmt = con.prepareStatement("select Lat,Lng,CITYNAME from service.cities");
            ResultSet rs = stmt.executeQuery();
            rs.beforeFirst();
            while (rs.next()) {
                result += "create_marker(" + rs.getInt("Lat") + "," + rs.getInt("Lng") + ",\"" + rs.getString("CITYNAME") + "\",map);\n";
            }

            stmt.close();
            con.commit();
            con.close();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
