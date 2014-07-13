/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import model.utilities.stats.processes.DynamicProcess;

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
    /**
     * add a new observation
     * @param output the y of the sistem
     * @param input the input of the system
     * @param intercepts any other variable that affects y but it is not controlled like u is.
     */
    public void addObservation(double output, double input, double... intercepts);


    /**
     * get notified that an observation is skipped. This is usually to avoid having fake/wrong y_t - y_{t-1} from not considering the skipped observation
     */
    public void skipObservation(double skippedOutput, double skippedInput, double... skippedIntercepts);

    public double predictNextOutput(double input, double... intercepts);

    public double getTimeConstant();

    public double getGain();


    public double getIntercept();


    public int getDelay();

    public DynamicProcess generateDynamicProcessImpliedByRegression();
}
