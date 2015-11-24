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

                //Передвинуть караваны
                MoveCaravans(con);
                DoCaravanInCity(con);
                DoCaravanInAmbush(con);
                //Проверить есть ли караваны рядом с засадами
                //Для каждого каравана рядом с засадой удалить маршрут
                //Указать в качестве конечной точки домашний город владельца засады
                //Удадить засаду.
                stmt = con.prepareStatement("select count(1) as cnt from service.PROCESS_CONTROL where PROCESS_NAME='MAIN' and STOP_FLAG='Y'");
                ResultSet rs = stmt.executeQuery();
                rs.first();
                if (rs.getInt("cnt") == 0) new Thread(task).start();
                stmt.close();
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
    public void MoveCaravans(Connection con) throws SQLException {
        //Update all Caravans
        PreparedStatement stmt;
            stmt = con.prepareStatement("UPDATE caravan a, aobject b SET b.Lat = a.Lat + a.SpdLat, b.Lng = a.Lng + a.SpdLng, a.Lat = a.Lat + a.SpdLat, a.Lng = a.Lng + a.SpdLng");
            stmt.execute();
            //Maybe do it in

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
        //Check Cross with Cities


    }
    public void DoCaravanInCity(Connection con) throws SQLException {
        PreparedStatement stmt;
        //Проверить есть ли караваны рядом с конечными точками
        String inSQL="and c.guid in (SELECT a.GUID " +
                "FROM caravan a, cities b " +
                "WHERE a.endpoint = b.guid " +
                "AND  a.lat between b.lat-ABS(a.SpdLat) and b.lat+ABS(a.SpdLat)" +
                "AND  a.lng between b.lng-ABS(a.SpdLng) and b.lng+ABS(a.SpdLng))";
        //Для каждого каравана  рядом с конечной точкой начислить хозяину деньги,
        //Для каждого каравана поменять начальную и конечную точку и умножить скорость на -1
        String sql="UPDATE gplayers a,\n" +
                "caravan c,\n" +
                "cities t1,\n" +
                "cities t2 SET a.gold = a.gold + ROUND( 6378137 * ACOS( COS( t1.lat* PI( ) /180 ) * COS( t2.lat* PI( ) /180 ) * COS( t1.lng* PI( ) /180 - t2.lng* PI( ) /180 ) + SIN( t1.lat* PI( ) /180 ) * SIN( t2.lat* PI( ) /180 ) ) /1000 ) ,\n" +
                "c.endpoint = t1.guid,\n" +
                "c.startpoint = t2.guid,\n" +
                "c.spdLat = c.spdLat * -1,\n" +
                "c.spdLng = c.spdLng * -1 WHERE a.guid = c.owner AND c.startpoint = t1.guid AND c.endpoint = t2.guid \n"+inSQL;
        stmt=con.prepareStatement(sql);
        stmt.execute();
    }
    public void DoCaravanInAmbush(Connection con) throws SQLException {
        PreparedStatement stmt;
        //Проверить есть ли караваны рядом с засадами
        //Для каждого каравана рядом с засадой удалить маршрут
        //Указать в качестве конечной точки домашний город владельца засады
        //Удадить засаду.
      String sql="update caravan c, traps a,gplayers g,cities t\n" +
              "set c.ENDPOINT = g.HomeCity,\n" +
              "spdLat=(c.lat-t.lat)*(ROUND( 6378137 * ACOS( COS( c.lat * PI( ) /180 ) * COS( a.lat * PI( ) /180 ) * COS( a.lng * PI( ) /180 - c.lng * PI( ) /180 ) + SIN( a.lat * PI( ) /180 ) * SIN( c.lat * PI( ) /180 ) ) /1000 )/1666),\n" +
              "spdLng=(c.lng-t.lng)*(ROUND( 6378137 * ACOS( COS( c.lat * PI( ) /180 ) * COS( a.lat * PI( ) /180 ) * COS( a.lng * PI( ) /180 - c.lng * PI( ) /180 ) + SIN( a.lat * PI( ) /180 ) * SIN( c.lat * PI( ) /180 ) ) /1000 )/1666),\n" +
              "c.owner=g.guid,\n" +
              "a.owner=''\n" +
              "\n" +
              "WHERE 1 =1\n" +
              "AND ABS( a.lat - c.lat ) <1000\n" +
              "AND ABS( a.lng - c.lng ) <1000\n" +
              "AND ROUND( 6378137 * ACOS( COS( c.lat * PI( ) /180 ) * COS( a.lat * PI( ) /180 ) * COS( a.lng * PI( ) /180 - c.lng * PI( ) /180 ) + SIN( a.lat * PI( ) /180 ) * SIN( c.lat * PI( ) /180 ) ) /1000 ) <1666\n" +
              "AND ROUND( 6378137 * ACOS( COS( c.lat * PI( ) /180 - c.spdLat * PI( ) /180 ) * COS( a.lat * PI( ) /180 ) * COS( a.lng * PI( ) /180 - c.lng * PI( ) /180 + c.spdLng * PI( ) /180 ) + SIN( a.lat * PI( ) /180 ) * SIN( c.lat * PI( ) /180 - c.SpdLat * PI( ) /180 ) ) /1000 ) <1666\n" +
              "and g.guid=a.owner\n" +
              "and g.HomeCity=t.guid";
        stmt=con.prepareStatement(sql);
        stmt.execute();
        stmt=con.prepareStatement("DELETE FROM aobject WHERE guid IN (\n" +
                "SELECT guid\n" +
                "FROM traps\n" +
                "WHERE owner =  ''\n" +
                ")");
        stmt.execute();
        stmt.execute("delete FROM traps WHERE owner =  ''");
        stmt.execute();
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
