/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

/**
 * <h4>Description</h4>
 * <p/> A simple interface smoothing and filtering observations
 * <p/> It accepts any number but the computations are all done through float value call
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
public interface Filter<T extends Number> {

    /**
     * adds a new observation to the filter!
     * @param observation a new observation!
     */
    public void addObservation(T observation);

    /**
     * the smoothed observation
     * @return the smoothed observation
     */
    public float getSmoothedObservation();
}
