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
 */
public class Generate {
    /**
     * Generate city from coordinates.
     * String wrapper
     * @param Lat1 Latitude
     * @param Lng1 Longtitude
     * @param Lat2 Latitude
     * @param Lng2 Longtitude
     * @param count Number of cities
     * @return Generation info
     */
    public static String GenCity(String Lat1, String Lng1, String Lat2, String Lng2, String count) {
        String result;
        try {
            int Lat1N = Integer.parseInt(Lat1);
            int Lat2N = Integer.parseInt(Lat2);
            int Lng1N = Integer.parseInt(Lng1);
            int Lng2N = Integer.parseInt(Lng2);
            int countN = Integer.parseInt(count);
            result = GenCity(Lat1N, Lng1N, Lat2N, Lng2N, countN);
        } catch (NumberFormatException e) {
            result = e.toString();
        }
        return result;
    }

    /**
     * Get city name from database
     * @param con Connection to database
     * @return Cityname
     * @throws SQLException
     */
    private static String genCityName(Connection con) throws SQLException {
        String result="";
        PreparedStatement stmt = con.prepareStatement("SELECT CONCAT( af.part, al.part ) \n" +
                "FROM cityparts af, cityparts al, (\n" +
                "\n" +
                "SELECT ROUND( RAND( ) *10000000 MOD 109576 +1 ) cnt\n" +
                ")a, (\n" +
                "\n" +
                "SELECT ROUND( RAND( ) *10000000 MOD 971 +1 ) cnt\n" +
                ")b\n" +
                "WHERE af.randomizer = a.cnt\n" +
                "AND al.randomizer = b.cnt\n" +
                "AND al.type =3\n" +
                "AND af.type =10");
        ResultSet rs = stmt.executeQuery();
        rs.beforeFirst();
        while (rs.next()) {
            result =rs.getString(1);
        }

        stmt.close();
        return result;
    }
    /**
     * Gen Cities in target area.
     * @param Lat1     Latitude of start of rect
     * @param Lng1     Longtitude of start of rect
     * @param Lat2     Latitude of end of rect
     * @param Lng2     Longtitude of end of rect
     * @param count Count of cities
     * @return Generation information
     */
    public static String GenCity(int Lat1, int Lng1, int Lat2, int Lng2, int count) {
        //Count valid coord;
        String result;
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
        result = "L" + Lat1N + "X" + Lng1N + "\n";
        result += "L" + Lat2N + "X" + Lng2N + "\n";
        int width = Lat2N - Lat1N;
        int height = Lng2N - Lng1N;
        result += "W" + width + " H" + height + "\n";
        result += "Count:" + count;
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
                stmt = con.prepareStatement("INSERT INTO cities(GUID,Lat,Lng,CITYNAME)VALUES(?,?,?,?)");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX() + Lat1N);
                stmt.setInt(3, (int) a.getY() + Lng1N);
                stmt.setString(4,genCityName(con));
                stmt.execute();
                stmt = con.prepareStatement("INSERT INTO aobject(GUID,Lat,Lng,ObjectType)VALUES(?,?,?,'CITY')");
                stmt.setString(1, GUID);
                stmt.setInt(2, (int) a.getX() + Lat1N);
                stmt.setInt(3, (int) a.getY() + Lng1N);
                stmt.execute();
                con.commit();
                result += "GUID:" + GUID + " Lat:" + (a.getX() + Lat1N) + " Lng:" + (a.getY() + Lng1N) + "\n";
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
        return result;
        //Generate positions
        //write to db

    }

    /**
     * Basic function of generation delete all objects.
     * @deprecated
     * @param x     Width
     * @param y     Height
     * @param count Count of cities
     * @return Result of operation
     */

    public static String GenCity(int x, int y, int count) {
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
}
