/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> Sum up the last x observations fed in
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-20
 * @see
 */
public class MovingSum<T extends Number> implements Filter<T>
{

    /**
     * Where we keep all the observations
     */
    LinkedList<T> lastElements = new LinkedList<>();

    /**
     * The size of the queue
     */
    final private int size;

    public MovingSum(int size) {
        this.size = size;
    }

    /**
     * Add a new observation to the moving average
     * @param observation
     */
    public void addObservation(T observation){

        //add the last observation
        lastElements.addLast(observation);
        //if the queue is full, remove the first guy
        if(lastElements.size()>size)
            lastElements.removeFirst();

        assert lastElements.size() <=size;


    }


    /**
     * the smoothed observation
     *
     * @return the smoothed observation
     */
    @Override
    public float getSmoothedObservation()
    {
        if(!isReady())
            return Float.NaN;

        float total  =0;
        for(T element : lastElements)
            total += element.floatValue();

        return total;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     * @return
     */
    @Override
    public boolean isReady() {
        return !lastElements.isEmpty();
    }



    public int getSize() {
        return size;
    }

    public int numberOfObservations(){
        return lastElements.size();
    }
}
