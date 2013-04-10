/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> Price taker checks periodically (like the undercuttingPricing) for the lowest price around. Then, if possible, it copies it.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-23
 * @see
 */
public class PriceImitator extends UndercuttingAskPricing {


    /**
     * Price taker checks periodically (like the undercuttingPricing) for the lowest price around. Then, if possible, it copies it.
     * @param sales the sales department the strategy is attached to
     */
    public PriceImitator(SalesDepartment sales) {
        super(sales);
    }

    /**
     * Copies the best price it found by searching
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public long price(Good g) {
        if(getBestOpponentPriceFound() == -1){
            //if we didn't find a single opponent, just return the cost
            return (long) (g.getLastValidPrice() * (1f + getSales().getFirm().getModel().getCluelessDefaultMarkup()));
        }
        else{
            //return the copied price
            return Math.max(getBestOpponentPriceFound(),g.getLastValidPrice());
        }
    }
    /**
     * tries to sell everything
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return inventorySize == 0; //tries to sell everything

    }



}
