package cn.geobeans.app.lib.location;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * GeoLocation主类,提供主要的接口方法
 * Created by ghx on 2017/3/27.
 */

public class GeoLocation {

    private static final String TAG = GeoLocation.class.getName();

    public static final String KEY_LOCATION = "location";

    private GeoLocationService mService;
    private ServiceConnection mConnection;

    private static GeoLocation mInstance;
    private static boolean initialized;

    public static synchronized GeoLocation getInstance(){
        if(mInstance != null){
            return mInstance;
        }else{
            mInstance = new GeoLocation();
            return mInstance;
        }
    }

    /**
     * 初始化
     * @param context 应用上下文
     */
    public  void init(Context context){
       init(context,null,null);

    }

    /**
     * 初始化
     * @param context   应用上下文
     * @param second    监听时间周期
     * @param meter     监听距离间隔
     */
    public  void init(Context context,Integer second,Integer meter){
        Log.i(TAG,"初始化GeoLocation");
        Intent intent = new Intent(context,GeoLocationService.class);
        intent.putExtra(GeoLocationService.KEY_TIME,second);
        intent.putExtra(GeoLocationService.KEY_DISTANCE,meter);

        context.startService(intent);
        mConnection =  new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((GeoLocationService.LocalBinder)iBinder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mService = null;
            }
        };
        context.bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
        initialized = true;
    }

    /**
     * 销毁
     * @param context       应用上下文
     * @throws Exception    异常
     */
    public void destroy(Context context) throws Exception {
        Log.i(TAG,"销毁GeoLocation");
        if(initialized) {
            Intent intent = new Intent(context, GeoLocationService.class);
            context.unbindService(mConnection);
            context.stopService(intent);
            initialized = false;
        }else{
            throw getNotInitException();
        }
    }


    /**
     * 设置监听位置变化回调
     * @param changeHandler    位置变化回调
     * @throws Exception         异常
     */
    public void listen(Handler changeHandler) throws Exception {
        if(initialized) {
            GeoLocationService.mChangeHandlers.add(changeHandler);
        }else{
            throw getNotInitException();
        }
    }



    /**
     * 检查当前应用是否有权限
     * @param context 检查权限所在的activity
     */
    public static void requestPermission(Activity context){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    /**
     * 获取最后位置
     * @return 最后位置
     * @throws Exception 异常
     */
    public Location getLastLocation() throws Exception {
        if(initialized) {
           return mService.getLastKnown();
        }else{
            throw getNotInitException();
        }
    }

    private  Exception getNotInitException() {
        return new Exception("GeoLocation 未初始化");
    }
}
