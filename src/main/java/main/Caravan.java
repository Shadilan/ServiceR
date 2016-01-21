package main;

import java.sql.Connection;

/**
 * Created by Well on 17.01.2016.
 */
public class Caravan {
    public Caravan() {
    }

    public static String StartRoute(String PGUID, String CGUID, int PLAT, int PLNG, Connection con) {
        //check range to city, start route
        return "1";
    }

    public static String FinishRoute(String PGUID, String CGUID, int PLAT, int PLNG, Connection con) {
        //check range to city, finish route
        return "1";
    }

    public static String DropRoute(String PGUID, Connection con) {
        //
        return "1";
    }
}
