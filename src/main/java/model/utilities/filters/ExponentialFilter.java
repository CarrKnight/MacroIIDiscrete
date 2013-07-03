/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

/**
 * <h4>Description</h4>
 * <p/> A filter that takes exponential average of its observations
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-03
 * @see
 */
public class ExponentialFilter<T extends Number> implements Filter<T>
{
    float smoothedValue = Float.NaN;

    float weight = 0.5f;


    public ExponentialFilter() {
    }

    public ExponentialFilter(float weight) {
        this.weight = weight;
    }

    /**
     * adds a new observation to the filter!
     *
     * @param observation a new observation!
     */
    @Override
    public void addObservation(T observation) {

        if(Float.isNaN(smoothedValue))
            smoothedValue = observation.floatValue();
        else
            smoothedValue = weight * observation.floatValue() + (1f-weight) * smoothedValue;


    }

    /**
     * the smoothed observation
     *
     * @return the smoothed observation
     */
    @Override
    public float getSmoothedObservation() {
        return smoothedValue;
    }

    /**
     * Get the exponent of the EMA
     */
    public float getWeight() {
        return weight;
    }

    /**
     * Set the exponent of the EMA
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     * @return
     */
    public boolean isReady(){
        return !Float.isNaN(smoothedValue);

    }
}
