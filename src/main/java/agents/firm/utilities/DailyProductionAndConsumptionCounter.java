/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h4>Description</h4>
 * <p/> this is a simple object that keeps track of today and yesterday production and consumption of the firm for each good type.
 * It is steppable and deactivable, it steps itself when start() is called and stop rescheduling itself when turnOff()
 * is called
 * <p/> It counts 3 things:
 * <ul>
 *     <li> Times consume() was called</li>
 *     <li> Times receive() is called</li>
 *     <li> Times countNewProduction() is called</li>
 * </ul>
 * This allows it give 3 informations:
 * <ul>
 *     <li> Consumptions today</li>
 *     <li> Production today</li>
 *     <li> Goods Bought today</li>
 * </ul>
 * Where bought is the difference between receive() [which is called both when buying and producing] and the
 * number of times notifyOfProduction() is called
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-06
 * @see
 */
public class DailyProductionAndConsumptionCounter implements Steppable, Deactivatable
{
    private EnumMap<GoodType,AtomicInteger> consumedToday;

    private EnumMap<GoodType,AtomicInteger> boughtOrProducedToday;

    private EnumMap<GoodType,AtomicInteger> producedToday;

    private EnumMap<GoodType,AtomicInteger> consumedYesterday;

    private EnumMap<GoodType,AtomicInteger> boughtOrProducedYesterday;

    private EnumMap<GoodType,AtomicInteger> producedYesterday;

    private boolean active = true;
    private boolean startWasCalled = false;

    /**
     * The constructor instantiate all the lists but doesn't actually do anything else.
     * You need start() to step yourself
     */
    public DailyProductionAndConsumptionCounter()
    {
        //instantiate all the lists
        consumedToday = new EnumMap<>(GoodType.class);
        boughtOrProducedToday = new EnumMap<>(GoodType.class);
        producedToday = new EnumMap<>(GoodType.class);
        consumedYesterday = new EnumMap<>(GoodType.class);
        boughtOrProducedYesterday = new EnumMap<>(GoodType.class);
        producedYesterday = new EnumMap<>(GoodType.class);

    }


    public void start(MacroII state)
    {
        Preconditions.checkState(active, "trying to start a turnedoff Counter");
        Preconditions.checkArgument(!startWasCalled);
        startWasCalled = true;

        //step yourself
        state.scheduleSoon(ActionOrder.DAWN,this);

    }

    /**
     * step once a day to reset all counters
     */
    @Override
    public void step(SimState state) {
        //step!
        assert state instanceof  MacroII;
        if(!active)
            return;

        newDay();
        //reschedule tomorrow!
        ((MacroII) state).scheduleTomorrow(ActionOrder.DAWN, this);


    }

    /**
     * shifts the "today" maps to yesterday, and creates new maps for the new today!
     */
    private void newDay()
    {
        consumedYesterday = consumedToday;
        boughtOrProducedYesterday = boughtOrProducedToday;
        producedYesterday = producedToday;

        consumedToday = new EnumMap<>(GoodType.class);
        boughtOrProducedToday = new EnumMap<>(GoodType.class);
        producedToday = new EnumMap<>(GoodType.class);
    }


    /**
     * Tell the counter something has been consumed
     */
    public void countNewConsumption(GoodType type)
    {
        increaseByOne(consumedToday,type);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewConsumption(GoodType type, Integer n)
    {
        increaseByN(consumedToday,type,n);
    }

    /**
     * Tell the counter something has been consumed
     */
    public void countNewProduction(GoodType type)
    {
        increaseByOne(producedToday,type);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewProduction(GoodType type, Integer n)
    {
        increaseByN(producedToday,type,n);
    }

    /**
     * Tell the counter something has been consumed
     */
    public void countNewReceive(GoodType type)
    {
        increaseByOne(boughtOrProducedToday,type);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewReceive(GoodType type, Integer n)
    {
        increaseByN(boughtOrProducedToday,type,n);
    }

    /**
     * get today consumption
     */
    public int getTodayConsumption(GoodType type)
    {
        return lookupMap(consumedToday,type);

    }

    /**
     * get yesterday consumption
     */
    public int getYesterdayConsumption(GoodType type)
    {
        return lookupMap(consumedYesterday,type);

    }


    /**
     * get today Production
     */
    public int getTodayProduction(GoodType type)
    {
        return lookupMap(producedToday,type);

    }

    /**
     * get yesterday Production
     */
    public int getYesterdayProduction(GoodType type)
    {
        return lookupMap(producedYesterday,type);

    }


    /**
     * get today Production
     */
    public int getTodayAcquisitions(GoodType type)
    {
        int acquisitions = lookupMap(boughtOrProducedToday,type) -  lookupMap(producedToday,type);
        assert acquisitions >= 0;
        return acquisitions;

    }

    /**
     * get yesterday Production
     */
    public int getYesterdayAcquisitions(GoodType type)
    {
        int acquisitions = lookupMap(boughtOrProducedYesterday,type) -  lookupMap(producedYesterday,type);
        assert acquisitions >= 0;
        return acquisitions;
    }


    /**
     * a simple lookup that returns 0 every time the map doesn't actually map the type you are looking for
     */
    private int lookupMap(EnumMap<GoodType,AtomicInteger> map, GoodType type)
    {

        AtomicInteger toReturn = map.get(type);
        if(toReturn == null)
            return 0;
        else
            return toReturn.get();


    }

    /**
     * a simple way to increase by one an entry in the map
     */
    private void increaseByOne(EnumMap<GoodType,AtomicInteger> map, GoodType type)
    {

        increaseByN(map,type,1);
    }

    /**
     * a simple way to increase by n an entry in the map
     */
    private void increaseByN(EnumMap<GoodType,AtomicInteger> map, GoodType type, int n)
    {

        Preconditions.checkArgument(n >0);
        AtomicInteger currentCount = map.get(type);
        if(currentCount == null)
            map.put(type,new AtomicInteger(n));
        else
        {
            assert currentCount.get() > 0;
            currentCount.addAndGet(n);
            assert currentCount.get() == map.get(type).get();
        }

    }

    @Override
    public void turnOff() {
        active=false;

    }
}
