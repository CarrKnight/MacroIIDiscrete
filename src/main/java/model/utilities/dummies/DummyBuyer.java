/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import agents.firm.DummyProfitReport;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import financial.Market;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

public class DummyBuyer extends Firm {
    /**
     * This is just so that I can have buyers return a quote without having to build the goods and all that
     */

    public long quotedPrice;

    public boolean boughtToday = false;

    final private Market market;

    public DummyBuyer(MacroII model, long price,Market market) {
        super(model,false);
        quotedPrice = price;
        this.market = market;
        getProfitReport().turnOff();
        setProfitReport(new DummyProfitReport());
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
    public void reactToFilledBidQuote(final Good g, long price, EconomicAgent seller) {
        boughtToday = true;
        getModel().scheduleASAP(new Steppable() {
            @Override
            public void step(SimState state) {
                consume(g.getType());
                getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
                    @Override
                    public void step(SimState state) {
                        boughtToday=false;
                    }
                });
            }
        });


    }

    /**
     * Dummy buyers have a fixed price with which they pretend to buy
     * @return
     */
    public long getFixedPrice() {
        return quotedPrice;
    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        if(!boughtToday && market.getLastPrice() <= quotedPrice)
            return 1;
        else
            return 0;
    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public int estimateSupplyGap(GoodType type) {
        return 0;
    }
}