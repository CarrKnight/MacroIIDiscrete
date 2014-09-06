/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
    public int maxPrice(GoodType type);

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    public int maxPrice(Good good);


    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    public void turnOff();






}
