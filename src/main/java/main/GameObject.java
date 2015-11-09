package main;

import java.sql.Connection;
import java.sql.SQLException;

public interface GameObject {
	void GetDBData(Connection con, String GUID) throws SQLException;

	void SetDBData(Connection con) throws SQLException;

	String toString();
	String action(String Token, int PLat, int PLng, String TargetGUID, String Action) {
		PlayerObj player = new PlayerObj(Token);
		switch (Action) {
			case "createRoute":
				RouteObj route = new RouteObj(player.GetGUID(), TargetGUID);
				if (route.checkCreateRoute(player.GetGUID()) == "ОК") {
					return route.createRoute(player.GetGUID(), TargetGUID);
				} else {
					return route.checkCreateRoute(player.GetGUID());
				}
				break;
			case "createAmbush":
				PlayerObj ambush = new PlayerObj();
				if (ambush.checkCreateAmbush(PLat, PLng) == "ОК") {
					return ambush.createAmbush(player.GetGUID(), PLat, PLng);
				} else {
					return ambush.checkCreateAmbush(PLat, PLng);
				}
				break;
			default:
				return "Действие не определено";
		}

	}

}
