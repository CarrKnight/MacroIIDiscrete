/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This decorator filters target (y^*). This is useful if the set point is also noisy,
 * By default it filters the first target, buy any target can be smoothed
 * <p/> @see ControllerInput
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-03
 * @see
 */
public class ExponentialFilterTargetDecorator extends ControllerDecorator {



    private final ExponentialFilter<Float> exponentialFilter;

    /**
     * The position of the target to smooth
     */
    private final Integer position;

    /**
     * this decorator smooths through EMA the first target (position 0) that is fed in the decorated controller
     * @param toDecorate the controller to decorate
     * @param weight the weight of the EMA
     */
    public ExponentialFilterTargetDecorator(Controller toDecorate, float weight) {

        this(toDecorate, weight,0);



    }

    /**
     * this decorator smooths through EMA the target (at the position passed) that is fed in the decorated controller
     * @param toDecorate the controller to decorate
     * @param weight the weight of the EMA
     * @param position which target this decorator smooths (0 is the first, 1 is the second and so on)
     */
    public ExponentialFilterTargetDecorator(Controller toDecorate, float weight, int position) {
        super(toDecorate);

        exponentialFilter = new ExponentialFilter<>(weight);
        this.position = position;



    }


    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input the input object
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input,  boolean isActive, MacroII simState, Steppable user,ActionOrder phase) {
        exponentialFilter.addObservation(input.getTarget(position));
        input.setTarget(position,exponentialFilter.getSmoothedObservation());



        toDecorate.adjust(input,isActive,simState,user,phase);


    }

    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        return toDecorate.getCurrentMV();
    }




}
