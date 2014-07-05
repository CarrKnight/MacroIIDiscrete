/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

/**
 * <h4>Description</h4>
 * <p>  A simple table lookup of what the P, I and the D parameters ought to be given parameter processes
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-03
 * @see
 */
public interface PIDTuningTable {


    public float getProportionalParameter(float processGain, float timeConstant, float intercept, int delay);

    public float getIntegralParameter(float processGain, float timeConstant, float intercept, int delay);

    public float getDerivativeParameter(float processGain, float timeConstant, float intercept, int delay);

    /**
     *  if true, the tuning table will be asked for a new controller offset.
     */
    public boolean shouldISetNewBaseline(float processGain, float timeConstant, float intercept, int delay);

    /**
     * the new offset to put on the controller
     */
    public float getBaseline(float processGain, float timeConstant, float intercept, int delay);


}


