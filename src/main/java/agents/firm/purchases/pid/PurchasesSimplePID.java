/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pid;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.SimpleInventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.MarketEvents;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerFactory;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> This is a controller class that doubles as both a pricing and an inventory control strategy for purchases department. This implementation retrofit SimpleInventoryControl by extending it.
 * <p/>  It steps independently until receiving a turnoff signal. In the adjust process it adjust through its controller nature
 * <p/> This particular implementation uses the standard formula in spite of the fact that the observations are discrete
 * <h4>Notes</h4>
 * It's not drawable at random since the rule generator wouldn't know how to set it for both pricing and inventory control
 *  * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-12
 * @see sim.engine.Steppable
 */
public class PurchasesSimplePID extends SimpleInventoryControl implements BidPricingStrategy, Steppable {

    /**
     * The controller used to choose prices
     */
    private Controller controller;


    /**
     * This is the standard constructor needed to generate at random this strategy.
     * @param purchasesDepartment the department controlled by this strategy
     */
    public PurchasesSimplePID( PurchasesDepartment purchasesDepartment) {
        super(purchasesDepartment);                                                                                //.5f,2f,.05f
        float proportionalGain = (float) (.5f + purchasesDepartment.getRandom().nextGaussian()*.01f);
        float integralGain = (float) (2f + purchasesDepartment.getRandom().nextGaussian()*.05f);
        float derivativeGain =(float) (.05f + purchasesDepartment.getRandom().nextGaussian()*.005f);
        controller = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
    }

    public PurchasesSimplePID( PurchasesDepartment purchasesDepartment, float proportionalGain, float integralGain,
                              float derivativeGain) {
        super(purchasesDepartment);
        controller = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
    }

    /**
     * Creates a simple inventory controller using a Controller of a specific type given by the department
     * @param purchasesDepartment the purchase department to control
     * @param controllerType the controller type to use
     * @param macroII a link to the model
     */
    public PurchasesSimplePID( PurchasesDepartment purchasesDepartment,
                               Class<? extends Controller > controllerType,
                              MacroII macroII)
    {
        super(purchasesDepartment);
        controller = ControllerFactory.buildController(controllerType, macroII);

    }


    /**
     * The adjust is the main part of the controller controller. It checks the new error and set the MV (which is the price, really)
     * @param simState MacroII object if I am worth anything as a programmer
     */
    @Override
    public void step(SimState simState) {

        long oldprice = maxPrice(getGoodTypeToControl());
        int target = getSingleProductionRunNeed();
        controller.adjust(getControllerInput(target), isActive(),(MacroII) simState, this, ActionOrder.PREPARE_TO_TRADE);
        long newprice = maxPrice(getGoodTypeToControl());

        //log the change in policy
        if(MacroII.hasGUI())

            getPurchasesDepartment().getFirm().logEvent(getPurchasesDepartment(),
                MarketEvents.CHANGE_IN_POLICY,
                getPurchasesDepartment().getFirm().getModel().getCurrentSimulationTimeInMillis(),
                "target: " + target + ", inventory:" + getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()) +
                        "; oldprice:" + oldprice + ", newprice:" + newprice);


        if(oldprice != newprice && newprice >=0) //if pid says to change prices, change prices
            getPurchasesDepartment().updateOfferPrices();
    }


    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(GoodType type) {
        return Math.round(controller.getCurrentMV());
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(Good good) {
        return maxPrice(good.getType());
    }

    /**
     * This controller strategy is always buying. It is using prices to control its inventory
     * @return true
     */
    @Override
    public boolean canBuy() {
        return  true;

    }

    /**
     * This is the method overriden by the subclass of inventory control to decide whether the current inventory levels call for a new good being bought
     *
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    @Override
    protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {
        return true;
    }

    /**
     * Calls super turnoff to kill all listeners and then set active to false
     * */
    @Override
    public void turnOff() {
        super.turnOff();
        assert !isActive();
    }

    /**
     * Whenever set the controller is reset
     */
    public void setInitialPrice(float initialPrice) {
        controller.setOffset(initialPrice);
    }

    public float getInitialPrice() {
        return controller.getOffset();
    }

    /**
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible                      <br>
     * In addition the controller methods adjust their controller once.
     */
    @Override
    public void start() {
        getPurchasesDepartment().getFirm().getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,
                new Steppable() {
                    @Override
                    public void step(SimState state) {
                        controller.adjust(getControllerInput(getSingleProductionRunNeed()), isActive(),
                                getPurchasesDepartment().getFirm().getModel(), this,ActionOrder.PREPARE_TO_TRADE);
                    }
                });
        super.start();
    }

}
