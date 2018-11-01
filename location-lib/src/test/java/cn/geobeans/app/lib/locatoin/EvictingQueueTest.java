package cn.geobeans.app.lib.locatoin;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Queue;

import cn.geobeans.app.lib.location.EvictingQueue;


public class EvictingQueueTest extends TestBase{

    @Test
    public void calculator(){
        //System.out.println("It works.");
        EvictingQueue<String> queue = new EvictingQueue<>(2);
        queue.add("GPS");
        queue.print();
        queue.add("NETWORK");
        queue.print();
        queue.add("PASSIVE");
        queue.print();
        queue.add("BEIDOU");
        queue.print();

    }
}
