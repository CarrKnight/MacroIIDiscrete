/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.hillClimbers;

import ec.util.MersenneTwisterFast;


/**
 * <h4>Description</h4>
 * <p/>  A modification of the hill-climber maximizer: this one has a temperature of .5 that makes it take the opposite direction from time to time
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
public class AnnealingMaximizer extends HillClimberMaximizer {

    public static float initialTemperature =1f;

    public static float temperatureDecay =.7f;


    /**
     * decreases by 10% each time adjust
     */
    private float temperature = initialTemperature;

    /**
     * flag that says whether this turn we are moving in the wrong direction
     */
    private boolean misStep = false;

    final private MersenneTwisterFast random;

    public AnnealingMaximizer(long weeklyFixedCosts, int minimumWorkers, int maximumWorkers,  MersenneTwisterFast random) {
        super(weeklyFixedCosts, minimumWorkers, maximumWorkers);
        this.random = random;
    }

    /**
     * Asks the subclass what the next worker target will be!
     *
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits what are the new profits
     * @param newRevenues what are the new revenues
     *@param newCosts what are the new costs
     * @param oldRevenues what were the old revenues
     * @param oldCosts what were the old costs
     * @param oldWorkerTarget what was the target last time we changed them
     * @param oldProfits what were the profits back then   @return the new worker targets. Any negative number means to check again!
     * */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {

        misStep = false;
        int newTarget =  super.chooseWorkerTarget(currentWorkerTarget, newProfits, newRevenues, newCosts, oldRevenues, oldCosts, oldWorkerTarget, oldProfits);    //To change body of overridden methods use File | Settings | File Templates.
        temperature = temperature * temperatureDecay;
        return newTarget;
    }

    /**
     * Should I increase or decrease worker targets?  For hillclimber that's just ( sign(newProfits - oldProfits) * sign(currentTarget - oldTarget)). <br>
     *
     * @return +1 if I should increase, -1 if I should decrease or 0 if I should stay the same. Any other result throws an exception
     */
    @Override
    protected int direction(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {
        //are we going in the wrong direction?
        misStep = random.nextBoolean(temperature);
        int direction = super.direction(currentWorkerTarget,newProfits, oldWorkerTarget, oldProfits);
        //don't let it stop if the temperature is still far from 0
        if(direction <=0 && currentWorkerTarget == 0 && temperature > .1f)
            direction = 1;

        //if not go on.
        if(!misStep)
            return direction;
        else
        if(direction != 0)
            return -direction;
        else
        {

            return  random.nextBoolean() ? + 1 : -1; //return a random direction of either 1 or -1
            }

    }


    /**
     * We let temperature over-ride memory
     * @return true if the future target is not in memory or the memory of it is BETTER than the current profits
     */
    @Override
    public boolean checkMemory(int futureTarget, float currentProfits) {
        if(misStep)
            return true;
        else
            return super.checkMemory(futureTarget,currentProfits);
    }


    public static float getInitialTemperature() {
        return initialTemperature;
    }

    public static void setInitialTemperature(float initialTemperature) {
        AnnealingMaximizer.initialTemperature = initialTemperature;
    }

    public static float getTemperatureDecay() {
        return temperatureDecay;
    }

    public static void setTemperatureDecay(float temperatureDecay) {
        AnnealingMaximizer.temperatureDecay = temperatureDecay;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }


    /**
     * Memory search!
     * Memory search is denied whenever temperature is above 20%
     */
    @Override
    public int getBestWorkerTargetInMemory() {
        if(temperature > .20f)
            return -1;
        else
           return super.getBestWorkerTargetInMemory();

    }

    public void setMisStep(boolean misStep) {
        this.misStep = misStep;
    }
}
