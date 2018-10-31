package cn.geobeans.app.example.location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.geobeans.app.lib.location.GeoLocation;
import cn.geobeans.app.lib.location.GeoLocationListener;

/**
 * 启动首页
 * Created by ghx on 2017/3/27.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String KEY_CACHE = "cache_log";

    private Button mBtnRefresh;
    private Button mBtnClear;
    private LinearLayout mList;

    private GeoLocation mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnRefresh = (Button) findViewById(R.id.btn_refresh);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mList = (LinearLayout) findViewById(R.id.list_log);
        // 检查权限

        mLocation = GeoLocation.getInstance(this);

        initEvent();
    }


    private void initEvent() {
        mBtnRefresh.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        try {
            mLocation.setListener(new GeoLocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //super.onLocationChanged(location);
                    final Location loc = location;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendToList(loc);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入界面
     * @param loc   位置对象
     */
    private void appendToList(Location loc) {
        if(loc != null) {
            drawItem(loc);
        }
    }

    private void drawItem(Location loc) {
        TextView view = (TextView) getLayoutInflater().inflate(R.layout.item_log, null);
        view.setText(getTextInfo(loc));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 10);
        view.setLayoutParams(params);
        mList.addView(view);
    }

    private String getTextInfo(Location loc) {
        if(loc != null){
            return String.format("Provider:%s\nTime：%s\nLongitude：%s\nLatitude：%s\nAltitude：%s\nSpeed：%s",
                    loc.getProvider(),
                    SimpleDateFormat.getDateInstance().format(new Date(loc.getTime())),
                    loc.getLongitude(),
                    loc.getLatitude(),
                    loc.getAltitude(),
                    loc.getSpeed());
        }
        return null;
    }


    @Override
    public void onClick(View view) {
        TextView t = (TextView) view;
        switch (view.getId()){
            case R.id.btn_clear:
                mList.removeAllViews();
                break;
            case R.id.btn_refresh:
                try {
                    Location loc = mLocation.getLastKnownLocation();
                    appendToList(loc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }
}
