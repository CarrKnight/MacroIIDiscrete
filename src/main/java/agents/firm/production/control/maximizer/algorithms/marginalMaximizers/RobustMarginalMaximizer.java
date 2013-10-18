/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.*;

/**
 * <h4>Description</h4>
 * <p/> This is very similar to the original marginal maximizer but it's designed to work when there is noise in the currentWorkers of the human resources.
 * All it does is call the super class being careful when currentWorkerTarget != numberCurrentWorkers (basically maximizing in disequilibrium).
 * When that happens the targets are not numberCurrentWorkers +-1, rather currentWorkerTarget+-1
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-09-06
 * @see
 */
public class RobustMarginalMaximizer implements WorkerMaximizationAlgorithm {

    /**
     * this is the original maximizer that checks MB and MC to increase/decrease worker targets by 1
     */
    MarginalMaximizer maximizer;


    private int numberOfChoices = 0;


    public final static Logger LOGGER = Logger.getLogger(MarginalMaximizerStatics.class.getName());

    private static FileHandler fh = null;

    static
    {

        LOGGER.setUseParentHandlers(false);

        try{
            fh = new FileHandler("/mnt/0EA6C8BFA6C8A913/maximization2.log",false);
            fh.setFormatter(new XMLFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        LOGGER.setLevel(Level.ALL);
    }


    /**
     * Creates a maximizer that acts as hill-climber but rather than "experimenting" it infers the slope by checking marginal costs
     *
     * @param hr      Human resources
     * @param control The plant control
     * @
     */
    public RobustMarginalMaximizer(@Nonnull HumanResources hr, @Nonnull PlantControl control, @Nonnull Plant p, @Nonnull Firm owner) {
        maximizer = new MarginalMaximizer(hr,control,p,owner);
    }

    /**
     * Creates a maximizer that acts as hill-climber but rather than "experimenting" it infers the slope by checking marginal costs
     *
     * @param maximizer the maximizer to make robust
     * @
     */
    public RobustMarginalMaximizer(final MarginalMaximizer maximizer) {
        this.maximizer =maximizer;
    }



    /**
     * Simply checks profit in the 2 directions around the currentWorkerTarget. Choose the highest of the three
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param newRevenues
     * @param newCosts
     * @param oldRevenues
     * @param oldCosts
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {
        LOGGER.log(Level.INFO,"-----------------------------------------" + maximizer.getHr().getFirm());
        numberOfChoices++;


        int currentWorkerNumber = maximizer.getHr().getNumberOfWorkers();
        int futureTarget = maximizer.chooseWorkerTarget(currentWorkerNumber, newProfits, newRevenues, newCosts, oldRevenues, oldCosts, oldWorkerTarget, oldProfits);    //To change body of overridden methods use File | Settings | File Templates.



        LOGGER.log(Level.INFO,"worker number: " + currentWorkerNumber + ", actual target: " + currentWorkerTarget + ", future target: " + futureTarget);

        if(futureTarget == currentWorkerNumber)
        {
            LOGGER.log(Level.INFO, "----> unchanged at: " +currentWorkerTarget);
            return currentWorkerTarget;

        }
        if(futureTarget == currentWorkerNumber + 1)
        {
            if(futureTarget>currentWorkerTarget)
            {
                LOGGER.log(Level.INFO, "----> increased at at: " +(currentWorkerTarget+1));
                return currentWorkerTarget+1;
            }
            else
            {
                LOGGER.log(Level.INFO, "----> unchanged at: " +currentWorkerTarget);
                return currentWorkerTarget;
            }
        }
        if(futureTarget== currentWorkerNumber -1)
        {

            if(futureTarget<currentWorkerTarget)
            {
                LOGGER.log(Level.INFO, "----> decreased at at: " +(currentWorkerTarget-1));
                if(numberOfChoices < 1000)
                    return Math.max(currentWorkerTarget-1,1);
                else
                    return currentWorkerTarget-1;
            }
            else
            {
                LOGGER.log(Level.INFO, "----> unchanged at: " +currentWorkerTarget);
                return currentWorkerTarget;
            }
        }
        if(futureTarget == -1)
            return -1; //delay exception!

        throw new RuntimeException("the marginal maximizer didn't return anything I expected, # of workers: " +
                currentWorkerNumber + ", target: " + currentWorkerTarget + ", futureTarget: " + futureTarget);

    }


    @Override
    public void turnOff() {
        maximizer.turnOff();
    }

    /**
     * The maximizer tells you to start over, probably because of change in machinery
     *
     * @param maximizer the maximizer resetting you
     * @param p         the plant it is controlling
     */
    @Override
    public void reset(WorkforceMaximizer maximizer, Plant p) {
        this.maximizer.reset(maximizer, p);
    }
}
