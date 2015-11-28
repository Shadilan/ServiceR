package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Shadilan
 * @author Zlodiak
 */
public class AdminTools {
    public static int speed=1666;
    final Runnable task = new Runnable() {
        public void run() {
            String Step = "";
            try {
                Thread.sleep(60000);
                Connection con;
                con = DBUtils.ConnectDB();
                PreparedStatement stmt;

                //Передвинуть караваны
                try {
                    Step = "MoveCaravans";
                    MoveCaravans(con);
                    Step = "DoCaravanInCity";
                    DoCaravanInCity(con);
                    Step = "DoCaravanInAmbush";
                    DoCaravanInAmbush(con);
                } catch (Exception e) {
                    stmt = con.prepareStatement("update PROCESS_CONTROL set last_error=? where PROCESS_NAME='MAIN'");
                    stmt.setString(1, Step + ":" + e.toString());
                    stmt.execute();
                }
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

        String sql;
      /*sql="update caravan c, traps a,gplayers g,cities t\n" +
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
              "and g.HomeCity=t.guid";*/
        sql="select c,route,g.HomeCity,c.lat clat,t.lat tlat,a.lat alat,a.lng alng,c.lng clng,t.lng tlng,c.spdLat,c.spdLng, g.GUID gGUID, c.guid cGUID,t.GUID tGUID,\n" +
                "              c.startpoint\n" +
                "from caravan c, traps a,gplayers g,cities t\n" +
                "WHERE 1 =1\n" +
                "AND (ABS( a.lat - c.lat ) <ABS(c.spdLat) or ABS( a.lat - c.lat ) <ABS(c.spdLng))\n" +
                "AND (ABS( a.lng - c.lng ) <ABS(c.spdLat) or ABS( a.lng - c.lng ) <ABS(c.spdLng))\n" +
                "and g.guid=a.owner\n" +
                "and g.HomeCity=t.guid\n" +
                "and stealed!='R'";
        stmt=con.prepareStatement(sql);
        ResultSet rs=stmt.executeQuery();
        rs.beforeFirst();
        while (rs.next()) {
            int dLat = rs.getInt("spdLat");
            int dLng = rs.getInt("spdLng");
            int sLat = rs.getInt("clat");
            int sLng = rs.getByte("clng");
            int aLat = rs.getInt("alat");
            int aLng = rs.getInt("alng");
            int eLat =rs.getInt("tLat");
            int eLng=rs.getInt("tLng");
            int tLat;
            int tLng;
            double u = ((aLat - sLat) * dLat + (aLng - sLng) * dLng) / (dLat * dLat + dLng * dLng);
            if (u < 0) {
                tLat = sLat;
                tLng = sLng;
            } else if (u > 1) {
                tLat = sLat + dLat;
                tLng = sLng + dLng;
            } else {
                tLat = (int) (sLat + dLat * u);
                tLng = (int) (sLng + dLng * u);
            }
            PreparedStatement stmt2;
            //Проверяем что караван не угнан и засада все еще стоит.
            stmt2=con.prepareStatement("select count(1) cnt from caravans where guid=? and stealed!='R'");
            stmt2.setString(1,rs.getString("cGUID"));
            ResultSet rs2=stmt2.executeQuery();
            int cnt1=rs2.getInt("cnt");
            stmt2=con.prepareStatement("select count(1) cnt from trap where guid=?");
            stmt2.setString(1,rs.getString("aGUID"));
            rs2=stmt2.executeQuery();
            int cnt2=rs2.getInt("cnt");
            int distance = (int) MyUtils.distVincenty(aLat, aLng, tLat, tLng);
            if (distance < 200 && cnt1==1 && cnt2==1) {
                //Для каждого каравана рядом с засадой удалить маршрут
                stmt2 = con.prepareStatement("delete from routes where guid=?");
                stmt2.setString(1, rs.getString("route"));
                stmt2.execute();
                //Удалить вэйпоинты
                stmt2 = con.prepareStatement("delete from waypoints where route=?");
                stmt2.setString(1, rs.getString("route"));
                stmt2.execute();
                //Создать маршрут
                stmt2 = con.prepareStatement("insert into routes (guid,owner,start,finish) values(?,?,?,?)");
                String GUID_ROUTE = UUID.randomUUID().toString();
                stmt2.setString(1,GUID_ROUTE);
                stmt2.setString(2, rs.getString("gGuid"));
                stmt2.setString(3, rs.getString("startpoint"));
                stmt2.setString(4, rs.getString("homecity"));
                stmt2.execute();
                //Создать Вэйпоинты
                stmt2 = con.prepareStatement("insert into waypoints (guid,lat,lng,number,route) values(?,?,?,1,?),(?,?,?,2,?)");
                String w1GUID = UUID.randomUUID().toString();
                String w2GUID = UUID.randomUUID().toString();
                stmt2.setString(1, w1GUID);
                stmt2.setInt(2, tLat);
                stmt2.setInt(3, tLng);
                stmt2.setString(4, GUID_ROUTE);
                stmt2.setString(1, w2GUID);
                stmt2.setInt(2, eLat);
                stmt2.setInt(3, eLng);
                stmt2.setString(4, GUID_ROUTE);
                stmt2.execute();
                //Указать в качестве конечной точки домашний город владельца засады
                stmt2 = con.prepareStatement("update caravan set  EndPoint=?, Owner=?, stealed=\"R\",Lat=?,Lng=?,spdLat=?,spdLng=?,route=? where guid=?");
                stmt2.setString(1, rs.getString("HomeCity"));
                stmt2.setString(2, rs.getString("gGuid"));
                stmt2.setInt(3, tLat);
                stmt2.setInt(4,tLng);
                double k=MyUtils.distVincenty(tLat,tLng,eLat,eLng)/speed;
                //Создать новый маршрут.
                stmt2.setInt(5, (int) ((eLat - tLat) / k));
                stmt2.setInt(6, (int) ((eLng - tLng) / k));
                stmt2.setString(7, GUID_ROUTE);
                stmt2.setString(8, rs.getString("cGuid"));
                stmt2.execute();
                //Удадить засаду.
                stmt2 = con.prepareStatement("delete from traps where guid=?");
                stmt2.setString(1, rs.getString("aGuid"));
                stmt2.execute();
                stmt2 = con.prepareStatement("delete from aobject where guid=?");
                stmt2.setString(1, rs.getString("aGuid"));
                stmt2.execute();
            }
        }
        stmt=con.prepareStatement("update caravan set stealed='Y' where stealed='R'");
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
