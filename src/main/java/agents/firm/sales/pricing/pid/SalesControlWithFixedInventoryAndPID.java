/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.BaseAskPricingStrategy;
import com.google.common.base.Preconditions;
import financial.MarketEvents;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.CascadePToPIDController;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerFactory;
import model.utilities.pid.ControllerInput;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This is a very generic sales department control that doesn't count stockouts but rather just tinkers with the price
 * to keep a fixed inventory level above 0.
 * <p/> By default this level is 5. But it can be set at any point during runtime. "Fixed inventory" refers to the fact that
 * the control itself doesn't change its targets, but any external object can set it
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-06
 * @see
 */
public class SalesControlWithFixedInventoryAndPID extends BaseAskPricingStrategy implements Steppable
{


    /**
     * how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them
     */
    private int targetInventory = defaultTargetInventory;


    private int roundedPrice;

    private float slavePIDOriginalError = 0;

    public static final int defaultTargetInventory =  100;

    /**
     * A controller to change prices as inventory changes. By default it's a cascade control
     */
    private final CascadePToPIDController controller;


    /**
     * the sales department linked controlled by the object
     */
    private final SalesDepartment department;

    public SalesControlWithFixedInventoryAndPID(SalesDepartment department) {

        this(department,defaultTargetInventory); //default to 5 units of inventory
    }


    /**
     * The sales department with an initial target inventory and a PID controller
     * @param department the sales department that is controlled by this strategy
     * @param targetInventory the inventory to target
     */
    public SalesControlWithFixedInventoryAndPID(SalesDepartment department, int targetInventory)
    {
        this(department,targetInventory, CascadePToPIDController.class);
        //by default, set it proportional


    }

    /**
     * The full constructor with Sales department
     * @param department the sales department to link to
     * @param targetInventory the target inventory of this control
     * @param controllerType the type of controller to use
     */
    public SalesControlWithFixedInventoryAndPID(SalesDepartment department,int targetInventory,
                                                Class<? extends CascadePToPIDController> controllerType )
    {
        //use default constructor
        this(department,targetInventory,ControllerFactory.
                buildController(controllerType,department.getFirm().getModel()));



    }
    /**
     * The full constructor with Sales department  and an instantiated controller
     * @param department the sales department to link to
     * @param targetInventory the target inventory of this control
     * @param controller the type of controller to use
     */
    public SalesControlWithFixedInventoryAndPID(SalesDepartment department, int targetInventory,CascadePToPIDController controller) {
        this.controller = controller;
        this.department = department;
        this.targetInventory = targetInventory;
        department.getFirm().getModel().scheduleSoon(ActionOrder.ADJUST_PRICES,this);

        roundedPrice = Math.round(controller.getCurrentMV());

    }


    /**
     * The full constructor with Sales department  and an instantiated controller
     * @param department the sales department to link to
     * @param targetInventory the target inventory of this control
     *
     */
    public SalesControlWithFixedInventoryAndPID(SalesDepartment department, int targetInventory,
                                                float proportionalGain,float integrativeGain, float derivativeGain) {
        //use default constructor
        this(department,targetInventory,ControllerFactory.buildController(CascadePToPIDController.class,department.getFirm().getModel()));
        controller.setGainsSlavePID(proportionalGain,integrativeGain,derivativeGain);


    }

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public int price(Good g) {
        return price();
    }


