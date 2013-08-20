/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import agents.EconomicAgent;
import agents.firm.Department;
import financial.*;
import financial.utilities.*;
import goods.Good;
import goods.GoodType;
import lifelines.LifelinesPanel;
import lifelines.data.DataManager;
import lifelines.data.GlobalEventData;
import model.MacroII;
import model.utilities.ExchangeNetwork;
import model.utilities.stats.collectors.MarketData;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.*;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The original interface of the good market. As of now, completely empty.
 * User: Ernesto
 * Date: 7/5/12
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Market{

    /**
     * The array holding all the trade listeners.
     */
    private List<TradeListener> tradeListeners = new LinkedList<>();

    /**
     * The array holding all the bid listeners. It's protected because subclasses need to use it!
     */
    protected LinkedList<BidListener> bidListeners = new LinkedList<>();

    /**
     * How many goods were traded this week
     */
    protected int weeklyVolume = 0;

    /**
     * How many goods were traded yesterday
     */
    protected int yesterdayVolume = 0;

    /**
     * How many goods have been traded so far today
     */
    protected int todayVolume = 0;


    /**
     * What was the last closing price yesterday?
     */
    protected long yesterdayLastPrice = 0;

    /**
     * last price sold
     */
    protected long lastPrice = -1;

    protected float todaySumOfClosingPrices = 0;


    protected float yesterdaySumOfClosingPrices = 0;

    /**
     * last filled bid
     */
    protected long lastFilledBid = -1;

    /**
     * last filled ask
     */
    protected long lastFilledAsk = -1;

    /**
     * A time series holding on all the sales records
     */
    protected TimeSeries prices = new TimeSeries("prices","time","prices");

    /**
     * A time series holding on all the weekly volume sales
     */
    protected TimeTableXYDataset volume = new TimeTableXYDataset(TimeZone.getDefault());


    /**
     * A time series of the markups in the market;
     */
    protected TimeSeries markups = new TimeSeries("markups","time","markups");

    /**
     * If this is true, there is no data collection. Useful for testing only
     */
    public static boolean  TESTING_MODE = false;

    /**
     * the inspector showing the closing prices
     */
    Inspector closingPriceInspector;

    /**
     * If the gui is on and we want the timeline this object keeps its data
     */
    private TimelineManager records;

    /**
     *if the GUI is on, here we'll keep information/visualization info regarding the exchange network
     */
    private ExchangeNetwork network;

    /**
     * The type of good being sold here
     */
    final protected GoodType goodType;
    /**
     * volume traded last week
     */
    private int lastWeekVolume;

    /**
     * the data collector, created at start()
     */
    private MarketData marketData;

    protected Market(GoodType goodType) {
        this.goodType = goodType;
        //if the market is a labor market, buyers hire and sellers get hired. Otherwise buyers receive and sellers earn
        if(goodType.isLabor())
            policy = new SimpleHiringTradePolicy();
        else
            policy = SimpleGoodTradePolicy.getInstance();



        //build the gui inspector
        if(MacroII.hasGUI()){
            //create the panel holding the timeline
            GlobalEventData.getInstance().reset();

            //create the panel that will hold the records
            recordPanel = new LifelinesPanel(null,new Dimension(500,500));

            records = new TimelineManager(recordPanel);


            //create the network
            network = new ExchangeNetwork(getGoodType());
            buildInspector();



        }
    }



    /**
     * What is the seller role in this market?
     * @return the actions allowed to the seller in this market
     */
    @Nonnull
    abstract public ActionsAllowed getSellerRole();

    /**
     * What is the buyer role in this market?
     * @return the actions allowed to the buyer in this market
     */
    abstract public ActionsAllowed getBuyerRole();


    private final Set<EconomicAgent> buyers = new HashSet<>();

    /**
     * Get all agents that belong in the market as buyers
     * @return all people who somehow can buy in this market
     */
    @Nonnull
    public Set<EconomicAgent> getBuyers(){
        return Collections.unmodifiableSet(buyers);
    }

    /**
     * Strategy to resolve a trade
     */
    private final TradePolicy policy;

    /**
     * Add a buyer to the registry as they enter the market
     * @param buyer buyer entering the market
     */
    public void registerBuyer(EconomicAgent buyer){
        boolean isNew = buyers.add(buyer);   //addSalesDepartmentListener it to the set
        assert isNew;   //make sure it wasn't there before!

        //record it, if necessary
        if(MacroII.hasGUI())
        {
            records.addAgent(buyer);
            records.event(buyer, MarketEvents.REGISTERED_AS_BUYER,
                    buyer.getModel().getCurrentSimulationTimeInMillis());


            network.addAgent(buyer);


        }

    }





    /**
     * Remove a buyer from the registry as they exit the market
     * @param buyer buyer exiting the market
     */
    public void deregisterBuyer(EconomicAgent buyer){
        boolean isNew = buyers.remove(buyer);
        assert isNew;   //make sure it wasn't there before!

        if(MacroII.hasGUI())
        {
            records.event(buyer, MarketEvents.DEREGISTERED_AS_BUYER,
                    buyer.getModel().getCurrentSimulationTimeInMillis());

            network.removeAgent(buyer);


        }
    }

    /**
     * The registry containing all the sellers in the market
     */
    private final Set<EconomicAgent> sellers = new HashSet<>();



    /**
     * Get all agents that belong in the market as buyers
     * @return all people who somehow can buy in this market
     */
    @Nonnull
    public Set<EconomicAgent> getSellers(){
        return Collections.unmodifiableSet(sellers);
    }

    /**
     * Add a seller to the registry as they enter the market
     * @param seller buyer entering the market
     */
    public void registerSeller(@Nonnull EconomicAgent seller){
        boolean isNew = sellers.add(seller);   //addSalesDepartmentListener it to the set
        assert isNew;   //make sure it wasn't there before!

        //record it, if necessary
        if(MacroII.hasGUI())
        {
            records.addAgent(seller);
            records.event(seller,MarketEvents.REGISTERED_AS_SELLER,
                    seller.getModel().getCurrentSimulationTimeInMillis());

            //add agent to the network
            network.addAgent(seller);


        }
    }

    /**
     * Remove a seller from the registry as they exit the market
     * @param seller seller exiting the market
     */
    public void deregisterSeller(@Nonnull EconomicAgent seller){
        boolean isNew = sellers.remove(seller);
        assert isNew;   //make sure it wasn't there before!



        if(MacroII.hasGUI())
        {
            records.event(seller, MarketEvents.DEREGISTERED_AS_SELLER,
                    seller.getModel().getCurrentSimulationTimeInMillis());


            //remove agent to the network
            network.removeAgent(seller);
        }
    }





    /**
     * Submit a sell quote on a specific good
     * @param seller the agent making the sale
     * @param price the price at/above which the agent is willing to sell
     * @param good the good to sell
     * @return the quote made
     */

    @Nonnull
    abstract public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price,@Nonnull Good good );

    /**
     * Submit a sell quote on a specific good
     * @param seller the agent making the sale
     * @param price the price at/above which the agent is willing to sell
     * @param good the good to sell
     * @param department the department making the order or null if it's done by the agent himself
     * @return the quote made
     */

    @Nonnull
    abstract public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price,@Nonnull Good good, @Nullable Department department );



    /**
     * If the seller changes its mind and wants to remove its sell quote, call this
     * @param q quote to cancel
     */
    abstract public void removeSellQuote(Quote q);


    /**
     * Submit a buy quote
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @param department the department making the order or null if it was done by the economic agent himself
     * @return quote made
     */
    @Nonnull
    abstract public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price, @Nullable Department department);

    /**
     * Submit a buy quote
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @return quote made
     */
    @Nonnull
    abstract public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price);

    /**
     * If the buyer changes its mind and wants to remove its purchase quote, call this
     * @param q quote to cancel
     */
    abstract public void removeBuyQuote(Quote q);


    /**
     * This method is called whenever two agents agree on an exchange. It can be called by the market or the agents themselves.
     * @param buyer the buyer
     * @param seller the seller
     * @param good the good being exchanged
     * @param price the price
     */
    public PurchaseResult trade(@Nonnull EconomicAgent buyer,@Nonnull EconomicAgent seller,@Nonnull Good good, long price,
                                @Nonnull Quote buyerQuote,@Nonnull Quote sellerQuote)
    {
        assert getBuyers().contains(buyer) : buyer.toString() + " ----- " + buyers;
        assert getSellers().contains(seller);
        double sellerCost = good.getLastValidPrice(); //record the price of the good BEFORE it is traded, so we can learn its markup


        PurchaseResult result = policy.trade(buyer,seller,good,price,buyerQuote,sellerQuote,this);
        //  prices.setMaximumItemCount(100);
        markups.setMaximumItemCount(3);

        if(result == PurchaseResult.SUCCESS)
        {

            //record
            lastPrice = price;
            todaySumOfClosingPrices +=price;
            lastFilledAsk = sellerQuote.getPriceQuoted();
            lastFilledBid = buyerQuote.getPriceQuoted();
            weeklyVolume++;
            todayVolume++;


            try{
                Hour timeOfTrade = new Hour(new Date(buyer.getModel().getCurrentSimulationTimeInMillis()));
                prices.addOrUpdate(timeOfTrade, price);  //addSalesDepartmentListener it to the time series!
                markups.addOrUpdate(timeOfTrade, (price - sellerCost) / sellerCost); //addSalesDepartmentListener it to the markups
            }catch (Exception e){
                if(!TESTING_MODE)  //might not be an issue if we are testing stuff.
                    throw e;
            }

            //tell the listeners!
            for(TradeListener tl : tradeListeners)
                tl.tradeEvent(buyer,seller,good,price,sellerQuote,buyerQuote);

            //if there is GUI tell the records
            if(MacroII.hasGUI())
            {
                //record it on the timeline
                long time = buyer.getModel().getCurrentSimulationTimeInMillis();
                records.event(buyer,MarketEvents.BOUGHT,time,"price: " + price + ", seller: " +seller );
                records.event(seller,MarketEvents.SOLD,time,"price: " + price + ", buyer: " +buyer);
                //register it on the network!
                registerTradeOnNetwork(seller,buyer,good.getType(),1);
            }


        }


        return result;
    }


    /**
     * During weekends the market stores the volume
     * @param model the simstate
     */
    public void weekEnd(MacroII model){
        lastWeekVolume = weeklyVolume;
        volume.add(new Week(model.getCurrentSimulationTime()),weeklyVolume,"volume");
        //       if(!getGoodType().isLabor())
        //  volume.add(new Week(model.getWeeksPassed(), (int) (model.getWeeksPassed() / model.getWeekLength())),weeklyVolume); //addSalesDepartmentListener it to the weeks
        weeklyVolume = 0;
        //if there is GUI, clear the network
        if(MacroII.hasGUI()){
            network.weekEnd();
            records.weekEnd();
        }
    }


    /**
     * asks the market if users are allowed to see the best price for a good on sale
     */
    abstract public boolean isBestSalePriceVisible();

    /**
     * Asks the market to return the best (lowest) price for a good on sale at the market
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    abstract public long getBestSellPrice() throws IllegalAccessException;


    /**
     * Asks the market to return the owner of the best ask price in the market
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Nullable
    abstract public EconomicAgent getBestSeller() throws IllegalAccessException;



    /**
     * asks the market if users are allowed to see the best offer to buy a good
     */
    abstract public boolean isBestBuyPriceVisible();

    /**
     * Asks the market to return the best (highest) offer for buying a good at the market
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    abstract public long getBestBuyPrice() throws IllegalAccessException;

    public int getWeeklyVolume() {
        return weeklyVolume;
    }


    public TimeSeries getMarkups() {
        return markups;
    }

    public GoodType getGoodType() {
        return goodType;
    }

    /**
     * The last closing price in the market
     * @return the last closing price or -1 if there is no history
     */
    public long getLastPrice() {
        return lastPrice;
    }

    /**
     * Asks the market to return the owner of the best offer in the market
     *
     * @return the best buyer or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    public abstract EconomicAgent getBestBuyer() throws IllegalAccessException;


    public List<TradeListener> getTradeListeners() {
        return Collections.unmodifiableList(tradeListeners);
    }

    /**
     * Add a trade listener to this market
     */
    public void addTradeListener(TradeListener listener){
        tradeListeners.add(listener);
    }

    /**
     * Remove a trade listener from the market
     */
    public boolean removeTradeListener(TradeListener listener){
        return tradeListeners.remove(listener);
    }


    /**
     * Get an unmodifiable view of the bid listeners
     * @return  the bid listeners
     */
    public List<BidListener> getBidListeners(){
        return Collections.unmodifiableList(bidListeners);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     *
     * @param bidListener element to be appended to this list
     * @return {@code true} (as specified by {@link java.util.Collection#add})
     */
    public boolean addBidListener(BidListener bidListener) {
        return bidListeners.add(bidListener);
    }

    /**
     * remove a specific bidListenerlistener
     * @param listener the listener to remove from the list
     * @return true if it was successfully removed
     */

    public boolean removeBidListener(BidListener listener) {
        return bidListeners.remove(listener);
    }

    /**
     * Can I get an iterator to cycle through all the quotes?
     * @return true if it's possible
     */
    public abstract boolean areAllQuotesVisibile();

    /**
     * Get an iterator to cycle through all the bids
     * @return  the iterator
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    public abstract Iterator<Quote> getIteratorForBids() throws IllegalAccessException;


    /**
     * Get an iterator to cycle through all the bids
     * @return  the iterator
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    public abstract Iterator<Quote> getIteratorForAsks() throws IllegalAccessException;

    /**
     * Here we hold the inspector for the GUI
     */
    private TabbedInspector marketInspector;


    /**
     * the JPanel containing the lifeline and all the utilities.
     */
    private LifelinesPanel recordPanel;


    /**
     * tells the market that the model is starting up, and it would be a good idea to start the data collector
     * @param model
     */
    public void start(MacroII model)
    {
        marketData = new MarketData();

        marketData.start(model,this);

    }

    public void turnOff()
    {
        //the game is over, tell the rest of the crew
        marketData.turnOff();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "-->" + getGoodType();
    }

    private void buildInspector(){

        //if there is no GUI, forget about it
        if(!MacroII.hasGUI())
            return;

        marketInspector = new TabbedInspector(true);
        marketInspector.setName(toString() + " inspector");

        /*********************************************
         * Prices
         ********************************************/

        //I fuckin hate JFreeChart.
        // DynamicTimeSeriesCollection priceSeries;
        TimeSeriesCollection priceSeries = new TimeSeriesCollection();
        priceSeries.addSeries(prices);
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Last Closing Prices","time","price",
                priceSeries,false,true,false);
        ChartPanel panel = new ChartPanel(chart,true);
        assert prices!= null;




        //we are going to add the new JPanel IN the new inspector
        closingPriceInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() { public void run() {
                    repaint();
                }});
            }
        };
        closingPriceInspector.setVolatile(true);
        closingPriceInspector.setLayout(new BorderLayout()); //this centers it
        closingPriceInspector.add(panel);

        marketInspector.addInspector(closingPriceInspector,"Closing price");


        /*********************************************
         * VOLUME
         ********************************************/

        JFreeChart volumeChart = ChartFactory.createXYBarChart("volume","week",true,"Volume Traded",volume, PlotOrientation.VERTICAL,false,true,false);
        // StandardChartTheme.createDarknessTheme().apply(volumeChart);
        panel = new ChartPanel(volumeChart);

        //we are going to add the new JPanel IN the new inspector
        Inspector volumePriceInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() { public void run() {
                    repaint();
                }});
            }
        };
        volumePriceInspector.setVolatile(true);
        volumePriceInspector.setLayout(new BorderLayout()); //this centers it
        volumePriceInspector.add(panel);

        marketInspector.addInspector(volumePriceInspector,"Volume");


        /*********************************************
         * TIMELINE
         ********************************************/
        // marketRecord = new Record(toString());
        Inspector marketRecordsInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() { public void run() {
                    repaint();
                }});
            }
        };
        marketRecordsInspector.setLayout(new BorderLayout());
        //register the events globally


        //get the data manager for records so we can build a jpanel that displays it
        DataManager recordManager = records.getRecordManager();
        assert recordManager != null;
        //register all possible events names
        GlobalEventData.getInstance().reset();
        for(MarketEvents event : MarketEvents.values()){
            GlobalEventData.getInstance().registerEventName(event.name());
        }
        for(MarketEvents event : MarketEvents.values()){
            recordManager.addEventName(event.ordinal());
        }



        //initialize teh panel
        recordPanel.openData(recordManager);
        //add the panel to the inspector
        marketRecordsInspector.add(recordPanel);
        //add it as a tab!
        marketInspector.addInspector(marketRecordsInspector,"Timeline");

        /***************************************************
         * Network
         ***************************************************/
        Inspector networkInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() { public void run() {
                    repaint();
                }});
            }
        };
        networkInspector.setLayout(new BorderLayout());
        //add the visualization
        networkInspector.add(network.getVisualization());
        marketInspector.addInspector(networkInspector,"Network");





    }

    public TabbedInspector getMarketInspector() {
        return marketInspector;
    }

    /**
     * Get the timeline data structure manager
     * @return
     */
    protected TimelineManager getRecords() {
        return records;
    }


    /**
     * This is called when somebody is fired/quit/removed from the plant. So far it's purely decorative (tells the network if the GUI is on)
     * @param employer the employer
     * @param employee the employee
     */
    public void registerFiring(@Nonnull EconomicAgent employer,
                               @Nonnull EconomicAgent employee){
        //with no GUI, forget about it
        if(!MacroII.hasGUI())
            return;


        boolean removedSuccessfully = network.removeEdges(employer,employee);
        assert removedSuccessfully;


    }


    /**
     * This is called onyl if the GUI is on, and only to show the trade on the network
     * @param sender the agent giving up the goods
     * @param receiver the agent receiving the goods
     * @param g what kind of good?
     * @param quantity how much is exchanged?
     */
    private void registerTradeOnNetwork(@Nonnull EconomicAgent sender,
                                        @Nonnull EconomicAgent receiver,
                                        @Nonnull GoodType g, int quantity)
    {
        //if graphing is not active, don't bother
        assert MacroII.hasGUI();

        assert network.containsAgent(sender);
        assert network.containsAgent(receiver);

        //if both are registered
        network.registerInventoryDelivery(sender,receiver,g,quantity);
    }

    /**
     * The price policy of the market
     */
    protected PricePolicy pricePolicy= new AveragePricePolicy();

    /**
     * Set the policy used to determine the price given a match
     */
    public void setPricePolicy(PricePolicy pricePolicy) {
        this.pricePolicy = pricePolicy;
    }

    /**
     * Get the price of a trade according to the policy set by the market
     * @param sellerPrice seller max price
     * @param buyerPrice buyer max price
     * @return the price
     */
    public long price(long sellerPrice, long buyerPrice) {
        assert buyerPrice>=sellerPrice;
        return pricePolicy.price(sellerPrice, buyerPrice);
    }

    public int getLastWeekVolume() {
        return lastWeekVolume;
    }


    /**
     * Gets The price policy of the market.
     *
     * @return Value of The price policy of the market.
     */
    public PricePolicy getPricePolicy() {
        return pricePolicy;
    }


    /**
     * Gets last filled ask.
     *
     * @return Value of last filled ask.
     */
    public long getLastFilledAsk() {

        if(isBestSalePriceVisible())
            return lastFilledAsk;
        else
            throw new IllegalStateException("You can't see the LastFilledAsk in this kind of market!");
    }

    /**
     * Gets last filled bid.
     *
     * @return Value of last filled bid.
     */
    public long getLastFilledBid() {
        if(isBestSalePriceVisible())
            return lastFilledBid;
        else
            throw new IllegalStateException("You can't see the lastFilledBid in this kind of market!");
    }


    /**
     * Resets "today volume" and sets yesterday volume and last price
     */
    public void collectDayStatistics()
    {
        yesterdayLastPrice = lastPrice;
        yesterdaySumOfClosingPrices = todaySumOfClosingPrices;
        yesterdayVolume = todayVolume;
        todayVolume=0;
        todaySumOfClosingPrices = 0;


    }


    /**
     * Gets What was the last closing price yesterday?.
     *
     * @return Value of What was the last closing price yesterday?.
     */
    public long getYesterdayLastPrice() {
        return yesterdayLastPrice;
    }

    /**
     * Gets How many goods were traded yesterday.
     *
     * @return Value of How many goods were traded yesterday.
     */
    public int getYesterdayVolume() {
        return yesterdayVolume;
    }

    /**
     * Gets How many goods have been traded so far today.
     *
     * @return Value of How many goods have been traded so far today.
     */
    public int getTodayVolume() {
        return todayVolume;
    }

    /**
     * utility method for summing up all production by all sellers
     * @return
     */
    public int countYesterdayProductionByRegisteredSellers()
    {
        int sum = 0;
        for(EconomicAgent a : sellers)
            sum += a.getYesterdayProduction(getGoodType());
        return sum;
    }

    /**
     * utility method for summing up all production by all sellers
     * @return
     */
    public int countTodayProductionByRegisteredSellers()
    {
        int sum = 0;
        for(EconomicAgent a : sellers)
            sum += a.getTodayProduction(getGoodType());
        return sum;
    }


    /**
     * utility method for summing up all consumption by all buyers
     * @return
     */
    public int countYesterdayConsumptionByRegisteredBuyers()
    {
        int sum = 0;
        for(EconomicAgent a : buyers)
            sum += a.getYesterdayConsumption(getGoodType());
        return sum;
    }


    public float getTodayAveragePrice()
    {
        return todaySumOfClosingPrices /((float) todayVolume);
    }

    public int sumDemandGaps(){
        int sum = 0;
        for(EconomicAgent a : buyers)
            sum += a.estimateDemandGap(getGoodType());
        return sum;


    }

    public int sumSupplyGaps(){
        int sum = 0;
        for(EconomicAgent a : sellers)
            sum += a.estimateSupplyGap(getGoodType());
        return sum;


    }

    /**
     * utility method for summing up all consumption by all buyers
     * @return
     */
    public int countTodayConsumptionByRegisteredBuyers()
    {
        int sum = 0;
        for(EconomicAgent a : buyers)
            sum += a.getTodayConsumption(getGoodType());
        return sum;
    }


    /**
     * how many days worth of observations are here?
     */
    public int numberOfObservations() {
        return marketData.numberOfObservations();
    }

    /**
     * check which days have observations that are okay with the acceptor!
     */
    public int[] getAcceptableDays(@Nonnull int[] days, MarketData.MarketDataAcceptor acceptor) {
        return marketData.getAcceptableDays(days, acceptor);
    }

    /**
     * returns a copy of all the observed last prices so far!
     */
    public double[] getAllRecordedObservations(MarketDataType type) {
        return marketData.getAllRecordedObservations(type);
    }

    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(MarketDataType type, @Nonnull int[] days) {
        return marketData.getObservationsRecordedTheseDays(type, days);
    }

    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(MarketDataType type, int beginningDay, int lastDay) {
        return marketData.getObservationsRecordedTheseDays(type, beginningDay, lastDay);
    }

    /**
     * utility method to analyze only  a specific day
     */
    public double getObservationRecordedThisDay(MarketDataType type, int day) {
        return marketData.getObservationRecordedThisDay(type, day);
    }

    /**
     * return the latest price observed
     */
    public Double getLatestObservation(MarketDataType type) {
        return marketData.getLatestObservation(type);
    }

    /**
     * a method to check if, given the acceptor, the latest observation is valid
     * @param acceptor
     * @return
     */
    public boolean isLastDayAcceptable(MarketData.MarketDataAcceptor acceptor) {
        return marketData.isLastDayAcceptable(acceptor);
    }

    public int getLastObservedDay() {
        return marketData.getLastObservedDay()-1;
    }
}
