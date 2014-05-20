/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import agents.HasInventory;
import goods.GoodType;


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
     * @param delta by how much did the inventory change (always positive!)
     */
    public void inventoryIncreaseEvent( HasInventory source, GoodType type, int quantity, int delta);


    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     * @param source the agent with the inventory that is calling the listener
     * @param type which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     * @param delta by how much did the inventory change (always positive!)
     */
    public void inventoryDecreaseEvent( HasInventory source, GoodType type, int quantity, int delta);

    /**
     * This method is called by departments (plants usually) that need this input but found none. It is called
     * @param source the agent with the inventory
     * @param type the good type demanded
     * @param numberNeeded how many goods were needed
     */
    public void failedToConsumeEvent( HasInventory source,  GoodType type, int numberNeeded);

}
