/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.market;

import agents.EconomicAgent;
import agents.firm.Department;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import financial.Bankruptcy;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import model.MacroII;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> Basically accepts quotes from buyers and sellers, but clears them only with the lowest priority of TRADE one by one by asking the buyer to choose
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-03
 * @see
 */
public class GeographicalMarket extends Market implements Steppable{


    private boolean isActive = true;


    /**
     * call the super method and then schedule itself (also registers among the deactivables)
     *
     * @param model
     */
    @Override
    public void start(MacroII model) {
        super.start(model);
        //now schedule yourself
        model.registerDeactivable(this); //so that we get deactivated directly by the model when simulation is over!

        //schedule yourself already!
        model.scheduleSoon(ActionOrder.TRADE,this, Priority.FINAL);
    }

    /**
     * here we store both all the firms that made a quote and their quotes if multiples. It's handy because
     * it keeps the iteration order steady
     */
    private final Multimap<GeographicalFirm,Quote> sellersWhoPlacedAQuote;

    /**
     * the multimap "buyersWhoPlacedAQuote" uses this map as the workhorse. I am keeping it as a separate field that is observable
     * so that it takes care of all the listeners blah blah
     *
     */
    private final ObservableMap<GeographicalCustomer,Collection<Quote>> buyerBackerMap;


    /**
     * here we store both all the firms that made a quote and their quotes if multiples. It's handy because
     * it keeps the iteration order steady                     -
     */
    private final Multimap<GeographicalCustomer,Quote> buyersWhoPlacedAQuote;

    /**
     * this is set to false when we are going through the quotes to clear them, and set to true whenever
     */
    private boolean changedMap = false;

    /**
     * this is used to cycle through the buyers
     */
    private int indexToLoopTo = 0;


    public GeographicalMarket(GoodType goodType) {
        super(goodType);

        //the buyers quote are kept in a priority queue from the highest to the lowest
        //but because these are oilCustomers only, I can "cheat" and order the keys too by
        TreeMap<GeographicalCustomer, Collection<Quote>> backingBuyerTreeMapPOJO = new TreeMap<>(new Comparator<GeographicalCustomer>() {
            @Override
            public int compare(GeographicalCustomer o1, GeographicalCustomer o2) {
                int priceComparison =  -Long.compare(o1.getMaxPrice(), o2.getMaxPrice());
                if(priceComparison != 0)
                    return priceComparison;
                else
                    return Integer.compare(o1.hashCode(),o2.hashCode()); //otherwise just order them by hash

            }
        });
        //now we are going to encase the treemap in an observable property so that I don't have to write a million listeners there
        buyerBackerMap = FXCollections.observableMap(backingBuyerTreeMapPOJO);

        buyersWhoPlacedAQuote = Multimaps.newMultimap(
                buyerBackerMap,
                new Supplier<PriorityQueue<Quote>>() {
                    @Override
                    public PriorityQueue<Quote> get() {
                        return new PriorityQueue<>(1,new Comparator<Quote>() {
                            @Override
                            public int compare(Quote o1, Quote o2) {
                                return -Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());


                            }

                        });
                    }
                });



