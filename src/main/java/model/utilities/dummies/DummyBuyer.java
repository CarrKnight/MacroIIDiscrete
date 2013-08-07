/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.ProfitReport;
import agents.firm.sales.SalesDepartment;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;

import static org.mockito.Mockito.mock;

public class DummyBuyer extends Firm {
    /**
     * This is just so that I can have buyers return a quote without having to build the goods and all that
     */

    public long quotedPrice;

    public DummyBuyer(MacroII model, long price) {
        super(model,false);
        quotedPrice = price;
        getProfitReport().turnOff();
        setProfitReport(mock(ProfitReport.class));
    }

    /**
     *     simple utility to tell one agent to try and shop somewhere specific. This is already coded in Purchase-departments but it is still useful for dummy buyers

     */
    public static void goShopping(DummyBuyer buyer, SalesDepartment dept, GoodType type)
    {
        Quote askedPrice = dept.askedForASalePrice(buyer);
        long priceQuoted =askedPrice.getPriceQuoted();
        if(priceQuoted >=0 && buyer.getFixedPrice() >= priceQuoted)
        {
            Quote buyerQuote = Quote.newBuyerQuote(buyer,buyer.getFixedPrice(),type);
            //TRADE
            dept.shopHere(buyerQuote,askedPrice);

        }
    }


    /**
     *     simple utility to tell one agent to try and shop somewhere specific. This is already coded in Purchase-departments but it is still useful for dummy buyers

     */
    public static void goShopping(DummyBuyer buyer, EconomicAgent dept, GoodType type)
    {
        Quote askedPrice = dept.askedForASaleQuote(buyer,type);
        long priceQuoted =askedPrice.getPriceQuoted();

        if(priceQuoted >=0 && buyer.getFixedPrice() >= priceQuoted)
        {
            Quote buyerQuote = Quote.newBuyerQuote(buyer,buyer.getFixedPrice(),type);
            //TRADE
            dept.shopHere(buyerQuote,askedPrice);

        }
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long askedForABuyOffer(GoodType t) {
        return quotedPrice;
    }

    public void setQuotedPrice(long quotedPrice) {
        this.quotedPrice = quotedPrice;
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long maximumOffer(Good g) {
        return quotedPrice;
    }

    @Override
    public String toString() {
        return "DummyBuyer with demand" + getFixedPrice();
    }

    @Override
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        //don't react
    }

    @Override
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
        consume(g.getType());

    }

    /**
     * Dummy buyers have a fixed price with which they pretend to buy
     * @return
     */
    public long getFixedPrice() {
        return quotedPrice;
    }
}