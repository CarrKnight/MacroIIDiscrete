/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

/**
 * <h4>Description</h4>
 * <p/> An object to compute the moving average of whatever is put in.
 * <p/> It accepts any number but the computations are all done through float value call
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-15
 * @see
 */
public class MovingAverage<T extends Number> implements Filter<T>{

    final private MovingSum sum;

    /**
     * the constructor that creates the moving average object
     */
    public MovingAverage(int movingAverageSize) {
        sum = new MovingSum(movingAverageSize);
    }



    public float getSmoothedObservation()
    {
        if(!isReady())
        {
            assert sum.numberOfObservations() == 0;
            return Float.NaN;
        }
        assert sum.numberOfObservations()>0;
        assert sum.numberOfObservations() <= sum.getSize();
        return sum.getSmoothedObservation()/((float)sum.numberOfObservations());


    }



    public String toString() {
        return String.valueOf(getSmoothedObservation());
    }

    /**
     * Add a new observation to the moving average
     * @param observation
     */
    public void addObservation(Number observation) {
        sum.addObservation(observation);
    }

    public int getSize() {
        return sum.getSize();
    }

    /**
     * If it can predict (that is, if I call getSmoothedObservation i don't get a NaN)
     *
     * @return
     */
    @Override
    public boolean isReady() {
        return sum.isReady();
    }

    public int numberOfObservations() {
        return sum.numberOfObservations();
    }
}


