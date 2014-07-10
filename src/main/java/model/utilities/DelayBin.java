/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities;

import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.Deque;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p> A simple queue. Every new item gets placed at the back of the queue. When it's full, you can pop it getting back the first element of the queue. If it's not full, when you pop it you actually get back
 * the default value and you don't modify the queue
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-25
 * @see
 */
public class DelayBin<N extends Number> {


    private final int size;

    private final Deque<N> bin;

    private final N defaultValue;


    public DelayBin(int size, N defaultValue) {
        Preconditions.checkState(size>=0);
        this.size = size;
        this.defaultValue = defaultValue;
        bin = new LinkedList<>();
    }


    public DelayBin(DelayBin<N> toCopy){
        this.size = toCopy.size;
        this.defaultValue = toCopy.defaultValue;
        this.bin = new LinkedList<>(toCopy.bin);


    }


    /**
     * add element last and pop first element or return default value if not enough elements in the bin
     * @param element the element to add
     * @return the oldest element or default if there aren't enough
     */
    public N addAndRetrieve(N element){

        assert bin.size()<=size;
        bin.addLast(element);
        if(bin.size()>size) {
            assert bin.size() == size+1;
            return bin.pop();
        }
        else
            return defaultValue;

    }

    /**
     * if the delay queue is full, peek the topmost element, otherwise returns default
     */
    public N peek(){
        if(bin.size() == size)
            return bin.peekFirst();
        else
            return defaultValue;
    }

    /**
     * Retrieves all that is currently in queue.
     */
    public N[] peekAll(Class<? extends N> arrayClass){

        @SuppressWarnings("unchecked")
        N[]toReturn = (N[])Array.newInstance(arrayClass,bin.size());

        int i=0;
        for(N number: bin)
        {
            toReturn[i] = number;
            i++;
        }


        return toReturn;

    }


    public int getDelay(){
        return size;
    }




}
