/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import com.google.common.base.Preconditions;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> While not properly a filter like the MovingAverage class, it works more or less the same way, except that it weights (weight>0) entries
 * <p/> O is the type of observations, W is the type of weights
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-19
 * @see
 */
public class WeightedMovingAverage<O extends Number,W extends Number>
{

    /**
     * Where we keep all the observations
     */
    LinkedList<PairOfNumbers<O,W>> lastElements = new LinkedList<>();

    /**
     * The size of the queue
     */
    final private int movingAverageSize;


    public WeightedMovingAverage(int movingAverageSize) {
        this.movingAverageSize = movingAverageSize;
    }

    public void addObservation(O observation, W weight)
    {
        Preconditions.checkArgument(weight.doubleValue()>=0, " can't deal with negative weights!");
        lastElements.addLast(new PairOfNumbers<O, W>(observation,weight));
        if(lastElements.size() > movingAverageSize)
            lastElements.removeFirst();

        assert lastElements.size() <= movingAverageSize;




    }

    public float getSmoothedObservation()
    {
        //if you have no observations, return nan
        if(lastElements.isEmpty())
            return Float.NaN;

        assert lastElements.size()<=movingAverageSize;

        float numerator = 0;
        float denumerator = 0;
        for(PairOfNumbers element : lastElements)
        {
            float weight = element.getWeight().floatValue();
            numerator+=element.getObservation().floatValue()* weight;
            denumerator+= weight;
        }
        if(denumerator ==0)
            return Float.NaN;

        return numerator/denumerator;
    }

    private class PairOfNumbers<O extends Number, W extends  Number>
    {

        private final O observation;

        private final W weight;

        private PairOfNumbers(O observation, W weight) {
            this.observation = observation;
            this.weight = weight;
        }

        private O getObservation() {
            return observation;
        }

        private W getWeight() {
            return weight;
        }
    }
}
