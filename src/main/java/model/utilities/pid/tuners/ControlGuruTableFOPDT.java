/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

/**
 * <h4>Description</h4>
 * <p>  Found these on controlguru.com. I have no idea where they take these from. PI parameters for FOPTD
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
public class ControlGuruTableFOPDT implements PIDTuningTable{

    @Override
    public float getProportionalParameter(float processGain, float timeConstant, int delay) {
        return (timeConstant / (timeConstant + delay)) / processGain;
    }

    @Override
    public float getIntegralParameter(float processGain, float timeConstant, int delay) {

        return (1 / (timeConstant + delay)) / processGain;


    }

    @Override
    public float getDerivativeParameter(float processGain, float timeConstant, int delay) {
        return 0;
    }


}
