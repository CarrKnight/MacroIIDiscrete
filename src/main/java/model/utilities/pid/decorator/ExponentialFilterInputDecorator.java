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
 * <p/> This decorator filters the controller by exponential averaging one input. By default it filters input in position 0, but any position can be chosen.
 * <p/> @see ControllerInput
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
public class ExponentialFilterInputDecorator extends ControllerDecorator {

    /**
     * The filter we are going to use to take EMA
     */
    private final ExponentialFilter<Float> filter;

    /**
     * the position of the input to decorate
     */
    private final Integer position;

    /**
     * Creates an EMA filter for the first input for the controller toDecorate
     * @param toDecorate the controller whose first input is going to be filtered
     * @param weight the exponential weight
     */
    public ExponentialFilterInputDecorator(Controller toDecorate, float weight) {
        this(toDecorate, weight,0);
    }

    public ExponentialFilterInputDecorator(Controller toDecorate, float weight, Integer position) {
        super(toDecorate);
        filter = new ExponentialFilter<>(weight);
        this.position = position;
    }

    public float getWeight() {
        return filter.getWeight();
    }

    public void setWeight(float weight) {
        filter.setWeight(weight);
    }

    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *

     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input,  boolean isActive, MacroII simState, Steppable user,ActionOrder phase) {

        filter.addObservation(input.getInput(position));
        input.setInput(position,filter.getSmoothedObservation());
        toDecorate.adjust(input,isActive,simState,user,phase);

    }

    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        return toDecorate.getCurrentMV();
    }

    public ExponentialFilterInputDecorator(Controller toDecorate) {
        super(toDecorate);
        filter = new ExponentialFilter<>(.35f);
        position = 0;
    }


}
