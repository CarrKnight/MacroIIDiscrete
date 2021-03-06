/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
    private final ControllerInput.Position position;

    /**
     * Creates an EMA filter for the first input for the controller toDecorate
     * @param toDecorate the controller whose first input is going to be filtered
     * @param weight the exponential weight
     */
    public ExponentialFilterInputDecorator(Controller toDecorate, float weight) {
        this(toDecorate, weight, ControllerInput.Position.FLOW);
    }

    public ExponentialFilterInputDecorator(Controller toDecorate, float weight, ControllerInput.Position position) {
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

        if(position.equals(ControllerInput.Position.FLOW)) {
            filter.addObservation(input.getFlowInput());
            input = new ControllerInput(input.getFlowTarget(),input.getStockTarget(),filter.getSmoothedObservation(),input.getStockInput());
        }
        else{
            assert position.equals(ControllerInput.Position.STOCK);
            filter.addObservation(input.getStockInput());
            input = new ControllerInput(input.getFlowTarget(),input.getStockTarget(),input.getStockTarget(),filter.getSmoothedObservation());

        }
        toDecorate.adjust(input,isActive,simState,user,phase);

    }



    public ExponentialFilterInputDecorator(Controller toDecorate) {
        this(toDecorate,.35f);
    }


}
