package cn.geobeans.app.lib.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 位置服务
 * Created by ghx on 2017/3/27.
 */
public class GeoLocationService extends Service {

    private static final String TAG = GeoLocationService.class.getName();

    static final String KEY_TIME = "time";
    static final String KEY_DISTANCE = "distance";

    //位置变化回调
    protected static Set<Handler> mChangeHandlers = new LinkedHashSet<>();

    private Context mContext;
    private static int mFrequency = 60; //获取位置间隔时间，单位秒
    private static int mDistance = 1; //获取位置间隔距离，单位米
    private LocationManager mLocationManager;
    private LocalBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();

        initLocationManager(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获取最新的位置
     *
     * @return 位置对象
     */
    public Location getLastKnown() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastAlert();
        } else {
            Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            loc = loc == null ? mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : loc;
            loc = loc == null ? mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) : loc;
            loc = loc == null ? getFromSp(mContext.getSharedPreferences(GeoLocation.KEY_LOCATION, MODE_PRIVATE)) : loc;

            return loc;
        }
        return null;
    }

    private void initLocationManager(Intent intent) {

        mFrequency = intent.getIntExtra(KEY_TIME,mFrequency);
        mDistance = intent.getIntExtra(KEY_DISTANCE,mDistance);

        Log.i(TAG,String.format("Frequency: time=%s, distance=%s ",mFrequency,mDistance));
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            List<String> providers = mLocationManager.getAllProviders();
            Log.e(TAG, "可用位置提供者：" + JSON.toJSONString(providers));


            LocationListener listener = getLocationListener();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               toastAlert();
            } else {
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

    private void toastAlert() {
        Log.e(TAG, "没有开启定位方式");
        Toast.makeText(mContext, "请在设置开启定位并为应用开启权限", Toast.LENGTH_LONG).show();
    }

    /**
     * 新位置写入SP
     *
     * @param rs
     * @param sp
     */
    private static void writeToSp(Location rs, SharedPreferences sp) {
        if(rs != null) {
            try {
                SharedPreferences.Editor editor = sp.edit();
                String str = getLocationJson(rs);//JSON.toJSONString(rs);
                editor.putString(GeoLocation.KEY_LOCATION, str);
                editor.apply();
                Log.i(TAG, "SP写入最后新位置：" + str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getLocationJson(Location rs) {
        Map<String,Object> map = new HashMap<>();

        map.put("accuracy",rs.getAccuracy());
        map.put("bearing",rs.getBearing());
        map.put("latitude",rs.getLatitude());
        map.put("longitude",rs.getLongitude());
        map.put("altitude",rs.getAltitude());
        map.put("provider",rs.getProvider());
        map.put("speed",rs.getSpeed());
        map.put("time",rs.getTime());

        return JSON.toJSONString(map);
    }

    /**
     * 从SP中获取最后位置
     *
     * @param sp
     * @return
     */
    private static Location getFromSp(SharedPreferences sp) {
        Location rs = null;
        String json = sp.getString(GeoLocation.KEY_LOCATION, null);
        if (json != null) {
            rs = JSON.parseObject(json, Location.class);
            Log.i(TAG, "SP读取最后一次位置：" + rs.toString());
        }
        return rs;
    }


    private LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "[Handler:"+mChangeHandlers.size()+"][Provider:"+location.getProvider()+"]监听位置变化：" + location.toString());

                if (mChangeHandlers != null && mChangeHandlers.size() > 0) {
                    //TODO：回调

                    for (Handler h : mChangeHandlers) {
                        try {
                            Message msg = h.obtainMessage();
                            Bundle data = new Bundle();
                            data.putParcelable(GeoLocation.KEY_LOCATION, location);
                            msg.setData(data);
                            h.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                writeToSp(location, mContext.getSharedPreferences(GeoLocation.KEY_LOCATION, MODE_PRIVATE));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "监听状态变化：" + s);
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "监听开启位置提供者：" + s);
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "监听禁用位置提供者：" + s);
            }
        };
    }


    /******内部类**********/

    class LocalBinder extends Binder {
        GeoLocationService getService() {
            return GeoLocationService.this;
        }
    }


}
