/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pid;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.pricing.BidPricingStrategy;
import goods.Good;
import goods.GoodType;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import sim.engine.SimState;

/**
 * A simple facade representing a Purchases fixed + an autotuner
 * Created by carrknight on 8/22/14.
 */
public class AdaptivePurchasesFixedPID implements BidPricingStrategy{



    private final PurchasesFixedPID delegate;

    public AdaptivePurchasesFixedPID( PurchasesDepartment purchasesDepartment){

        delegate = new PurchasesFixedPID(purchasesDepartment);
        delegate.makeAdaptive();

    }

    public AdaptivePurchasesFixedPID( PurchasesDepartment purchasesDepartment, int specificTarget){
        delegate = new PurchasesFixedPID(purchasesDepartment,specificTarget);
        delegate.makeAdaptive();

    }

    public AdaptivePurchasesFixedPID( PurchasesDepartment purchasesDepartment, float proportionalGain, float integralGain,
                              float derivativeGain, int specificTarget){

        delegate = new PurchasesFixedPID(purchasesDepartment,proportionalGain,integralGain,derivativeGain,specificTarget);
        delegate.makeAdaptive();
    }


    /**
     * The adjust is the main part of the controller controller. It checks the new error and set the MV (which is the price, really)
     * @param simState MacroII object if I am worth anything as a programmer
     */
    
    public void step(SimState simState) {
        delegate.step(simState);
    }

    /**
     * Sets new The target inventory.
     *
     * @param inventoryTarget New value of The target inventory.
     */
    public void setInventoryTarget(int inventoryTarget) {
        delegate.setInventoryTarget(inventoryTarget);
    }

    /**
     * Returns the class of the controller.
     */
    public Class<? extends Controller> getKindOfController() {
        return delegate.getKindOfController();
    }

    public float getOffset() {
        return delegate.getOffset();
    }

    /**
     * Decorates the controller so that the input it actually receive is the moving average rather than the point itself
     * @param weight patience
     * @param position which input to filter
     */
    public void filterInputExponentially(float weight, ControllerInput.Position position) {
        delegate.filterInputExponentially(weight, position);
    }

    public void filterTargetExponentially(float weight, ControllerInput.Position position) {
        delegate.filterTargetExponentially(weight, position);
    }

    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     *  @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     * @param delta the difference from the previous quantity owned (always a positive number)
     */
    
    public void inventoryDecreaseEvent(HasInventory source, GoodType type, int quantity, int delta) {
        delegate.inventoryDecreaseEvent(source, type, quantity, delta);
    }

    /**
     * Sets new so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     * howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     *
     * @param howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch
     *         New value of so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     *         howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     */
    public void setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(float howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch) {
        delegate.setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch);
    }

    /**
     * This is the method overriden by the subclass of inventory control to decide whether the current inventory levels call for a new good being bought
     *
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    
    public boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {
        return delegate.shouldIBuy(source, type, quantity);
    }

    /**
     * Whenever set the controller is reset
     * @param initialPrice
     */
    public void setInitialPrice(float initialPrice) {
        delegate.setInitialPrice(initialPrice);
    }

    /**
     * Decorates the controller so that the output (u_t) is a moving average
     * @param weight patience
     */
    public void filterOutputExponentially(float weight) {
        delegate.filterOutputExponentially(weight);
    }

    /**
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible                      <br>
     * In addition the controller methods adjust their controller once.
     */
    
    public void start() {
        delegate.start();
    }

    /**
     * How much does control need?
     */
    public int getInventoryTarget() {
        return delegate.getInventoryTarget();
    }

    /**
     * This method is called by departments (plants usually) that need this input but found none. It is called
     *  @param source       the agent with the inventory
     * @param type         the good type demanded
     * @param numberNeeded how many goods were needed
     */
    
    public void failedToConsumeEvent(HasInventory source, GoodType type, int numberNeeded) {
        delegate.failedToConsumeEvent(source, type, numberNeeded);
    }

    /**
     * decorates the control with an auto-tuner to dynamically find P and I
     */
    public void makeAdaptive() {
        delegate.makeAdaptive();
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    
    public int estimateDemandGap() {
        return delegate.estimateDemandGap();
    }

    /**
     * Gets so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     * howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     *
     * @return Value of so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     *         howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     */
    public float getHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch() {
        return delegate.getHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch();
    }

    /**
     * Decorates the controller so that the input it actually receive is the moving average rather than the point itself
     * @param size patience
     * @param position
     */
    public void filterInputMovingAverage(int size, ControllerInput.Position position) {
        delegate.filterInputMovingAverage(size, position);
    }

    /**
     * This method returns the inventory control rating on the level of inventories. <br>
     * Implementation wise this is just a template method. It checks whether the inventory control is active or not.
     * If it is  it calls inventoryRating which is an abstract method implemented by subclasses
     * @return the rating on the inventory conditions or null if the department is not active.
     */
    public Level rateCurrentLevel() {
        return delegate.rateCurrentLevel();
    }

    public GoodType getGoodTypeToControl() {
        return delegate.getGoodTypeToControl();
    }

    /**
     * This controller strategy is always buying. It is using prices to control its inventory
     * @return true
     */
    
    public boolean canBuy() {
        return delegate.canBuy();
    }

    public void setSpeed(int speed) {
        delegate.setSpeed(speed);
    }

    /**
     * The abstract inventory control checks if it is active, if so it asks the implementing subclass whether or not to buy a good.
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     * @param delta the difference from the previous quantity owned (always a positive number)
     */
    
    public void inventoryIncreaseEvent(HasInventory source, GoodType type, int quantity, int delta) {
        delegate.inventoryIncreaseEvent(source, type, quantity, delta);
    }

    public boolean isActive() {
        return delegate.isActive();
    }

    /**
     * Calls super turnoff to kill all listeners and then set active to false
     * */
    
    public void turnOff() {
        delegate.turnOff();
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    
    public int maxPrice(Good good) {
        return delegate.maxPrice(good);
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    
    public int maxPrice(GoodType type) {
        return delegate.maxPrice(type);
    }
}