    /**
     * the PID MV rounded to the nearest integer
     *
     */
    public int price(){
        return roundedPrice;

    }


    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
    }

    /**
     * this step manages the controller. It runs every ADJUST_PRICES phase
     * @param state the MacroII object
     */
    @Override
    public void step(SimState state)
    {
        MacroII macroII = (MacroII) state;
        Preconditions.checkState(macroII.getCurrentPhase().equals(ActionOrder.ADJUST_PRICES), "I wanted to act on ADJUST_PRICES, " +
                "but I acted on " + macroII.getCurrentPhase());
        //run the controller to adjust prices
        //notice that i use todayOutflow as a measure of outflow because it gets reset at PREPARE_TO_TRADE and
        // I am calling this at ADJUST_PRICES (which is after TRADE, which is after PREPARE_TO_TRADE)
        ControllerInput input = getControllerInput();
        //memorize old price before adjustment (this is so that if there is a change, we update old quotes)
        long oldPrice =  (long)Math.round(controller.getCurrentMV());

        //first PID tries to deal with difference in inventory
        //second PID is fed in AS INPUT the difference in flows, the first PID is only a P so when the inventory is of the right size
        //the first PID feeds in to the second the target 0, which means that inflows and outflows have to equalize
        slavePIDOriginalError = input.getFlowTarget()-input.getFlowInput();
        controller.adjust(input,
                department.getFirm().isActive(), macroII, this, ActionOrder.ADJUST_PRICES);
        roundedPrice = Math.round(controller.getCurrentMV());

        //log it
        handleNewEvent(new LogEvent(this, LogLevel.DEBUG, "old price:{} newprice:{}\n inventory:{} targetInventory{}\n inflow:{} outflow{}",
                oldPrice,roundedPrice,department.getHowManyToSell(),targetInventory,department.getTodayInflow(),department.getTodayOutflow()));



        if(oldPrice != roundedPrice )
            department.updateQuotes();


    }


    /**
     * Gets how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     *
     * @return Value of how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     */
    public int getTargetInventory() {
        return targetInventory;
    }

    /**
     * Sets new how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     *
     * @param targetInventory New value of how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     */
    public void setTargetInventory(int targetInventory) {
        this.targetInventory = targetInventory;
        getDepartment().getFirm().logEvent(getDepartment(), MarketEvents.CHANGE_IN_TARGET,getDepartment().getFirm().getModel().getCurrentSimulationTimeInMillis(),"new target inventory: " + targetInventory);
    }

    /**
     * Resets the PID at this new price
     * @param price the new price (can't be a negative number)
     */
    public void setInitialPrice(long price) {
        Preconditions.checkArgument(price >= 0);
        controller.setOffset(price, true);
        roundedPrice = Math.round(controller.getCurrentMV());
        department.updateQuotes();
    }


    /**
     * Gets the sales department linked controlled by the object.
     *
     * @return Value of the sales department linked controlled by the object.
     */
    public SalesDepartment getDepartment() {
        return department;
    }




    /**
     * Set the sampling speed of the controller (how often it updates, in days)
     * @param samplingSpeed the sampling speed
     */
    public void setSpeed(int samplingSpeed) {
        controller.setSpeed(samplingSpeed);
    }

    /**
     * tries to sell everything
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return inventorySize >= targetInventory;
    }


    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public float estimateSupplyGap()
    {

        //return department.getHowManyToSell() - targetInventory;

        if(department.getHowManyToSell() == 0)
            return 100;
        else
        {
            return 0;
        }
        //return Math.round(controller.getMasterMV() + department.getTodayInflow() - department.getTodayOutflow());

    }

    public Controller getController() {
        return controller;
    }

    /**
     * Generic controller input to feed in the generic controller (so it's not only PID)
     * @return controller input object, good for all controllers
     */
    private ControllerInput getControllerInput()
    {
        return new ControllerInput(department.getTodayInflow(),getTargetInventory(),department.getTodayOutflow(),department.getHowManyToSell());


    }

    public float getProportionalGain() {

        return controller.getProportionalGain();

    }


    /**
     * Change the gains of the second PID
     * @param proportionalGain
     * @param integralGain
     * @param derivativeGain
     */
    public void setGainsSlavePID(float proportionalGain, float integralGain, float derivativeGain) {
        controller.setGainsSlavePID(proportionalGain, integralGain, derivativeGain);
    }

    public float getIntegralGain() {

        return controller.getIntegralGain();

    }

    public float getDerivativeGain() {
        return controller.getDerivativeGain();
    }
}
