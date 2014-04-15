/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Iterator;

/**
 * <h4>Description</h4>
 * <p/> Simplest stockout checker: at the end of trade check how many bids are left at a price higher than what we usually ask
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-03-08
 * @see
 */
public class AfterTradeCounter implements StockoutEstimator, Steppable {


    final private Market market;

    int stockouts = 0;

    final private SalesDepartment department;


    public AfterTradeCounter(Market market, SalesDepartment department) {
        this.market = market;
        this.department = department;

        department.getModel().scheduleSoon(ActionOrder.POST_TRADE_STATISTICS,this);
    }

    @Override
    public void newPIDStep(Market market) {

    }


    @Override
    public void step(SimState simState) {

        if(!department.isActive()) //don't proceed if the department is off
         return;


        long price = department.hypotheticalSalePrice();
        stockouts = 0;

        if(price<0)
        {
            return;
        }
        else
        {
            try {
                final Iterator<Quote> bids = market.getIteratorForBids();

                while(bids.hasNext())
                {
                     if(bids.next().getPriceQuoted() >= price)
                         stockouts++;
                }

            } catch (IllegalAccessException e) {
                throw  new RuntimeException("Shouldn't use this stockout counter with markets where bids are not visible!");

            }
        }

        ((MacroII)simState).scheduleTomorrow(ActionOrder.POST_TRADE_STATISTICS,this);
    }

    @Override
    public int getStockouts() {
        return stockouts;
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer   the agent placing the bid
     * @param price   the price of the good
     * @param bestAsk the best ask when the bid was made
     */
    @Override
    public void newBidEvent( EconomicAgent buyer, long price, Quote bestAsk) {
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent( EconomicAgent buyer,  Quote quote) {
    }

    /**
     * Tell the listener the firm just tasked the salesdepartment to sell a new good
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     * @param good  the good being sold
     */
    @Override
    public void sellThisEvent( Firm owner,  SalesDepartment dept,  Good good) {
    }

    /**
     * This logEvent is fired whenever the sales department managed to sell a good!
     *  @param dept   The department
     * @param good
     * @param price
     */
    @Override
    public void goodSoldEvent( SalesDepartment dept, Good good, Long price) {
    }

    /**
     * Tell the listener a peddler just came by and we couldn't service him because we have no goods
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     * @param buyer
     */
    @Override
    public void stockOutEvent( Firm owner,  SalesDepartment dept,  EconomicAgent buyer) {
    }

    /**
     * Tell the listener a trade has been carried out
     *
     * @param buyer         the buyer of this trade
     * @param seller        the seller of this trade
     * @param goodExchanged the good that has been traded
     * @param price         the price of the trade
     * @param sellerQuote
     * @param buyerQuote
     */
    @Override
    public void tradeEvent(EconomicAgent buyer, EconomicAgent seller, Good goodExchanged, long price, Quote sellerQuote, Quote buyerQuote) {
    }
}
