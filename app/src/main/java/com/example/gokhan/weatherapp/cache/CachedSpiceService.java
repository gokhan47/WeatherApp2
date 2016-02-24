package com.example.gokhan.weatherapp.cache;

import android.app.Application;
import android.util.Log;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.memory.LruCacheStringObjectPersister;

import roboguice.util.temp.Ln;

/**
 * Created by GOKHAN on 2/23/2016.
 */

public class CachedSpiceService extends SpiceService {

    @Override
    public void onCreate() {
        super.onCreate();
            // Logging really causes the app to chug with this many requests
            Ln.getConfig().setLoggingLevel(Log.ERROR);
    }


    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager manager = new CacheManager();

        LruCacheStringObjectPersister memoryPersister = new LruCacheStringObjectPersister(500000);
        manager.addPersister(memoryPersister);

        return manager;

    }
}
