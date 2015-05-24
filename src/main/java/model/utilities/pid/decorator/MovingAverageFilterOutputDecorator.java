package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import model.utilities.filters.MovingAverage;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import sim.engine.Steppable;

/**
 * Smooths through moving average the controller policy
 * Created by carrknight on 5/13/15.
 */
public class MovingAverageFilterOutputDecorator extends ControllerDecorator {

    private final MovingAverage<Float> filter;


    public MovingAverageFilterOutputDecorator(Controller toDecorate, int averageSize) {
        super(toDecorate);
        filter = new MovingAverage<>(averageSize);
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

        if(filter.isReady()) {
            return filter.getSmoothedObservation();
        }
        else
            return super.getCurrentMV();

    }




}
