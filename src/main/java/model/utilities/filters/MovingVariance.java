/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p>  Here I am using the algorithm from: http://stackoverflow.com/a/14638138/975904
 * <p>  Basically a "fast" way to keep the variance measured
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-06
 * @see
 */
public class MovingVariance<T extends Number> implements Filter<T> {

    private float average = Float.NaN;

    private float variance = Float.NaN;


    private final Deque<T> observations = new LinkedList<>();


    private final int size;


    public MovingVariance(int size) {
        this.size = size;
    }

    /**
     * adds a new observation to the filter!
     *
     * @param newObservation a new observation!
     */
    @Override
    public void addObservation(T newObservation) {

        assert observations.size() < size || (observations.size() == size && Float.isFinite(variance)) : variance +"----" + newObservation + " ---- " + (observations.size() < size);

        observations.addLast(newObservation);
        if(observations.size() == size && Float.isNaN(average))
        {
            average = computeBatchAverage();
            variance = computeInitialVarianceThroughCompensatedSummation();
        }
        else if(observations.size() > size)
        {
            //need to correct!
            float oldestValue = observations.pop().floatValue();
            final float newValue = newObservation.floatValue();
            float oldAverage = average;
            average = average + (newValue -oldestValue)/size;
            variance = variance +  (newValue-average + oldestValue-oldAverage)*(newValue - oldestValue)/(size);  //might have to add a Max(0,variance) if there are numerical issues!
            assert Float.isFinite(variance) : average;
        }
    }

    /**
     * the variance. If variance is below .0001 it returns 0.
     *
     * @return the smoothed observation
     */
    @Override
    public float getSmoothedObservation() {

        if(variance<.0001f)
            return 0;
        return variance;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     * @return
     */
    @Override
    public boolean isReady() {
        return Float.isFinite(variance);
    }


    private float computeBatchAverage(){
        assert observations.size() == size;
        float sum = 0;
        for(T n : observations )
            sum+= n.floatValue();

        return sum/size;
    }

    //from the wikipedia.
    private float computeInitialVarianceThroughCompensatedSummation(){
        assert observations.size() == size;
        assert Float.isFinite(average);

        float squaredSum=0;
        float compensatingSum=0;
        for(T observation : observations )
        {
            squaredSum +=  Math.pow(observation.floatValue()-average,2);
            compensatingSum +=  observation.floatValue()-average;
        }

        return (float) ((squaredSum-Math.pow(compensatingSum,2)/size)/size);

    }

    public float getAverage() {
        return average;
    }


    /**
     * get the absolute value of the ratio between standard deviation and mean. if average = 0, returns just the standard deviation. //todo this is weird
     * @return
     */
    public float getRelativeStandardDeviation(){

        if(getAverage() == 0)
            return (float) Math.sqrt(getSmoothedObservation());
        return (float) Math.abs(Math.sqrt(getSmoothedObservation())/getAverage());
    }
}
