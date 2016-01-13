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

    //CСкорость каравана
    private int Speed;
    private String GUID;
    private String PGUID;
    private String Start;
    private String Finish;
    private int Lat;
	private int Lng;
    private String LastError;
    /**
     * Constructor
     * @param route Route for which caravan spawned
     */
    public CaravanObj(RouteObj route) {
        GUID = UUID.randomUUID().toString();
        this.PGUID = route.GetOwner();
        this.Lat = route.GetSLat();
        this.Lng = route.GetSLng();
        this.Finish = route.GetREnd();
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
        return this.PGUID;
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
        stmt = con.prepareStatement("SELECT a.GUID, a.PGUID,a.START, a.Finish, a.Speed,\n" +
                "b.Lat,b.Lng\n" +
                "FROM  `caravan` a,\n" +
                "GameObjects b\n" +
                "WHERE a.GUID=b.GUID\n" +
                "and a.GUID=? LIMIT 0,1");
        stmt.setString(1, GUID);
        ResultSet rs = stmt.executeQuery();
        rs.first();
        this.GUID = rs.getString("GUID");
        this.PGUID = rs.getString("PGUID");
        this.Start = rs.getString("START");
        this.Finish = rs.getString("Finish");
        this.Speed = rs.getInt("Finish");
        this.Lat = rs.getInt("Lat");
        this.Lng = rs.getInt("Lng");
        stmt.close();
    }
    /**
     *
     * @return Gold of count
     */
    public int GetGold(Connection con) throws SQLException {
        CityObj startPoint = new CityObj(con, this.Start);
        CityObj endPoint = new CityObj(con, this.Finish);
        return (int) (MyUtils.distVincenty(startPoint.GetLat() / 1e6, startPoint.GetLng() / 1e6, endPoint.GetLat() / 1e6, endPoint.GetLng() / 1e6) / 1000);
    }

    /**
     * Write data to DB
     * @param con Connection to DB
     */
	@Override
    public void SetDBData(Connection con) throws SQLException {
        PreparedStatement stmt;
        stmt = con.prepareStatement("INSERT INTO caravan(GUID,PGUID,Start,Finish,Speed) VALUES ("
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                    + "?,"
                + ") ON DUPLICATE KEY UPDATE Start=?,Finish=?,Speed=?");
            stmt.setString(1, GUID);
        stmt.setString(2, PGUID);
        stmt.setString(3, Start);
        stmt.setString(4, Finish);
        stmt.setInt(5, Speed);
        stmt.setString(6, Start);
        stmt.setString(7, Finish);
        stmt.setInt(8, Speed);
            stmt.execute();
        stmt = con.prepareStatement("INSERT INTO GameObject(GUID,Type,Lat,Lng) VALUES("
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
        //Загрузить целевой город.
        CityObj TargetCity = new CityObj(con, Finish);
        //Определить растояние до целевого города от текущей точки
        double distance = MyUtils.distVincenty(Lat, Lng, TargetCity.GetLat(), TargetCity.GetLng());
        //Расчитать сколько участков по Скорость каравана в растоянии
        double cnt = distance / (double) Speed;
        //Расчитать SpeedX
        double SpeedLat = (TargetCity.GetLat() - Lat) / cnt;
        //Расчитать SpeedY
        double SpeedLng = (TargetCity.GetLng() - Lng) / cnt;
        //Изменить Положение
        if (Math.abs(cnt) < 1) {
            SpeedLat = (TargetCity.GetLat() - Lat);
            SpeedLng = (TargetCity.GetLng() - Lng);
        }
        Lat += SpeedLat;
        Lng += SpeedLng;
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
