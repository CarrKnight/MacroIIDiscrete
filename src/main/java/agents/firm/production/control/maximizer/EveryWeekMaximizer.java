/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.technology.Machinery;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This maximizer simply asks the maximization algorithm a new target every week. It does't wait for that target to be achieved before asking again.
 * This is done with the hope that during competitive scenarios the maximization won't keep targeting something that is now unreacheable/unprofitable because a competitor has changed its behavior
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-22
 * @see
 */
public class EveryWeekMaximizer<ALG extends WorkerMaximizationAlgorithm> implements WorkforceMaximizer<ALG>, Steppable
{
    /**
     * set to false at turnedOff
     */
    private boolean isActive = true;

    /**
     * The model link, needed for rescheduling!
     */
    private final MacroII model;

    /**
     * the owner, to ask for profits and such
     */
    private final Firm owner;

    /**
     * the object we are controlling, really.
     */
    private final HumanResources hr;

    /**
     * from here we know how many workers we are hiring today!
     */
    private final Plant plant;

    /**
     * the control object of the plant
     */
    private final PlantControl control;

    /**
     * the maximization to use
     */
    private final ALG workerMaximizationAlgorithm;

    /**
     * How many days must pass between asking the WorkerMaximizationAlgorithm
     */
    private int howManyDaysBeforeEachCheck = 7;


    public EveryWeekMaximizer(MacroII model, Firm owner, HumanResources hr, Plant plant, PlantControl control, ALG workerMaximizationAlgorithm) {
        this.model = model;
        this.owner = owner;
        this.hr = hr;
        this.plant = plant;
        this.control = control;
        this.workerMaximizationAlgorithm = workerMaximizationAlgorithm;
    }

    /**
     * Method to start the workforce maximizer
     */
    @Override
    public void start() {
        Preconditions.checkState(isActive,"Can't start a turnedOff() maximizer");
        model.scheduleAnotherDay(ActionOrder.THINK,this,howManyDaysBeforeEachCheck);


    }

    @Override
    public void turnOff() {
        Preconditions.checkState(isActive,"Can't turn off a maximizer twice!");
        isActive = false;

    }

    /**
     * ignored
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }

    /**
     * ignored
     */
    @Override
    public void plantShutdownEvent(Plant p) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
    }

    @Override
    public void step(SimState state) {
        if(!isActive)
            return;



       /*



        //what's the future target?
        int futureTarget = workerMaximizationAlgorithm.chooseWorkerTarget(control.getTarget(),newProfits,newRevenues , newCosts,
                oldRevenue,oldCosts, oldWorkerTarget, oldProfits);


        //if the future target is negative, do it again next week (the subclass wants more info)
        if(futureTarget < 0){
            //System.out.println("delay");
            reschedule(nextCheck + weeksToMakeObservation*7);

        }
        else {


            //log it
            hr.getFirm().logEvent(hr,
                    MarketEvents.CHANGE_IN_TARGET,
                    hr.getFirm().getModel().getCurrentSimulationTimeInMillis(),
                    "old Profits: " + oldProfits + ", new profits: " + newProfits +
                            "; old workerTarget:" + oldWorkerTarget + ", new target:" + futureTarget);

            //remember
            oldProfits = newProfits;
            oldRevenue = newRevenues;
            oldCosts = newCosts;
            oldWorkerTarget = control.getTarget();


            //tell control/targeter about new target
            control.setTarget(futureTarget);

            //if we did change targets, next week is not observation week
            checkWeek = false; //set it to false




            //try again!
            reschedule(nextCheck);



        }



        throw new RuntimeException("not implemented yet!");

        model.scheduleAnotherDay(ActionOrder.THINK,this,howManyDaysBeforeEachCheck);
         */
    }
}
