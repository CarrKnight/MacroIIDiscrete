/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import goods.Good;
import goods.GoodType;
import model.utilities.Deactivatable;

/**
 * <h4>Description</h4>
 * <p/>  This is the interface used by all strategy objects used by PURCHASES department to define their maximum ask price.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-02
 * @see
 */
public interface BidPricingStrategy extends Deactivatable {

    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    public long maxPrice(GoodType type);

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    public long maxPrice(Good good);


    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    public void turnOff();






}
