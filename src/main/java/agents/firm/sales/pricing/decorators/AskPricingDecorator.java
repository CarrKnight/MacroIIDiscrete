/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.decorators;

import agents.firm.sales.pricing.AskPricingStrategy;
import goods.Good;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> This is the abstract decorator for ask pricing strategy. Useful to add run-time addons to the usual ask pricing.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-12
 * @see
 */
@NonDrawable
public abstract class AskPricingDecorator implements AskPricingStrategy{


    final protected AskPricingStrategy toDecorate;

    /**
     *  This is the abstract decorator for ask pricing strategy. Useful to add run-time addons to the usual ask pricing.
     * @param toDecorate  the strategy to decorate
     */
    protected AskPricingDecorator(AskPricingStrategy toDecorate) {
        this.toDecorate = toDecorate;
    }

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public long price(Good g) {
        return toDecorate.price(g);
    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {
        toDecorate.turnOff();
    }

    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
        toDecorate.weekEnd();
    }

    /**
     * asks the pricing strategy if the inventory is acceptable
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return toDecorate.isInventoryAcceptable(inventorySize);
    }

    /**
     * Returns the strategy being decorated
     */
    public AskPricingStrategy getDecorated() {
        return toDecorate;
    }
}
