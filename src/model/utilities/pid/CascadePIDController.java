package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.Steppable;

import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> This is a cascade control: two PIDs so that the Mv of the first is the setpoint of the second.
 * Basically like the human centipede but less functional and more hideous.
 * <p/> Notice that, at least by default, the first pid controller can go negative and doesn't stop windup.
 * <p/>  This is a bit of mess: the controller interface plays very poorly with this design
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-16
 * @see
 */
public class CascadePIDController implements Controller{
    private float secondTarget;

    /**
     * The master PID
     */
    private PIDController pid1;

    /**
     * The slave PID
     */
    private PIDController pid2;

    public CascadePIDController(float proportional1, float integrative1, float derivative1,
                                float proportional2, float integrative2, float derivative2,
                                MersenneTwisterFast random) {
        pid1 = new PIDController(proportional1,integrative1,derivative1,random);
    //    pid1.setCanGoNegative(true); pid1.setWindupStop(false);
        pid1.setCanGoNegative(false); pid1.setWindupStop(true);
        pid2 = new PIDController(proportional2,integrative2,derivative2,pid1.getSpeed(),random);


    }



    /**
     * This is the MV of the first PID and the set point of the second
     * @return y^* of the second PID
     */
    public float getSecondTarget() {
        return secondTarget;
    }

    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input    the controller input must have 2 inputs and 1 target
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, @Nullable MacroII simState, @Nullable Steppable user,
                       ActionOrder phase){

        this.adjust(input.getTarget(0),input.getInput(0),input.getInput(1),isActive,simState,user, phase);

    }

    /**
     /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param firstTarget the stock target
     * @param firstInput the stock input
     * @param secondInput the flow input
     * @param isActive true if the pid is not turned off
     * @param state   the simstate link to schedule the user
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)     * @param firstTarget
     */
    public void adjust(float firstTarget,float firstInput, float secondInput, boolean isActive,
                       @Nullable MacroII state, @Nullable Steppable user,  ActionOrder phase)
    {
        //master
        pid1.adjust(firstTarget,firstInput,isActive,state,user,phase);
        //slave
        secondTarget = pid1.getCurrentMV();
        pid2.adjust(secondTarget, secondInput, isActive, null, null,null);

    }

    /**
     * Get the current u_t
     */
    public float getCurrentMV() {
        return pid2.getCurrentMV();
    }

    /**
     * Set initial offset of the second PID
     * @param initialPrice the offset
     */
    @Override
    public void setOffset(float initialPrice) {
        pid2.setOffset(initialPrice);
    }


    /**
     * Get the "zero" of the second controller
     *
     * @return the "zero" of the second controller
     */
    @Override
    public float getOffset() {
       return pid2.getOffset();
    }

    /**
     * Change the gains of the first PID
     */
    public void setGainsMasterPID(float proportionalGain, float integralGain, float derivativeGain) {
        pid1.setGains(proportionalGain, integralGain, derivativeGain);
    }

    /**
     * change the speed of the cascade controller
     * @param speed the new speed
     */
    public void setSpeed(int speed) {
        pid1.setSpeed(speed);
        pid2.setSpeed(speed); //this is actually useless, but I think it helps when I debug
    }

    /**
     * Get the sampling speed of the controller (how often it updates)
     *
     * @return the sampling speed
     */
    @Override
    public int getSpeed() {
        return pid1.getSpeed();

    }

    /**
     * Change the gains of the second PID
     */
    public void setGainsSlavePID(float proportionalGain, float integralGain, float derivativeGain) {
        pid2.setGains(proportionalGain, integralGain, derivativeGain);
    }

    /**
     * Get the proportional gain of the first PID
     * @return the proportional gain of the first PID
     */
    public float getMasterProportionalGain() {
        return pid1.getProportionalGain();
    }

    /**
     * Get the integral gain of the first PID
     * @return the integral gain of the first PID
     */
    public float getMasterIntegralGain() {
        return pid1.getIntegralGain();
    }

    /**
     * Get the derivative gain of the first PID
     * @return the derivative gain of the first PID
     */
    public float getMasterDerivativeGain() {
        return pid1.getDerivativeGain();
    }

    /**
     * Get the proportional gain of the second PID
     * @return the proportional gain of the second PID
     */
    public float getSlaveProportionalGain() {
        return pid2.getProportionalGain();
    }

    /**
     * Get the integral gain of the second PID
     * @return the integral gain of the second PID
     */
    public float getSlaveIntegralGain() {
        return pid2.getIntegralGain();
    }

    /**
     * Get the derivative gain of the second PID
     * @return the derivative gain of the second PID
     */
    public float getSlaveDerivativeGain() {
        return pid2.getDerivativeGain();
    }
}
