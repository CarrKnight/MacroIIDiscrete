/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleSellerSearch;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import financial.Bankruptcy;
import financial.market.Market;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> A simple consumer that doesn't save. It has a fixed demand in terms of how much
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-17
 * @see
 */
public class DummyPerson extends Person{

    /**
     * how much this person is willing to pay for a specific good (basically its "max price").
     */
    Table<GoodType,DemandComponent,Long> demand;

    /**
     * Stores the quotes made by the consumer to buy something
     */
    EnumMap<GoodType,Quote> quotesMade;

    /**
     *  Remembers which market for which good
     */
    EnumMap<GoodType,Market> markets;

    /**
     *  Remembers the status of the consumer for each good they demand
     */
    EnumMap<GoodType,ShoppingStatus> status;

    /**
     * Stores the search algorithm for each good demand
     */
    EnumMap<GoodType,SellerSearchAlgorithm> shopSearch;


    /**
     * Creates a dummy person( for now it has no demand!)
     * @param model the model object
     * @param cash initial cash
     * @param minimumWageRequired how much wage is needed to accept a job offer
     * @param laborMarket a link to the labor market
     */
    public DummyPerson(@Nonnull MacroII model, long cash, int minimumWageRequired,@Nonnull Market laborMarket) {
        super(model, cash, minimumWageRequired, laborMarket);
        //create a demand table
        demand = TreeBasedTable.create();
        //create a map of quotes
        quotesMade = new EnumMap<>(GoodType.class);
        markets = new EnumMap<>(GoodType.class);
        status = new EnumMap<>(GoodType.class);
        shopSearch = new EnumMap<>(GoodType.class);
    }


    /**
     * Add (or replace) the demand for a specific good
     * @param type type of good demanded
     * @param price the price for that good
     * @param consumptionTime the time it takes to consume such good
     * @param shoppingSpeed how much time it takes to try again at shopping if there can't be quotes
     * @param market the market in which the good should be bought
     */
    public void addDemand(@Nonnull GoodType type,long price,long consumptionTime,long shoppingSpeed,@Nonnull Market market)
    {

        this.addDemand(type,price,consumptionTime,shoppingSpeed,market,new SimpleSellerSearch(market,this));

    }

    /**
     * Add (or replace) the demand for a specific good
     * @param type type of good demanded
     * @param price the price for that good
     * @param consumptionTime the time it takes to consume such good
     * @param shoppingSpeed how much time it takes to try again at shopping if there can't be quotes
     * @param market the market in which the good should be bought
     * @param shopSearch the search algorithm to use to search for a good
     */
    public void addDemand(@Nonnull GoodType type,long price,long consumptionTime,long shoppingSpeed, @Nonnull Market market,@Nonnull SellerSearchAlgorithm shopSearch)
    {

        assert price >= 0; assert consumptionTime >=0;
        //check that the market is correct
        Preconditions.checkArgument(market.getGoodType() == type);


        //add to demand
        demand.put(type,DemandComponent.MAX_PRICE,price);
        demand.put(type,DemandComponent.CONSUMPTION_TIME,consumptionTime);
        demand.put(type,DemandComponent.SHOPPING_SPEED,shoppingSpeed);
        this.shopSearch.put(type,shopSearch);
        markets.put(type,market);

        assert status.get(type) != ShoppingStatus.READY; //shouldn't in general be ready

        //if it's new go buy!
        if(!status.containsKey(type))
            buy(type);
        else{
            if(status.get(type) == ShoppingStatus.WAITING_FOR_MONEY)
                //check if now you can place the quote
                checkForTooHighQuotes(null);
        }

    }


    /**
     * this is called when demand is updated while we were waiting for a quote. It removes the quote and start over
     * @param q
     */
    private void updateQuote(GoodType type)
    {
        assert status.get(type) == ShoppingStatus.WAITING_FOR_QUOTE;
        assert quotesMade.containsKey(type);
        assert quotesMade.get(type) != null;
        //remove the quote
        markets.remove(quotesMade.get(type));
        quotesMade.put(type,null);
        //try again
        status.put(type,ShoppingStatus.READY);
        buy(type);
    }





