package com.example.radu5.turistgroupchat.Utils;

/**
 * Created by radu5 on 4/13/2018.
 */

public class PathFindingUtils
{
    public static double havesineDistance(double lat1,double lng1,double lat2,double lng2)
    {
        final int R=6371;//Earth radius in KM
        double dlat=Math.toRadians((lat2-lat1));
        double dlng=Math.toRadians((lng2-lng1));

        lat1=Math.toRadians(lat1);
        lat2=Math.toRadians(lat2);

        double a= haversin(dlat) + Math.cos(lat1) * Math.cos(lat2) * haversin(dlng);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R*c;
        //distance in killometers

    }
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
