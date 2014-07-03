/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

/**
 * <h4>Description</h4>
 * <p> An interface for the regressions dealing with Single Input Single Output control models. I assume all the implementations are iterative or at the very least can deal with data being fed iteratively.
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-02
 * @see
 */
public interface SISORegression {
    public void addObservation(float output, float input);

    public float predictNextOutput(float input);

    public float getTimeConstant();

    public float getGain();

    public int getDelay();
}