    /**
     * What composes the demand for any good
     */
    public enum DemandComponent
    {

        /**
         * How much time it takes to consume/eat something
         */
        CONSUMPTION_TIME,

        /**
         * Max price the consumer is willing to pay for a good
         */
        MAX_PRICE,

        /**
         * If there is no order book how much time it takes to try again!
         */
        SHOPPING_SPEED



    }

    /**
     * What is the consumer doing regarding its goods demand
     */
    public enum ShoppingStatus
    {



        /**
         * The consumer placed a quote but it is waiting for it to be filled
         */
        WAITING_FOR_QUOTE,

        /**
         * The consumer is searching the market (can't place quotes so it'll never know if the price is good enough)
         */
        SHOPPING,

        /**
         * The consumer has bought stuff and he is in the process of consuming it
         */
        CONSUMING,

        /**
         * The consumer can't place a new quote until he gets more money!
         */
        WAITING_FOR_MONEY,

        /**
         * The consumer is free and about to do something about its goods very soon!
         */
        READY

    }

    /**
     * This is called by the person if he can't place quotes
     * @param type the good type to use
     */
    private void shop(@Nonnull final GoodType type){
        //todo this is just a modified code from PurchaseDepartent, I don't like code duplication

        //make sure we are called at the right status
        ShoppingStatus thisStatus = status.get(type);
        assert thisStatus != null;
        assert thisStatus == ShoppingStatus.READY || thisStatus == ShoppingStatus.SHOPPING;
        assert markets.get(type).getBuyerRole() == ActionsAllowed.SEARCH;

        //we are now shopping!
        status.put(type,ShoppingStatus.SHOPPING);


        //search for a shop
        EconomicAgent seller = shopSearch.get(type).getBestInSampleSeller();
        //if we couldn't find any
        if(seller == null){
            shopSearch.get(type).reactToFailure(seller, PurchaseResult.NO_MATCH_AVAILABLE);

            //try again soon
            getModel().schedule.scheduleOnceIn(tryAgainNextTime(type), new Steppable() {
                @Override
                public void step(SimState simState) {
                    shop(type); //call shop again soon!
                }
            });
        }
        else
        {
            //we found somebody!
            Quote sellerQuote = seller.askedForASaleQuote(this, type); //ask again for a price offer
            assert sellerQuote.getPriceQuoted() >= 0 ; //can't be negative!!!
            if(demand.get(type,DemandComponent.MAX_PRICE) >= sellerQuote.getPriceQuoted()) //if the match is good:
            {
                Market market = markets.get(type); assert market != null;
                //get the final price
                long finalPrice = market.price(sellerQuote.getPriceQuoted(),demand.get(type,DemandComponent.MAX_PRICE));

                //build a fake buyer quote for stat collection
                Quote buyerQuote = Quote.newBuyerQuote(this,demand.get(type,DemandComponent.MAX_PRICE),type);

                //TRADE
                market.trade(this,seller,sellerQuote.getGood(),finalPrice,buyerQuote,sellerQuote);



                shopSearch.get(type).reactToSuccess(seller, PurchaseResult.SUCCESS);

                //make sure you have at least one of the goods you want
                assert hasAny(type);
                //start consumption
                status.put(type,ShoppingStatus.CONSUMING);
                eat(type);

                //now the consumption-code should have kicked in (or consumpion time is 0)
                assert status.get(type) == ShoppingStatus.CONSUMING ||
                        (status.get(type) ==ShoppingStatus.READY && demand.get(type,DemandComponent.CONSUMPTION_TIME) == 0);


            }
            else{   //the match is not good! try again soon
                shopSearch.get(type).reactToFailure(seller, PurchaseResult.PRICE_REJECTED);
                getModel().schedule.scheduleOnceIn(tryAgainNextTime(type), new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        assert status.get(type) == ShoppingStatus.SHOPPING;
                        shop(type); //call shop again soon!
                    }
                });
            }

        }



    }


    /**
     * Place a new quote to buy a good, if you have the money
     * @param type the good you are trying to buy
     */
    public void placeQuote(@Nonnull GoodType type)
    {
        //there shouldn't be a previous quote
        assert !quotesMade.containsKey(type) || quotesMade.get(type) == null;
        assert status.get(type) == ShoppingStatus.READY; //you should be ready
        assert markets.get(type).getBuyerRole() == ActionsAllowed.QUOTE;

        //do you have enough money?
        if(!hasEnoughCash(demand.get(type,DemandComponent.MAX_PRICE)))
        {
            //if you are here you don't have enough!
            assert getCash() < demand.get(type,DemandComponent.MAX_PRICE);
            //you'll have to wait till the money comes
            status.put(type,ShoppingStatus.WAITING_FOR_MONEY);

            return;
        }


        //just place a quote, you'll be fine
        assert getCash() >= demand.get(type,DemandComponent.MAX_PRICE);
        status.put(type,ShoppingStatus.WAITING_FOR_QUOTE);


        Quote q = markets.get(type).submitBuyQuote(this,demand.get(type,DemandComponent.MAX_PRICE));

        //if you returned a null quote, it was immediately filled!
        if(q.getPriceQuoted() == -1) //if the quote is null
        {
            //you must be consuming/ immediate consumption
            assert  status.get(type) == ShoppingStatus.CONSUMING ||
                    (status.get(type) == ShoppingStatus.READY && demand.get(type,DemandComponent.CONSUMPTION_TIME) == 0);

            //there shouldn't be any quote
            assert !quotesMade.containsKey(type) || quotesMade.get(type) == null;


            //let consumption takes care of the rest
        }
        else
        {
            assert q.getAgent() == this; //make sure we got back the right quote
            assert q.getPriceQuoted() >= 0; //make sure it's positive!

            //the quote is valid, let's wait
            quotesMade.put(type,q);
        }

    }


    /**
     * Tell the buyer to either shop/place quote
     * @param goodType the good to buy
     */
    public void buy(@Nonnull GoodType goodType)
    {
        //get the market
        Market m = markets.get(goodType);
        assert m != null;

        if(!status.containsKey(goodType)) //if you don't have a status, now you do
            status.put(goodType,ShoppingStatus.READY);
        //you should be ready
        assert status.get(goodType) == ShoppingStatus.READY;

        if(m.getBuyerRole() == ActionsAllowed.QUOTE)
        {
            placeQuote(goodType);
        }
        else
        {
            shop(goodType);
        }




    }

    /**
     * basically schedules itself to consume the good and try to rebuy later.
     * If the good was bought through a "quote" then it is called by reactToFilledQuote, if it is found through shopping then the method is called by "shop()"
     */
    public void eat(final GoodType type)
    {
        assert status.get(type) == ShoppingStatus.CONSUMING; //either reactToFilledBidQuote or receive must have set the status to consuming before calling the method!
        assert hasAny(type); //you should have one to consume!

        //does it take time to eat?
        if(demand.get(type,DemandComponent.CONSUMPTION_TIME) == 0) //eating is immediate!
        {
            //eat it
            consume(type);
            //set the status to ready
            status.put(type,ShoppingStatus.READY);
            //buy it, again!
            buy(type);
        }
        else{
            //it takes time, schedule yourself!
            getModel().schedule.scheduleOnceIn(
                    Math.max(0.01, Math.abs( demand.get(type,DemandComponent.CONSUMPTION_TIME)+ getModel().drawRandomSchedulingNoise())),
                    new Steppable() {
                        @Override
                        public void step(SimState simState) {

                            //eat it
                            consume(type);
                            //set the status to ready
                            status.put(type,ShoppingStatus.READY);
                            //buy it, again!
                            buy(type);

                        }
                    }
            );

        }

    }


    /**
     * React to filled quote checks that you were actually waiting for a quote, calls eat() and removes the quote from memory. Eat will call buy() again!
     * @param g the good bought
     * @param price the price paid for it!
     * @param seller who sold it to us!
     */
    @Override
    public void reactToFilledBidQuote(@Nonnull Good g, long price,@Nonnull EconomicAgent seller) {

        //you should have been waiting for the good!
        assert status.get(g.getType()) == ShoppingStatus.WAITING_FOR_QUOTE;

        //you might have a quote in your memory but maybe not (depending on whether it was immediate or not)
        quotesMade.put(g.getType(),null); //empty it, just in case

        //call "eat" to consume it
        status.put(g.getType(),ShoppingStatus.CONSUMING); //consuming mode!
        eat(g.getType());

    }



    /**
     * How much time to wait between one shopping spree and another?
     */
    public float tryAgainNextTime(@Nonnull GoodType type)
    {
        float shoppingSpeed = demand.get(type, DemandComponent.SHOPPING_SPEED);

        return (float) Math.max(0.01, Math.abs(shoppingSpeed + getModel().drawRandomSchedulingNoise()));

    }

    /**
     * Returns the shopping status of the agent for this particular good
     */
    public @Nullable ShoppingStatus getStatus(@Nonnull GoodType type){
        return status.get(type);

    }

    /**
     * Returns a cell of the demand table
     */
    public @Nullable Long getDemand(@Nonnull GoodType goodType, @Nonnull DemandComponent component)
    {
        return  demand.get(goodType,component);
    }


    /**
     * Returns the quote in memory we are waiting for
     */
    public @Nullable Quote getQuote(GoodType goodType)
    {
        Quote q = quotesMade.get(goodType);
        assert q == null || status.get(goodType) == ShoppingStatus.WAITING_FOR_QUOTE;
        return q;

    }


    /**
     * When the person earns new money, it checks if it was waiting for money to place quotes; if so it places them!
     */
    @Override
    public void earn(long money) {
        super.earn(money);
        //go through all the possible good-types
        Set<GoodType> types = status.keySet();
        for(GoodType type : types)
        {
            //if you are waiting for money
            if(status.get(type) == ShoppingStatus.WAITING_FOR_MONEY)
            {
                //try again!
                status.put(type,ShoppingStatus.READY);
                buy(type);
            }

        }

    }

    /**
     * This method is called when the person pays out some money. It checks the quotes it has in place and remove the ones that NOW he can't afford.
     * @param beingPaid if this if fired by pay() ignore the beingPaid good as it might be in the process of being filled!
     */
    private void checkForTooHighQuotes(GoodType beingPaid){

        //go through all the possible good-types
        Set<GoodType> types = status.keySet();
        for(GoodType type : types)
        {
            Quote q = quotesMade.get(type);
            assert (q == null && status.get(type) != ShoppingStatus.WAITING_FOR_QUOTE) ||
                    (q != null && status.get(type) == ShoppingStatus.WAITING_FOR_QUOTE) ||
                    (type == beingPaid) : q + " --- "+ q.getType() + " --- " + beingPaid;

            if(q != null && q.getType() != beingPaid && q.getPriceQuoted() > getCash())
            {
                //remove the quote that now is too expensive!
                markets.get(type).removeBuyQuote(q);
                //set your status to waiting for money
                status.put(type,ShoppingStatus.WAITING_FOR_MONEY);
            }

        }
    }


    /**
     * This is probably used only in testing.
     *
     * @param money
     * @throws financial.Bankruptcy
     */
    @Override
    public void burnMoney(long money) throws Bankruptcy {
        super.burnMoney(money);
        checkForTooHighQuotes(null);
    }

    /**
     * Removes cash from this agent and transfers it to the receiver
     *
     * @param money    how much money paid
     * @param receiver who should receive it?
     * @throws financial.Bankruptcy if you end up with negative money. If thrown the receiver received nothing!
     */
    @Override
    public void pay(long money, @Nonnull EconomicAgent receiver,Market reason) throws Bankruptcy {
        super.pay(money, receiver, reason);
        if(reason != null)
            checkForTooHighQuotes(reason.getGoodType());
        else
            checkForTooHighQuotes(null);

    }

    /**
     * reset cash holdings to 0
     */
    @Override
    public void zeroCash() {
        super.zeroCash();
        checkForTooHighQuotes(null);

    }


    private String name = null;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {

        if(name == null)
            return super.toString();
        else
            return "person: " + name;

    }
}
