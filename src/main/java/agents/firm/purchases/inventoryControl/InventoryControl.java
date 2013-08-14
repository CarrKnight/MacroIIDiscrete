/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.inventoryControl;

import agents.InventoryListener;
import model.utilities.Control;
import model.utilities.Deactivatable;

import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/> Inventory control has two main tasks:
 * <ul>
 *     <li>
 *         To order the purchase department to buy stuff.
 *     </li>
 *     <li>
 *          To answer when asked to judge the overall level of inventory
 *     </li>
 * <p/> To work it has to have a single constructor with "PurchasesDepartment" there.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-04
 * @see
 */
public interface InventoryControl extends InventoryListener, Deactivatable, Control {

    /**
     * This method returns the inventory control rating on the level of inventories. <br>
     * @return the rating on the inventory conditions or null if the department is not active.
     */
    @Nullable
    public Level rateCurrentLevel();


    /**
     * Call this if we change/remove the inventory control to stop it from giving more orders.Turn off is irreversible
     */
    public void turnOff();

    /**
     * This is used by the purchases department for either debug or to ask the inventory control if
     * we can accept offer from peddlers.<br>
     * Asserts expect this to be consistent with the usual behavior of inventory control. But if asserts are off, then there is no other check
     * @return
     */
    public boolean canBuy();


    /**
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    public void start();


}
