/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import goods.GoodType;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This interface is used by strategies and agents that react to changes in the inventory.
 * <p/> The way I expect it to work is that you'll be able to register the listener for a list of goodTypes.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-03
 * @see
 */
public interface InventoryListener {


    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has increased
     * @param source the agent with the inventory that is calling the listener
     * @param type which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    public void inventoryIncreaseEvent(@Nonnull HasInventory source,@Nonnull GoodType type, int quantity);


    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     * @param source the agent with the inventory that is calling the listener
     * @param type which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    public void inventoryDecreaseEvent(@Nonnull HasInventory source,@Nonnull GoodType type, int quantity);

    /**
     * This method is called by departments (plants usually) that need this input but found none. It is called
     * @param source the agent with the inventory
     * @param type the good type demanded
     * @param numberNeeded how many goods were needed
     */
    public void failedToConsumeEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int numberNeeded);

}
