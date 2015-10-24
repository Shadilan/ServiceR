package main;

import javax.swing.plaf.SliderUI;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RouteObj implements GameObject {

	private String GUID;
	private String Owner;
	private String RStart;
	private String REnd;
	private int Gold;
	private int HP;
	private int Cooldown;
	private Date Next;
	private String LastError;
	private int SLat;
	private int SLng;
	private int ELat;
	private int ELng;
	public String GetLastError(){
		return LastError;
	}
	public String GetGuid(){
		return GUID;
	}

    public int GetSLat(){return SLat;}
    public int GetSLng(){return SLng;}
    public int GetELat(){return ELat;}
    public int GetELng(){return ELng;}


    public RouteObj(String Owner,String RStart,String REnd){
		GUID=UUID.randomUUID().toString();
		this.Owner=Owner;
		this.RStart=RStart;
		this.REnd=REnd;
		Gold=0;
		HP=10;
	}

	public RouteObj(Connection con,String GUID){
		GetDBData(con,GUID);
	}
	@Override
	public void GetDBData(Connection con, String GUID) {
		PreparedStatement stmt=null;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
			LastError=e.toString();
		}
		
	}

	@Override
	public void SetDBData(Connection con) {
		PreparedStatement stmt=null;
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			LastError=e.toString();
		}

	}
	
	@Override
	public String toString(){
		String result="{"+
				"GUID:"+'"'+GUID+'"'+
				",Owner:"+'"'+Owner+'"'+
				",Start:"+'"'+RStart+'"'+
				",End:"+'"'+REnd+'"'+
				",Gold:"+Gold+
				",HP:"+HP+
				",Cooldown:"+Cooldown+
				",Next:"+Next.getTime()+
				"}";
		return result;
		
	}

}
