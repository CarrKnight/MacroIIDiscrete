/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * A basic strategy, as much as possible tries to place both a bid and an ask. Remove both quotes at the end of the day.
 * Focuses only on 1 good.
 * Created by carrknight on 5/20/14.
 */
public class OneGoodBuyingAndSellingStrategy implements PersonalTradingStrategy, Steppable {

    private Quote saleQuote;

    private Quote buyQuote;

    private final Market goodMarket;

    private final Person p;

    private final MacroII model;

    /**
     * if the order is immediately handled we get "react to blah" called while in the loop. Doesn't really affect anything
     * except being used in some asserts
     */
    private boolean placeQuotesLoop = false;


    /**
     * Creates the strategy and register the person as a buyer and a seller
     * @param goodMarket the good market to trade into
     * @param p the person this strategy commands
     * @param model the model itself
     */
    public OneGoodBuyingAndSellingStrategy(Market goodMarket, Person p, MacroII model) {
        this.goodMarket = goodMarket;
        this.p = p;
        this.model = model;

        goodMarket.registerBuyer(p);
        goodMarket.registerSeller(p);
    }

    @Override
    public void beginTradingDay(Person p, MacroII model) {
        Preconditions.checkState(saleQuote==null && buyQuote==null, "did not clear quotes properly at the end of the last day!");

        //prepare to make a trade!
        rescheduleYourself();

    }



    public void removeSaleQuote() {
        assert saleQuote!= null;
        goodMarket.removeSellQuote(saleQuote);
        saleQuote = null;
    }

    public void removeBuyQuote() {
        assert buyQuote != null;
        goodMarket.removeBuyQuote(buyQuote);
        buyQuote = null;
    }

    private Quote createAskQuote(Person p, Market market)
    {

        GoodType sold = market.getGoodType();
        GoodType bought = market.getMoney();
        if(!p.hasAny(sold)) //nothing to sell, really.
            return null;

        float rateOfSubstitution = p.howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(sold,bought);
        if(rateOfSubstitution <=0) //if it can't be done, don't bother
            return null;

        int ceiledRate = (int) Math.ceil(rateOfSubstitution);
        if(Math.abs(ceiledRate - rateOfSubstitution)<.01) //if rate of substitution is already an integer
            ceiledRate++; //then plus one

        assert  ceiledRate >=1; //never sell at 0, even if it's worth 0.

        //for this price the utility will surely increase if it gets filled
        return Quote.newSellerQuote(p,ceiledRate,p.peekGood(sold));


    }


    private Quote createBidQuote(Person p, Market market)
    {
        GoodType bought = market.getGoodType();
        GoodType sold = market.getMoney();

        int moneyOwned =p.hasHowMany(sold); //you have no money to buy
        if(moneyOwned ==0)
            return null;

        assert moneyOwned > 0; //god forbid it's negative

        float rateOfSubstitution = p.howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(bought,sold);
        if(rateOfSubstitution <=0) //if it can't be done, don't bother
            return null;

        int flooredRate = (int) Math.floor(rateOfSubstitution);
        if (flooredRate >0 && Math.abs(flooredRate - rateOfSubstitution)<.01) //if rate of substitution is already an integer
            flooredRate--; //then plus one
        flooredRate = Math.min(moneyOwned,flooredRate);
        assert flooredRate <= moneyOwned;

        return Quote.newBuyerQuote(p,flooredRate,bought);



    }

    @Override
    public void endTradingDay(Person p, MacroII model) {
        if(buyQuote != null)
            removeBuyQuote();
        if(saleQuote != null)
            removeSaleQuote();
        assert saleQuote == null && buyQuote == null;
    }

    @Override
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, Person seller, EconomicAgent buyer) {
        //if it happened during the loop, don't bother with it, the loop will take care of it
        if(placeQuotesLoop)
            return;
        //otherwise
        assert saleQuote != null;
        assert saleQuote.getGood().equals(g);
        assert quoteFilled.equals(saleQuote);
        //forget the sale quote
        saleQuote = null;
        //if you have a buy quote forget it
        if(buyQuote != null)
            removeBuyQuote();

        rescheduleYourself();

    }

    @Override
    public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, Person buyer, EconomicAgent seller) {
        //if it happened during the loop, don't bother with it, the loop will take care of it
        if(placeQuotesLoop)
            return;
        assert buyQuote != null;
        assert buyQuote.equals(quoteFilled);
        //forget it
        buyQuote=null;
        //if you have a sale quote, forget it
        if(saleQuote != null)
            removeSaleQuote();

        rescheduleYourself();


    }


    private void rescheduleYourself()
    {
        model.scheduleSoon(ActionOrder.TRADE,this);
    }

    @Override
    public void step(SimState state) {
        assert ((MacroII)state).getCurrentPhase().equals(ActionOrder.TRADE); //trading here!
        assert p.isActive();

        placeQuotesLoop = true;
        do{
            assert saleQuote==null && buyQuote==null;
            //prepare the quotes
            Quote preparedSaleQuote = createAskQuote(p,goodMarket); //sale
            Quote preparedBidQuote = createBidQuote(p,goodMarket);
            //now try to place it!
            if(preparedSaleQuote != null) { //if we have anything to place
                saleQuote = goodMarket.submitSellQuote(p, preparedSaleQuote.getPriceQuoted(), preparedSaleQuote.getGood());
                if (saleQuote.getPriceQuoted() == -1) //bing! immediately sold!
                {
                    saleQuote = null;
                    continue; //restart the loop
                }
            }
            //otherwise we can do the buy too
            if(preparedBidQuote != null) {
                buyQuote = goodMarket.submitBuyQuote(p,preparedBidQuote.getPriceQuoted());
                if (buyQuote.getPriceQuoted() == -1) //bing! immediately bought!
                {
                    buyQuote = null;
                    //if needed remove the sale quote!
                    if(saleQuote != null)
                    {
                        removeSaleQuote();
                    }
                    continue; //restart the loop
                }
            }
            //if we reach here we may have placed some quotes, the rest is up to react methods
            assert  !(preparedSaleQuote!=null) || saleQuote != null : "sale quote: " + saleQuote + ", prepared sale quote: " + preparedSaleQuote;
            assert  !(preparedBidQuote!=null) || buyQuote != null : "bid quote: " + buyQuote + ", prepared bid quote: " + preparedBidQuote;
            break;
        }
        while(true);
        placeQuotesLoop = false;
    }

    @Override
    public void turnOff() {
        goodMarket.deregisterSeller(p);
        goodMarket.deregisterBuyer(p);
    }
}
