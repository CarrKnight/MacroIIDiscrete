package model.utilities;

import agents.firm.purchases.inventoryControl.Level;

import javax.annotation.Nullable;

/**
 * <h4>Description</h4>
 * <p/>  This is just the inteface both Inventory and Plant control inherit from. It's used by all control problems to remind me that they are one and the saem
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-20
 * @see
 */
public interface Control {


    /**
     * This method returns the control rating on current stock held <br>
     * @return the rating on the current stock conditions or null if the department is not active.
     */
    @Nullable
    public Level rateCurrentLevel();

    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    public void turnOff();

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     * @return
     */
    public boolean canBuy();


    /**
     * When instantiated the control doesn't move until it receives a stimulus OR start() is called. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    public void start();
}
