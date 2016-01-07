package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Caravan object
 */
public class CaravanObj implements GameObject {

    private String GUID;
    private String Owner;
    private String StartPoint;
    private String EndPoint;
    private int Lat;
	private int Lng;
    private int ELat;
    private int ELng;
    private String Stealed;
    private int SpdLat;
    private int SpdLng;
    private String LastError;
    /**
     * Constructor
     * @param route Route for which caravan spawned
     */
    public CaravanObj(RouteObj route) {
        GUID = UUID.randomUUID().toString();
        this.Owner = route.GetOwner();
        this.Lat = route.GetSLat();
        this.Lng = route.GetSLng();
        this.EndPoint = route.GetREnd();
        this.ELat = route.GetELat();
        this.ELng = route.GetELng();
        this.Stealed = "N";
        SpeedCount();

    }

    /**
     * Create data from DB
     * @param con Connection to DB
     * @param GUID GUID of object
     */
    public CaravanObj(Connection con, String GUID) throws SQLException {
        GetDBData(con, GUID);
    }

    public String GetGUID() {
        return GUID;
    }

    public String GetOwner() {
        return this.Owner;
    }

    public String GetLastError() {
        return LastError;
    }

    /**
     * Load data from DB
     * @param con Connection to DB
     * @param GUID GUID of object
     */
    @Override
    public void GetDBData(Connection con, String GUID) throws SQLException {

        PreparedStatement stmt;
        stmt = con.prepareStatement("SELECT a.GUID, a.OWNER,a.STARTPOINT, a.ENDPOINT, a.LAT, a.LNG, a.STEALED, b.lat ELat, b.lng ELng,a.SpdLat,a.SpdLng\n" +
                "FROM  `caravan` a, cities b WHERE a.GUID=? LIMIT 0,1");
        stmt.setString(1, GUID);
        ResultSet rs = stmt.executeQuery();
        rs.first();
        this.GUID = rs.getString("GUID");
        this.Owner = rs.getString("OWNER");
        this.StartPoint = rs.getString("STARTPOINT");
        this.EndPoint = rs.getString("ENDPOINT");
        this.Stealed = rs.getString("STEALED");
        this.Lat = rs.getInt("Lat");
        this.Lng = rs.getInt("Lng");
        this.ELat = rs.getInt("ELat");
        this.ELng = rs.getInt("ELng");
        this.SpdLat = rs.getInt("SpdLat");
        this.SpdLng = rs.getInt("SpdLng");
        stmt.close();

    }

    /**
     * Count speed of caravan in Lat and Lng
     */
    public void SpeedCount() {
        double TimeToGo = MyUtils.distVincenty(Lat / 1e6, Lng / 1e6, ELat / 1e6, ELng / 1e6) / 100;
        SpdLat = (int) ((ELat - Lat) / TimeToGo);
        SpdLng = (int) ((ELng - Lng) / TimeToGo);
    }

    /**
     *
     * @return Gold of count
     */
    public int GetGold(Connection con) throws SQLException {
        CityObj startPoint = new CityObj(con, this.StartPoint);
        CityObj endPoint = new CityObj(con, this.EndPoint);
        return (int) (MyUtils.distVincenty(startPoint.GetLat() / 1e6, startPoint.GetLng() / 1e6, endPoint.GetLat() / 1e6, endPoint.GetLng() / 1e6) / 1000);
    }

    /**
     * Write data to DB
     * @param con Connection to DB
     */
	@Override
    public void SetDBData(Connection con) throws SQLException {
        PreparedStatement stmt;

            stmt = con.prepareStatement("INSERT INTO caravan(GUID,Owner,StartPoint,EndPoint,Lat,Lng,Stealed,SpdLat,SpdLng) VALUES ("
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?"
                    + ") ON DUPLICATE KEY UPDATE EndPoint=?,Lat=?,Lng=?,ELat=?,ELng=?,Stealed=?");
            stmt.setString(1, GUID);
            stmt.setString(2, Owner);
            stmt.setString(3, StartPoint);
            stmt.setString(4, EndPoint);
            stmt.setInt(5, Lat);
            stmt.setInt(6, Lng);
            stmt.setString(7, Stealed);
            stmt.setInt(8, SpdLat);
            stmt.setInt(9, SpdLng);
            stmt.setString(10, EndPoint);
            stmt.setInt(11, Lat);
            stmt.setInt(12, Lng);
            stmt.setInt(13, ELat);
            stmt.setInt(14, ELng);
            stmt.setString(15, Stealed);
            stmt.execute();
            stmt = con.prepareStatement("INSERT INTO aobject(GUID,ObjectType,Lat,Lng) VALUES("
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?"
                    + ") ON DUPLICATE KEY UPDATE Lat=?,Lng=?");
            stmt.setString(1, GUID);
            stmt.setString(2, "CARAVAN");
            stmt.setInt(3, Lat);
            stmt.setInt(4, Lng);
            stmt.setInt(5, Lat);
            stmt.setInt(6, Lng);
            stmt.execute();
            stmt.close();

    }

    public void Move(Connection con) throws SQLException {
        //Передвинуть караван
        //Загрузить целевой город.
        CityObj TargetCity = new CityObj(con, EndPoint);
        //Определить растояние до целевого города от текущей точки
        //Расчитать сколько участков по Скорость каравана в растоянии
        //Расчитать SpeedX
        //Расчитать SpeedY
        //Изменить Положение
    }

    public boolean CheckAmbush() {
        //Проверить наличие засады в радиусе
        //Отобрать все засады в квадрате10000 от X и Y
        //Для каждой найденной засады.
        //Загрузить засаду
        //Проверить пересекает ли прамая радиус засады
        //Если пересекает то сохранить засады и выйти из цикла
        //Если есть найденная засада
        //Загрузить игрок влаедльца засады
        //Начислить деньги владельцу
        //Удалить караван
        //Удалить засаду
        //Вернуть засаду к доступным засадам игрока.
        //Вернуть True
        //Если нет засады вернуть FALSE
        return false;
    }

    public boolean CheckCity() {
        //Проверить достижение караваном города
        //Загрузить целевой город
        //Определить растояние до города
        //Если растояние меньше скорости то
        //Расчитать награду
        //Начислить деньги городу старта
        //Начислить деньги городу конца
        //Начислить деньги герою
        //Установить текущую позицию в позицию города
        //Поменять точку начала и точку конца местами.
        return false;
    }


}
