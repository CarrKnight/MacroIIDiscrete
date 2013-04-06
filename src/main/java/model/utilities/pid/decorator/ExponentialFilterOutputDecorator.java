/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.NonDrawable;
import model.utilities.filters.ExponentialFilter;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This decorator filters the output control variable by taking its exponential average with the old control variable
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-14
 * @see
 */
@NonDrawable
public class ExponentialFilterOutputDecorator extends ControllerDecorator {

    private final ExponentialFilter<Float> filter;

    public ExponentialFilterOutputDecorator(Controller toDecorate, float weight) {
        super(toDecorate);
        filter = new ExponentialFilter<>(weight);
    }




    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input the controller input object
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input,  boolean isActive, MacroII simState, Steppable user,ActionOrder phase) {


        toDecorate.adjust(input,isActive,simState,user,phase);


        filter.addObservation(toDecorate.getCurrentMV());


    }


    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {

        return filter.getSmoothedObservation();

    }



}
