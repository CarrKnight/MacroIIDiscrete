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
import model.utilities.filters.ExponentialFilter;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogListener;
import model.utilities.logs.Loggable;
import model.utilities.pid.CascadePToPIDController;
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
public class salesControlWithSmoothedinventoryAndPID implements AskPricingStrategy, Steppable
{

    /**
     * the moving average object used to smooth out inventory targets
     */
    private final ExponentialFilter<Integer> movingAverage;

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
    private final CascadePToPIDController controllerUsedByDelegate;

    private int howManyTimesTheDailyInflowShouldTheInventoryBe = 10;

    /**
     * Creates the default SmoothedDailyInventoryPricingStrategy by creating the default SalesControlWithFixedInventoryAndPID
     * and adding a 10 period MA with 10 fake initial observations at 0;
     * Also starts stepping itself
     * @param salesDepartment the sales department to price for
     */
    public salesControlWithSmoothedinventoryAndPID(SalesDepartment salesDepartment) {
        this.salesDepartment = salesDepartment;

        //I am creating the PID controller here so that I can set the gains; I am sure it's a PID controller so I am sure the setGains() method exists
        controllerUsedByDelegate = ControllerFactory.buildController(CascadePToPIDController.class,
                salesDepartment.getFirm().getModel());

        delegate = new SalesControlWithFixedInventoryAndPID(salesDepartment,0,controllerUsedByDelegate);
        assert delegate.getController().equals(controllerUsedByDelegate);
        assert controllerUsedByDelegate.getMasterProportionalGain() > 0;
        assert controllerUsedByDelegate.getMasterIntegralGain() ==0;
        assert controllerUsedByDelegate.getMasterDerivativeGain() == 0;


        movingAverage = new ExponentialFilter<>(.1f);
      //  for(int i=0; i< movingAverage.getMovingAverageSize(); i++)
        movingAverage.addObservation(0);


        //step yourself
        salesDepartment.getFirm().getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);


        //parameters found through genetic algorithm
        setMasterProportionalGain(0.035f);

      /*  float proportionalGain = 0.04954876f + ((float) salesDepartment.getRandom().nextGaussian()) / 100f;
        float integralGain = 0.45825003f + ((float) salesDepartment.getRandom().nextGaussian()) / 100f;
        float derivativeGain = 0.000708338f + ((float) salesDepartment.getRandom().nextGaussian() / 10000f);
        */
        setGainsSlavePID(salesDepartment.getModel().drawProportionalGain()/5,
                salesDepartment.getModel().drawIntegrativeGain()/5,
                salesDepartment.getModel().drawDerivativeGain());

        //System.out.println(proportionalGain + " - " + integralGain + " - " + derivativeGain);





    }

    /**
     * the gains refer to the SLAVE (velocity) PID
     */
    public salesControlWithSmoothedinventoryAndPID(SalesDepartment salesDepartment, float proportionalGain,
                                                   float integrativeGain, float derivativeGain)
    {
        this(salesDepartment);
        //scale the master proportional too
        float originalScale = controllerUsedByDelegate.getMasterProportionalGain()/controllerUsedByDelegate.getProportionalGain();



        controllerUsedByDelegate.setGainsSlavePID(proportionalGain,integrativeGain,derivativeGain);

        assert controllerUsedByDelegate.getMasterProportionalGain() > 0;
        assert controllerUsedByDelegate.getMasterIntegralGain() ==0;
        assert controllerUsedByDelegate.getMasterDerivativeGain() == 0;

        controllerUsedByDelegate.setProportionalGain(proportionalGain * originalScale);



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

        movingAverage.addObservation(salesDepartment.getTodayInflow()* howManyTimesTheDailyInflowShouldTheInventoryBe);

        delegate.setTargetInventory((int) movingAverage.getSmoothedObservation());
        //     System.out.println("target inventory: " + getTargetInventory() + ", actual inventory: " + salesDepartment.getHowManyToSell());

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
    public int price(Good g) {
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
    public float estimateSupplyGap() {
        //percentile distance from target inventory
        /*if(delegate.getTargetInventory() != 0)
            return (100*(delegate.getTargetInventory() - salesDepartment.getHowManyToSell()))/delegate.getTargetInventory();
        else
        if(salesDepartment.getHowManyToSell() == 0)
            return  0;
        else
            return -100;*/
        if(salesDepartment.getHowManyToSell() == 0)
            return controllerUsedByDelegate.getMasterError();
        else

            return (delegate.estimateSupplyGap());
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


    /**
     * Change the gains of the second PID
     */
    public void setGainsSlavePID(float proportionalGain, float integralGain, float derivativeGain) {
        controllerUsedByDelegate.setGainsSlavePID(proportionalGain, integralGain, derivativeGain);
    }

    public void setMasterProportionalGain(float proportionalGain) {
        controllerUsedByDelegate.setMasterProportionalGain(proportionalGain);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmoothedDailyInventoryPricingStrategy{");
        sb.append("controllerUsedByDelegate=").append(controllerUsedByDelegate);
        sb.append('}');
        return sb.toString();
    }

    public int getHowManyTimesTheDailyInflowShouldTheInventoryBe() {
        return howManyTimesTheDailyInflowShouldTheInventoryBe;
    }

    public void setHowManyTimesTheDailyInflowShouldTheInventoryBe(int howManyTimesTheDailyInflowShouldTheInventoryBe) {
        this.howManyTimesTheDailyInflowShouldTheInventoryBe = howManyTimesTheDailyInflowShouldTheInventoryBe;
    }

    /**
     * Get the proportional gain of the first PID
     * @return the proportional gain of the first PID
     */
    public float getMasterProportionalGain() {
        return controllerUsedByDelegate.getMasterProportionalGain();
    }

    /**
     * Get the integral gain of the first PID
     * @return the integral gain of the first PID
     */
    public float getMasterIntegralGain() {
        return controllerUsedByDelegate.getMasterIntegralGain();
    }

    /**
     * Get the derivative gain of the first PID
     * @return the derivative gain of the first PID
     */
    public float getMasterDerivativeGain() {
        return controllerUsedByDelegate.getMasterDerivativeGain();
    }


    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return delegate.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return delegate.removeLogEventListener(toRemove);
    }

    @Override
    public void handleNewEvent(LogEvent logEvent) {
        delegate.handleNewEvent(logEvent);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return delegate.stopListeningTo(branch);
    }

    @Override
    public boolean listenTo(Loggable branch) {
        return delegate.listenTo(branch);
    }
}
