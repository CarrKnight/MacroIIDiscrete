/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.BaseAskPricingStrategy;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.Filter;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> The idea of this class is to create a simple flow seller pid that:
 * <ul>
 *     <li>Uses inventory rather than stockouts</li>
 *     <li> Targets sales = inflows when inventory are acceptable</li>
 *     <li> Targets no sales when inventory is below a minimum value</li>
 * </ul>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-19
 * @see
 */
public class SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly  extends BaseAskPricingStrategy implements Steppable {

    private final SalesDepartment department;


    /**
     * Inventory below which the firm sets itself to sell nothing
     */
    private int minimumInventory = 10;

    /**
     * When this inventory is reached, the
     */
    private int acceptableInventory = 50;

    /**
     * are we building up inventories or can we sell?
     */
    private SimpleInventoryAndFlowPIDPhase phase = SimpleInventoryAndFlowPIDPhase.BUILDUP;

    /**
     * The PID controller that deals with it
     */
    private PIDController controller;

    /**
     * if given, this will filter the outflow from the sales department to deal with infrequent changes
     */
    private Filter<Integer> outflowFilter =null;

    /**
     * is the strategy active?
     */
    private boolean isActive;


                                              /*
                                               float proportionalGain =
        float integralGain = ;
        float derivativeGain = ;
                                               */


    /**
     * Build a simple inventory and flow pid with preset inventory levels
     * @param department the sales department to inform
     */
    public SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly(SalesDepartment department) {
        this(department,10,200,department.getFirm().getModel(),
                department.getFirm().getModel().drawProportionalGain()/5f,
                department.getFirm().getModel().drawIntegrativeGain()/5f,
                department.getFirm().getModel().drawDerivativeGain(),
                department.getRandom());
    }

    /**
     * The same constructor as above, just with everything spelled out. The sales department starts its price at a random number between 50 and 100
     * @param department the sales department to use
     * @param minimumInventory the minimum inventory below which the department goes into buildup
     * @param acceptableInventory the inventory level above which the firm stops building up and starts selling
     * @param state the link to the model, to schedule itself
     * @param proportionalGain the P of the PID
     * @param integrativeGain the I of the PID
     * @param derivativeGain  the D of the PID
     * @param random the randomizer
     */
    public SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly(SalesDepartment department, int minimumInventory, int acceptableInventory,
                                                                      MacroII state, float proportionalGain, float integrativeGain, float derivativeGain,
                                                                      MersenneTwisterFast random) {
        this.department = department;
        this.minimumInventory = minimumInventory;
        this.acceptableInventory = acceptableInventory;
        controller = new PIDController(proportionalGain,integrativeGain,derivativeGain,random);
        controller.setOffset(50+ random.nextInt(51));
        isActive=true;
        state.scheduleSoon(ActionOrder.ADJUST_PRICES,this);

    }

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public long price(Good g) {

        return getPrice(); //always teh same price
    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {

        super.turnOff();
        isActive=false;
    }

    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
    }

    /**
     * The step in this strategy does two things:
     * <ul>
     *     <li>
     *         Updates the phase (check if inventories need to be building up or we are ready to sell)
     *     </li>
     *     <li>
     *         Tell the PID to update!
     *     </li>
     * </ul>
     * @throws IllegalStateException if the step is called in any other phase other thank think (we need to make it
     * choose price at ADJUST_PRICES because at that point we know clearly the inflow and outflow)
     * @throws IllegalArgumentException if the state passed is not an instance of MacroII
     * @param state
     */
    @Override
    public void step(SimState state)
    {
        Preconditions.checkArgument(state instanceof MacroII);
        Preconditions.checkState(((MacroII) state).getCurrentPhase().equals(ActionOrder.ADJUST_PRICES) );
        long oldprice =getPrice();
        if(!isActive)
            return;

        //get today ouflow (smoothed if you attached a filter)
        float outflow = getFilteredOutflow();


        //change phase
        switch (phase)
        {
            case BUILDUP:
                if(department.getHowManyToSell() >= acceptableInventory)  //switch to sell if you are above acceptable
                    phase = SimpleInventoryAndFlowPIDPhase.SELL;
                break;
            case SELL:
                if(department.getHowManyToSell() < minimumInventory)  //switch to buildup if you are below minimum
                    phase = SimpleInventoryAndFlowPIDPhase.BUILDUP;
                break;
        }



       // System.out.println("ouflow: " + outflow +", target: " + getTarget() + " ----> " + controller.getCurrentMV());


        controller.adjustOnce(outflow-getTarget(),isActive);

        handleNewEvent(new LogEvent(this, LogLevel.INFO, "inventory: {}, outlow:{}, target:{}\n whichphase? {}, oldPrice: {}, newprice{}",
                department.getHowManyToSell(),outflow,getTarget(),phase,oldprice,controller.getCurrentMV()));



        if(getSpeed()==0)
            ((MacroII) state).scheduleTomorrow(ActionOrder.ADJUST_PRICES,this);
        else
        {
            assert getSpeed() > 1;
            ((MacroII) state).scheduleAnotherDay(ActionOrder.ADJUST_PRICES,this,getSpeed());
        }


        //update if needed
        if(getPrice() != oldprice)
            department.updateQuotes();

    }

