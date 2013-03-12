package agents.firm.production.control.maximizer.algorithms.hillClimbers;

import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;

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
public class AlwaysMovingHillClimber implements WorkerMaximizationAlgorithm
{

    /**
     * the minimum number of workers needed
     */
    private int minimumWorkersNeeded;

    /**
     * the maximum number of workers
     */
    private int maximumWorkersPossible;


    public AlwaysMovingHillClimber(int minimumWorkersNeeded, int maximumWorkersPossible) {
        this.minimumWorkersNeeded = minimumWorkersNeeded;
        this.maximumWorkersPossible = maximumWorkersPossible;
    }

    /**
     * Asks the subclass what the next worker target will be!
     *
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param newRevenues
     *@param newCosts
     * @param oldRevenues
     * @param oldCosts
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {

        int newVelocity = velocity(currentWorkerTarget,newProfits,oldWorkerTarget,oldProfits);

        return  Math.min(Math.max(
                currentWorkerTarget + newVelocity,
                minimumWorkersNeeded),
                maximumWorkersPossible);


    }


    protected int velocity(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits)
    {
        if(currentWorkerTarget == oldWorkerTarget)
            if(newProfits==oldProfits){
                return 0;
            }
            else
                return  newProfits > oldProfits ? 1 : -1;


        int  increasedProfits = (int) Math.signum(newProfits - oldProfits);
        //if the profits are exactly the same? default to 1
        increasedProfits = increasedProfits == 0 ? 1 : increasedProfits;
        //otherwise continue with the equation
        int increasedWorkers = Integer.signum(currentWorkerTarget - oldWorkerTarget);

        assert  increasedProfits * increasedWorkers == -1 ^ increasedProfits * increasedWorkers == 1 : "ip: " + increasedProfits + ", iw: " + increasedWorkers;

        return increasedProfits * increasedWorkers;

    }

    /**
     * The maximizer tells you to start over, probably because of change in machinery
     *
     * @param maximizer the maximizer resetting you
     * @param p         the plant it is controlling
     */
    @Override
    public void reset(WorkforceMaximizer maximizer, Plant p) {
        //nothing in particular happens here

    }

    @Override
    public void turnOff() {
        //nothing in particular happens here!
    }
}
