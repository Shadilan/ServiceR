package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Shadilan
 */

public class RouteObj implements GameObject {

	private String GUID;
	private String Owner;
	private String RStart;
	private String REnd;
	private int SLat;
	private int SLng;
	private int ELat;
	private int ELng;
	private String StartName;
	private String FinishName;
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


    /**
     * Fill object from DB
     * @param con Connection to DB
     * @param GUID GUID of route
     */
	@Override
	public void GetDBData(Connection con, String GUID) throws SQLException {
		PreparedStatement stmt;

			ResultSet rs;
			stmt=con.prepareStatement("SELECT r.GUID, r.Owner, r.start, r.finish,c1.CITYNAME start_name,c2.CITYNAME finish_name,\n" +
					"c1.Lat as slat,c1.Lng as slng, c2.lat as elat,c2.lng as elng \n" +
					"FROM routes r, cities c1, cities c2 " +
					"WHERE r.GUID = ? " +
					"AND r.start = c1.guid " +
					"AND r.finish = c2.guid " +
					"LIMIT 0,1");
			stmt.setString(1, GUID);
			rs=stmt.executeQuery();
			rs.first();
			this.GUID=rs.getString("GUID");
			Owner=rs.getString("Owner");
			RStart=rs.getString("start");
			REnd=rs.getString("finish");
            SLat=rs.getInt("slat");
            SLng=rs.getInt("slng");
            ELat=rs.getInt("elat");
            ELng=rs.getInt("elng");
			StartName=rs.getString("start_name");
			FinishName=rs.getString("finish_name");
			stmt.close();


	}

	@Override
	public void SetDBData(Connection con) throws SQLException {
		PreparedStatement stmt;
		stmt=con.prepareStatement("insert into routes(GUID,Owner,Start,Finish) VALUES (?,?,?,?) on DUPLICATE KEY UPDATE Owner=?,Start=?,Finish=?");
		stmt.setString(1, GUID);
		stmt.setString(2, Owner);
		stmt.setString(3, RStart);
		stmt.setString(4, REnd);
		stmt.setString(5, Owner);
		stmt.setString(6, RStart);
		stmt.setString(7, REnd);
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
				",Finish:"+'"'+REnd+'"'+
				",StartName:"+'"'+StartName+'"'+
				",FinishName:"+'"'+FinishName+'"'+
				"}";

		
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
				return "Ok";//Здесь наверное можно оставить Ok чтобы потом проще проверять было
			}
		} catch (SQLException e) {
			return MyUtils.getJSONError("DBError", e.toString());
		} catch (NamingException e) {
			return MyUtils.getJSONError("DBError", e.toString());
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
			con.commit();
			con.close();
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
			//Выбрать точки начала и конца
			stmt = con.prepareStatement("select c1.GUID start, c2.GUID finish, c1.lat lat1,c1.lng lng1,c2.lat lat2,c2.lng lng2 from routes r,cities c1, cities c2 " +
					"where c1.guid=r.start and c2.guid=r.finish and r.guid=?");
			stmt.setString(1, Route);

			rs = stmt.executeQuery();
			//Рассчитать скорость по Lat и Lng
			rs.first();
			int lat1 = rs.getInt("lat1");
			int lng1 = rs.getInt("lng1");
			int lat2 = rs.getInt("lat2");
			int lng2 = rs.getInt("lng2");
			String start = rs.getString("start");
			String finish = rs.getString("finish");
			double k = MyUtils.distVincenty(lat1, lng1, lat2, lng2) / 1666;
			int spdLat = (int) ((lat2 - lat1) / k);
			int spdLng = (int) ((lng2 - lng1) / k);
			String caravan = UUID.randomUUID().toString();
			//Сохранить караван
			stmt = con.prepareStatement("insert into caravan(GUID,Owner,route,StartPoint,EndPoint,Lat,Lng,SpdLat,SpdLng) " +
					"Values(?,?,?,?,?,?,?,?,?)");
			stmt.setString(1, caravan);
			stmt.setString(2, Owner);
			stmt.setString(3, GUID);
			stmt.setString(4, start);
			stmt.setString(5, finish);
			stmt.setInt(6, lat1);
			stmt.setInt(7, lng1);
			stmt.setInt(8, spdLat);
			stmt.setInt(9, spdLng);
			stmt.execute();
			stmt = con.prepareStatement("insert into aobject(GUID,ObjectType,Lat,Lng) VALUES(?,?,\"CARAVAN\",?)");
			stmt.setString(1, caravan);
			stmt.setInt(2, lat1);
			stmt.setInt(3, lng1);
			stmt.execute();
			con.commit();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}

		return MyUtils.getJSONSuccess("Route created.");
	}

	//получаем GUID незавершенного маршрута игрока (такой маршрут только один может быть)
	public String getUnfinishedRoute(String Owner) {
		PreparedStatement stmt;
		ResultSet rs;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("select GUID from routes where owner=? and finish is null");
			stmt.setString(1, Owner);
			stmt.execute();
			rs = stmt.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.first();
				return rs.getString("GUID");
			}
			else return "Not Found";
			//Плохо, если не получили результата то будет хрен знает какая строка и не понятные ошибки...
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
	}
//Zlodiak имхо такая функция не нужна
	/*
	public String cancelUnfinishedRoute(String Owner) {
		PreparedStatement stmt;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("delete from routes where FINISH is null and OWNER=?");
			stmt.setString(1, Owner);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();
		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
		return MyUtils.getJSONSuccess("Route canceled.");
	}
	*/
	public String dropRoute(String GUID) {
		PreparedStatement stmt;
		try {
			Connection con = DBUtils.ConnectDB();
			stmt = con.prepareStatement("delete from routes where GUID=?");
			stmt.setString(1, GUID);
			stmt.execute();
			stmt = con.prepareStatement("delete from aobject where GUID=?");
			stmt.setString(1, GUID);
			stmt.execute();
			//Shadilan: Удаляем караваны при удалении маршрута.
			stmt = con.prepareStatement("delete from aobject where GUID=(select guid from caravan where route=?)");
			stmt.setString(1, GUID);
			stmt.execute();
			stmt = con.prepareStatement("delete from caravan where GUID=?");
			stmt.setString(1, GUID);
			stmt.execute();
			con.commit();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return e.toString();

		} catch (NamingException e) {
			e.printStackTrace();
			return e.toString();
		}
		return MyUtils.getJSONSuccess("Route dropped.");
	}

}
