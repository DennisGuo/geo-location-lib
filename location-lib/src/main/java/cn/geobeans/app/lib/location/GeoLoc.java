package cn.geobeans.app.lib.location;

import android.location.Location;
import android.os.Handler;

public interface GeoLoc {

    // 监听变化
    void setListener(GeoLocationListener listener);
    // 设置触发频率
    void setFrequency(int frequency);
    // 设置触发距离
    void setDistance(int distance);
    // 获取最后一次位置
    Location getLastKnownLocation();
}
