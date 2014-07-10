/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

/**
 * <h4>Description</h4>
 * <p> A simple (mostly P only) table useful for IPD. Couple of modifications: 1- sets baseline 2- minimum delay is 1 when it comes to P
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-07
 * @see
 */
public class AstromHagglundIPDTable implements PIDTuningTable {
    @Override
    public float getProportionalParameter(float processGain, float timeConstant, float intercept, int delay) {
        delay = Math.max(1,Math.abs(delay));

        return  0.94f/(1f/processGain * delay);

    }

    @Override
    public float getIntegralParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 2f*delay;
    }

    @Override
    public float getDerivativeParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 0.5f*delay;
    }

    /**
     * if true, the tuning table will be asked for a new controller offset.
     *
     * @param processGain
     * @param timeConstant
     * @param intercept
     * @param delay
     */
    @Override
    public boolean shouldISetNewBaseline(float processGain, float timeConstant, float intercept, int delay) {
        return true;
    }

    /**
     * the new offset to put on the controller
     *
     * @param processGain
     * @param timeConstant
     * @param intercept
     * @param delay
     */
    @Override
    public float getBaseline(float processGain, float timeConstant, float intercept, int delay) {
        return -intercept/processGain;
    }
}
