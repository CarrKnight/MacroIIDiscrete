/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import agents.people.Person;
import agents.firm.Firm;
import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;


/**
 * <h4>Description</h4>
 * <p/> Very simple trade policy, whenever there is a trade, the buyer hires the seller.
 * <p/>
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
public class SimpleHiringTradePolicy implements TradePolicy {



    public PurchaseResult trade(Firm buyer, Person seller, Good good, int price,
                                Quote buyerQuote, Market market)
    {


        assert seller.getEmployer() == null;

        if(buyer.hasHowMany(market.getMoney()) < price) //check that the buyer has money!
            throw new RuntimeException("Hirer is out of a job!");

        seller.hired(buyer,price);
        buyer.hire(seller,buyerQuote.getOriginator());


        return PurchaseResult.SUCCESS;
    }

    @Override
    public PurchaseResult trade( EconomicAgent buyer,  EconomicAgent seller,  Good good, int price,
                                 Quote buyerQuote, Quote sellerQuote, Market market) {
        Preconditions.checkArgument(market.getGoodType().isLabor());
        //make sure arguments make sense
        if(!(buyer instanceof Firm))
            throw new IllegalArgumentException("Buyer is not a firm, it can't hire people!");
        if(!(seller instanceof Person))
            throw new IllegalArgumentException("Seller is not a person, it can't be hired!");

        assert good.getType().isLabor();

        return trade((Firm) buyer, (Person) seller, good,price,buyerQuote, market);

    }
}
