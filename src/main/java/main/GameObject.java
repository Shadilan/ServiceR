package main;

import java.sql.Connection;
import java.sql.SQLException;

public interface GameObject {
	void GetDBData(Connection con, String GUID) throws SQLException;

	void SetDBData(Connection con) throws SQLException;

	String toString();

	String action(String Token, int PLat, int PLng, String TargetGUID, String Action) {
		switch (Action) {
			case "createRoute":
				RouteObj route = new RouteObj();
				PlayerObj player = new PlayerObj(Token);
				if (!route.checkCreateRoute(player.GetGUID())) {
					route.createRoute(player.GetGUID(), TargetGUID);
				}


		}
	}


}
