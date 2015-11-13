package main;

import javax.naming.NamingException;
import java.sql.*;
import java.util.UUID;

/**
 * @author Shadilan
 */

public class RouteObj implements GameObject {

	private String GUID;
	private String Owner;
	private String RStart;
	private String REnd;
	private int Gold;
	private int HP;
	private int Cooldown;
	private Date Next;
	private int SLat;
	private int SLng;
	private int ELat;
	private int ELng;
	//TODO: Change to Exception;
	private String LastError;

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

	public RouteObj(String Owner, String StartCity) {

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
	public RouteObj(Connection con, String GUID) throws SQLException {
		GetDBData(con,GUID);
	}

	public String GetGuid() {
		return GUID;
	}

	public String GetOwner() {
		return this.Owner;
	}

	public String GetRStart() {
		return RStart;
	}

	public String GetREnd() {
		return REnd;
	}

	public int GetSLat() {
		return SLat;
	}

	public int GetSLng() {
		return SLng;
	}

	public int GetELat() {
		return ELat;
	}

	public int GetELng() {
		return ELng;
	}

	public String GetLastError() {
		return LastError;
	}

    /**
     * Fill object from DB
     * @param con Connection to DB
     * @param GUID GUID of route
     */
	@Override
	public void GetDBData(Connection con, String GUID) throws SQLException {
		PreparedStatement stmt;

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


	}

	public void SetNext(){
		Next.setTime(Next.getTime()+Cooldown);
	}
    /**
     * Write data to DB
     * @param con Connection to DB
     */
	@Override
	public void SetDBData(Connection con) throws SQLException {
		PreparedStatement stmt;
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

	@Override
	public String action(Connection con, String Token, int PLat, int PLng, String TargetGUID, String Action) {
		return null;
	}

	//проверяем возможность создания маршрута. если есть незавершенный маршрут, то нельзя
	public String checkCreateRoute(String Owner) {
		PreparedStatement stmt;
		try {
			Connection con = DBUtils.ConnectDB();
			ResultSet rs;
			stmt = con.prepareStatement("select count(1) cnt from routes where Owner=? and finish is null");
			stmt.setString(1, Owner);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt") > 0) {
				return MyUtils.getJSONError("RouteAlreadyStarted", "Сначала завершите текущий маршрут");
			} else {
				return "OK";//Здесь наверное можно оставить Ok чтобы потом проще проверять было
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	//создаем незавершенный (из одного города) маршрут, проверок внутри нет - все проверки отдельно (ну или надо обсуждать)
	public String createRoute(String Owner, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		int c_lat;
		int c_lng;
		try {
			Connection con = DBUtils.ConnectDB();
			String GUID_ROUTE = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into routes (guid,owner,start) values(?,?,?)");
			stmt.setString(1, GUID_ROUTE);
			stmt.setString(2, Owner);
			stmt.setString(3, City);
			stmt.execute();

			stmt = con.prepareStatement("select lat, lng from cities where GUID=?");
			stmt.setString(1, City);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			c_lat = rs.getInt("lat");
			c_lng = rs.getInt("lng");

			String GUID = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into waypoints values(?,?,'1',?,?)");
			stmt.setString(1, GUID);
			stmt.setString(2, GUID_ROUTE);
			stmt.setInt(3, c_lat);
			stmt.setInt(4, c_lng);
			stmt.execute();
			return MyUtils.getJSONSuccess("Route created.");
		} catch (SQLException e) {
			return MyUtils.getJSONError("DBError", e.toString());
		} catch (NamingException e) {
			return MyUtils.getJSONError("NamingError", e.toString());
		}
	}

	//проверяем возможность завершения маршрута в этом городе. если уже есть маршрут между двумя этими городами, то отказываем
	public String checkFinishRoute(String Owner, String Route, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("select count(1) cnt from routes where (start,finish) in (select start,? from routes where guid=?) or (finish,start) in (select start,? from routes where guid=?)");
			stmt.setString(1, City);
			stmt.setString(2, Route);
			stmt.setString(3, City);
			stmt.setString(4, Route);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			if (rs.getInt("cnt") > 0) {
				return MyUtils.getJSONError("RouteAlreadyExists","Route already exists.");
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

	//завершаем маршрут в указанном городе
	public String finishRoute(String Owner, String Route, String City) {
		PreparedStatement stmt;
		ResultSet rs;
		int c_lat;
		int c_lng;
		try {
			Connection con = DBUtils.ConnectDB();
			//добавим конечный город в вэйпойнты
			stmt = con.prepareStatement("select lat, lng from cities where GUID=?");
			stmt.setString(1, City);
			stmt.execute();
			rs = stmt.executeQuery();
			rs.first();
			c_lat = rs.getInt("lat");
			c_lng = rs.getInt("lng");
			String GUID = UUID.randomUUID().toString();
			stmt = con.prepareStatement("insert into waypoints values(?,?,'1',?,?)");
			stmt.setString(1, GUID);
			stmt.setString(2, Route);
			stmt.setInt(3, c_lat);
			stmt.setInt(4, c_lng);
			stmt.execute();

			//незавершенный маршрут сделаем завершенным
			stmt = con.prepareStatement("update routes set finish=? where guid=?");
			stmt.setString(1, City);
			stmt.setString(2, Route);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
		return MyUtils.getJSONSuccess("Route created.");
	}
}
