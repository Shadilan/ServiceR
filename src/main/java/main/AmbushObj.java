package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Shadilan on 15.11.2015.
 */
public class AmbushObj implements GameObject {
    private String GUID;
    private String Owner;
    private int Lat;
    private int Lng;

    public String getGUID() {
        return GUID;
    }

    public AmbushObj(String Owner, int lat, int lng) {
        GUID = UUID.randomUUID().toString();
        Lat = lat;
        Lng = lng;
        this.Owner = Owner;
    }

    public AmbushObj(Connection con, String GUID) throws SQLException {
        GetDBData(con, GUID);
    }

    public AmbushObj()  {

    }

    @Override
    public void GetDBData(Connection con, String GUID) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("select GUID,Owner,Lat,Lng from traps where GUID=?");
        stmt.setString(1, GUID);
        ResultSet rs = stmt.executeQuery();
        rs.beforeFirst();
        if (rs.isBeforeFirst()) {
            rs.next();
            this.GUID = rs.getString("GUID");
            Owner = rs.getString("Owner");
            Lat = rs.getInt("Lat");
            Lng = rs.getInt("Lng");
        }
        stmt.close();
    }

    @Override
    public void SetDBData(Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("insert ignore into traps(GUID,Owner,Lat,Lng) VALUES(?,?,?,?)");
        stmt.setString(1, GUID);
        stmt.setString(2, Owner);
        stmt.setInt(3, Lat);
        stmt.setInt(4, Lng);
        stmt.execute();
        stmt = con.prepareStatement("insert ignore into aobject(GUID,Lat,Lng,ObjectType) VALUES(?,?,?,'AMBUSH')");
        stmt.setString(1, GUID);
        stmt.setString(2, Owner);
        stmt.setInt(3, Lat);
        stmt.setInt(4, Lng);
        stmt.execute();
        stmt.close();
    }

    public String checkCreateAmbush(int Lat, int Lng) {
        PreparedStatement stmt;
        ResultSet rs;
        try {
            Connection con = DBUtils.ConnectDB();
            //Correct CityDef => 50
            stmt = con.prepareStatement("select count(1) cnt from cities where ((ABS(lat-?)<=50) and (ABS(lng-?)<=50) and (POW((?-lat),2)+POW((?-lng),2)<POW(50,2)))");
            stmt.setInt(1, Lat);
            stmt.setInt(2, Lng);
            stmt.setInt(3, Lat);
            stmt.setInt(4, Lng);
            stmt.execute();
            rs = stmt.executeQuery();
            rs.first();
            if (rs.getInt("cnt") > 0) {
                return MyUtils.getJSONError("AmbushNearCity","Нельзя ставить засады так близко к городу. Засада будет уничтожена защитой города!");
            } else {
                return "Ok";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }
    public String createAmbush(String Owner, int Lat, int Lng) {
        PreparedStatement stmt;
        Connection con = null;
        try {
            con = DBUtils.ConnectDB();
            String GUID = UUID.randomUUID().toString();
            //TODO: Need to make object Ambush and do creation through it
            stmt = con.prepareStatement("insert into traps (GUID, OWNER, LAT, LNG) VALUES (?,?,?,?)");
            stmt.setString(1, GUID);
            stmt.setString(2, Owner);
            stmt.setInt(3, Lat);
            stmt.setInt(4, Lng);
            stmt.execute();
            stmt = con.prepareStatement("insert into aobject (GUID, LAT, LNG,ObjectType) VALUES (?,?,?,'AMBUSH')");
            stmt.setString(1, GUID);
            stmt.setInt(2, Lat);
            stmt.setInt(3, Lng);
            stmt.execute();
        } catch (SQLException e) {
            try {
                if (con != null && !con.isClosed()) {
                    con.rollback();
                }
            } catch (SQLException en)
            {
                return MyUtils.getJSONError("DBError", en.toString());
            }
            return MyUtils.getJSONError("DBError", e.toString());
        } catch (NamingException e) {
            return MyUtils.getJSONError("DBError", e.toString());
        }finally {
            try {
                if (con!=null && !con.isClosed()) {
                    con.commit();
                    con.close();
                }
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
        }
        return MyUtils.getJSONSuccess("Ambush created.");
    }

    public String checkRemoveAmbush(int PLat, int PLng, String GUID) {
        PreparedStatement stmt;
        ResultSet rs;
        try {
            Connection con = DBUtils.ConnectDB();
            stmt = con.prepareStatement("select lat,lng from traps where GUID=?");
            stmt.setString(1, GUID);
            stmt.execute();
            rs = stmt.executeQuery();
            rs.first();
            //lat=rs.getInt("lat");
            //lng=rs.getInt("lng");
            //Correct CityDef => 50
            double lat1=PLat/1e6*Math.PI/180;
            double lat2=rs.getInt("lat")/1e6*Math.PI/180;
            double lng1=PLng/1e6*Math.PI/180;
            double lng2=rs.getInt("lng")/1e6*Math.PI/180;
            if ( Math.round(6378137*Math.acos(Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng1 - lng2)+Math.sin(lat1)*Math.sin(lat2))) > 50) {
            //if ( Math.pow(PLat-rs.getInt("lat"),2) + Math.pow(PLng-rs.getInt("lng"),2) > Math.pow(50,2)) {
                return MyUtils.getJSONError("AmbushTooFar","Засада слишком далеко, подойдите ближе!:"+Math.round(6378137*Math.acos(Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng1-lng2)+Math.sin(lat1)*Math.sin(lat2))));
            } else {
                return "Ok";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }
    public String removeAmbush(String PGuid, String AGuid) {
        PreparedStatement stmt;
        ResultSet rs;
        try {
            Connection con = DBUtils.ConnectDB();
            stmt = con.prepareStatement("delete from traps where GUID=?");
            stmt.setString(1, AGuid);
            stmt.execute();
            stmt = con.prepareStatement("delete from aobject where GUID=?");
            stmt.setString(1, AGuid);
            stmt.execute();
            stmt.close();
//Zlodiak: Реализовать увеличение количества ловушек у владельца ловушки, запустить кулдаун восстановление (если он будет на снятии, а не на установке)
//для этого и передаю PGuid, который сейчас не используется
//хмм, для этого AGuid нужен, а не PGuid, т.е. не факт, что PGuid будет использоваться и в будущем...
//Хотя можно статистику снятых чужих засад вести, тогда пригодится...
//Вот так и появляются разные медальки - веселее придумать функциональность под переменную, чем код поправить )))
            return "Ok";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (NamingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String toString(){
        String result="{GUID:\""+GUID+"\",Owner:\""+Owner+"\",Lat:"+Lat+",Lng:"+Lng+"}";
        return result;
    }

}
