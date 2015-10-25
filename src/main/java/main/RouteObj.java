package main;

import javax.swing.plaf.SliderUI;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Shadilan
 */

public class RouteObj implements GameObject {

    private String GUID; public String GetGuid(){return GUID;}
	private String Owner; public String GetOwner(){return this.Owner;}
	private String RStart; public String GetRStart(){return RStart;}
	private String REnd;public String GetREnd(){return REnd;}
	private int Gold;
	private int HP;
	private int Cooldown;
	private Date Next;
	private int SLat; public int GetSLat(){return SLat;}
	private int SLng;public int GetSLng(){return SLng;}
	private int ELat;public int GetELat(){return ELat;}
	private int ELng;public int GetELng(){return ELng;}

    //TODO: Change to Exception;
    private String LastError; public String GetLastError(){return LastError;}

    /**
     * Creator of object
     * @param Owner Owner of route
     * @param RStart StartPoint of route
     * @param REnd  EndPoint of route
     */
    public RouteObj(String Owner,String RStart,String REnd){
		GUID=UUID.randomUUID().toString();
		this.Owner=Owner;
		this.RStart=RStart;
		this.REnd=REnd;
		Gold=0;
		HP=10;
	}

    /**
     * Constructor Create route from player and cities info
     * @param player Owner of route
     * @param StartCity Start city
     * @param TargetCity End city
     */
    public RouteObj(PlayerObj player,CityObj StartCity,CityObj TargetCity){
        GUID=UUID.randomUUID().toString();
        this.Owner=player.GetGUID();
        this.RStart=StartCity.GetGUID();
        this.REnd=TargetCity.GetGUID();
        if (Owner.equals("")) LastError="Player not found";
        else if (RStart.equals("")) LastError="Player City not found";
        else if (REnd.equals("")) LastError="Target City not found";
        else if (RStart.equals(REnd)) LastError="Target and Home city is one city";

        Gold=0;
        HP=10;
    }
    /**
     * Creator of object
     * @param con Connection to DB
     * @param GUID GUID of route
     */
	public RouteObj(Connection con,String GUID){
		GetDBData(con,GUID);
	}

    /**
     * Fill object from DB
     * @param con Connection to DB
     * @param GUID GUID of route
     */
	@Override
	public void GetDBData(Connection con, String GUID) {
        PreparedStatement stmt;
		try {
			ResultSet rs;
			stmt=con.prepareStatement("SELECT r.GUID, r.Owner, r.RStart, r.REnd, r.Gold, r.HP, r.Cooldown, r.Next, c1.Lat AS Lat1, c1.Lng AS Lng1, c2.Lat Lat2, c2.Lng AS Lng2\n" +
                    "FROM route r, cities c1, cities c2 " +
                    "WHERE r.GUID = ? " +
                    "AND r.rstart = c1.guid " +
                    "AND r.rend = c2.guid " +
                    "LIMIT 0,1");
			stmt.setString(1, GUID);
			rs=stmt.executeQuery();
			rs.first();
			this.GUID=rs.getString("GUID");
			Owner=rs.getString("Owner");
			RStart=rs.getString("RStart");
			REnd=rs.getString("REnd");
            SLat=rs.getInt("Lat1");
            SLng=rs.getInt("Lng1");
            ELat=rs.getInt("Lat2");
            ELng=rs.getInt("Lng2");
			Gold=rs.getInt("Gold");
			HP=rs.getInt("HP");
			Cooldown=rs.getInt("Cooldown");
			Next=rs.getDate("Next");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LastError=e.toString();
		}
		
	}

	public void SetNext(){
		Next.setTime(Next.getTime()+Cooldown);
	}
    /**
     * Write data to DB
     * @param con Connection to DB
     */
	@Override
	public void SetDBData(Connection con) {
        PreparedStatement stmt;
		try {
			ResultSet rs;
			if (Gold==0) {
				stmt=con.prepareStatement("SELECT Lat,Lng from route where GUID=? or GUID=?");
				stmt.setString(1, RStart);
				stmt.setString(2, REnd);
				rs=stmt.executeQuery();
				rs.first();
				double Lat1=rs.getInt("Lat")/1e6;
				double Lng1=rs.getInt("Lng")/1e6;
				rs.next();
				double Lat2=rs.getInt("Lat")/1e6;
				double Lng2=rs.getInt("Lng")/1e6;
				Gold=(int) Math.floor(MyUtils.distVincenty(Lat1, Lng1, Lat2, Lng2)/1000);
			}
			stmt=con.prepareStatement("INSERT INTO route(GUID,Owner,RStart,REnd,Gold,HP,Cooldown,Next) VALUES ("
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?,"
					+ "?"
					+ ")"
					+ "  ON DUPLICATE KEY UPDATE Next=?");
			stmt.setString(1, GUID);
			stmt.setString(2, Owner);
			stmt.setString(3, RStart);
			stmt.setString(4, REnd);
			stmt.setInt(5, Gold);
			stmt.setInt(6, HP);
			stmt.setInt(7, Cooldown);
			stmt.setDate(8, Next);
			stmt.setDate(9, Next);
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LastError=e.toString();
		}

	}

    /**
     * Generate JSON
     * @return JSON of Object
     */
	@Override
	public String toString(){
        return "{"+
				"GUID:"+'"'+GUID+'"'+
				",Owner:"+'"'+Owner+'"'+
				",Start:"+'"'+RStart+'"'+
				",End:"+'"'+REnd+'"'+
				",Gold:"+Gold+
				",HP:"+HP+
				",Cooldown:"+Cooldown+
				",Next:"+Next.getTime()+
				"}";

		
	}

}
