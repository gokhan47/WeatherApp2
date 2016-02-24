package com.example.gokhan.weatherapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gokhan.weatherapp.CustomProgressDialog;
import com.example.gokhan.weatherapp.R;
import com.example.gokhan.weatherapp.RestConstants;
import com.example.gokhan.weatherapp.cache.WeatherCacheService;
import com.example.gokhan.weatherapp.data.Channel;
import com.example.gokhan.weatherapp.data.Items;
import com.example.gokhan.weatherapp.data.WeatherServiceListener;
import com.example.gokhan.weatherapp.cache.CachedSpiceService;
import com.example.gokhan.weatherapp.restrequest.WeatherRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by GOKHAN on 2/23/2016.
 */

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, LocationListener, WeatherServiceListener {

    protected SpiceManager spiceManager = new SpiceManager(CachedSpiceService.class);
    private LocationManager locationManager;
    private String locationProvider;
    private LocationListener locationListener;
    private GoogleMap mMap;
    private CustomProgressDialog progress;
    private Context context;
    private Location location;
    private String cityName;

    // counter for failed weather service attempts
    private int weatherServiceFailures = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.mapping_activty_title);
        }

        Button weatherButton;weatherButton = (Button) findViewById(R.id.weatherButton);

        // To get Weather info
        weatherButton.setOnClickListener(weatherInfoListener());
        //Initialize GPS
        initGPSListener();

        //Initialize Progress Bar
        progress = new CustomProgressDialog(context, true);
        initProgressListener();

        MapsInitializer.initialize(this);
        setUpMapIfNeeded();


        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();

        assert mapView != null;
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));  //Set center of us
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(7));
                }
            });
        }

    }

    @Override
    public void serviceSuccess(Channel channel) {
        progress.hide();

         TextView temperatureTextView;
         TextView conditionTextView;
         TextView locationTextView;

        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        conditionTextView = (TextView) findViewById(R.id.conditionTextView);
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        Items item = channel.getItem();

        String temperatureLabel = (item.getCondition().getTemperature() + "/" + channel.getUnits().getTemperature());

        temperatureTextView.setText(temperatureLabel);
        conditionTextView.setText(item.getCondition().getDescription());
        locationTextView.setText(channel.getLocation());
       // Log.d(getClass().getSimpleName() + "temp: "+  temperatureLabel, " des: " +  item.getCondition().getDescription().toString() + " location: " + channel.getLocation().toString() );
    }

    @Override
    public void serviceFailure(Exception exception) {

        // display error if this is the second failure
        if (weatherServiceFailures > 0) {
            progress.hide();
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            WeatherCacheService cacheService; cacheService = new WeatherCacheService(this);
            // error doing reverse geocoding, load weather data from cache
            weatherServiceFailures++;
            cacheService.load(this);
        }
    }

    private View.OnClickListener weatherInfoListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherInfo(v);
            }
        };
    }

    // Weather Button listener method
    private void weatherInfo(View v) {
        performWeatherCall(cityName);
       // performWeatherCall(location.toString());
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void performWeatherCall(String loc) {
        //Show progress spinner
        progress = new CustomProgressDialog(this, false);
        progress.startCustomProgressDialog();
        WeatherRequest requestWeather = new WeatherRequest(loc);

        //Make weather details a high priority request
        requestWeather.setPriority(SpiceRequest.PRIORITY_HIGH);

        //Set retry policy
        if (RestConstants.NO_RETRY_POLICY) requestWeather.setRetryPolicy(null);

        //If data is valid get data from cache unless expired
        getSpiceManager().execute(requestWeather, requestWeather.createCacheKey(), RestConstants.WEATHER_DETAILS_CACHE_DURATION, new WeatherDetailListener(requestWeather.createCacheKey()));

    }

    private class WeatherDetailListener implements RequestListener<String> {

        private String cacheKey;

        public WeatherDetailListener(String cacheKey) {
            this.cacheKey = cacheKey;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            progress.stopCustomProgressDialog();

        }

        @Override
        public void onRequestSuccess(String result) {
            progress.stopCustomProgressDialog();
            if (result == null) {
                getSpiceManager().removeDataFromCache(String.class, cacheKey);
                Log.d(getClass().toString() + "onRequestSuccess", "result = null");

                return;
            }
            Channel channel = new Channel();
             Exception error=null;

            try {
//                ResponseWeather weatherResponse = new ResponseWeather();
//                weatherResponse = mapper.readValue(result, ResponseWeather.class);

                JSONObject data = new JSONObject(result);

                JSONObject queryResults = data.optJSONObject("query");

                int count = queryResults.optInt("count");

                if (count == 0) {
                    Log.d(getClass().getSimpleName(),"No weather information found");
                    return;
                }

                JSONObject channelJSON = queryResults.optJSONObject("results").optJSONObject("channel");
                channel.populate(channelJSON);

               // serviceSuccess(channel);

            Log.d(getClass().toString() + "onRequestSuccess", result);
            } catch (Exception e) {
                error = e;
                getSpiceManager().removeDataFromCache(String.class, cacheKey);
                Log.d(getClass().toString() + "onRequestSuccess", e.toString());
            }

            if (channel == null && error != null) {
                serviceFailure(error);
            } else {
                serviceSuccess(channel);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


        private void setUpMap() {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            // Set listeners for marker events.  See the bottom of this class for their behavior.
            mMap.setOnInfoWindowClickListener(this);
            mMap.setOnMapLongClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }


        private void initProgressListener() {
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //Stop all running tasks
                    if (locationManager != null) locationManager.removeUpdates(locationListener);
                    progress.stopCustomProgressDialog();
                }
            });
        }

        private void initGPSListener() {
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationProvider = LocationManager.GPS_PROVIDER;

            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            location = locationManager.getLastKnownLocation(bestProvider);
            getCurrentCityName(location);

            // Define a listener that responds to location updates
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    progress.stopCustomProgressDialog();
                    //Stop updates until GPS button is pressed again
                    if (locationManager != null) locationManager.removeUpdates(locationListener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
        }

        private void getCurrentCityName(Location location) {
            TextView currentLocationStr;
            currentLocationStr = (TextView) findViewById(R.id.currentLocationName);
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                    currentLocationStr.setText(cityName);
                }
            } catch (IOException ignored) {
            }

        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            //Launch Google Navigation with the selected location.
            LatLng point = marker.getPosition();
            double latitude = point.latitude;
            double longitude = point.longitude;
            String url = "google.navigation:q=" + latitude + "," + longitude;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }


        @Override
        public void onMapLongClick(LatLng point) {
            //Stop any location update request to prevent conflict with zip request
            if (locationManager != null) locationManager.removeUpdates(locationListener);
        }

        @Override
        public boolean onMyLocationButtonClick() {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Register the listener with the Location Manager to receive location update
                locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                //Show finding GPS progress bar
            } else {
                showErrorDialog();
            }
            return false;
        }

        private void showErrorDialog() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(context.getResources().getString(R.string.gps_location_off_title))
                    .setMessage(context.getResources().getString(R.string.gps_location_off_body))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        protected void onStart() {
            super.onStart();
            spiceManager.start(this);
        }

        @Override
        protected void onStop() {
            spiceManager.shouldStop();
            super.onStop();
        }


        @Override
        public void onResume() {
            super.onResume();
            setUpMapIfNeeded();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            //remove updates to prevent crashes when activity is exited.
            locationManager.removeUpdates(locationListener);
        }

        protected SpiceManager getSpiceManager() {
            return spiceManager;
        }
    }