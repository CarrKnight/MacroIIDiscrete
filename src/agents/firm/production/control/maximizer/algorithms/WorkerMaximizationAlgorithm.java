package agents.firm.production.control.maximizer.algorithms;

import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import model.utilities.Deactivatable;

/**
 * <h4>Description</h4>
 * <p/> This interface refers to the maximization algorithm that has to deal with HOW to choose the new X given current and past X
 * and current and past Y
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-12
 * @see
 */
public interface WorkerMaximizationAlgorithm extends Deactivatable {

    /**
     * Asks the maximizer what the next worker target should be!
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits what are the new profits
     * @param newRevenues what are the new revenues
     *@param newCosts what are the new costs
     * @param oldRevenues what were the old revenues
     * @param oldCosts what were the old costs
     * @param oldWorkerTarget what was the target last time we changed them
     * @param oldProfits what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts,
                                              float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits);

    /**
     * The maximizer tells you to start over, probably because of change in machinery
     * @param maximizer the maximizer resetting you
     * @param p the plant it is controlling
     */
    public void reset(WorkforceMaximizer maximizer, Plant p);

}
