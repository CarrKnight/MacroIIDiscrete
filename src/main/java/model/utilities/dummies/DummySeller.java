/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import agents.firm.utilities.DummyProfitReport;
import agents.firm.Firm;
import financial.market.Market;
import financial.MarketEvents;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> Used only for testing. Always return the same quote when asked what to buy
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-26
 * @see
 */
public class DummySeller extends Firm {


    public int saleQuote;

    private Market market;

    boolean soldToday = false;

    public DummySeller(MacroII model,int quote) {
        super(model,false);
        saleQuote = quote;
        getProfitReport().turnOff();
        setProfitReport(new DummyProfitReport());
    }


    public DummySeller(MacroII model, int saleQuote, Market market) {
        super(model);
        this.saleQuote = saleQuote;
        this.market = market;
    }

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        Good good = peekGood(type);
        if(good != null)
            return Quote.newSellerQuote(this,saleQuote,good);
        else
            return Quote.emptySellQuote(null);

    }

    public void setSaleQuote(int saleQuote) {
        this.saleQuote = saleQuote;
    }

    @Override
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent buyer) {
        //don't react
        /**
         * how "far" purchases inventory are from target.
         */
        soldToday = true;

        model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {

                soldToday = false;
            }
        });



    }


    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */

    @Override
    public PurchaseResult shopHere( Quote buyerQuote,  Quote sellerQuote) {
        int finalPrice = market.price(sellerQuote.getPriceQuoted(),buyerQuote.getPriceQuoted());
        assert sellerQuote.getGood() != null;
        assert this.has(sellerQuote.getGood());


        //exchange hostages
        market.trade(buyerQuote.getAgent(),this,sellerQuote.getGood(),finalPrice,buyerQuote,sellerQuote);
        this.logEvent(this, MarketEvents.SOLD, this.getModel().getCurrentSimulationTimeInMillis(), "price: " + finalPrice + ", through buyFromHere()"); //sold a good
        assert !this.has(sellerQuote.getGood());


        PurchaseResult toReturn = PurchaseResult.SUCCESS;
        toReturn.setPriceTrade(finalPrice);
        return toReturn;
    }

    /**
     * this is a "utility" method that should be used sparingly. What it does is it creates a mock object, passes it to the sales department
     * and ask for it for a price. It is no guarantee that the firm actually will charge such price when a real good gets created.
     *
     * @param goodType
     * @return
     */
    @Override
    public int hypotheticalSellPrice(GoodType goodType) {

        return saleQuote;
    }


    /**
     * this is a "utility" method to avoid tren wreck calls. Basically returns the outflow recorded by the sales department dealing with that good type
     *
     * @param goodType the good type the sales department deals with
     * @return
     */
    @Override
    public int getSalesDepartmentRecordedOutflow(GoodType goodType) {
        throw new UnsupportedOperationException("still to code");

    }

    /**
     * this is a "utility" method to avoid tren wreck calls. Basically returns the outflow recorded by the sales department dealing with that good type
     *
     * @param goodType the good type the sales department deals with
     * @return
     */
    @Override
    public int getSalesDepartmentRecordedInflow(GoodType goodType) {
        throw new UnsupportedOperationException("still to code");
    }


    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        return 0;
    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public float estimateSupplyGap(GoodType type) {
        if(market==null)
            return 0;
        if(!soldToday && market.getLastPrice() >= saleQuote)
            return 1;
        else
            return 0;    }
}
