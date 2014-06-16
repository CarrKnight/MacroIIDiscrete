/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithmFactory;
import agents.firm.production.technology.Machinery;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.stats.collectors.enums.PlantDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This maximizer simply asks the maximization algorithm a new target every week. It does't wait for that target to be achieved before asking again.
 * This is done with the hope that during competitive allScenarios the maximization won't keep targeting something that is now unreacheable/unprofitable because a competitor has changed its behavior
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
public class PeriodicMaximizer<ALG extends WorkerMaximizationAlgorithm> extends BaseWorkforceMaximizer<ALG> implements Steppable
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

    public static int defaultAverageCheckFrequency = 20;
    /**
     * How many days must pass between asking the WorkerMaximizationAlgorithm
     */
    private int howManyDaysBeforeEachCheck = defaultAverageCheckFrequency;

    /**
     * when this is true, the day the check is actually made varies
     */
    private boolean randomizeDays = true;

    /**
     * when we set the last worker target, what was it?
     */
    private int lastWorkerTarget = 0;

    /**
     * Creates an PeriodicMaximizer with a pre-made algorithm
     * @param hr the human resources
     * @param control the plant control
     * @param algorithm the pre-made algorithm
     */
    public PeriodicMaximizer(HumanResources hr, PlantControl control, ALG algorithm) {
        this(hr.getModel(),hr.getFirm(),hr,hr.getPlant(),control,algorithm);

    }

    /**
     * Creates a weekly workforce maximizer with the type of algorithm to build
     * @param hr the human resources
     * @param control the plant control
     * @param algorithmClass the type of algorithm to make!
     */
    public PeriodicMaximizer(HumanResources hr, PlantControl control, Class<ALG> algorithmClass) {
        this(hr,control, WorkerMaximizationAlgorithmFactory.buildMaximizationAlgorithm(hr, control, algorithmClass));

    }

    public PeriodicMaximizer(MacroII model, Firm owner, HumanResources hr, Plant plant, PlantControl control, ALG workerMaximizationAlgorithm) {
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
        control.setTarget(1);
        Preconditions.checkState(isActive,"Can't start a turnedOff() maximizer");
        reschedule();


    }

    private void reschedule() {
        if(randomizeDays) {
            float howManyDaysBeforeEachCheck1 = (float) howManyDaysBeforeEachCheck;
            model.scheduleAnotherDayWithFixedProbability(ActionOrder.THINK, this, 1f / howManyDaysBeforeEachCheck1);
        }
        else
            model.scheduleAnotherDay(ActionOrder.THINK, this, howManyDaysBeforeEachCheck);
    }

    @Override
    public void turnOff() {
        super.turnOff();
        Preconditions.checkState(isActive,"Can't turn off a maximizer twice!");
        isActive = false;
        plant.removeListener(this);

    }

    /**
     * remembers it as the last day there was a change in workforce!
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
        //if you are not on target, decide tomorrow
        if(control.getTarget() != plant.getNumberOfWorkers() && plant.getNumberOfWorkers()==0)
        {
            model.scheduleTomorrow(ActionOrder.THINK, this);
            return;
        }


        float newProfits = owner.getPlantProfits(plant);
        float newRevenues = owner.getPlantRevenues(plant);
        float newCosts = owner.getPlantCosts(plant);

        float oldProfits;
        float oldRevenue;
        float oldCosts;

        //now for the past, if we never saw a change before then old and new profits are all just -1
        int lastWorkerChangeDay = plant.getLastDayAMeaningfulChangeInWorkforceOccurred();
        if(lastWorkerChangeDay == -1){
            oldProfits = 0;
            oldRevenue=0;
            oldCosts=0;
        }
        else
        {
            assert lastWorkerChangeDay > 0 : "last worker change day shouldn't be negative";
            //make sure that the last worker change day was the day when we actually moved to this new number of workers!
   /*         assert model.getMainScheduleTime() == lastWorkerChangeDay  || //make an exception if the change was today, because that day wouldn't be stored just yet
                    plant.getObservationRecordedThisDay(PlantDataType.TOTAL_WORKERS,lastWorkerChangeDay)== plant.getNumberOfWorkers();
            assert  plant.getObservationRecordedThisDay(PlantDataType.TOTAL_WORKERS,lastWorkerChangeDay-1) != plant.getNumberOfWorkers(); //should have been a meaningful change!
        */
            int dayToCheck= lastWorkerChangeDay-1;
            oldProfits = (float) plant.getObservationRecordedThisDay(PlantDataType.PROFITS_THAT_WEEK,dayToCheck);
            oldRevenue = (float) plant.getObservationRecordedThisDay(PlantDataType.REVENUES_THAT_WEEK,dayToCheck);
            oldCosts = (float) plant.getObservationRecordedThisDay(PlantDataType.COSTS_THAT_WEEK,dayToCheck);
        }

        //what's the future target?

        int futureTarget = workerMaximizationAlgorithm.chooseWorkerTarget(control.getTarget(),newProfits,newRevenues , newCosts,
                oldRevenue,oldCosts, lastWorkerTarget, oldProfits);
        //if the future target is negative, do it again next week (the subclass wants more info)
        if(futureTarget < 0){
            handleNewEvent(new LogEvent(this, LogLevel.INFO,"maximizer told to wait for better predictions",
                    oldProfits,newProfits,lastWorkerTarget,futureTarget));
        }
        else {

            handleNewEvent(new LogEvent(this, LogLevel.INFO,"old Profits:{} , new profits:{}, old target:{}, new target:{}",
                    oldProfits,newProfits,lastWorkerTarget,futureTarget));



            //tell control/targeter about new target
            control.setTarget(futureTarget);
        }

        lastWorkerTarget = futureTarget;


        reschedule();


    }


    /**
     * Gets How many days must pass between asking the WorkerMaximizationAlgorithm.
     *
     * @return Value of How many days must pass between asking the WorkerMaximizationAlgorithm.
     */
    public int getHowManyDaysBeforeEachCheck() {
        return howManyDaysBeforeEachCheck;
    }

    /**
     * Sets new How many days must pass between asking the WorkerMaximizationAlgorithm.
     * @param howManyDaysBeforeEachCheck New value of How many days must pass between asking the WorkerMaximizationAlgorithm.
     */
    public void setHowManyDaysBeforeEachCheck(int howManyDaysBeforeEachCheck) {
        this.howManyDaysBeforeEachCheck = howManyDaysBeforeEachCheck;
    }


    /**
     * Sets new when this is true, the day the check is actually made varies.
     *
     * @param randomizeDays New value of when this is true, the day the check is actually made varies.
     */
    public void setRandomizeDays(boolean randomizeDays) {
        this.randomizeDays = randomizeDays;
    }

    /**
     * Gets when this is true, the day the check is actually made varies.
     *
     * @return Value of when this is true, the day the check is actually made varies.
     */
    public boolean isRandomizeDays() {
        return randomizeDays;
    }

    /**
     * Gets The maximization algorithm that chooses the new workers given the results..
     *
     * @return Value of The maximization algorithm that chooses the new workers given the results.
     */
    public ALG getMaximizationAlgorithm() {
        return workerMaximizationAlgorithm;
    }


    /**
     * Sets new defaultAverageCheckFrequency.
     *
     * @param newDefault New value of defaultAverageCheckFrequency.
     */
    public static void setDefaultAverageCheckFrequency(int newDefault) {
        defaultAverageCheckFrequency = newDefault;
    }

    /**
     * Gets defaultAverageCheckFrequency.
     *
     * @return Value of defaultAverageCheckFrequency.
     */
    public static int getDefaultAverageCheckFrequency() {
        return defaultAverageCheckFrequency;
    }
}
