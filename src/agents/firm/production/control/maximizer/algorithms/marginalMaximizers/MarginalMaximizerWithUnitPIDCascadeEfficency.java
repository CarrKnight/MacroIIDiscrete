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
import ec.util.MersenneTwisterFast;
import financial.Market;
import financial.utilities.changeLooker.ChangeLookupMAMarket;
import model.utilities.pid.PIDController;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is a cascade maximizer where the slave is just MarginalMaximizerWithUnitPID and the master deviates the efficiency
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-27
 * @see
 */
public class MarginalMaximizerWithUnitPIDCascadeEfficency extends MarginalMaximizerWithUnitPID {


    /**
     * pid controller that is going to be master?
     */
    PIDController lookAhead;

    /**
     * the object we use to store price change
     */
    ChangeLookupMAMarket changeLookup;

    /**
     * Very simple marginal maximizer that chooses worker by targeting MB/MC = 1
     * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1.
     * If the other controller is not set up AFTER this controller is called, then this object is just a MarginalMaximizerWithUnitPID
     * @param hr      the human resources
     * @param control the plant control.
     */
    public MarginalMaximizerWithUnitPIDCascadeEfficency(@Nonnull HumanResources hr, @Nonnull PlantControl control, @Nonnull Plant p, @Nonnull Firm owner, @Nonnull MersenneTwisterFast random, int currentWorkerSize) {
        super(hr, control, p, owner, random, currentWorkerSize);
    }

    /**
     * Very simple marginal maximizer that chooses worker by targeting MB/MC = 1
     * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1.
     *
     * @param hr      the human resources
     * @param control the plant control.
     */
    public MarginalMaximizerWithUnitPIDCascadeEfficency(@Nonnull HumanResources hr, @Nonnull PlantControl control, @Nonnull Plant p, @Nonnull Firm owner, @Nonnull MersenneTwisterFast random, int currentWorkerSize, float proportional, float integral, float derivative) {
        super(hr, control, p, owner, random, currentWorkerSize, proportional, integral, derivative);
    }

    /**
     * With this method we set up the MASTER pid (that sets efficency away from 0 given
     * @param daysToAverage we are going to use the moving average
     * @param proportional the proportional parameter of the PID
     * @param integrative  the integrative parameter of the PID
     * @param derivative   the derivative parameter of the PID
     * @param market  the market
     */
    public void setupLookup(int daysToAverage,float proportional, float integrative, float derivative, Market market)
    {
        changeLookup = new ChangeLookupMAMarket(market,daysToAverage,getHr().getFirm().getModel());
        lookAhead = new PIDController(proportional,integrative,derivative,getHr().getRandom());
        lookAhead.setCanGoNegative(true); lookAhead.setWindupStop(false);
        assert lookAhead.getCurrentMV()==0;
    }


    /**
     * If the lookAhead MASTER PID has been set up, call it first. Then call super
     * @return
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits,
                                     float newRevenues, float newCosts, float oldRevenues,
                                     float oldCosts, int oldWorkerTarget, float oldProfits) {

        if(lookAhead!=null){  //if you have been setup, change efficency target
            lookAhead.adjustOnce(changeLookup.getChange(),0f,true);
            super.setTargetEfficiency(Math.max(1 + lookAhead.getCurrentMV(),0)); //never let it go below 0
        }
        else {
            //otherwise, ignore it!
            assert getTargetEfficiency() == 1;
        }
        //let the superclass deal with it.
        return super.chooseWorkerTarget(currentWorkerTarget, newProfits, newRevenues, newCosts,
                oldRevenues, oldCosts, oldWorkerTarget, oldProfits);





    }
}
