package main;

import java.sql.Connection;

/**
 * Created by Well on 17.01.2016.
 */
public class Caravan {
    public Caravan() {
    }

    public static void StartRoute(String PGUID, String CGUID, double PLAT, double PLNG, Connection con) {
        //check range to city, start route
    }

    public static void FinishRoute(String PGUID, String CGUID, double PLAT, double PLNG, Connection con) {
        //check range to city, finish route
    }

    public static void DropRoute(String PGUID, Connection con) {
        //
    }
}
