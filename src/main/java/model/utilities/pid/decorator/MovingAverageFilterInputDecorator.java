/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.MovingAverage;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> Filters the input by return a moving average of it
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-15
 * @see
 */
public class MovingAverageFilterInputDecorator extends ControllerDecorator {


    private MovingAverage<Float> filter;

    private final ControllerInput.Position position;

    public MovingAverageFilterInputDecorator(Controller toDecorate, int maSize) {
        this(toDecorate, maSize, ControllerInput.Position.FLOW);
    }

    public MovingAverageFilterInputDecorator(Controller toDecorate, int maSize, ControllerInput.Position position) {
        super(toDecorate);
        filter = new MovingAverage<>(maSize);
        this.position = position;
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
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user, ActionOrder phase) {

        if(position.equals(ControllerInput.Position.FLOW)) {
            filter.addObservation(input.getFlowInput());
            input = new ControllerInput(input.getFlowTarget(),input.getStockTarget(),filter.getSmoothedObservation(),input.getStockInput());
        }
        else{
            assert position.equals(ControllerInput.Position.STOCK);
            filter.addObservation(input.getStockInput());
            input = new ControllerInput(input.getFlowTarget(),input.getStockTarget(),input.getStockTarget(),filter.getSmoothedObservation());

        }
        toDecorate.adjust(input,isActive,simState,user, phase);


    }

    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        return toDecorate.getCurrentMV();
    }



}
