package com.example.gokhan.weatherapp.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by GOKHAN on 2/24/2016.
 */
public class Channel implements JSONPopulator {
    private Units units;
    private Items item;
    private String location;

    public Units getUnits() {
        return units;
    }

    public Items getItem() {
        return item;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void populate(JSONObject data) {

        units = new Units();
        units.populate(data.optJSONObject("units"));

        item = new Items();
        item.populate(data.optJSONObject("item"));

        JSONObject locationData = data.optJSONObject("location");

        String region = locationData.optString("region");
        String country = locationData.optString("country");

        location = String.format("%s, %s", locationData.optString("city"), (region.length() != 0 ? region : country));
    }

    @Override
    public JSONObject toJSON() {

        JSONObject data = new JSONObject();

        try {
            data.put("units", units.toJSON());
            data.put("item", item.toJSON());
            data.put("requestLocation", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

}
