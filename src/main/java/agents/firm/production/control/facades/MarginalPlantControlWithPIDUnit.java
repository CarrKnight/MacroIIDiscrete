/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.facades;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.control.FactoryProducedTargetAndMaximizePlantControl;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.WeeklyWorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPID;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.inventoryControl.Level;
import goods.Good;
import goods.GoodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> Similar to marginalPlantControl but using pid to find the step size  and targeting sigmoid( x= MB/MC and targeting x/(1-x)-->0.5)
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-03
 * @see
 */
public class MarginalPlantControlWithPIDUnit implements PlantControl, PlantListener {


    /**
     * the control we delegate to
     */
    private final TargetAndMaximizePlantControl control;

    private final  FactoryProducedTargetAndMaximizePlantControl<PIDTargeter,
            WeeklyWorkforceMaximizer<MarginalMaximizerWithUnitPID>>
            generatedControl;


    /**
     * A facade for a marginal plant control with PID used as a way to select the step size
     * @param hr
     */
    public MarginalPlantControlWithPIDUnit(@Nonnull HumanResources hr)
    {

        generatedControl = TargetAndMaximizePlantControl.PlantControlFactory(hr,
                PIDTargeter.class,
                //the next argument is why the java generics guys really needed drug screening when they were thinking this
                //of course I couldn't figure this out, it's from here: http://stackoverflow.com/questions/1079279/class-object-of-generic-class-java
                (Class<WeeklyWorkforceMaximizer<MarginalMaximizerWithUnitPID>>)(Class<?>) WeeklyWorkforceMaximizer.class,
                MarginalMaximizerWithUnitPID.class);


        generatedControl.getWorkforceMaximizer().getMaximizationAlgorithm().setSigmoid(true);


        control = generatedControl.getControl();



    }



    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * currentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap() {
        return control.estimateDemandGap();
    }

    /**
     * A link to the object build, so we can change its parameters
     * @return
     */
    public FactoryProducedTargetAndMaximizePlantControl<PIDTargeter,
            WeeklyWorkforceMaximizer<MarginalMaximizerWithUnitPID>> getGeneratedControl() {
        return generatedControl;
    }

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return control.canBuy();
    }

    /**
     * Set the flag to allow or ban the hr from hiring people
     * @param canBuy true if the hr can hire more people at this wage.
     */
    @Override
    public void setCanBuy(boolean canBuy) {
        control.setCanBuy(canBuy);
    }

    public boolean isActive() {
        return control.isActive();
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        control.plantShutdownEvent(p);
    }

    /**
     * Get the wage offered (for subclasses only)
     * @return the wage offered.
     */
    @Override
    public long getCurrentWage() {
        return control.getCurrentWage();
    }


    /**
     * Returns the plant monitored by the HR
     */
    public Plant getPlant() {
        return control.getPlant();
    }


    /**
     * The targeter is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        control.setTarget(workerSizeTargeted);
    }

    /**
     * Ask the targeter what is the current worker target
     * @return the number of workers the strategy is targeted to find!
     */
    @Override
    public int getTarget() {
        return control.getTarget();
    }

    /**
     * Answer the question: how much am I willing to pay for this kind of labor?
     * Notice that NO UPDATING SHOULD TAKE PLACE in calling this method. Human Resources expects maxPrice() to be consistent from one call to the next.
     * To notify hr of inconsistencies call updateEmployeeWages(). <br>
     * In short,for plant control, <b>this should be a simple getter.</b>. If you are a subclass and want to change it, use the current wage setter.
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(@Nonnull GoodType type) {
        return control.maxPrice(type);
    }

    /**
     * Answer the question: how much am I willing to pay for this specific kind of labor?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(@Nonnull Good good) {
        return control.maxPrice(good);
    }

    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    @Override
    public void turnOff() {
        control.turnOff();
    }

    /**
     * Sets the wage, update offers and then updates wages of current workers
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(long newWage) {
        control.setCurrentWage(newWage);
    }

    /**
     * Returns the human resources object
     */
    @Override
    public HumanResources getHr() {
        return control.getHr();
    }

    /**
     * the method just calls the start of the Targeter and the Maximizer
     */
    @Override
    public void start() {
        control.start();
    }

    /**
     * pass the message down
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
        control.changeInWorkforceEvent(p, workerSize);
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        control.changeInMachineryEvent(p, machinery);
    }

    /**
     * This method returns the control rating on current stock held <br>
     *
     * @return the rating on the current stock conditions or null if the department is not active.
     */
    @Nullable
    @Override
    public Level rateCurrentLevel() {
        return control.rateCurrentLevel();
    }

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     *
     * @param wage       the new wage
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
        control.changeInWageEvent(p, workerSize, wage);
    }



    /**
     * Sets new when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     *
     * @param randomspeed New value of when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     */
    public void setRandomspeed(boolean randomspeed) {
        generatedControl.getWorkforceMaximizer().setRandomspeed(randomspeed);
    }

    /**
     * Gets when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     *
     * @return Value of when this is true rather than rescheduling itself every x days, it sets  probability of being reschedueled every day.
     */
    public boolean isRandomspeed() {
        return generatedControl.getWorkforceMaximizer().isRandomspeed();
    }

    public void setWeeksToMakeObservation(int weeksToMakeObservation) {
        generatedControl.getWorkforceMaximizer().setWeeksToMakeObservation(weeksToMakeObservation);
    }

    public int getWeeksToMakeObservation() {
        return generatedControl.getWorkforceMaximizer().getWeeksToMakeObservation();
    }
}
