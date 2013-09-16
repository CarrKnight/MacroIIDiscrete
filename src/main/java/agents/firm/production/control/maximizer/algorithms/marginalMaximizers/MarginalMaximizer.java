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
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import model.utilities.DelayException;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> Creates a maximizer that acts as hill-climber but rather than "experimenting" it infers the slope by checking marginal costs
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public class MarginalMaximizer implements WorkerMaximizationAlgorithm
{
    /**
     * The plant to maximize
     */
    final private Plant p;

    /**
     * The firm owning the plant
     */
    final private Firm owner;

    /**
     * The human resources object
     */
    final private HumanResources hr;

    /**
     * The plant control object
     */
    final private PlantControl plantControl;

    /**
     * What to do if some predictions don't exist?
     */
    private RandomizationPolicy policy = RandomizationPolicy.MORE_TIME;

    /**
     * Each time the marginal maximizer is called,
     */

    /**
     * Creates a maximizer that acts as hill-climber but rather than "experimenting" it infers the slope by checking marginal costs
     * @param hr Human resources
     * @param control The plant control
     * @
     */
    public MarginalMaximizer(@Nonnull HumanResources hr, @Nonnull PlantControl control,
                             @Nonnull Plant p, @Nonnull Firm owner) {
        this.hr = hr;
        this.plantControl = control;
        this.p = p;
        this.owner = owner;
    }


    /**
     * Simply checks profit in the 2 directions around the currentWorkerTarget. Choose the highest of the three
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
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits)
    {

        //compute profits if we increase

        try{
            float profitsIfWeIncrease = currentWorkerTarget < p.maximumWorkersPossible() ? //if we can increase production
                    MarginalMaximizerStatics.computeMarginalProfits(owner, p, hr, plantControl, policy, currentWorkerTarget, currentWorkerTarget + 1) //check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise don't go there!


            //compute profits if we decrease
            float profitsIfWeDecrease = currentWorkerTarget > p.minimumWorkersNeeded() ? //can we decrease production?
                    MarginalMaximizerStatics.computeMarginalProfits(owner, p, hr, plantControl, policy, currentWorkerTarget, currentWorkerTarget - 1) //if so check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise ignore this choice!

            if(profitsIfWeDecrease < 0 && profitsIfWeIncrease < 0)
            {

                //if profits decrease in both direction, stay where you are

                return currentWorkerTarget;

            }
            else
            if(profitsIfWeIncrease >= profitsIfWeDecrease){ //if we increase profits going up, let's do that
                assert profitsIfWeIncrease >= 0;
                return currentWorkerTarget+1;
            }
            else
            {
                assert profitsIfWeDecrease >=0;


                return currentWorkerTarget-1;

            }

        }catch (DelayException e)
        {
            //if an exception was thrown, it must be because we need more time!
            return -1;
        }



    }

    /**
     * what happens if the predictor finds it impossible to give a price prediction?
     */
    public enum RandomizationPolicy{
        /**
         * Randomly choose a number from 0 to 100
         */
        RANDOM,
        /**
         * Checks the last closing price of the market, if it's -1 randomly choose a number from 0 to 100
         */
        MARKET_THEN_RANDOM,

        /**
         * Ask for more time
         */
        MORE_TIME;

        /**
         *
         * ask the randomization policy what to do when a prediction doesn't exist
         * @param market  the market where prediction could not be found
         * @param random a randomizer
         * @return a replacement for the missing prediction
         * @throws Exception an exception is thrown when the policy is to ask for more time
         */
        public long replaceUnknownPrediction(Market market, MersenneTwisterFast random) throws DelayException {

            switch (this){
                default:
                case RANDOM:
                    return random.nextLong(100); //return a random price
                case MARKET_THEN_RANDOM:
                    long marketLastClosingPrice = market.getLastPrice();
                    if(marketLastClosingPrice > 0 ) //if there is a last closing price return that one
                        return marketLastClosingPrice;
                    else
                        return random.nextLong(100); //otherwise return a random price
                case MORE_TIME:
                    throw new DelayException("We need more time to make predictions!");



            }

        }

    }

    public RandomizationPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(RandomizationPolicy policy) {
        this.policy = policy;
    }


    /**
     * Gets The plant to maximize.
     *
     * @return Value of The plant to maximize.
     */
    public Plant getP() {
        return p;
    }

    /**
     * Gets The firm owning the plant.
     *
     * @return Value of The firm owning the plant.
     */
    public Firm getOwner() {
        return owner;
    }

    /**
     * The maximizer tells you to start over, probably because of change in machinery
     *
     * @param maximizer the maximizer resetting you
     * @param p         the plant it is controlling
     */
    @Override
    public void reset(WorkforceMaximizer maximizer, Plant p) {

        //nothing really changes
    }

    @Override
    public void turnOff() {
        //not our problem
    }


    /**
     * Gets The plant control object.
     *
     * @return Value of The plant control object.
     */
    public PlantControl getControl() {
        return plantControl;
    }

    /**
     * Gets The human resources object.
     *
     * @return Value of The human resources object.
     */
    public HumanResources getHr() {
        return hr;
    }


}