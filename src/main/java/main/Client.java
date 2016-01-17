package main;

/**
 * Created by Well on 17.01.2016.
 */
public class Client {
    public Client() {
        //create connection to DB
    }

    public void sendData(String ACTION, String PGUID, int PLAT, int PLNG) {
        //It's ScanRange
        Server.ScanRange(PGUID, PLAT, PLNG);
    }

    public void sendData(String ACTION, String PGUID, int PLAT, int PLNG, int LAT, int LNG) {
        //It's SetAmbush
        Ambush.Set(PGUID, PLAT, PLNG, LAT, LNG);
    }

    public void sendData(String ACTION, String PGUID, String TGUID, int PLAT, int PLNG) {
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
}
