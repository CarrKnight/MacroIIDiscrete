/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.BaseAskPricingStrategy;
import com.google.common.base.Preconditions;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Simply tries to make stock = inventory. In reality a facade for adaptive stock seller.
 * Created by carrknight on 8/28/14.
 */
public class SimpleStockSeller extends BaseAskPricingStrategy implements Steppable
{


    private int targetInventory;


    protected Controller controller;


    protected final PIDController rootController;



    private final SalesDepartment department;

    private int price;



    public SimpleStockSeller(SalesDepartment department) {
        this(department,100,
                new PIDController(department.getModel().drawProportionalGain()/5,department.getModel().drawIntegrativeGain()/5f,0f));
    }

    public SimpleStockSeller(SalesDepartment sales, int targetInventory, PIDController pid) {
        Preconditions.checkArgument(targetInventory > 0, "target inventory should be positive");
        this.department = sales;
        this.targetInventory = targetInventory;
        pid.setControllingFlows(false);
        rootController = pid;
        controller = pid;
        //random initial price
        price = sales.getRandom().nextInt(100);
        rootController.setOffset(price,true);
        //schedule yourself
        sales.getFirm().getModel().scheduleSoon(ActionOrder.ADJUST_PRICES, this);

    }

    @Override
    public void step(SimState state) {

        controller.adjust(new ControllerInput(department.getTodayInflow(),targetInventory,department.getTodayOutflow(),
                department.getHowManyToSell()),true,(MacroII)state,this,ActionOrder.ADJUST_PRICES);
        price = Math.round(controller.getCurrentMV());


        handleNewEvent(new LogEvent(this, LogLevel.DEBUG,
                "PID policy change, inventory: {}, target:{}, newprice:{}",department.getHowManyToSell(),targetInventory,
                controller.getCurrentMV()));

        //we are being restepped by the controller so just wait.
        department.updateQuotes();

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
        return price;
    }

    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
    }

    /**
     * asks the pricing strategy if the inventory is acceptable
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return  inventorySize == targetInventory;
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public float estimateSupplyGap() {
        if(department.getHowManyToSell() == 0 || department.getTodayOutflow() == 0 )
            return  1000;
        else
            return 0;
    }

    public int getTargetInventory() {
        return targetInventory;
    }

    public void setTargetInventory(int targetInventory) {
        this.targetInventory = targetInventory;
    }


    /**
     * setting 3 parameters. I am using here the PID terminology even though it doesn't have to be the case.
     * @param proportionalGain the first parameter
     * @param integralGain the second parameter
     * @param derivativeGain the third parameter
     */
    public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
        controller.setGains(proportionalGain, integralGain, derivativeGain);
    }

    public float getProportionalGain() {
        return rootController.getProportionalGain();
    }

    public float getIntegralGain() {
        return rootController.getIntegralGain();
    }

    public float getDerivativeGain() {
        return rootController.getDerivativeGain();
    }



}
