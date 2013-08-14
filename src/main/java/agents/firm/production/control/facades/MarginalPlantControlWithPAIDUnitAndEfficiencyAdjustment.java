/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.facades;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.WeeklyWorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPIDCascadeEfficency;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.inventoryControl.Level;
import financial.Market;
import goods.Good;
import goods.GoodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> facade of PIDTargeter + MarginalMaximizerWithUnitPIDCascadeEfficency
 * <p/> It delegates all the methods of the control plus the setup comand for the MarginalMaximizerWithUnitPIDCascadeEfficency
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-04
 * @see
 */
public class MarginalPlantControlWithPAIDUnitAndEfficiencyAdjustment implements PlantControl, PlantListener {


    /**
     * the control we delegate to
     */
    private TargetAndMaximizePlantControl control;

    private final PIDTargeter targeter;

    private final MarginalMaximizerWithUnitPIDCascadeEfficency maximizer;


    /**
     * A facade for a marginal plant control with PID used as a way to select the step size
     * @param hr the human resources we are collaborating with
     */
    public MarginalPlantControlWithPAIDUnitAndEfficiencyAdjustment(@Nonnull HumanResources hr)
    {
        control = TargetAndMaximizePlantControl.emptyTargetAndMaximizePlantControl(hr);
        targeter = new PIDTargeter(hr,control);
        control.setTargeter(targeter);
        maximizer = new MarginalMaximizerWithUnitPIDCascadeEfficency(hr,control,hr.getPlant(),hr.getFirm(),
                hr.getRandom(),hr.getPlant().workerSize());
        control.setMaximizer(new WeeklyWorkforceMaximizer<>(
                hr,control,maximizer));



    }

    /**
     * With this method we set up the MASTER pid (that sets efficency away from 0 given
     * @param daysToAverage we are going to use the moving average
     * @param proportional the proportional parameter of the PID
     * @param integrative  the integrative parameter of the PID
     * @param derivative   the derivative parameter of the PID
     * @param market  the market
     */
    public void setupLookup(int daysToAverage, float proportional, float integrative, float derivative, Market market) {
        maximizer.setupLookup(daysToAverage, proportional, integrative, derivative, market);
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
}

