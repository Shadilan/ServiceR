package main;

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
    public void removeAmbush(Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("delete from traps where GUID=?");
        stmt.setString(1, GUID);
        stmt.execute();
        stmt = con.prepareStatement("delete from aobject where GUID=?");
        stmt.setString(1, GUID);
        stmt.execute();
        stmt.close();
    }
    public String toString(){
        String result="{GUID:\""+GUID+"\",Owner:\""+Owner+"\",Lat:"+Lat+",Lng:"+Lng+"}";
        return result;
    }

}
