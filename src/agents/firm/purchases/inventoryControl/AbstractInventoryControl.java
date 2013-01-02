package agents.firm.purchases.inventoryControl;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import goods.GoodType;
import model.utilities.pid.ControllerInput;

import javax.annotation.Nonnull;
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
 * </ul>
 * <p/> This abstract class provides a simple skeletal implementation of the interface InventoryControl so that subclasses only need to implement 2 methods to get up and running
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-04
 * @see agents.InventoryListener
 */
public abstract class AbstractInventoryControl implements InventoryControl{

    @Nonnull
    private PurchasesDepartment purchasesDepartment;

    private final GoodType goodTypeToControl;

    private boolean isActive = true;

    /**
     * A simple counter for all new goods
     */
    private int inflow =0;

    /**
     * A simple counter for all new goods
     */
    private int outflow =0;


    /**
     * Basic inventory control needs a link to the purchase department it needs to control (from where it can access the firm and the inventory).
     * IT also sets itself up to adjust at the next possible moment
     */
    public AbstractInventoryControl(@Nonnull final PurchasesDepartment purchasesDepartment) {
        this.purchasesDepartment = purchasesDepartment;
        this.goodTypeToControl = purchasesDepartment.getGoodType();
        purchasesDepartment.getFirm().addInventoryListener(this);  //addSalesDepartmentListener yourself as inventory listener

        //adjust yourself to start buying


    }

    /**
     * This method returns the inventory control rating on the level of inventories. <br>
     * Implementation wise this is just a template method. It checks whether the inventory control is active or not.
     * If it is  it calls inventoryRating which is an abstract method implemented by subclasses
     * @return the rating on the inventory conditions or null if the department is not active.
     */
    @Nullable
    final public Level rateCurrentLevel(){

        if(!isActive)
            return null;
        else
            return rateInventory();

    }


    /**
     * This is the method to be overriden by the subclasses so that the public method rateCurrentLevel is meaningful
     * @return the inventory level rating
     */
    @Nonnull
    abstract protected Level rateInventory();


    /**
     * The abstract inventory control checks if it is active, if so it asks the implementing subclass whether or not to buy a good.
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    @Override
    public void inventoryIncreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {

        if(!isActive || type != goodTypeToControl) //if you have been turned off or this isn't the good you are controlling for, don't bother
            return;

        //count it
        inflow++;


        assert source == purchasesDepartment.getFirm(); //should be our own firm, otherwise it's weird
//        assert goodTypeToControl == type; //we should have been notified only of stuff that relates to us
        assert getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()) == quantity; //make sure the quantity supplied is valid


        if(shouldIBuy(source,type,quantity)) //if I should buy, buy
            purchasesDepartment.buy();
        else
            System.err.println("No need to buy!~");

    }

    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     *
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    @Override
    public void inventoryDecreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {

        if(!isActive) //if you have been turned off, don't bother
            return;

        //count it
        outflow++;


        assert source == purchasesDepartment.getFirm(); //should be our own firm, otherwise it's weird
        assert goodTypeToControl == type; //we should have been notified only of stuff that relates to us
        assert getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()) == quantity; //make sure the quantity supplied is valid


        if(shouldIBuy(source,type,quantity)) //if I should buy, buy
            purchasesDepartment.buy();
        else
            System.err.println("No need to buy!~");


    }

    /**
     * This method is called by departments (plants usually) that need this input but found none. It is called
     *
     * @param source       the agent with the inventory
     * @param type         the good type demanded
     * @param numberNeeded how many goods were needed
     */
    @Override
    public void failedToConsumeEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int numberNeeded) {
        outflow= outflow + numberNeeded; //count it as outflow

    }

    /**
     * This is the method overriden by the subclass of inventory control to decide whether the current inventory levels call for a new good being bought
     * @param source The firm
     * @param type The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    abstract protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity);


    /**
     * Turns off the inventory control. Useful if we are going out of business or being replaced.
     */
    public void turnOff(){
        isActive = false; //remember you are off
        boolean stopped = purchasesDepartment.getFirm().removeInventoryListener(this);  //stop listening.
        //assert stopped; //make sure!


    }


    /**
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    @Override
    public void start() {
        if(canBuy())
            purchasesDepartment.buy();

    }

    @Nonnull
    protected PurchasesDepartment getPurchasesDepartment() {
        return purchasesDepartment;
    }

    public GoodType getGoodTypeToControl() {
        return goodTypeToControl;
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * The number of goods bought since the last reset
     */
    protected int getInflow() {
        return inflow;
    }

    protected void resetInflow(){
        inflow = 0;
    }

    public int getOutflow() {
        return outflow;
    }


    protected void resetOutflow(){
        outflow=0;
    }



    /**
     * If invertInputs is active, the first input is the flow and the second is the stock
     */
    private boolean invertInputs = false;

    /**
     * If invertTargets is active, the first target is the flow and the second is the stock
     */
    private boolean invertTargets = false;


    /**
     * A simple utility method creating an controller input object having at position 0 stock and at position 1 flows both as input and targets.
     * They can be switched through invertInputs and invertTargets flags. <br>
     * Also, target position 2 is 0 if the stock is acceptable and 1 otherwise
     * @return the controller input
     */
    protected ControllerInput getControllerInput(float target) {
        ControllerInput.ControllerInputBuilder inputBuilder =  new ControllerInput.ControllerInputBuilder();
        //prepare inputs
        if(invertInputs)
            inputBuilder.inputs((float)getInflow(),
                    (float)getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()));
        else
            inputBuilder.inputs((float)getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()),
                    (float)getInflow());
        resetInflow();

        //prepare targets
        if(invertTargets)
            inputBuilder.targets((float)getOutflow(),target);
        else
            inputBuilder.targets(target,(float)getOutflow());
        resetOutflow();



        float acceptable = rateInventory().equals(Level.ACCEPTABLE) ? 0f : -1f;

        inputBuilder.targets(acceptable);


        return inputBuilder.build();
    }

    public boolean isInvertInputs() {
        return invertInputs;
    }

    public void setInvertInputs(boolean invertInputs) {
        this.invertInputs = invertInputs;
    }

    public boolean isInvertTargets() {
        return invertTargets;
    }

    public void setInvertTargets(boolean invertTargets) {
        this.invertTargets = invertTargets;
    }
}
