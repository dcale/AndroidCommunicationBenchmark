package ch.papers.androidcommunicationbenchmark.utils;

import java.util.ArrayList;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 02/01/16.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class FixedSizeArrayList<K> extends ArrayList<K> {
    private int maxSize;

    public FixedSizeArrayList(int size){
        this.maxSize = size;
    }

    public boolean add(K k){
        boolean r = super.add(k);
        if (this.size() > maxSize){
            this.removeRange(0, this.size() - maxSize - 1);
        }
        return r;
    }
}
