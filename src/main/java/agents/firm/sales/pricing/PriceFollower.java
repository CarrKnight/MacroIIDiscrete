/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> This strategy copies the last price in the market whenever possible.
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
public class PriceFollower extends BaseAskPricingStrategy {


    SalesDepartment sales;

    /**
     * This strategy copies the last price in the market whenever possible.
     */
    public PriceFollower(SalesDepartment sales) {
        this.sales = sales;
    }

    /**
     * Copies the last closing price of the market, if available.
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public int price(Good g) {
        int priceToFollow =sales.getMarket().getLastPrice();
        if(priceToFollow == -1)
            //if you know nothing, just act cluelessly
            return (int) (g.getLastValidPrice() * (1f + sales.getFirm().getModel().getCluelessDefaultMarkup()));
        else
            //if you you can copy from the last price
            return Math.max(priceToFollow,g.getLastValidPrice());
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
