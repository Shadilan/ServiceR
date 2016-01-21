package main;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Well on 17.01.2016.
 */
public class Client {
    String result = "";
    Connection con = null;
    public Client() {
        //create connection to DB
        try {
            con = DBUtils.ConnectDB();
        } catch (SQLException e) {
            result = MyUtils.getJSONError("DBError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (NamingException e) {
            result = MyUtils.getJSONError("ResourceError", e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        if (!result.equals("")) result = "No access to DB: " + result;
    }

    public Connection getCon() {
        return con;
    }

    public void sendData(String ReqName, String PGUID, String TGUID, double PLAT, double PLNG, double LAT, double LNG) {
        switch (ReqName) {
            case "ScanRange":
                Player.ScanRange(PGUID, PLAT, PLNG, con);
                break;
            case "SetAmbush":
                Ambush.Set(PGUID, PLAT, PLNG, LAT, LNG, con);
                break;
            case "DestroyAmbush":
                Ambush.Destroy(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "StartRoute":
                Caravan.StartRoute(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "FinishRoute":
                Caravan.FinishRoute(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "BuyUpgrade":
                Player.BuyUpgrade(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "DropRoute":
                Caravan.DropRoute(PGUID, con);
                break;
            case "GetPlayerInfo":
                Player.GetPlayerInfo(PGUID, con);
                break;
            default:
                //result = MyUtils.getJSONError("ActionNotFound", "Действие не определено");
        }
    }

/*
    public void sendData(String ACTION, String PGUID, double PLAT, double PLNG) {
        //It's ScanRange
        if (result.equals("")) Server.ScanRange(PGUID, PLAT, PLNG, con);
        //else Server.SendData(..)
    }

    public void sendData(String ACTION, String PGUID, double PLAT, double PLNG, double LAT, double LNG) {
        //It's SetAmbush
        Ambush.Set(PGUID, PLAT, PLNG, LAT, LNG, con);
    }

    public void sendData(String ACTION, String PGUID, String TGUID, double PLAT, double PLNG) {
        //It's DestroyAmbush or StartRoute or FinishRoute or BuyUpgrade
        switch (ACTION) {
            case "DestroyAmbush":
                Ambush.Destroy(PGUID, TGUID, PLAT, PLNG);
                break;
            case "StartRoute":
                Caravan.StartRoute(PGUID, TGUID, PLAT, PLNG);
                break;
            case "FinishRoute":
                Caravan.FinishRoute(PGUID, TGUID, PLAT, PLNG);
                break;
            case "BuyUpgrade":
                Player.BuyUpgrade(PGUID, TGUID, PLAT, PLNG);
                break;
            default:
                //result = MyUtils.getJSONError("ActionNotFound", "Действие не определено");
        }
    }

    public void sendData(String ACTION, String PGUID) {
        //It's DropRoute or GetPLayerInfo
        switch (ACTION) {
            case "DropRoute":
                Caravan.DropRoute(PGUID);
                break;
            case "GetPlayerInfo":
                Player.GetPlayerInfo(PGUID);
                break;
            default:
                //result = MyUtils.getJSONError("ActionNotFound", "Действие не определено");
        }
    }
    */
}
