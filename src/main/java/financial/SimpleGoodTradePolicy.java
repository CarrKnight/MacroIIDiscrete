/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.UndifferentiatedGoodType;


/**
 * <h4>Description</h4>
 * <p/> This is the simplest trade policy: two people meet, they exchange a good and they are done.
 * <p/> It's a singleton.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-20
 * @see
 */
public class SimpleGoodTradePolicy implements TradePolicy {

    private static SimpleGoodTradePolicy instance = null;

    private SimpleGoodTradePolicy(){

    }

    /**
     * Get an instance of the strategy (it's a singleton)
     * @return
     */
    public static SimpleGoodTradePolicy getInstance(){
        if(instance == null)
            instance = new SimpleGoodTradePolicy();

        return instance;
    }



    /**
     * This method is called whenever two agents agree on an exchange. It can be called by the market or the agents themselves.
     * @param buyer the buyer
     * @param seller the seller
     * @param good the good being exchanged
     * @param price the price
     */
    @Override
    public PurchaseResult trade( EconomicAgent buyer,  EconomicAgent seller,  Good good, int price,
                                 Quote buyerQuote, Quote sellerQuote, Market market) {


        assert seller.has(good); //the seller should have the good!!


        final UndifferentiatedGoodType money = market.getMoney();
        if(buyer.hasHowMany(money)<price) //check that the buyer has money!
        {
            System.err.println(buyer + " is bankrupt!");
            throw new Bankruptcy(buyer);
        }

        //exchange money
        if(price>0) {
            buyer.deliverMany(money,seller,price);
        }
        else
            assert price == 0;
        //exchange goods
        seller.deliver(good,buyer,price); //the seller gives the good to the buyer


        return PurchaseResult.SUCCESS;
    }


}
