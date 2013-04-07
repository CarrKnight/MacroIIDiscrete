/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;

import javax.annotation.Nonnull;

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
    public PurchaseResult trade(@Nonnull EconomicAgent buyer, @Nonnull EconomicAgent seller, @Nonnull Good good, long price,
                                @Nonnull Quote buyerQuote,@Nonnull Quote sellerQuote, Market market) {


        assert seller.has(good); //the seller should have the good!!

        if(!buyer.hasEnoughCash(price)) //check that the buyer has money!
        {
            System.err.println(buyer + " is bankrupt!");
            throw new Bankruptcy(buyer);
        }


        buyer.pay(price,seller,market); //the buyer pays the seller

        seller.deliver(good,buyer,price); //the seller gives the good to the buyer


        return PurchaseResult.SUCCESS;
    }


}
