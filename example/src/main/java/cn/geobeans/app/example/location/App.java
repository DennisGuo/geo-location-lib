package cn.geobeans.app.example.location;

import android.app.Application;

import cn.geobeans.app.lib.location.GeoLocation;

/**
 * Created by ghx on 2017/3/27.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GeoLocation.getInstance().init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            GeoLocation.getInstance().destroy(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

