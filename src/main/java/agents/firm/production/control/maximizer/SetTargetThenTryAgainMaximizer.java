/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithmFactory;
import agents.firm.production.technology.Machinery;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import sim.engine.SimState;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> This is a maximizer that sets a target for itself and then wait until the target has been achieved for a while before
 * asking the maximization algorithm what should be the new target.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-23
 * @see
 */
public class SetTargetThenTryAgainMaximizer<ALG extends WorkerMaximizationAlgorithm> extends BaseWorkforceMaximizer<ALG>
        implements Steppable {



    /**
     * the human resources object
     */
    private final HumanResources hr;

    /**
     * the control object
     */
    private final PlantControl control;

    /**
     * The maximization algorithm that chooses the new workers given the results.
     */
    private ALG maximizationAlgorithm;

    /**
     * keeps stepping as long as this is active!
     */
    boolean isActive = true;

    /**
     * here we memorize the last time we checked for profits
     */
    private float oldProfits = 0;

    /**
     * here we memorize the last time we checked for revenues
     */
    private float oldRevenue = 0;


    /**
     * here we memorize the last time we checked for costs
     */
    private float oldCosts = 0;


    /**
     * here we memorize the last worker target of ours
     */
    private int oldWorkerTarget = 0;

    /**
     * when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day
     */
    private boolean randomspeed= false;


    /**
     * Profit check to change target is only activated at the start of the week when we have the right number of workers (or the observation makes no sense). When that week starts we set this flag to true
     */
    private boolean checkWeek = false;
    /**
     * Number of weeks to pass before we can trust the profit report we get.
     */
    private int weeksToMakeObservation = 3;

    /**
     * Creates a weekly workforce maximizer with a pre-made algorithm
     * @param hr the human resources
     * @param control the plant control
     * @param algorithm the pre-made algorithm
     */
    public SetTargetThenTryAgainMaximizer(HumanResources hr, PlantControl control, ALG algorithm) {
        this.hr = hr;
        this.control = control;
        this.maximizationAlgorithm = algorithm;

    }

    /**
     * Creates a weekly workforce maximizer with the type of algorithm to build
     * @param hr the human resources
     * @param control the plant control
     * @param algorithmClass the type of algorithm to make!
     */
    public SetTargetThenTryAgainMaximizer(HumanResources hr, PlantControl control, Class<ALG> algorithmClass) {
        this.hr = hr;
        this.control = control;
        this.maximizationAlgorithm = WorkerMaximizationAlgorithmFactory.buildMaximizationAlgorithm(hr,control,algorithmClass);

    }

    /**
     * Method to switch the strategy off. Irreversible
     */
    @Override
    public void turnOff()
    {
        super.turnOff();
        isActive = false;
        maximizationAlgorithm.turnOff();
    }

    /**
     * ignored
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }

    /**
     ignored
     */
    @Override
    public void plantShutdownEvent(Plant p) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        //reset the algorithm
        maximizationAlgorithm.reset(this,p);
        //start over
        this.start();
    }

    /**
     * The profit check steps work as follow
     * <ul>
     *     <li>
     *         Wait until the targeter achieved the number of workers we set for it
     *     </li>
     *     <li>
     *         Once the targeter has achieved its targets we wait  "weeksToMakeObservation" so that all the other departments adapt to the new production (and to avoid initial noise)
     *     </li>
     *     <li>
     *         Checkweek: we waited enough weeks, compare old profits with new profits and select a new worker target
     *     </li>
     * </ul>
     * @param state
     */
    public void step(SimState state){

        if(!isActive) //if you are not active, you are done!
            return;

        //you are going to run this again in a week (with slight noise)
        int nextCheck;
        nextCheck = 7 + (state.random.nextInt(6) -3) ;    assert nextCheck > 0;


        //if you haven't achieved your worker objective you can't really make a judgment so just try again in next week
        if(hr.getPlant().getNumberOfWorkers() != control.getTarget()){
            reschedule(nextCheck);
            checkWeek = false; //if it was observation week and you missed on your target, start over :(
            return;
        }
        //if we are at the right target then we move to checkWeek status
        if(hr.getPlant().getNumberOfWorkers() == control.getTarget() && !checkWeek){
            checkWeek = true; //next observation is check week!
            reschedule(nextCheck + weeksToMakeObservation*7);
            return;
        }




        //if we are here, it's observation week!
        assert checkWeek;
        //todo this happens very rarely during messy world, should I be concerned?
//        assert hr.getPlant().getNumberOfWorkersDuringProduction() == control.getTarget() :  hr.getPlant().getNumberOfWorkersDuringProduction() + "," +  control.getTarget();

        //get profits
        float newProfits = hr.getFirm().getPlantProfits(hr.getPlant());
        float newRevenues = hr.getFirm().getPlantRevenues(hr.getPlant());
        float newCosts = hr.getFirm().getPlantCosts(hr.getPlant());



        //what's the future target?
        int futureTarget = maximizationAlgorithm.chooseWorkerTarget(control.getTarget(),newProfits,newRevenues , newCosts,
                oldRevenue,oldCosts, oldWorkerTarget, oldProfits);


        //if the future target is negative, do it again next week (the subclass wants more info)
        if(futureTarget < 0){
            reschedule(nextCheck + weeksToMakeObservation*7);

        }
        else {



            handleNewEvent(new LogEvent(this, LogLevel.INFO,"old Profits:{} , new profits:{}, old target:{}, new target:{}",
                    oldProfits,newProfits,oldWorkerTarget,futureTarget));


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




    }

    /**
     * Call this to reschedule the step function some days from now
     * @param daysAway
     */
    private void reschedule(int daysAway) {


        if(randomspeed)
        {
            float probability = 1f/(float)daysAway;
            hr.getFirm().getModel().scheduleAnotherDayWithFixedProbability(ActionOrder.THINK,this, probability);
        }

        else
            hr.getFirm().getModel().scheduleAnotherDay(ActionOrder.THINK,this, daysAway);

    }


    /**
     * Method to start the workforce maximizer
     */
    @Override
    public void start() {
        //set target to 1
        control.setTarget(1);
        oldProfits = -getHr().getPlant().getCostStrategy().weeklyFixedCosts();
        oldWorkerTarget = 0;
        //adjust on it
        hr.getPlant().getModel().scheduleSoon(ActionOrder.THINK, this);

    }

    public boolean isActive() {
        return isActive;
    }






    public HumanResources getHr() {
        return hr;
    }

    public PlantControl getControl() {
        return control;
    }


    public int getWeeksToMakeObservation() {
        return weeksToMakeObservation;
    }

    public void setWeeksToMakeObservation(int weeksToMakeObservation) {
        this.weeksToMakeObservation = weeksToMakeObservation;
    }


    public boolean isCheckWeek() {
        return checkWeek;
    }

    public int getOldWorkerTarget() {
        return oldWorkerTarget;
    }

    public float getOldProfits() {
        return oldProfits;
    }


    /**
     * Gets when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     *
     * @return Value of when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     */
    public boolean isRandomspeed() {
        return randomspeed;
    }

    /**
     * Sets new when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     *
     * @param randomspeed New value of when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     */
    public void setRandomspeed(boolean randomspeed) {
        this.randomspeed = randomspeed;
    }


    /**
     * Gets The maximization algorithm that chooses the new workers given the results..
     *
     * @return Value of The maximization algorithm that chooses the new workers given the results.
     */
    public ALG getMaximizationAlgorithm() {
        return maximizationAlgorithm;
    }


    /**
     * Sets new The maximization algorithm that chooses the new workers given the results.
     * It turns off the previous one
     *
     * @param maximizationAlgorithm New value of The maximization algorithm that chooses the new workers given the results.
     */
    protected void setMaximizationAlgorithm( ALG maximizationAlgorithm) {
        if(this.maximizationAlgorithm != null)
            this.maximizationAlgorithm.turnOff();

        this.maximizationAlgorithm = maximizationAlgorithm;
    }




}
