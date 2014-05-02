/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> Copies the last closing price of the market, if available.
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
public class MarkupFollower extends BaseAskPricingStrategy {

    SalesDepartment sales;

    public MarkupFollower(SalesDepartment sales) {
        this.sales = sales;
    }

    /**
     * Copies the last closing price of the market, if available.
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public long price(Good g) {
        long priceToFollow =sales.getMarket().getLastPrice();
        double markup = sales.getMarket().getLastMarkup();
        if(priceToFollow == -1)
            //if you know nothing, just act cluelessly
            return (long) (g.getLastValidPrice() * (1f + sales.getFirm().getModel().getCluelessDefaultMarkup()));
        else
        {//if you you can copy the last markup
            //i hope this works



            return (long) Math.max(priceToFollow,g.getLastValidPrice()*(1d+markup));
        }
    }


    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {
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

    /**
     * All inventory is unwanted
     */
    @Override
    public float estimateSupplyGap() {
        return sales.getHowManyToSell();
    }
}
