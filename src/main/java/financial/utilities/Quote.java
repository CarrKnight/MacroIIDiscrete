/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

import agents.EconomicAgent;
import agents.firm.Department;
import com.google.common.base.Objects;
import goods.Good;
import goods.GoodType;


/**
 * A quote is a price promise for one good type
 * User: carrknight
 * Date: 7/16/12
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Quote {

    final private EconomicAgent agent;

    final private int priceQuoted;

    /**
     * Bid quotes want a general kind of good, this is it.
     */

    private GoodType type;

    /**
     * Ask quotes sell a SPECIFIC good, this is it
     */

    private Good good;

    /**
     * If this quote was originated by a department within the economic agent, you can record it here
     */

    private Department originator = null;


    private Quote(EconomicAgent agent, int priceQuoted) {
        this.agent = agent;
        this.priceQuoted = priceQuoted;
    }


    public static Quote newSellerQuote( EconomicAgent seller, int priceQuoted, Good good){
        Quote ask = new Quote(seller,priceQuoted);
        ask.good = good;
        ask.type = good.getType();
        return ask;
    }

    public static Quote newBuyerQuote(EconomicAgent buyer, int priceQuoted, GoodType type){
        Quote bid = new Quote(buyer,priceQuoted);
        bid.type = type;
        return bid;
    }

    /**
     * an empty quote is a quote with agent NULL and price -1
     * @return an empty quote
     */
    public static Quote emptySellQuote(Good g){
        Quote nullQuote = new Quote(null,-1);
        nullQuote.good = g;
        return nullQuote;

    }

    /**
     * an empty quote is a quote with agent NULL and price -1
     * @return an empty quote
     */
    public static Quote emptyBidQuote(GoodType t){
        Quote nullQuote = new Quote(null,-1);
        nullQuote.type = t;
        return nullQuote;

    }


    public EconomicAgent getAgent() {
        return agent;
    }

    public int getPriceQuoted() {
        return priceQuoted;
    }


    public Good getGood() {
        return good;
    }

    public GoodType getType() {
        return type;
    }

    public Department getOriginator() {
        return originator;
    }

    public void setOriginator(Department originator) {
        this.originator = originator;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("priceQuoted", priceQuoted)
                .toString();
    }
}
