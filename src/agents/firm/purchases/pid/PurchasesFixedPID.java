package agents.firm.purchases.pid;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import com.google.common.base.Preconditions;
import financial.MarketEvents;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.*;
import model.utilities.pid.decorator.ExponentialFilterInputDecorator;
import model.utilities.pid.decorator.ExponentialFilterOutputDecorator;
import model.utilities.pid.decorator.ExponentialFilterTargetDecorator;
import model.utilities.pid.decorator.MovingAverageFilterInputDecorator;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is a controller class that doubles as both a pricing and an inventory control strategy for purchases department. This implementation retrofit FixedInventoryControl by extending it.
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
public class PurchasesFixedPID extends FixedInventoryControl implements BidPricingStrategy, Steppable {


    /**
     * Basically we use the controller for everything and we delegate to it. This is a dumb way of avoiding writing the same code for every controller controller I need
     */
    private Controller controller;

    /**
     * I might end up decorating the controller but if I need to change gains/speeds with this reference I can do it directly without having to ask each decorator to give me the controller it is decorating
     */
    final private Controller rootController;


    /**
     * This is the standard constructor needed to generate at random this strategy.
     * @param purchasesDepartment the department controlled by this strategy
     */
    public PurchasesFixedPID(@Nonnull PurchasesDepartment purchasesDepartment) {
        super(purchasesDepartment);                                                                                //.5f,2f,.05f
        float proportionalGain = (float) (.5f + purchasesDepartment.getRandom().nextGaussian()*.01f);
        float integralGain = (float) (.5f + purchasesDepartment.getRandom().nextGaussian()*.05f);
        float derivativeGain =(float) (0.01f + purchasesDepartment.getRandom().nextGaussian()*.005f);
        rootController = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
        controller = rootController; //remember it
    }


    /**
     * This is the standard constructor needed to generate at random this strategy.
     * @param purchasesDepartment the department controlled by this strategy
     */
    public PurchasesFixedPID(@Nonnull PurchasesDepartment purchasesDepartment, int specificTarget) {
        super(purchasesDepartment,specificTarget);                                                                                //.5f,2f,.05f
        float proportionalGain = (float) (.5f + purchasesDepartment.getRandom().nextGaussian()*.01f);
        float integralGain = (float) (.5f + purchasesDepartment.getRandom().nextGaussian()*.05f);
        float derivativeGain =(float) (.01f + purchasesDepartment.getRandom().nextGaussian()*.005f);
        rootController = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
        controller = rootController; //remember it

    }

    /**
     * Create a fixed inventory control using a custom controller. Notice that the controller input will always be: <br>
     * target1: stock, target2: flow, input1: stock, input2: flow.
     * @param purchasesDepartment the purchase department to control
     * @param specificTarget the specific target
     * @param controllerType the type of control to use
     */
    public PurchasesFixedPID(@Nonnull PurchasesDepartment purchasesDepartment, int specificTarget,
                             Class<? extends Controller> controllerType, MacroII model)
    {
        super(purchasesDepartment,specificTarget);
        rootController = ControllerFactory.buildController(controllerType,model);
        controller = rootController;


    }





    /**
     * Constructor that specifies gains
     * @param purchasesDepartment the department controlled by this strategy
     */
    public PurchasesFixedPID(@Nonnull PurchasesDepartment purchasesDepartment, float proportionalGain, float integralGain,
                             float derivativeGain, int specificTarget) {
        super(purchasesDepartment,specificTarget);
        rootController = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
        controller = rootController; //remember it


    }


    /**
     * Constructor that specifies gains
     * @param purchasesDepartment the department controlled by this strategy
     */
    public PurchasesFixedPID(@Nonnull PurchasesDepartment purchasesDepartment, float proportionalGain, float integralGain,
                             float derivativeGain) {
        super(purchasesDepartment);
        rootController = new PIDController(proportionalGain,integralGain,derivativeGain,purchasesDepartment.getRandom()); //instantiate the controller
        controller = rootController; //remember it


    }

    /**
     * The adjust is the main part of the controller controller. It checks the new error and set the MV (which is the price, really)
     * @param simState MacroII object if I am worth anything as a programmer
     */
    @Override
    public void step(SimState simState) {

        long oldprice = maxPrice(getGoodTypeToControl());
        ControllerInput input = getControllerInput(getTarget());


        controller.adjust(input, isActive(), (MacroII)simState, this, ActionOrder.THINK);
        long newprice = maxPrice(getGoodTypeToControl());
        //log the change in policy
        getPurchasesDepartment().getFirm().logEvent(getPurchasesDepartment(),
                MarketEvents.CHANGE_IN_POLICY,
                getPurchasesDepartment().getFirm().getModel().getCurrentSimulationTimeInMillis(),
                "target: " + getTarget() + ", control :" + input.getInput(0) +
                        "; oldprice:" + oldprice + ", newprice:" + newprice);

        if(oldprice != newprice) //if pid says to change prices, change prices
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
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible                      <br>
     * In addition the controller methods adjust their controller once.
     */
    @Override
    public void start() {

        getPurchasesDepartment().getFirm().getModel().scheduleSoon(ActionOrder.THINK,
                this);

        super.start();
    }

    /**
     * Whenever set the controller is reset
     */
    public void setInitialPrice(float initialPrice) {
        rootController.setOffset(initialPrice);
    }

    public float getOffset() {
        return rootController.getOffset();
    }


    public void setSpeed(int speed) {
        rootController.setSpeed(speed);
    }




    /**
     * Decorates the controller so that the input it actually receive is the moving average rather than the point itself
     * @param weight patience
     * @param position which input to filter
     */
    public void filterInputExponentially(float weight, int position)
    {
        Preconditions.checkState(controller != null, "You can't filter a controller that doesn't exist!");
        controller = new ExponentialFilterInputDecorator(controller,weight, position);
    }

    /**
     * Decorates the controller so that the input it actually receive is the moving average rather than the point itself
     * @param size patience
     */
    public void filterInputMovingAverage(int size, int position)
    {
        Preconditions.checkState(controller != null, "You can't filter a controller that doesn't exist!");
        controller = new MovingAverageFilterInputDecorator(controller,size,position);
    }


    public void filterTargetExponentially(float weight, int position)
    {
        Preconditions.checkState(controller != null, "You can't filter a controller that doesn't exist!");
        controller = new ExponentialFilterTargetDecorator(controller,weight,position);
    }

    /**
     * Decorates the controller so that the output (u_t) is a moving average
     * @param weight patience
     */
    public void filterOutputExponentially(float weight)
    {
        Preconditions.checkState(controller != null, "You can't filter a controller that doesn't exist!");
        controller = new ExponentialFilterOutputDecorator(controller,weight);
    }


    /**
     * Returns the class of the controller.
     */
    public Class<? extends Controller> getKindOfController(){
        return rootController.getClass();
    }
}
