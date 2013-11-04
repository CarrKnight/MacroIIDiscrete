/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;
import org.jfree.data.time.TimeSeries;

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
public class MarkupFollower implements AskPricingStrategy {

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
        TimeSeries markups = sales.getMarket().getMarkups();
        if(priceToFollow == -1)
            //if you know nothing, just act cluelessly
            return (long) (g.getLastValidPrice() * (1f + sales.getFirm().getModel().getCluelessDefaultMarkup()));
        else
        {//if you you can copy the last markup
            double markup = markups.getValue(markups.getItemCount() - 1).doubleValue();
            //i hope this works



            return (long) Math.max(priceToFollow,g.getLastValidPrice()*(1d+markup));
        }
    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {
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
    public int estimateSupplyGap() {
        return sales.getHowManyToSell();
    }
}
