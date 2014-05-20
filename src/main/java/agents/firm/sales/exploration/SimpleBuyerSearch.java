/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.exploration;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.utilities.PurchaseResult;

import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> This search algorithm just samples every day a fixed number of buyers. Has no memory and no adaptation!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-22
 * @see
 */
public class SimpleBuyerSearch implements BuyerSearchAlgorithm {

    /**
     * The market to search
     */
    private Market market;

    /**
     * The firm that is doing the search
     */
    private final EconomicAgent firm;

    /**
     * Max people sampled
     */
    private int searchDepth;

    /**
     * Construct the buyer search given a reference to the market to explore and the firm that owns it
     */
    public SimpleBuyerSearch(Market market, EconomicAgent firm)
    {
        this.market = market;
        this.firm = firm;
        searchDepth = firm.getModel().drawNewSimpleBuyerSearchSize(firm,market); //draw your search size

    }

    /**
     * look into the buyer registry and return what the search algorithm deems the best
     * @return the best buyer available or null if there were none
     */
    @Override
    public EconomicAgent getBestInSampleBuyer() {

        EconomicAgent[] sample = sampleBuyers(); //sample buyers

        //look for the highest maxOffer in the market
        EconomicAgent highestBidder=null; //highest bidder
        long highestOffer = -1; //highest offer
        for(EconomicAgent a : sample)
        {
            long offer = a.askedForABuyOffer(market.getGoodType());
            if(offer > highestOffer)
            {
                highestBidder = a; //record the best bid so far
                highestOffer = offer;
            }
        }


        return highestBidder;



    }

    /**
     * Go to the market and return a sample of buyers
     *
     * @return an array containing a sample of buyers
     */
    @Override
    public EconomicAgent[] sampleBuyers() {
        //just call full depth search and return
        return registrySubsample(market, firm, searchDepth,true);
    }

    /**
     * This search algorithm doesn't adapt
     * @param buyer  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToSuccess(EconomicAgent buyer, PurchaseResult reason) {
    }

    /**
     * This search algorithm doesn't adapt
     * @param buyer  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToFailure(EconomicAgent buyer, PurchaseResult reason) {
    }


    public static EconomicAgent[] registrySubsample(Market market,
                                                    EconomicAgent doNotDraw, int searchDepth, boolean buyers){


        Set<EconomicAgent> toSample;
        if(buyers)
            toSample = market.getBuyers();  //get all the buyers
        else
            toSample = market.getSellers();
        Preconditions.checkNotNull(toSample);
        MersenneTwisterFast random = doNotDraw.getModel().random; //get the randomizer

        int searchSize;
        int visited = 0; //count how many elements of the set you visited
        if(toSample.contains(doNotDraw)){
            searchSize = Math.min(searchDepth,toSample.size()-1);  //if the firm is among the buyers we don't want to sample it
            visited++; //consider the doNotDraw as a visited dude
        }
        else
            searchSize = Math.min(searchDepth, toSample.size());

        EconomicAgent[] sample = new EconomicAgent[searchSize];  //prepare the array where to store the sample

        int remainingToSample = searchSize;

        for(EconomicAgent b : toSample)
        {
            if(remainingToSample == 0) //if you have already drawn enough, quit
                break;
            if(b == doNotDraw) //don't bother drawing yourself
                continue;

            //draw at random with higher probability for
            if(random.nextDouble() < ((double) remainingToSample) / (toSample.size() - visited))
            {
                remainingToSample--;//drawn!
                sample[remainingToSample] = b; //put it in the array (in opposite direction)
            }
            visited++;

        }

        return sample;



    }


    public Market getMarket() {
        return market;
    }

    public EconomicAgent getFirm() {
        return firm;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    /**
     * Not used
     */
    @Override
    public void turnOff() {
    }


    /**
     * set the market where to search
     */
    @Override
    public void setMarket(Market market) {
        this.market = market;
    }
}

