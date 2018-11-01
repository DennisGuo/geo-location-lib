package cn.geobeans.app.lib.locatoin;

import android.location.Location;

import org.junit.Test;

import java.util.Date;

public class LocationTest extends TestBase {

    @Test
    public void dropLocation(){

        int mDropDistance = 100;

        Location last = new Location("GPS");
        last.setTime(new Date().getTime() - 10 * 1000);
        last.setLatitude(39.2345);
        last.setLongitude(116.3456);

        Location crt = new Location("GPS");
        crt.setTime(new Date().getTime());
        crt.setLatitude(36.2346);
        crt.setLongitude(116.3447);

        if (crt.getTime() - last.getTime() < 5 ) { // 如果位置之间的时间小于间隔时间的2倍
            float distance = crt.distanceTo(last);
            System.out.println("distance = "+distance);
            if(distance > mDropDistance) {
                // drop
                System.out.println("drop.");
            }

        }
        System.out.println("not drop.");
    }
}
