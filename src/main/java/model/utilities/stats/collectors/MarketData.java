/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import sim.engine.SimState;

import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/> Just a bunch of lists of prices/quantities and other data stored from the market at the end of each day.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-15
 * @see
 */
public class MarketData extends DataStorage<MarketDataType>
{




    /**
     * the market you are supposed to watch and memorize
     */
    private Market marketToFollow;


    public MarketData() {
        super(MarketDataType.class);
    }

    @Override
    public void step(SimState state) {
        if(!isActive())
            return;



        if(getStartingDay()==-1) {
            setCorrectStartingDate((MacroII) state);
        }
        assert getStartingDay() >=0;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        Preconditions.checkState(model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING));
        Preconditions.checkNotNull(marketToFollow);

        //note prices and volumes. These are easy!
        data.get(MarketDataType.CLOSING_PRICE).add((double) marketToFollow.getLastPrice());
        int todayVolume = marketToFollow.getTodayVolume();
        data.get(MarketDataType.VOLUME_TRADED).add((double) todayVolume);
        float todayAveragePrice = marketToFollow.getTodayAveragePrice();
        data.get(MarketDataType.AVERAGE_CLOSING_PRICE).add((double) todayAveragePrice);

        //new stuff
        double cashProduced = 0;
        double todayCashInventory=0;
        double totalConsumption = 0;
        double totalProduction = 0;
        double demandGap = 0;
        double supplyGap = 0;
        double sellerInventory=0;
        double buyerInventory=0;
        final UndifferentiatedGoodType money = marketToFollow.getMoney();
        final GoodType goodTraded = marketToFollow.getGoodType();

        //a single loop rather than a million small loops
        for(EconomicAgent a : marketToFollow.getBuyers())
        {
            cashProduced += a.getTodayProduction(money);
            todayCashInventory += a.hasHowMany(money);
            totalConsumption += a.getTodayConsumption(goodTraded);
            totalProduction += a.getTodayProduction(goodTraded);
            demandGap += a.estimateDemandGap(goodTraded);
            buyerInventory += a.hasHowMany(goodTraded);


        }
        for(EconomicAgent a : marketToFollow.getSellers())
        {
            cashProduced += a.getTodayProduction(money);
            todayCashInventory += a.hasHowMany(money);
            totalConsumption += a.getTodayConsumption(goodTraded);
            totalProduction += a.getTodayProduction(goodTraded);
            supplyGap+= a.estimateSupplyGap(goodTraded);
            sellerInventory += a.hasHowMany(goodTraded);

        }
        double todayOutputInventory = sellerInventory + buyerInventory;
        data.get(MarketDataType.SELLERS_INVENTORY).add(sellerInventory);
        data.get(MarketDataType.BUYERS_INVENTORY).add( buyerInventory);
        data.get(MarketDataType.VOLUME_CONSUMED).add(totalConsumption);
        data.get(MarketDataType.VOLUME_PRODUCED).add(totalProduction);
        data.get(MarketDataType.TOTAL_INVENTORY).add(todayOutputInventory);
        data.get(MarketDataType.CASH_PRODUCED).add(cashProduced);
        data.get(MarketDataType.CASH_RESERVES).add(todayCashInventory);
        data.get(MarketDataType.DEMAND_GAP).add(demandGap);
        data.get(MarketDataType.SUPPLY_GAP).add(supplyGap);







        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);





    }

    /**
     * called when the agent has to start acting!
     */
    public void start( MacroII state,  Market marketToFollow) {
        if(!isActive())
            return;

        //schedule yourself
        this.marketToFollow = marketToFollow;
        //we are going to set the starting day at -1 and then change it at our first step()
        setStartingDay(-1);

        state.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }







    /**
     * a method to check if, given the acceptor, the latest observation is valid
     * @param acceptor
     * @return
     */
    public boolean isLastDayAcceptable(MarketDataAcceptor acceptor)
    {

        checkThatThereIsAtLeastOneObservation();
        return acceptor.acceptDay(
                getLatestObservation(MarketDataType.CLOSING_PRICE),
                getLatestObservation(MarketDataType.VOLUME_TRADED),
                getLatestObservation(MarketDataType.VOLUME_PRODUCED),
                getLatestObservation(MarketDataType.VOLUME_CONSUMED),
                getLatestObservation(MarketDataType.DEMAND_GAP),
                getLatestObservation(MarketDataType.SUPPLY_GAP)

        );

    }


    /**
     * check which days have observations that are okay with the acceptor!
     */
    public int[] getAcceptableDays( int[] days, MarketDataAcceptor acceptor)
    {

        ArrayList<Integer> acceptedDays = new ArrayList<>();


        Preconditions.checkArgument(days.length > 0);
        ArrayList<Integer> goodDays = new ArrayList<>(); //to return!
        //fill in the array
        for (int day : days)
            if (acceptor.acceptDay(
                    data.get(MarketDataType.CLOSING_PRICE).getObservationRecordedThisDay(day),
                    data.get(MarketDataType.VOLUME_TRADED).getObservationRecordedThisDay(day),
                    data.get(MarketDataType.VOLUME_PRODUCED).getObservationRecordedThisDay(day),
                    data.get(MarketDataType.VOLUME_CONSUMED).getObservationRecordedThisDay(day),
                    data.get(MarketDataType.DEMAND_GAP).getObservationRecordedThisDay(day),
                    data.get(MarketDataType.SUPPLY_GAP).getObservationRecordedThisDay(day)

            ))
                goodDays.add(day);

        //ugly recasting
        if(goodDays.isEmpty())
            return new int[0];
        else
        {
            int[] toReturn = new int[goodDays.size()];
            int i =0;
            for(Integer goodDay : goodDays)
            {
                toReturn[i] = goodDay;
                i++;
            }
            return toReturn;
        }



    }

    @Override
    public void turnOff() {
        super.turnOff();
        marketToFollow = null;
    }

    /**
     * a simple comparator that gets all the observations in a day and returns a boolean. It's supposed to be used by observers
     * that want to ignore some observations (for example ignore days with no trade)
     */
    public  interface MarketDataAcceptor
    {
        /**
         *
         * @return true if the observation should NOT be ignored
         */

        public boolean acceptDay(Double lastPrice,Double volumeTraded, Double volumeProduced, Double volumeConsumed,
                                 Double demandGap, Double supplyGap);


    }


}
