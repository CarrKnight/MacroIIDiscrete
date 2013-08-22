/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.AskPricingStrategy;
import com.google.common.base.Preconditions;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.MovingAverage;
import model.utilities.pid.CascadePIDController;
import model.utilities.pid.ControllerFactory;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This ask pricing strategy is similar to {@link SalesControlWithFixedInventoryAndPID} in that
 * it tinkers with prices to target an inventory. This one tries to keep inventory level at the daily production rate.
 * It smooths inventory targets by looking at the Moving Average of daily production  (by default 10 days MA)
 * <p/> Code-wise, this delegates almost everything to a SalesControlWithFixedInventoryAndPID object.
 * <p/> This strategy learns about daily production very simply:
 * counts how many goods the sales department was asked to sell (that is, daily inflows in the sales department)
 * <p/> This strategy is steppable because at the beginning of the trading (PREPARE_TO_TRADE) it computes the new MA to pass to the
 * SalesControlWithFixedInventoryAndPID as target. I choose it to make it at PREPARE_TO_TRADE to lag one step behind the daily inflow
 * (which is reset at DAWN phase)
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-08
 * @see
 */
public class SmoothedDailyInventoryPricingStrategy implements AskPricingStrategy, Steppable
{

    /**
     * the moving average object used to smooth out inventory targets
     */
    private final MovingAverage<Integer> movingAverage;

    /**
     * This is the object that does the tinkering. We simply
     */
    private final SalesControlWithFixedInventoryAndPID delegate;

    /**
     * This is the reference to the sales department we are supposed to deal with
     */
    private final SalesDepartment salesDepartment;

    /**
     * the PID controller used by the delegate fixedInventory sales control. Useful to set gains and speed.
     */
    private final CascadePIDController controllerUsedByDelegate;

    /**
     * Creates the default SmoothedDailyInventoryPricingStrategy by creating the default SalesControlWithFixedInventoryAndPID
     * and adding a 10 period MA with 10 fake initial observations at 0;
     * Also starts stepping itself
     * @param salesDepartment the sales department to price for
     */
    public SmoothedDailyInventoryPricingStrategy(SalesDepartment salesDepartment) {
        this.salesDepartment = salesDepartment;

        //I am creating the PID controller here so that I can set the gains; I am sure it's a PID controller so I am sure the setGains() method exists
        controllerUsedByDelegate = ControllerFactory.buildController(CascadePIDController.class,
                salesDepartment.getFirm().getModel());

        delegate = new SalesControlWithFixedInventoryAndPID(salesDepartment,0,controllerUsedByDelegate);


        movingAverage = new MovingAverage<>(10);
        for(int i=0; i< movingAverage.getMovingAverageSize(); i++)
            movingAverage.addObservation(0);


        //step yourself
        salesDepartment.getFirm().getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);




    }


    /**
     * Reads in the daily inflow, computes the new average and sets it for the delegate.
     * @param state a MacroII object, used only in asserts and to reschedule itself
     */
    @Override
    public void step(SimState state)
    {


        assert ((MacroII)state).getCurrentPhase().equals(ActionOrder.PREPARE_TO_TRADE);
        Preconditions.checkState(salesDepartment.getTodayInflow() >=0, "Negative inflow is weird");

        movingAverage.addObservation(salesDepartment.getTodayInflow());

        delegate.setTargetInventory((int) movingAverage.getSmoothedObservation());

        ((MacroII)state).scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this);




    }

    /**
     * Resets the PID at this new price
     * @param price the new price (can't be a negative number)
     */
    public void setInitialPrice(long price) {
        delegate.setInitialPrice(price);
    }

    /**
     * Gets the sales department linked controlled by the object.
     *
     * @return Value of the sales department linked controlled by the object.
     */
    public SalesDepartment getDepartment() {
        return delegate.getDepartment();
    }

    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
        delegate.weekEnd();
    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
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
        return Math.max(delegate.price(g), 0);
    }

    /**
     * Gets how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     *
     * @return Value of how much inventory is correct to have. Any number below this should trigger the PID to raise prices, any number above this to lower them.
     */
    public int getTargetInventory() {
        return delegate.getTargetInventory();
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateSupplyGap() {
        return delegate.estimateSupplyGap();
    }

    /**
     * Set the sampling speed of the controller (how often it updates, in days)
     * @param samplingSpeed the sampling speed
     */
    public void setSpeed(int samplingSpeed) {
        delegate.setSpeed(samplingSpeed);
    }



    public int getSpeed() {
        return controllerUsedByDelegate.getSpeed();
    }



    /**
     * delegates
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return delegate.isInventoryAcceptable(inventorySize);
    }

}
