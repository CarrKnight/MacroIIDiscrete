/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.inventoryControl;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import goods.GoodType;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is the simplest possible inventory control. It ignores plants and only targets a fixed number of inventory.
 * <p/> This is useful for, say, market makers who need an inventory without actually having plants producing anything.
 * <p/> ACCEPTABLE levels are 100% of the target, Barely is between 50 and 100, too much is over 150%
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

    final int target;


    /**
     * return rates in respect to the inventory the firm has
     * @return the inventory level rating
     */
    @Nonnull
    @Override
    protected Level rateInventory() {
        //how much do we have?
        int currentLevel = getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl());
        return rateInventory(currentLevel);

    }

    /**
     * Just lik rateInventory() but with the inventory already queried
     * @param currentLevel  how much I have right now
     * @return the inventory level given what I have
     */
    @Nonnull
    private Level rateInventory(int currentLevel){
        if(currentLevel < .5f *  target)
            return Level.DANGER;
        else if(currentLevel <  target)
            return Level.BARELY;
        else if(currentLevel < 1.5f * target )
            return Level.ACCEPTABLE;
        else
        {
            assert currentLevel >= 1.5f*target;
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
        return rateInventory(quantity).compareTo(Level.ACCEPTABLE) < 0; //keep buying as long as it is not at acceptable levels
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
        return rateInventory().compareTo(Level.ACCEPTABLE) < 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * Basic inventory control needs a link to the purchase department it needs to control (from where it can access the firm and the inventory).
     * IT also sets itself up to adjust at the next possible moment
     */
    public FixedInventoryControl(@Nonnull final PurchasesDepartment purchasesDepartment) {
        super(purchasesDepartment);
        target = purchasesDepartment.getFirm().getModel().drawFixedInventoryTarget();

    }

    /**
     * Basic inventory control needs a link to the purchase department it needs to control (from where it can access the firm and the inventory).
     * IT also sets itself up to adjust at the next possible moment
     */
    public FixedInventoryControl(@Nonnull final PurchasesDepartment purchasesDepartment, int specificTarget) {
        super(purchasesDepartment);
        target = specificTarget;

    }

    /**
     * How much does control need?
     */
    public int getTarget() {
        return target;
    }
}
