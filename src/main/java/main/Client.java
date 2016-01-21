package main;

import org.json.simple.JSONObject;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

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

    public String sendData(String ReqName, String PGUID, String TGUID, int PLAT, int PLNG, int LAT, int LNG) {
        String result;
        switch (ReqName) {
            case "ScanRange":
                result = Player.ScanRange(PGUID, PLAT, PLNG, con);
                break;
            case "SetAmbush":
                result = Ambush.Set(PGUID, PLAT, PLNG, LAT, LNG, con);
                break;
            case "DestroyAmbush":
                result = Ambush.Destroy(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "StartRoute":
                result = Caravan.StartRoute(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "FinishRoute":
                result = Caravan.FinishRoute(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "BuyUpgrade":
                result = Player.BuyUpgrade(PGUID, TGUID, PLAT, PLNG, con);
                break;
            case "DropRoute":
                result = Caravan.DropRoute(PGUID, con);
                break;
            case "GetPlayerInfo":
                result = Player.GetPlayerInfo(PGUID, con);
                break;
            default:
                result = "{" + '"' + "Error" + '"' + ": " + '"' + "Unknown command." + '"' + "}";
        }
        return result;
    }

    public String GetToken(String Login, String Password) {
        PreparedStatement pstmt;
        String Token = "T" + UUID.randomUUID().toString();
        String PGUID;
        JSONObject jresult = new JSONObject();

        try {
            con = DBUtils.ConnectDB();
            pstmt = con.prepareStatement("SELECT GUID from Users WHERE Login=? and Password=?");
            pstmt.setString(1, Login);
            pstmt.setString(2, Password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.isBeforeFirst()) {
                PGUID = rs.getString(1);
                pstmt = con.prepareStatement("INSERT into Connections (PGUID,Token) Values(?,?)");
                pstmt.setString(1, PGUID);
                pstmt.setString(2, Token);
                pstmt.execute();
                con.commit();
                con.close();
                jresult.put("Token", Token);
            } else {
                jresult.put("Error", "User not found.");
            }
        } catch (SQLException e) {
            jresult.put("Error", "DBError. " + e.toString() + "\n" + Arrays.toString(e.getStackTrace()));

        } catch (NamingException e) {
            jresult.put("Error", "ResourceError. " + e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        return jresult.toString();
    }

}
