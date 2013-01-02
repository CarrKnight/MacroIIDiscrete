package model.utilities.pid;

import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> Any object that can be used to adjust policies to hit targets given outputs.
 * <p/> Each inheritor MUST have a constructor that only takes a mersenne twister fast
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
public interface Controller {


    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     *
     */
    public void adjust(ControllerInput input, boolean isActive, @Nullable SimState simState, @Nullable Steppable user);

    /**
     * Get the current u_t
     */
    public float getCurrentMV();


    /**
     * Set the "zero" of the controller
     * @param initialPrice the "zero" of the controller
     */
    public void setOffset(float initialPrice);

    /**
     * Get the "zero" of the controller
     * @return  the "zero" of the controller
     */
    public float getOffset();

    /**
     * Set the sampling speed of the controller (how often it updates)
     * @param samplingSpeed the sampling speed
     */
    public void setSpeed(float samplingSpeed);

    /**
     * Get the sampling speed of the controller (how often it updates)
     * @return the sampling speed
     */
    public float getSpeed();



}