    public float getFilteredOutflow() {
        return outflowFilter == null ? department.getTodayOutflow() : outflowFilter.getSmoothedObservation();
    }


    /**
     * Gets Inventory below which the firm sets itself to sell nothing.
     *
     * @return Value of Inventory below which the firm sets itself to sell nothing.
     */
    public int getMinimumInventory() {
        return minimumInventory;
    }

    /**
     * Sets new When this inventory is reached, the.
     *
     * @param acceptableInventory New value of When this inventory is reached, the.
     */
    public void setAcceptableInventory(int acceptableInventory) {
        this.acceptableInventory = acceptableInventory;
    }

    /**
     * Gets When this inventory is reached, the.
     *
     * @return Value of When this inventory is reached, the.
     */
    public int getAcceptableInventory() {
        return acceptableInventory;
    }

    /**
     * Sets new Inventory below which the firm sets itself to sell nothing.
     *
     * @param minimumInventory New value of Inventory below which the firm sets itself to sell nothing.
     */
    public void setMinimumInventory(int minimumInventory) {
        this.minimumInventory = minimumInventory;
    }


    public int getTargetInventory()
    {
        return getTarget();
    }

    public int getTarget() {

        if(phase.equals(SimpleInventoryAndFlowPIDPhase.BUILDUP))
            return 0;
        else
            return department.getTodayInflow();

    }

    public SimpleInventoryAndFlowPIDPhase getPhase() {
        return phase;
    }


    /**
     * acceptable as long as we have minimum inventory
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return inventorySize >= minimumInventory;
    }


    /**
     * All inventory is unwanted
     */
    @Override
    public float estimateSupplyGap() {
        int currentInventory = department.getHowManyToSell();
        if(phase.equals(SimpleInventoryAndFlowPIDPhase.SELL) && department.getHowManyToSell() >0 )
        {
            return 0;
        }
        else
        {
            return 1000; //ignore not sell.
        }
    }

    /**
     * since this controller puts the same price for every good, we can use this rather than passing a good
     * @return
     */
    public long getPrice() {
        return Math.round(controller.getCurrentMV());

    }

    /**
     * Set the price of the sales department; it really delegates to set offset of the pid.
     * @param price a positive or 0
     */
    public void setInitialPrice(long price) {
        Preconditions.checkArgument(price>=0);

        controller.setOffset(price);

    }

    /**
     * with this we know if we are in buildup phase or sell phase
     */
    public enum SimpleInventoryAndFlowPIDPhase {

        BUILDUP, //the phase the strategy is in as it tries to sell nothing and rebuild its inventory

        SELL //when you can finally sell your stuff!

    }

    /**
     * Change the gains of the PID
     */
    public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
        controller.setGains(proportionalGain, integralGain, derivativeGain);
    }

    public int getSpeed() {
        return controller.getSpeed();
    }

    public void setSpeed(int speed) {
        controller.setSpeed(speed);
    }

    public void setDerivativeGain(float derivativeGain) {
        controller.setDerivativeGain(derivativeGain);
    }

    public float getDerivativeGain() {
        return controller.getDerivativeGain();
    }

    public void setIntegralGain(float integralGain) {
        controller.setIntegralGain(integralGain);
    }

    public float getIntegralGain() {
        return controller.getIntegralGain();
    }

    public void setProportionalGain(float proportionalGain) {
        controller.setProportionalGain(proportionalGain);
    }

    public float getProportionalGain() {
        return controller.getProportionalGain();
    }


    public void attachFilter(final Filter<Integer> outflowFilter)
    {

        //set it
        this.outflowFilter = outflowFilter;
        outflowFilter.addObservation(department.getTodayOutflow());
        //schedule it
        department.getFirm().getModel().scheduleSoon(ActionOrder.THINK,new Steppable() {
            @Override
            public void step(SimState state) {
                if(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.this.outflowFilter != outflowFilter) //stop if you changed filter
                    return;

                //add observation
                outflowFilter.addObservation(department.getTodayOutflow());

                //reschedule yourself
                department.getFirm().getModel().scheduleTomorrow(ActionOrder.THINK,this);


            }
        }

        );

    }

    public SalesDepartment getDepartment() {
        return department;
    }
}
