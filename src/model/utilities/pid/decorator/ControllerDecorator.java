package model.utilities.pid.decorator;

import model.utilities.NonDrawable;
import model.utilities.pid.Controller;

/**
 * <h4>Description</h4>
 * <p/>
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
public abstract class ControllerDecorator implements Controller
{


    protected Controller toDecorate;


    public ControllerDecorator(Controller toDecorate) {
        this.toDecorate = toDecorate;
    }





    public Controller getToDecorate() {
        return toDecorate;
    }

    @Override
    public void setOffset(float initialPrice) {
        toDecorate.setOffset(initialPrice);
    }


    /**
     * Get the sampling speed of the controller (how often it updates)
     * @return the sampling speed
     */
    @Override
    public int getSpeed() {
        return toDecorate.getSpeed();
    }

    /**
     * Set the sampling speed of the controller (how often it updates)
     * @param samplingSpeed the sampling speed
     */
    @Override
    public void setSpeed(int samplingSpeed) {
        toDecorate.setSpeed(samplingSpeed);
    }

    /**
     * Get the "zero" of the controller
     * @return  the "zero" of the controller
     */
    @Override
    public float getOffset() {
        return toDecorate.getOffset();
    }
}
