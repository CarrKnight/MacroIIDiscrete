/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.exploration;

import agents.EconomicAgent;
import financial.market.Market;
import financial.utilities.PurchaseResult;

/**
 * <h4>Description</h4>
 * <p/> A firm using this method has a favorite seller it always poll first. It adapts to failure by changing favorite seller
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
public class SimpleFavoriteSellerSearch extends SimpleSellerSearch{

    /**
     * Here we keep in memory the favorite seller.
     */

    private EconomicAgent favorite = null;

    /**
     * An agent using this method has a favorite seller it always poll first. It adapts to failure by changing favorite seller
     * @param market  the market to search into
     * @param agent the agent searching
     */
    public SimpleFavoriteSellerSearch(Market market, EconomicAgent agent) {
        super(market, agent);
    }

    /**
     * Return favorite it there is one, or samples at random
     * @return the best buyer available or null if there were none
     */
    @Override
    public EconomicAgent getBestInSampleSeller() {
        //check the favorite is still in the market!

        if(favorite == null)
            return super.getBestInSampleSeller();    //use the simple search superclass
        else
        {
            //we have a favorite, but is it still in business?
            if(!getMarket().getSellers().contains(favorite))
            {
                favorite = null;      //he's dead, Jim.
                return super.getBestInSampleSeller(); //sample at random
            }
            else
            {
                return favorite;
            }

        }

    }

    /**
     * If successful the match made is the new favorite!
     * @param seller  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToSuccess(EconomicAgent seller, PurchaseResult reason) {
        assert favorite == seller || favorite == null; //only two ways this can happen:
        // you went at random because you didn't have a favorite or this was already your favorite

        assert reason == PurchaseResult.SUCCESS; //ought to be succesful
        assert getMarket().getSellers().contains(seller); //make sure it is a seller!
        favorite = seller;

    }

    /**
     * If unsuccessful, you aren't my favorite anymore
     * @param seller  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToFailure(EconomicAgent seller, PurchaseResult reason) {

        if(favorite != null) //if I had a favorite
        {
            assert favorite == seller; //I must have suggested the favorite!
            favorite = null; //sorry!
        }

    }


    /**
     * Not used
     */
    @Override
    public void turnOff() {
    }
}
