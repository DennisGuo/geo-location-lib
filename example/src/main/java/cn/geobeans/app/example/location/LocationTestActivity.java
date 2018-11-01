package cn.geobeans.app.example.location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

public class LocationTestActivity extends Activity implements View.OnClickListener {
    EditText mTimeView;
    EditText mLastView;
    EditText mNewView;
    TextView mResultView;
    Button mBtnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_test);
        mBtnView = (Button) findViewById(R.id.btn_calculate);
        mResultView = (TextView) findViewById(R.id.txt_result);
        mNewView = (EditText) findViewById(R.id.ipt_now);
        mLastView = (EditText) findViewById(R.id.ipt_last);
        mTimeView = (EditText) findViewById(R.id.ipt_time);

        mBtnView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_calculate:
                doCalculate();
                return;
            default:
                break;
        }

    }

    private void doCalculate() {
        String last = mLastView.getText().toString();
        String now = mNewView.getText().toString();
        int time = Integer.parseInt(mTimeView.getText().toString());
        Location lastLoc = getLocation(last);
        lastLoc.setTime(new Date().getTime() - time * 1000);
        Location newLoc = getLocation(now);
        boolean drop = false;
        StringBuilder sb = new StringBuilder("计算结果：");
        if (newLoc.getTime() - lastLoc.getTime() < 5 * 1000 ) { // 如果位置之间的时间小于间隔时间的2倍
            float distance = newLoc.distanceTo(lastLoc);
            sb.append("\ndrop distance = 100");
            sb.append("\ndistance = "+distance);
            if(distance > 100 ) {
                // drop
                sb.append("\ndrop.");
                drop = true;
            }
        }
        if(!drop) {
            sb.append("\nnot drop.");
        }
        mResultView.setText(sb.toString());
    }

    private Location getLocation(String last) {
        Location loc = new Location("");
        double lat = Double.parseDouble(last.split(",")[0]);
        double lng = Double.parseDouble(last.split(",")[1]);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setTime(new Date().getTime());
        return loc;
    }
}
