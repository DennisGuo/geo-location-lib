package cn.geobeans.app.lib.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * GeoLocation主类,提供主要的接口方法
 * Created by ghx on 2017/3/27.
 * updated by ghx on 2018/10/31.
 */

public class GeoLocation implements GeoLoc {

    private static final String TAG = GeoLocation.class.getName();
    public static final String KEY_LOCATION = "location";

    private static GeoLocation mInstance;
    //缓存位置队列，用于计算错误的位置
    private static EvictingQueue<Location> mCacheQueue = new EvictingQueue<>(2);

    private GeoLocationListener mLocationListener;
    private int mFrequency = 60; // 获取位置间隔时间，单位秒
    private int mDistance = 1; // 获取位置间隔距离，单位米
    private int mDropDistance = 0; // 丢弃的距离，单位米，最小值为50,0 代表不丢弃

    private Context mContext;
    private LocationManager mLocationManager;

    private GeoLocation(Context mContext,int mFrequency,int mDistance) {
        this.mContext = mContext;
        this.mFrequency = mFrequency;
        this.mDistance = mDistance;
        this.requestPermission();
        this.initLocationManager();
    }

    public static synchronized GeoLocation getInstance(Context mContext,int frequncy,int distance) {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new GeoLocation(mContext,frequncy,distance);
            return mInstance;
        }
    }
    public static synchronized GeoLocation getInstance(Context mContext) {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new GeoLocation(mContext,60,10);
            return mInstance;
        }
    }

    public void setDropDistance(int mDropDistance) {
        if(mDropDistance < 50) mDropDistance = 50;
        this.mDropDistance = mDropDistance;
    }

    /**
     * 设置监听位置变化回调
     *
     * @param listener 位置变化回调
     */
    public void setListener(GeoLocationListener listener) {
        this.mLocationListener = listener;
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext instanceof Activity) {
                ((Activity) mContext).requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 400);
            }
        }
    }

    /**
     * 获取最新的位置
     *
     * @return 位置对象
     */
    @SuppressLint("MissingPermission")
    public Location getLastKnownLocation() {
        Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        loc = loc == null ? mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : loc;
        loc = loc == null ? mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) : loc;
        loc = loc == null ? getFromSp(mContext.getSharedPreferences(GeoLocation.KEY_LOCATION, MODE_PRIVATE)) : loc;
        return loc;
    }

    @SuppressLint("MissingPermission")
    private void initLocationManager() {

        try {
            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            if (mLocationManager != null) {
                List<String> providers = mLocationManager.getAllProviders();
                Log.e(TAG, "All providers：" + providers.toString());

                LocationListener listener = getLocationListener();

                //GPS
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mFrequency * 1000, mDistance, listener);
                //网络
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mFrequency * 1000, mDistance, listener);
                //第三方应用
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, mFrequency * 1000, mDistance, listener);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新位置写入SP
     *
     * @param rs 位置
     * @param sp 共享存储
     */
    private static void writeToSp(Location rs, SharedPreferences sp) {
        if (rs != null) {
            try {
                SharedPreferences.Editor editor = sp.edit();
                String str = parseLocation(rs);//JSON.toJSONString(rs);
                editor.putString(GeoLocation.KEY_LOCATION, str);
                editor.apply();
                Log.i(TAG, "SP write location ：" + str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将位置数据定义为文本消息
     *
     * @param location
     * @return
     */
    public static String parseLocation(Location location) {
        return location == null ? null : String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                location.getProvider(),
                location.getTime(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getSpeed(),
                location.getBearing(),
                location.getAccuracy()
        );
    }

    /**
     * 将文本转为位置
     *
     * @param location
     * @return
     */
    public static Location readLocation(String location) {
        String[] arr = location.split(",");
        Location loc = new Location(arr[0]);
        loc.setTime(Long.parseLong(arr[1]));
        loc.setLatitude(Double.parseDouble(arr[2]));
        loc.setLongitude(Double.parseDouble(arr[3]));
        loc.setAltitude(Double.parseDouble(arr[4]));
        loc.setSpeed(Float.parseFloat(arr[5]));
        loc.setBearing(Float.parseFloat(arr[6]));
        loc.setAccuracy(Float.parseFloat(arr[7]));
        return loc;
    }

    /**
     * 从SP中获取最后位置
     *
     * @param sp
     * @return
     */
    private static Location getFromSp(SharedPreferences sp) {
        Location rs = null;
        String str = sp.getString(GeoLocation.KEY_LOCATION, null);
        if (str != null) {
            rs = readLocation(str);
        }
        Log.i(TAG, "SP get location：" + str);
        return rs;
    }

    @Override
    public EvictingQueue<Location> getLastQueue() {
        return mCacheQueue;
    }

    private LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO: drop wrong location
                Location loc = location;
                if (mDropDistance > 0 && mCacheQueue.size() > 0) {
                    Location last = mCacheQueue.pop();
                    if (location.getTime() - last.getTime() < mFrequency * 2) { // 如果位置之间的时间小于间隔时间的2倍
                        float distance = location.distanceTo(last);
                        if(distance > mDropDistance) {
                            // drop
                            loc = null;
                        }
                    }
                }
                if (loc != null) {
                    mCacheQueue.add(loc);
                    mLocationListener.onLocationChanged(location);
                    writeToSp(location, mContext.getSharedPreferences(GeoLocation.KEY_LOCATION, MODE_PRIVATE));
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "onStatusChanged：" + s + "," + i);
                mLocationListener.onStatusChanged(s, i, bundle);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "onProviderEnabled：" + s);
                mLocationListener.onProviderEnabled(s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "onProviderDisabled：" + s);
                mLocationListener.onProviderDisabled(s);
            }
        };
    }


}
