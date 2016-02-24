package com.example.gokhan.weatherapp;

/**
 * Created by GOKHAN on 2/23/2016.
 */
public class RestConstants {

    //Cache keys
    public static final String WEATHER_DETAILS = "WEATHER_DETAILS";
    final public static String appFormUrlEncoded= "application/x-www-form-urlencoded";
    final public static String methodPost = "POST";
    final public static String httpsStr = "https";
    final public static String contentType = "Content-Type";
    //Retry Policy
    public static final boolean NO_RETRY_POLICY = true;

    //web service response data cache duration
    public static final Integer WEATHER_DETAILS_CACHE_DURATION = 600000;   //10 minute


}
