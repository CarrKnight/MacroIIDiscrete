/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.decorators;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.inventoryControl.Level;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.technology.Machinery;

import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> A simple decorator abstract class (only for abstract plant control!!!)
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-26
 * @see
 */
public abstract class PlantControlDecorator implements PlantControl{


    /**
     * this is the object to decorate!
     */
    protected PlantControl toDecorate;

    /**
     * instantiate the decorator
     * @param toDecorate the plant control to decorate
     */
    protected PlantControlDecorator(PlantControl toDecorate) {
        this.toDecorate = toDecorate;
    }


    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap() {
        return toDecorate.estimateDemandGap();
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(Good good) {
        return toDecorate.maxPrice(good);

    }

    /**
     * Answer the question: how much am I willing to pay for this kind of labor?
     * Notice that NO UPDATING SHOULD TAKE PLACE in calling this method. Human Resources expects maxPrice() to be consistent from one call to the next.
     * To notify hr of inconsistencies call updateEmployeeWages(). <br>
     * In short,for plant control, <b>this should be a simple getter.</b>. If you are a subclass and want to change wages, use the current wage setter.
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(GoodType type) {
        return toDecorate.maxPrice(type);
    }

    /**
     * The controller sets wages for everybody. Probably only used by subcomponents
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(long newWage) {
        toDecorate.setCurrentWage(newWage);
    }

    /**
     * This method returns the control rating on current stock held <br>
     * @return the rating on the current stock conditions or null if the department is not active.
     */
    @Override
    @Nullable
    public Level rateCurrentLevel() {
        return toDecorate.rateCurrentLevel();
    }

    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    @Override
    public void turnOff() {
        toDecorate.turnOff();
    }

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     * @return
     */
    @Override
    public boolean canBuy() {
        return toDecorate.canBuy();
    }

    /**
     * When instantiated the control doesn't move until it receives a stimulus OR start() is called. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    @Override
    public void start() {
        toDecorate.start();
    }

    /**
     * Generic getter to know the human resources objects associated with the control
     */
    @Override
    public HumanResources getHr() {
        return toDecorate.getHr();
    }

    /**
     * get workforce size targeted
     */
    @Override
    public int getTarget() {
        return toDecorate.getTarget();
    }





    /**
     * pass the message down
     *
     * @param p          the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        toDecorate.changeInWorkforceEvent(p, workerSizeNow,workerSizeBefore );
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        toDecorate.changeInMachineryEvent(p, machinery);
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
        toDecorate.changeInWageEvent(p, workerSize, wage);
    }


    /**
     * The targeter is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        toDecorate.setTarget(workerSizeTargeted);
    }


    /**
     * Set the flag to allow or ban the hr from hiring people
     * @param canBuy true if the hr can hire more people at this wage.
     */
    @Override
    public void setCanBuy(boolean canBuy) {
        toDecorate.setCanBuy(canBuy);
    }


    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        toDecorate.plantShutdownEvent(p);
    }


    /**
     * Get the current wages paid by the control
     */
    @Override
    public long getCurrentWage() {
        return toDecorate.getCurrentWage();
    }
}
