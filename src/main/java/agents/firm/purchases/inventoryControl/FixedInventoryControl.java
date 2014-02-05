/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.inventoryControl;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import goods.GoodType;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is the simplest possible inventory control. It ignores plants and only targets a fixed number of inventory.
 * <p/> This is useful for, say, market makers who need an inventory without actually having plants producing anything.
 * <p/> ACCEPTABLE levels are 100% of the target, Barely is between 50 and 100, too much is over
 * howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch%
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-06
 * @see
 */
public class FixedInventoryControl extends AbstractInventoryControl {


    /**
     * The target inventory
     */
    private int inventoryTarget;

    /**
     * so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     * howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch
     */
    private float howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch=2f;


    /**
     * return rates in respect to the inventory the firm has
     * @return the inventory level rating
     */
    @Nonnull
    @Override
    protected Level rateInventory() {
        //how much do we have?
        int currentLevel = getPurchasesDepartment().getCurrentInventory();
        return rateInventory(currentLevel);

    }

    /**
     * Just lik rateInventory() but with the inventory already queried
     * @param currentLevel  how much I have right now
     * @return the inventory level given what I have
     */
    @Nonnull
    private Level rateInventory(int currentLevel){
        if(currentLevel < .5f * inventoryTarget)
            return Level.DANGER;
        else if(currentLevel < inventoryTarget)
            return Level.BARELY;
        else if(currentLevel < howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch * inventoryTarget)
            return Level.ACCEPTABLE;
        else
        {
            assert currentLevel >= howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch* inventoryTarget;
            return Level.TOOMUCH;
        }
    }


    /**
     * This is the method overriden by the subclass of inventory control to decide whether the current inventory levels call for a new good being bought
     *
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    @Override
    protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {
        return rateInventory(quantity).compareTo(Level.ACCEPTABLE) <= 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * This is used by the purchases department for either debug or to ask the inventory control if
     * we can accept offer from peddlers.<br>
     * Asserts expect this to be consistent with the usual behavior of inventory control. But if asserts are off, then there is no other check
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return rateInventory().compareTo(Level.ACCEPTABLE) <= 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * Basic inventory control needs a link to the purchase department it needs to control (from where it can access the firm and the inventory).
     * IT also sets itself up to adjust at the next possible moment
     */
    public FixedInventoryControl(@Nonnull final PurchasesDepartment purchasesDepartment) {
        this(purchasesDepartment,purchasesDepartment.getFirm().getModel().drawFixedInventoryTarget());

    }

    /**
     * Basic inventory control needs a link to the purchase department it needs to control (from where it can access the firm and the inventory).
     * IT also sets itself up to adjust at the next possible moment
     */
    public FixedInventoryControl(@Nonnull final PurchasesDepartment purchasesDepartment, int specificTarget) {
        super(purchasesDepartment);
        inventoryTarget = specificTarget;

    }

    /**
     * How much does control need?
     */
    public int getInventoryTarget() {
        return inventoryTarget;
    }


    /**
     * Gets so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     * howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     *
     * @return Value of so if you have more inventory than the target, when is that TOO MUCH? It is decided by
     *         howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch.
     */
    public float getHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch() {
        return howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch;
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
        Preconditions.checkArgument(howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch >= 1f, "the argument has to be above 1!");

                this.howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch = howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch;
    }


    /**
     * Sets new The target inventory.
     *
     * @param inventoryTarget New value of The target inventory.
     */
    public void setInventoryTarget(int inventoryTarget) {
        this.inventoryTarget = inventoryTarget;
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap() {
        //if for whatever reason there was nothing to buy left today, we can't really use today's trade as a decent observation on what the real supply is
        try{
        if(getPurchasesDepartment().getMarket().getBestSeller() == null && getPurchasesDepartment().getTodayInflow() > 0

                ||
                getPurchasesDepartment().getTodayInflow() == 0 && !getPurchasesDepartment().canBuy())
            //or another useless observation is when you bought nothing because you needed nothing
            return 1000;
        }
        catch (IllegalAccessException e){}
        if(rateInventory().equals(Level.ACCEPTABLE))
            return  0;
        else
        {
            int currentInventory = getPurchasesDepartment().getCurrentInventory();
            if( currentInventory< inventoryTarget)
                return  currentInventory-inventoryTarget;
            else
                return currentInventory - Math.round(inventoryTarget *howManyTimesOverInventoryHasToBeOverTargetToBeTooMuch);

        }
    }
}
