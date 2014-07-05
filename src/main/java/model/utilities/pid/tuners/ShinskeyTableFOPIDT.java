/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

/**
 * <h4>Description</h4>
 * <p>  Grabbed from the handbook of tuning, this is a PI tuning for integrating first order plus dead time
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
public class ShinskeyTableFOPIDT implements PIDTuningTable {
    @Override
    public float getProportionalParameter(float processGain, float timeConstant, float intercept, int delay) {
        return 0.952f/(processGain*(timeConstant+delay));
    }

    @Override
    public float getIntegralParameter(float processGain, float timeConstant, float intercept, int delay) {
        float kc = 0.952f/(processGain*(timeConstant+delay));
        float ti = 4*(timeConstant + delay);
        return kc/ti;
    }

    @Override
    public float getDerivativeParameter(float processGain, float timeConstant, float intercept, int delay) {
       return 0;
    }


    /**
     * never
     */
    @Override
    public boolean shouldISetNewBaseline(float processGain, float timeConstant, float intercept, int delay) {
        return false;
    }

    /**
     * never to be used, throws exception
     */
    @Override
    public float getBaseline(float processGain, float timeConstant, float intercept, int delay) {
        throw new IllegalStateException("This table never sets a new baseline");
    }
}
