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

    public String GenCity(String Lat1, String Lng1, String Lat2, String Lng2, String count) {
        String result;
        try {
            int Lat1N = Integer.parseInt(Lat1);
            int Lat2N = Integer.parseInt(Lng1);
            int Lng1N = Integer.parseInt(Lat2);
            int Lng2N = Integer.parseInt(Lng2);
            int countN = Integer.parseInt(count);
            result = GenCity(Lat1N, Lng1N, Lat2N, Lng2N, countN);
        } catch (NumberFormatException e) {
            result = e.toString();
        }
        return result;
    }
    /**
     * @param Lat1     Latitude of start of rect
     * @param Lng1     Longtitude of start of rect
     * @param Lat2     Latitude of end of rect
     * @param Lng2     Longtitude of end of rect
     * @param count Count of cities
     * @return Result of operation
     */
    public String GenCity(int Lat1, int Lng1, int Lat2, int Lng2, int count) {
        //Count valid coord;
        int Lat1N;
        int Lat2N;
        int Lng1N;
        int Lng2N;

        if (Lat1 < Lat2) {
            Lat1N = Lat1;
            Lat2N = Lat2;
        } else {
            Lat1N = Lat2;
            Lat2N = Lat1;
        }
        if (Lng1 < Lng2) {
            Lng1N = Lng1;
            Lng2N = Lng2;
        } else {
            Lng1N = Lng2;
            Lng2N = Lng1;
        }
        int width = Lat2N - Lat1N;
        int height = Lng2N - Lng1N;
        //Remove all current city
        try {
            Connection con = DBUtils.ConnectDB();
            PreparedStatement stmt;
            stmt = con.prepareStatement("delete from cities where Lat between ? and ? and Lng between ? and ?");
            stmt.setInt(1, Lat1N);
            stmt.setInt(2, Lat2N);
            stmt.setInt(3, Lng1N);
            stmt.setInt(4, Lng2N);
            stmt.execute();
            stmt = con.prepareStatement("delete from aobject where ObjectType='CITY' and Lat between ? and ? and Lng between ? and ?");
            stmt.setInt(1, Lat1N);
            stmt.setInt(2, Lat2N);
            stmt.setInt(3, Lng1N);
            stmt.setInt(4, Lng2N);
            stmt.execute();
            ArrayList<Point> cities = MyUtils.createCitiesOnMap(width, height, count);
            for (Point a : cities) {
                String GUID = UUID.randomUUID().toString();
                stmt = con.prepareStatement("INSERT INTO cities(GUID,Lat,Lng,CITYNAME)VALUES(?,?,?,'TEST')");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX() + Lat1N);
                stmt.setInt(3, (int) a.getY() + Lng1N);
                stmt.execute();
                stmt = con.prepareStatement("INSERT INTO aobject(GUID,Lat,Lng,ObjectType)VALUES(?,?,?,'CITY')");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX() + Lat1N);
                stmt.setInt(3, (int) a.getY() + Lng1N);
                stmt.execute();
                con.commit();
            }
            con.commit();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "Success";
        //Generate positions
        //write to db

    }

    /**
     * @param x     Width
     * @param y     Height
     * @param count Count of cities
     * @return Result of operation
     */
    public String GenCity(int x, int y, int count) {
        //Remove all current city
        try {
            Connection con = DBUtils.ConnectDB();
            PreparedStatement stmt;
            stmt = con.prepareStatement("delete from cities");
            stmt.execute();
            stmt = con.prepareStatement("delete from aobject where ObjectType='CITY'");
            stmt.execute();
            ArrayList<Point> cities = MyUtils.createCitiesOnMap(x, y, count);
            for (Point a : cities) {
                String GUID = UUID.randomUUID().toString();
                stmt = con.prepareStatement("INSERT INTO cities(GUID,Lat,Lng,CITYNAME)VALUES(?,?,?,'TEST')");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX());
                stmt.setInt(3, (int) a.getY());
                stmt.execute();
                stmt = con.prepareStatement("INSERT INTO aobject(GUID,Lat,Lng,ObjectType)VALUES(?,?,?,'CITY')");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX());
                stmt.setInt(3, (int) a.getY());
                stmt.execute();
                con.commit();
            }
            con.commit();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
        return "Success";
        //Generate positions
        //write to db

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
                result += "create_marker(" + rs.getInt("Lat") + "," + rs.getInt("Lng") + ",'" + rs.getString("CITYNAME") + "');\n";
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
