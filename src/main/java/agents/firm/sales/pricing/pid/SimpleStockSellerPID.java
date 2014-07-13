/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.BaseAskPricingStrategy;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.pid.decorator.PIDAutotuner;
import model.utilities.stats.regression.KalmanIPDRegressionWithKnownTimeDelay;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p> Basically an inventory targeting that just uses a pid to get today inventory = target (100 by default). This sounds simple but it is usually hopeless because it requires very well-tuned pids.
 * My autotuning is awful, but we'll try
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-05
 * @see
 */
public class SimpleStockSellerPID extends BaseAskPricingStrategy implements Steppable
{

    private int targetInventory;


    private final PIDAutotuner tunedController;

    private final SalesDepartment department;

    private int price;


    public SimpleStockSellerPID(SalesDepartment department) {
        this(department,100, new PIDController(department.getModel().drawProportionalGain(),department.getModel().drawIntegrativeGain(),0f));
    }

    public SimpleStockSellerPID(SalesDepartment sales, int targetInventory, PIDController pid) {
        this.department = sales;
        this.targetInventory = targetInventory;
        pid.setControllingFlows(false);
        this.tunedController = new PIDAutotuner(pid, KalmanIPDRegressionWithKnownTimeDelay::new, department);
        this.tunedController.setAdditionalInterceptsExtractor(controllerInput -> new double[]{controllerInput.getFlowTarget()});
        //keep it paused until you start piling up stuff
        tunedController.setPaused(true);
        tunedController.setExcludeLinearFallback(true);
        tunedController.setAfterHowManyDaysShouldTune(200);

        //random initial price
        price = sales.getRandom().nextInt(100);
        tunedController.setOffset(price, true);
        tunedController.setValidateInput(controllerInput -> !(department.numberOfObservations() == 0 ||
                department.getTodayOutflow() == 0 ||
                department.getHowManyToSell() == 0));

        //schedule yourself
        sales.getFirm().getModel().scheduleSoon(ActionOrder.ADJUST_PRICES, this);

    }

    @Override
    public void step(SimState state) {
        if(department.hasAnythingToSell() && department.hasTradedAtLeastOnce())
            tunedController.setPaused(false);

        tunedController.adjust(new ControllerInput(department.getTodayInflow(),targetInventory,department.getTodayOutflow(),
                department.getHowManyToSell()),true,(MacroII)state,this,ActionOrder.ADJUST_PRICES);
        price = Math.round(tunedController.getCurrentMV());


        handleNewEvent(new LogEvent(this, LogLevel.DEBUG,
                "PID policy change, inventory: {}, target:{}, newprice:{}",department.getHowManyToSell(),targetInventory,tunedController.getCurrentMV()));

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
}
