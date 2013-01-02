package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;

/**
 * <h4>Description</h4>
 * <p/> This is the simplest maximizer possible: if profits increased then keep direction otherwise revert it
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-06
 * @see
 */
public class AlwaysMovingHillClimber extends weeklyWorkforceMaximizer
{

    public AlwaysMovingHillClimber(HumanResources hr, PlantControl control) {
        super(hr, control);
    }

    /**
     * Asks the subclass what the next worker target will be!
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    @Override
    protected int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {

        int newVelocity = velocity(currentWorkerTarget,newProfits,oldWorkerTarget,oldProfits);

        return  Math.min(Math.max(currentWorkerTarget + newVelocity,getHr().getPlant().minimumWorkersNeeded()),getHr().getPlant().maximumWorkersPossible());


    }


    protected int velocity(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits)
    {
        if(currentWorkerTarget == oldWorkerTarget)
            return 0;

        int  increasedProfits = (int) Math.signum(newProfits - oldProfits);
        //if the profits are exactly the same? default to 1
        increasedProfits = increasedProfits == 0 ? 1 : increasedProfits;
        //otherwise continue with the equation
        int increasedWorkers = Integer.signum(currentWorkerTarget - oldWorkerTarget);

        assert  increasedProfits * increasedWorkers == -1 ^ increasedProfits * increasedWorkers == 1 : "ip: " + increasedProfits + ", iw: " + increasedWorkers;

        return increasedProfits * increasedWorkers;

    }
}
