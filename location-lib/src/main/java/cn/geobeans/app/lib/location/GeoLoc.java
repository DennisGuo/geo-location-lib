package cn.geobeans.app.lib.location;

import android.location.Location;
import android.os.Handler;

public interface GeoLoc {

    // 监听变化
    void setListener(GeoLocationListener listener);
    // 设置丢弃处理的距离
    void setDropDistance(int distance);
    // 设置不做丢弃的2点之间的时间间隔
    void setDropTime(int time);
    // 获取最后一次位置
    Location getLastKnownLocation();
    // 获取上2次的位置
    EvictingQueue<Location> getLastQueue();
}
