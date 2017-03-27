package cn.geobeans.app.example.location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.geobeans.app.lib.location.GeoLocation;

/**
 * 启动首页
 * Created by ghx on 2017/3/27.
 */

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

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

        mLocation = GeoLocation.getInstance();
        GeoLocation.requestPermission(this);

        initEvent();
    }

    private void initEvent() {
        mBtnRefresh.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        try {
            mLocation.listen(new Handler(getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Bundle data = msg.getData();
                    Location loc = data.getParcelable(GeoLocation.KEY_LOCATION);
                    appendToList(loc);
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
            TextView view = (TextView) getLayoutInflater().inflate(R.layout.item_log, null);
            view.setText(getTextInfo(loc));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 10);
            view.setLayoutParams(params);
            mList.addView(view);
        }
    }

    private String getTextInfo(Location loc) {
        if(loc != null){
            return String.format("时间：%s\n经度：%s\n纬度：%s\n高程：%s\n速度：%s",
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
        Toast.makeText(this,"执行："+ t.getText().toString(),Toast.LENGTH_SHORT).show();
        switch (view.getId()){
            case R.id.btn_clear:
                mList.removeAllViews();
                break;
            case R.id.btn_refresh:
                try {
                    Location loc = mLocation.getLastLocation();
                    Log.i(TAG,"获取最新位置："+ JSON.toJSONString(loc));
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
