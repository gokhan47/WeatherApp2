package com.example.gokhan.weatherapp.restrequest;

import android.net.Uri;
import android.util.Log;
import com.example.gokhan.weatherapp.RestConstants;
import com.octo.android.robospice.request.SpiceRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by GOKHAN on 2/23/2016.
 */

public class WeatherRequest extends SpiceRequest<String> {

    private String location;

    public WeatherRequest(String location) {
        super(String.class);
        this.location = location;
    }


    @Override
    public String loadDataFromNetwork() throws Exception {

        String data = "";

         //data = ( (new WeatherHttpClient()).getWeatherData(location));

                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", location);

        String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));
        try {
            URL url = new URL(endpoint);

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);

            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            data = result.toString();
        }catch (Exception ignored) {
        }

        Log.d(getClass().getSimpleName() + "###Gokhan-response###", data);
            return data;


    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * #@return
     */
    public String createCacheKey() {
        return RestConstants.WEATHER_DETAILS + location;
    }
}

