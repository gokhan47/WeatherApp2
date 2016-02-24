package com.example.gokhan.weatherapp.data;

/**
 * Created by GOKHAN on 2/24/2016.
 */
public interface WeatherServiceListener {
    void serviceSuccess(Channel channel);

    void serviceFailure(Exception exception);
}
