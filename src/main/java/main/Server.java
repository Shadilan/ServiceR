package main;

/**
 * Created by Well on 17.01.2016.
 */
public class Server {
    public Server() {

    }

    public static double RangeCheck(double PLAT, double PLNG, double LAT, double LNG) {
        return Math.round(6378137 * Math.acos(Math.cos(PLAT / 1e6 * Math.PI / 180) *
                Math.cos(LAT / 1e6 * Math.PI / 180) * Math.cos(PLNG / 1e6 * Math.PI / 180 - LNG / 1e6 * Math.PI / 180) +
                Math.sin(PLAT / 1e6 * Math.PI / 180) * Math.sin(LAT / 1e6 * Math.PI / 180)));
    }
}
