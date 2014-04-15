/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

import agents.EconomicAgent;
import agents.HasInventory;
import agents.InventoryListener;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> A very simple inventory listener I give to purchase departments to check inflow-outflow of a specific good
 * <p/> it is started by the start of purchase department and turned off with the purchase department.
 * <p/> It resets itself every day at dawn
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-13
 * @see
 */
public class InflowOutflowCounter implements Deactivatable, InventoryListener, Steppable

{

    boolean isActive = true;

    /**
     * used to schedule yourself
     */
    final private MacroII model;

    /**
     * the agent whose inventory you are going to listen to
     */
    final private EconomicAgent agent;

    /**
     * the type of good to listen to
     */
    private final GoodType type;



    /**
     * total inflow since dawn
     */
    private int yesterdayInflow=0;

    /**
     * total outflow since dawn
     */
    private int yesterdayOutflow=0;


    /**
     * total inflow since dawn
     */
    private int todayInflow;

    /**
     * total outflow since dawn
     */
    private int todayOutflow;

    /**
     * counts how many times a plant wanted to consume a product but failed to do so because it wasn't available.
     */
    private int todayFailuresToConsume;


    /**
     * called every dawn (and reschedules itself if active), resets all the counters
     */
    public void restartAtDawn()
    {
        Preconditions.checkArgument(model.getCurrentPhase().equals(ActionOrder.DAWN));

        if(isActive)
        {

            yesterdayInflow = todayInflow;
            todayInflow = 0;

            yesterdayOutflow = todayOutflow;
            todayOutflow = 0;

            todayFailuresToConsume = 0;

            model.scheduleTomorrow(ActionOrder.DAWN,this, Priority.BEFORE_STANDARD);


        }



    }

    /**
     * start resetting and listening
     */
    public void start()
    {
        assert isActive;
        //schedule your first reset
        model.scheduleSoon(ActionOrder.DAWN, this,Priority.BEFORE_STANDARD);
        //add yourself as a listener
        agent.addInventoryListener(this);


    }

    /**
     * stop rescheduling yourself
     */
    @Override
    public void turnOff() {
        isActive = false;
    }

    /**
     * The step is each morning reset your data.
     */
    @Override
    public void step(SimState state) {
        restartAtDawn();

    }

    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has increased
     *
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     * @param delta change in inventory (always positive)
     */
    @Override
    public void inventoryIncreaseEvent( HasInventory source,  GoodType type, int quantity, int delta) {

        if(this.type.equals(type))
        {
            Preconditions.checkArgument(delta > 0);
            todayInflow+= delta;
        }
    }

    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     *
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     * @param delta change in inventory (always positive)
     */
    @Override
    public void inventoryDecreaseEvent( HasInventory source,  GoodType type, int quantity,int delta) {
        if(this.type.equals(type)) {
            Preconditions.checkArgument(delta > 0);
            todayOutflow+= delta;
        }
    }

    /**
     * This method is called by departments (plants usually) that need this input but found none. It is called
     *
     * @param source       the agent with the inventory
     * @param type         the good type demanded
     * @param numberNeeded how many goods were needed
     */
    @Override
    public void failedToConsumeEvent( HasInventory source,  GoodType type, int numberNeeded) {
        if(this.type.equals(type))
            todayFailuresToConsume += numberNeeded;

    }

    /**
     * Creates the counter. It is going to start listening at start()
     * @param model the model (to reschedule yourself)
     * @param agent the agent to listen to
     * @param type the type of good you want to count inflow/outflow
     */
    public InflowOutflowCounter(MacroII model, EconomicAgent agent, GoodType type) {
        this.model = model;
        this.agent = agent;
        this.type = type;
    }

    /**
     * Answers how many days, at the current rate, will it take for all the inventories to be gone
     * @return If outflow > inflow it returns inventorySize/netOutflow, otherwise returns infinity
     */
    public float currentDaysOfInventory()
    {
        float netOutflow = todayOutflow - todayInflow;
        if( netOutflow > 0)
            return agent.hasHowMany(type) / netOutflow;
        else
            return Float.POSITIVE_INFINITY;

    }


    /**
     * Gets total outflow since dawn.
     *
     * @return Value of total outflow since dawn.
     */
    public int getTodayOutflow() {
        return todayOutflow;
    }

    /**
     * Gets total inflow since dawn.
     *
     * @return Value of total inflow since dawn.
     */
    public int getTodayInflow() {
        return todayInflow;
    }


    public int getTodayFailuresToConsume() {
        return todayFailuresToConsume;
    }


    public int getYesterdayInflow() {
        return yesterdayInflow;
    }

    public int getYesterdayOutflow() {
        return yesterdayOutflow;
    }
}
