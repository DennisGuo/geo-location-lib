package cn.geobeans.app.lib.location;

import android.location.Location;
import android.os.Bundle;

/**
 * 位置监听类
 */
public class GeoLocationListener {

    public void onLocationChanged(Location location) {}
    public void onStatusChanged(String provider, int status, Bundle extras){};
    public void onProviderEnabled(String provider){}
    public void onProviderDisabled(String provider){}
}