        //sellers quote are sorted from lowest to highest
        sellersWhoPlacedAQuote = Multimaps.newMultimap(
                new LinkedHashMap<>(),
                () -> new PriorityQueue<>(1, (o1, o2) -> Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted()))
        );


        //make sure the buyers and sellers were initialized
        assert getBuyers() != null;
        assert getBuyers()!=null;

        //you'll schedule yourself in the start


    }




    /**
     * Only accept hasLocation buyers!
     */
    @Override
    public void registerBuyer(EconomicAgent buyer) {
        Preconditions.checkArgument(buyer instanceof GeographicalCustomer, "only geographical agents accepted!");
        super.registerBuyer(buyer);

    }

    /**
     * Add a seller to the registry as they enter the market
     *
     * @param seller buyer entering the market
     */
    @Override
    public void registerSeller( EconomicAgent seller) {
        Preconditions.checkArgument(seller instanceof GeographicalFirm, "only geographical agents accepted!");
        super.registerSeller(seller);
    }


    /**
     * Sellers are supposed to quote
     */

    @Override
    public ActionsAllowed getSellerRole() {
        return ActionsAllowed.QUOTE;
    }

    /**
     * Buyers are supposed to quote
     */
    @Override
    public ActionsAllowed getBuyerRole() {
        return ActionsAllowed.QUOTE;
    }

    /**
     * Submit a sell quote on a specific good
     *
     * @param seller the agent making the sale
     * @param price  the price at/above which the agent is willing to sell
     * @param good   the good to sell
     * @return the quote made
     */

    @Override
    public Quote submitSellQuote( EconomicAgent seller, int price,  Good good)
    {
        return submitSellQuote(seller, price, good,null);


    }

    /**
     * Submit a sell quote on a specific good
     *
     * @param seller     the agent making the sale
     * @param price      the price at/above which the agent is willing to sell
     * @param good       the good to sell
     * @param department the department making the order or null if it's done by the agent himself
     * @return the quote made
     */

    @Override
    public Quote submitSellQuote( EconomicAgent seller, int price,  Good good,  Department department)
    {
        assert getSellers().contains(seller);
        assert seller instanceof GeographicalFirm;
        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            Preconditions.checkState(seller.getModel().getCurrentPhase().equals(ActionOrder.TRADE));
        Preconditions.checkArgument(price>=0);





        //ignore the quote
        GeographicalFirm sellerCast = (GeographicalFirm) seller;
        //put the quote in the collection and return it
        Quote quoteMade = Quote.newSellerQuote(seller, price, good);
        if(department!=null)
            quoteMade.setOriginator(department);
        sellersWhoPlacedAQuote.put(sellerCast, quoteMade);
        assert quoteMade.getPriceQuoted() >=0;



        //map has changed!
        changedMap = true;


        return quoteMade;
    }

    /**
     * If the seller changes its mind and wants to remove its sell quote, call this
     *
     * @param q quote to cancel
     */
    @Override
    public void removeSellQuote(Quote q) {


        boolean removedCorrectly =  sellersWhoPlacedAQuote.remove(q.getAgent(),q);
        Preconditions.checkState(removedCorrectly,"failed to remove correctly");

        //either you have been removed from the multimap or you had another quote!
        assert !sellersWhoPlacedAQuote.containsKey(q.getAgent()) ||
                (!sellersWhoPlacedAQuote.get((GeographicalFirm) q.getAgent()).isEmpty() && !sellersWhoPlacedAQuote.get((GeographicalFirm)q.getAgent()).contains(q));

        //map has changed!
        changedMap = true;


    }

    @Override
    public void removeSellQuotes(Collection<Quote> quotes)
    {
        for(Quote q : quotes)
        {
            boolean removedCorrectly =  sellersWhoPlacedAQuote.remove(q.getAgent(),q);
            if(removedCorrectly)
            {
                assert !sellersWhoPlacedAQuote.containsKey(q.getAgent()) ||
                        (!sellersWhoPlacedAQuote.get((GeographicalFirm) q.getAgent()).isEmpty() && !sellersWhoPlacedAQuote.get((GeographicalFirm)q.getAgent()).contains(q));

                //map has changed!
                changedMap = true;
            }
        }

    }

    /**
     * Submit a buy quote
     *
     * @param buyer      the agent trying to buy
     * @param price      the price at/below which the agent is willing to buy
     * @param department the department making the order or null if it was done by the economic agent himself
     * @return quote made
     */

    @Override
    public Quote submitBuyQuote( EconomicAgent buyer, int price,  Department department) {
        assert getBuyers().contains(buyer);
        assert buyer instanceof GeographicalCustomer;
        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            Preconditions.checkState(buyer.getModel().getCurrentPhase().equals(ActionOrder.TRADE));

        Preconditions.checkArgument(price>=0);



        //ignore the quote
        GeographicalCustomer buyerCast = (GeographicalCustomer) buyer;
        //put the quote in the collection and return it
        Quote quoteMade = Quote.newBuyerQuote(buyer, price,goodType);
        if(department!=null)
            quoteMade.setOriginator(department);
        buyersWhoPlacedAQuote.put(buyerCast, quoteMade);
        assert quoteMade.getPriceQuoted() >=0;



        //map has changed!
        changedMap = true;

        return quoteMade;
    }


    @Override
    public void removeBuyQuotes(Collection<Quote> quotes) {
        for(Quote q : quotes)
        {
            boolean removedCorrectly =  buyersWhoPlacedAQuote.remove(q.getAgent(),q);

            if(removedCorrectly) {
                //either you have been removed from the multimap or you had another quote!
                assert !buyersWhoPlacedAQuote.containsKey(q.getAgent()) ||
                        (!buyersWhoPlacedAQuote.get((GeographicalCustomer) q.getAgent()).isEmpty() && !buyersWhoPlacedAQuote.get((GeographicalCustomer) q.getAgent()).contains(q));

                //map has changed!
                changedMap = true;
            }
        }
    }

    /**
     * Submit a buy quote
     *
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @return quote made
     */

    @Override
    public Quote submitBuyQuote( EconomicAgent buyer, int price) {
        return this.submitBuyQuote(buyer,price,null);

    }

    /**
     * If the buyer changes its mind and wants to remove its purchase quote, call this
     *
     * @param q quote to cancel
     */
    @Override
    public void removeBuyQuote(Quote q) {
        boolean removedCorrectly =  buyersWhoPlacedAQuote.remove(q.getAgent(),q);
        Preconditions.checkState(removedCorrectly,"failed to remove buy quote correctly");

        //either you have been removed from the multimap or you had another quote!
        assert !buyersWhoPlacedAQuote.containsKey(q.getAgent()) ||
                (!buyersWhoPlacedAQuote.get((GeographicalCustomer) q.getAgent()).isEmpty() && !buyersWhoPlacedAQuote.get((GeographicalCustomer)q.getAgent()).contains(q));

        //map has changed!
        changedMap = true;
    }


    /**
     * Remove all these quotes by the buyer
     *
     *
     * @param buyer the buyer whose quotes we want to clear
     * @return the set of quotes removed
     */
    @Override
    public Collection<Quote> removeAllBuyQuoteByBuyer(EconomicAgent buyer) {

        if(buyersWhoPlacedAQuote.containsKey(buyer))
        {
            changedMap = true;
            return  buyersWhoPlacedAQuote.removeAll(buyer);

        }
        else
            return new HashSet<>();
    }

    /**
     * Remove all these quotes by the seller
     *
     * @param seller the seller whose quotes we want to clear
     * @return the set of quotes removed
     */
    @Override
    public Collection<Quote> removeAllSellQuoteBySeller(EconomicAgent seller) {
        if(sellersWhoPlacedAQuote.containsKey(seller))
        {
            changedMap = true;
            return  sellersWhoPlacedAQuote.removeAll(seller);

        }
        else
            return new HashSet<>();

    }

    /**
     * asks the market if users are allowed to see the best price for a good on sale
     */
    @Override
    public boolean isBestSalePriceVisible() {
        return true;
    }

    /**
     * Asks the market to return the best (lowest) price for a good on sale at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public int getBestSellPrice() throws IllegalAccessException {
        if(sellersWhoPlacedAQuote.isEmpty())
            return -1;


        //go through all the quotes and find the cheapest

        Quote lowestQuote = getLowestSellerQuote();
        assert lowestQuote.getPriceQuoted()>=0;

        return lowestQuote.getPriceQuoted();

    }

    private Quote getLowestSellerQuote() {
        return Collections.min(sellersWhoPlacedAQuote.values(), new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return Long.compare(o1.getPriceQuoted(), o2.getPriceQuoted());

            }
        });
    }

    private Quote getHighestBuyerQuote() {
        return Collections.max(buyersWhoPlacedAQuote.values(), new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return Long.compare(o1.getPriceQuoted(), o2.getPriceQuoted());

            }
        });
    }

    /**
     * Asks the market to return the owner of the best ask price in the market
     *
     * @return the best seller or null if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */

    @Override
    public EconomicAgent getBestSeller() throws IllegalAccessException {
        if(sellersWhoPlacedAQuote.isEmpty())
            return null;


        //go through all the quotes and find the cheapest

        Quote lowestQuote = getLowestSellerQuote();
        return lowestQuote.getAgent();
    }

    /**
     * asks the market if users are allowed to see the best offer to buy a good
     */
    @Override
    public boolean isBestBuyPriceVisible() {
        return true;
    }

    /**
     * Asks the market to return the best (highest) offer for buying a good at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
    +     */
    @Override
    public int getBestBuyPrice() throws IllegalAccessException {

        if(buyersWhoPlacedAQuote.isEmpty())
            return -1;


        //go through all the quotes and find the cheapest

        Quote highestQuote = getHighestBuyerQuote();
        assert highestQuote.getPriceQuoted()>=0;

        return highestQuote.getPriceQuoted();

    }

    /**
     * Asks the market to return the owner of the best offer in the market
     *
     * @return the best buyer or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestBuyer() throws IllegalAccessException {

        if(buyersWhoPlacedAQuote.isEmpty())
            return null;


        //go through all the quotes and find the cheapest

        Quote highestQuote = getHighestBuyerQuote();
        assert highestQuote.getPriceQuoted()>=0;

        return highestQuote.getAgent();

    }

    /**
     * Can I get an iterator to cycle through all the quotes?
     *
     * @return true if it's possible
     */
    @Override
    public boolean areAllQuotesVisibile() {
        return false;
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    @Override
    public Iterator<Quote> getIteratorForBids() throws IllegalAccessException {
        throw new IllegalAccessError();
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    @Override
    public Iterator<Quote> getIteratorForAsks() throws IllegalAccessException {
        throw new IllegalAccessError();
    }

    /**
     * this happens at FINAL priority, so all quotes have been settled.
     * It keeps rescheduling itself to iterate one more buyer
     * @param state
     */
    @Override
    public void step(SimState state) {

        if(!isActive)
            return;

        MacroII model = (MacroII)state;
        /**
         * if the map has changed since the last step, reset the iterator
         */
        if(changedMap)
        {
            indexToLoopTo = 0;
            changedMap = false;
        }

        //let's go!
        Iterator<GeographicalCustomer> buyerIterator = buyersWhoPlacedAQuote.keySet().iterator();
        for(int i=0; i<indexToLoopTo; i++)
        {
            assert buyerIterator.hasNext();   //should never fail, because we have been here before!
            buyerIterator.next(); //already processed
        }
        //if there are no more buyers or sellers, we are done!
        if(!buyerIterator.hasNext() || sellersWhoPlacedAQuote.isEmpty())
        {
            //restart this tomorrow
            indexToLoopTo = 0;
            model.scheduleTomorrow(ActionOrder.TRADE,this,Priority.FINAL);
            return;
        }

        //okay, then there is another buyer to process, let's do this
        GeographicalCustomer currentBuyer = buyerIterator.next();
        //make him choose among all the possible buyers
        GeographicalFirm sellerChosen = currentBuyer.chooseSupplier(Multimaps.unmodifiableMultimap(sellersWhoPlacedAQuote));

        if(sellerChosen!=null)
        {
            //remove the seller quote
            Quote sellerQuoteChosen = sellersWhoPlacedAQuote.get(sellerChosen).iterator().next();

            //remove the buyer quote
            Quote buyerQuoteToRemove = buyersWhoPlacedAQuote.get(currentBuyer).iterator().next();


            //make them trade!
            int finalPrice = pricePolicy.price(sellerQuoteChosen.getPriceQuoted(), buyerQuoteToRemove.getPriceQuoted());
            //make them trade!
            Good goodBought = sellerQuoteChosen.getGood();
            PurchaseResult result = trade(currentBuyer,sellerChosen, goodBought,finalPrice,sellerQuoteChosen,buyerQuoteToRemove);
            if(result == PurchaseResult.BUYER_HAS_NO_MONEY)
                throw new Bankruptcy(currentBuyer);
            assert result == PurchaseResult.SUCCESS;

            //remove the two crossing quotes
            boolean removedCorrectly = sellersWhoPlacedAQuote.remove(sellerChosen,sellerQuoteChosen);
            assert removedCorrectly;
            removedCorrectly = buyersWhoPlacedAQuote.remove(currentBuyer,buyerQuoteToRemove);
            assert removedCorrectly;

            //reactions!
            currentBuyer.reactToFilledBidQuote(buyerQuoteToRemove, goodBought, finalPrice, sellerChosen);
            sellerChosen.reactToFilledAskedQuote(sellerQuoteChosen, goodBought, finalPrice, currentBuyer);

        }
        else
        {
            //move on!
            indexToLoopTo++;
        }

        //reschedule yourself the same day!
        model.scheduleSoon(ActionOrder.TRADE,this,Priority.FINAL);


    }

    @Override
    public void turnOff()
    {
        super.turnOff();
        isActive = false;
    }



    public ObservableMap<GeographicalCustomer, Collection<Quote>> getBuyerBackerMap() {
        return buyerBackerMap;
    }
}
