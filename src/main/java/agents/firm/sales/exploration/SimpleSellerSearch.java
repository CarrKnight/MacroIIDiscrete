/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.exploration;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;

/**
 * <h4>Description</h4>
 * <p/> When asked to find sellers it just get some at random from the registry
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
public class SimpleSellerSearch implements SellerSearchAlgorithm{


    private Market market;
    private EconomicAgent agent;
    private int searchDepth;

    public SimpleSellerSearch( Market market, EconomicAgent agent) {

        Preconditions.checkNotNull(agent);
        Preconditions.checkNotNull(market);

        this.market = market;
        this.agent = agent;
        searchDepth = agent.getModel().drawNewSimpleSellerSearchSize(agent,market); //draw your search size
    }



    /**
     * look into the seller registry and return what the search algorithm deems the best
     *
     * @return the best seller available!
     */
    @Override
    public EconomicAgent getBestInSampleSeller() {
        EconomicAgent[] sample = sampleSellers(); //sample buyers

        //look for the highest maxOffer in the market
        EconomicAgent lowestSeller=null; //lowest seller
        long lowestOffer = Long.MAX_VALUE; //lowest price
        for(EconomicAgent a : sample)
        {
            final Quote quote = a.askedForASaleQuote(agent, market.getGoodType());
            if(quote==null)
                continue;
            long offer = quote.getPriceQuoted();
            if(offer < lowestOffer && offer >=0)
            {
                lowestSeller = a; //record the best bid so far
                lowestOffer = offer;
            }
        }


        return lowestSeller;


    }

    /**
     * Go to the market and return a sample of sellers
     *
     * @return an array containing a sample of sellers
     */
    @Override
    public EconomicAgent[] sampleSellers() {
        return SimpleBuyerSearch.registrySubsample(market,agent,searchDepth,false);
    }

    /**
     * Not Adapting
     */
    @Override
    public void reactToSuccess(EconomicAgent seller, PurchaseResult reason) {
    }

    /**
     * Not Adapting
     */
    @Override
    public void reactToFailure(EconomicAgent seller, PurchaseResult reason) {
    }


    /**
     * No need to turn off
     */
    @Override
    public void turnOff() {

    }

    public Market getMarket() {
        return market;
    }

    public EconomicAgent getAgent() {
        return agent;
    }
}
