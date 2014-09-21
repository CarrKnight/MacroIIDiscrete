/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import agents.firm.purchases.PurchasesDepartment;
import goods.Good;
import goods.GoodType;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> Cheater Pricing "cheats" by simply always offering the best ask price if there is one and it's visible and defaulting to
 * defaultOffer otherwise!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-06-07
 * @see
 */
public class PriceTaker implements BidPricingStrategy {

    /**
     * how much to offer for a good when the price is not visible.
     *
     */
    private int defaultOffer = 1000;



    /**
     * creates the pricing strategy that looks at the market
     * @param department reference needed because it sets the priority of its action as "low" (trying to act after the sales departments)
     */
    public PriceTaker(final PurchasesDepartment department) {
        department.setTradePriority(Priority.AFTER_STANDARD);

        department.getModel().scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {
                defaultOffer = 1000 + department.getRandom().nextInt(10000);
                department.getModel().scheduleTomorrow(ActionOrder.DAWN,this);

            }
        });
    }

    /**
     * Returns the best sale price if there is one and is visible or default offer
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {


            return defaultOffer;

    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        return maxPrice(good.getType()); //delegate
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
        //nothing really
    }


    /**
     * Sets new how much to offer for a good when the price is not visible..
     *
     * @param defaultOffer New value of how much to offer for a good when the price is not visible..
     */
    public void setDefaultOffer(int defaultOffer) {
        this.defaultOffer = defaultOffer;
    }

    /**
     * Gets how much to offer for a good when the price is not visible..
     *
     * @return Value of how much to offer for a good when the price is not visible..
     */
    public long getDefaultOffer() {
        return defaultOffer;
    }
}
