package com.example.gokhan.weatherapp.data;

import org.json.JSONObject;

/**
 * Created by GOKHAN on 2/24/2016.
 */
public interface JSONPopulator {
    void populate(JSONObject data);

    JSONObject toJSON();
}
