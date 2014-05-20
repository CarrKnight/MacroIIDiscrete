/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.decorators;

import agents.firm.sales.pricing.AskPricingStrategy;
import goods.Good;
import model.utilities.NonDrawable;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogListener;
import model.utilities.logs.Loggable;

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
    public int price(Good g) {
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
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public float estimateSupplyGap() {
        return toDecorate.estimateSupplyGap();
    }

    /**
     * Returns the strategy being decorated
     */
    public AskPricingStrategy getDecorated() {
        return toDecorate;
    }

    @Override
    public void handleNewEvent(LogEvent logEvent) {
        toDecorate.handleNewEvent(logEvent);
    }

    @Override
    public boolean listenTo(Loggable eventSource) {
        return toDecorate.listenTo(eventSource);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return toDecorate.stopListeningTo(branch);
    }

    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return toDecorate.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return toDecorate.removeLogEventListener(toRemove);
    }
}
