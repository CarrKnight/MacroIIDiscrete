/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

/**
 * <h4>Description</h4>
 * <p> Use this when the best fit is just y(t) = a + b*u(t); no time constant and so on.
 * <p> So all we need to do is assume u(t) = -a/b + y(t)/b
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-04
 * @see
 */
public class POnlyNonDynamicTable implements PIDTuningTable {
    @Override
    public float getProportionalParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 0;
    }

    @Override
    public float getIntegralParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 1f/processGain;
    }

    @Override
    public float getDerivativeParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 0;
    }

    /**
     * the baseline is always the intercept
     */
    @Override
    public boolean shouldISetNewBaseline(float processGain, float timeConstant, float intercept, int delay) {
        return true;
    }

    /**
     * the baseline is always the intercept
     */
    @Override
    public float getBaseline(float processGain, float timeConstant, float intercept, int delay) {
        return -intercept/processGain;
    }
}
