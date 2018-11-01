package cn.geobeans.app.lib.location;

import java.util.ArrayList;

public class EvictingQueue<T> {
    private int max;
    private ArrayList<T> array;

    public EvictingQueue(int max) {
        this.max = max;
        array = new ArrayList<>();
    }

    public void add(T elem) {
        if (array.size() == max)
            array.remove(0);

        array.add(elem);
    }

    public T pop() {
        return array.get(array.size()-1);
    }

    public T get(int n) { return array.get(size()-n-1); }

    public int size() {
        return array.size();
    }

    public void print() {
        System.out.println("#######################");
        for (int i=0;i< array.size();i++){
            System.out.println(i+" : "+array.get(i));
        }
    }
}
