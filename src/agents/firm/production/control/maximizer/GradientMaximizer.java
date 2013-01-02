package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;

/**
 * <h4>Description</h4>
 * <p/> This is like hill-climber but the adjust size is half the derivative (ceiled)
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-24
 * @see
 */
public class GradientMaximizer extends HillClimberMaximizer {
    /**
     * Create the hillclimber maximizer
     *
     * @param hr      the human resources object
     * @param control the controller it is attached to
     */
    public GradientMaximizer(HumanResources hr, PlantControl control) {
        super(hr, control);
    }


    /**
     * This should return the absolute value of the difference between futureTarget - currentTarget.
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the return can't be negative or an exception is thrown!!!
     */
    @Override
    protected int stepSize(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {

        //if you are at the same point, don't bother because the derivative would be infinite
        if(currentWorkerTarget == oldWorkerTarget || newProfits == oldProfits)
            return 1;
        else
        {
            float derivative = Math.abs((newProfits - oldProfits)/((float) (currentWorkerTarget - oldWorkerTarget)));
            int toReturn =  (int)Math.ceil(derivative); //return the derivative ceiled!
            assert toReturn > 0 ;
            return toReturn;
        }

    }
}
