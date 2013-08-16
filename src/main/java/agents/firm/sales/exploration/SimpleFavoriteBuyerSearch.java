/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.exploration;

import agents.EconomicAgent;
import com.sun.istack.internal.Nullable;
import financial.market.Market;
import financial.utilities.PurchaseResult;

/**
 * <h4>Description</h4>
 * <p/> A firm using this method has a favorite buyer it always poll first. It adapts to failure by changing favorite buyer
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
public class SimpleFavoriteBuyerSearch extends SimpleBuyerSearch {


    @Nullable
    private EconomicAgent favorite = null;

    public SimpleFavoriteBuyerSearch(Market market, EconomicAgent firm) {
        super(market, firm);
    }

    /**
     * Return favorite it there is one, or samples at random
     * @return the best buyer available or null if there were none
     */
    @Override
    public EconomicAgent getBestInSampleBuyer() {
        //check the favorite is still in the market!

        if(favorite == null)
            return super.getBestInSampleBuyer();    //use the simple search superclass
        else
        {
            //we have a favorite, but is it still in business?
            if(!getMarket().getBuyers().contains(favorite))
            {
                favorite = null;      //he's dead, Jim.
                return super.getBestInSampleBuyer(); //sample at random
            }
            else
            {
                return favorite;
            }

        }

    }

    /**
     * If successful the match made is the new favorite!
     * @param buyer  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToSuccess(EconomicAgent buyer, PurchaseResult reason) {
        assert favorite == buyer || favorite == null; //only two ways this can happen:
        // you went at random because you didn't have a favorite or this was already your favorite

        assert reason == PurchaseResult.SUCCESS; //ought to be succesful
        favorite = buyer;

    }

    /**
     * If unsuccessful, you aren't my favorite anymore
     * @param buyer  match made
     * @param reason purchase result of the transaction
     */
    @Override
    public void reactToFailure(EconomicAgent buyer, PurchaseResult reason) {

        if(favorite != null) //if I had a favorite
        {
            assert favorite == buyer; //I must have suggested the favorite!
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
