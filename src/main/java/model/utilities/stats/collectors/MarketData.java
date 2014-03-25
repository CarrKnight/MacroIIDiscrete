/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import com.google.common.base.Preconditions;
import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.jfree.data.time.TimeSeries;
import sim.engine.SimState;

import javax.annotation.Nonnull;
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
     * when it is set to off, it stops rescheduling itself!
     */
    private boolean active = true;

    /**
     * the market you are supposed to watch and memorize
     */
    private Market marketToFollow;



    public MarketData() {
        super(MarketDataType.class);
    }

    @Override
    public void step(SimState state) {
        if(!active)
            return;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        Preconditions.checkState(model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING));
        Preconditions.checkNotNull(marketToFollow);

        //memorize
        data.get(MarketDataType.CLOSING_PRICE).add((double) marketToFollow.getLastPrice());
        int todayVolume = marketToFollow.getTodayVolume();
        data.get(MarketDataType.VOLUME_TRADED).add((double) todayVolume);
        data.get(MarketDataType.VOLUME_CONSUMED).add((double) marketToFollow.countTodayConsumptionByRegisteredBuyers());
        data.get(MarketDataType.SELLERS_INVENTORY).add((double) marketToFollow.countTodayInventoryByRegisteredSellers());
        data.get(MarketDataType.BUYERS_INVENTORY).add((double) marketToFollow.countTodayInventoryByRegisteredBuyers());
        data.get(MarketDataType.VOLUME_PRODUCED).add((double) marketToFollow.countTodayProductionByRegisteredSellers());
        data.get(MarketDataType.DEMAND_GAP).add((double)marketToFollow.sumDemandGaps());
        data.get(MarketDataType.SUPPLY_GAP).add((double) marketToFollow.sumSupplyGaps());
        float todayAveragePrice = marketToFollow.getTodayAveragePrice();
        data.get(MarketDataType.AVERAGE_CLOSING_PRICE).add((double) todayAveragePrice);



        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);





    }

    /**
     * called when the agent has to start acting!
     */
    public void start(@Nonnull MacroII state, @Nonnull Market marketToFollow) {
        if(!active)
            return;

        //schedule yourself
        this.marketToFollow = marketToFollow;
        setStartingDay((int) Math.round(state.getMainScheduleTime())+1);

        for(DailyObservations obs : data.values())
            obs.setStartingDay(getStartingDay());

        Preconditions.checkState(getStartingDay() >=0);
        state.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }


    @Override
    public void turnOff() {
        active = false;
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
    public int[] getAcceptableDays(@Nonnull int[] days, MarketDataAcceptor acceptor)
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
