/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.otherMaximizers;

import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;

/**
 * <h4>Description</h4>
 * <p/> An extremely simple maximization algorithm that always targets the same number
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-08
 * @see
 */
public class FixedTargetMaximizationAlgorithm implements WorkerMaximizationAlgorithm
{

    /**
     * The worker target
     */
    private int workerTarget=5;

    /**
     * Asks the maximizer what the next worker target should be!
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param newRevenues         what are the new revenues
     * @param newCosts            what are the new costs
     * @param oldRevenues         what were the old revenues
     * @param oldCosts            what were the old costs
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues,
                                  float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {
        return workerTarget;
    }

    /**
     * The maximizer tells you to start over, probably because of change in machinery
     *
     * @param maximizer the maximizer resetting you
     * @param p         the plant it is controlling
     */
    @Override
    public void reset(WorkforceMaximizer maximizer, Plant p) {
    }

    @Override
    public void turnOff() {
    }


    /**
     * Gets The worker target.
     *
     * @return Value of The worker target.
     */
    public int getWorkerTarget() {
        return workerTarget;
    }

    /**
     * Sets new The worker target.
     *
     * @param workerTarget New value of The worker target.
     */
    public void setWorkerTarget(int workerTarget) {
        this.workerTarget = workerTarget;
    }
}
