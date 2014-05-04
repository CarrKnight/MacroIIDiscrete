/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.filters.ExponentialFilter;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/>  A simple object, resetting every DAWN, that tracks the average closing price of the purchase department.
 * Unlike inflow-outflow counter, this one doesn't listen but needs to be notified
 * <p/> Returns -1 when there were no trades
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-21
 * @see
 */
public class AveragePurchasePriceCounter implements Steppable, Deactivatable
{

    private int numberOfFilledQuotes=0;

    private long sumOfClosingPrices = 0;

    private boolean active=true;

    /**
     * average last week price weihted by outflow
     */
    private ExponentialFilter<Long> averagedPrice = new ExponentialFilter<>(.8f);

    private final PurchasesDepartment department;



    public AveragePurchasePriceCounter(PurchasesDepartment department) {
        this.department=department;
    }

    /**
     * schedules itself
     * @param state
     */
    public void start(MacroII state)
    {
        state.scheduleSoon(ActionOrder.DAWN,this);

    }

    /**
     * simply resets and reschedules
     * @param state
     */
    @Override
    public void step(SimState state) {
        if(!active)
            return;


        if(numberOfFilledQuotes > 0)
            averagedPrice.addObservation(department.getLastClosingPrice());

        numberOfFilledQuotes = 0;

        sumOfClosingPrices = 0;

        ((MacroII)state).scheduleTomorrow(ActionOrder.DAWN,this);


    }


    /**
     * notify the counter of a filled quote
     * @param closingPrice the closing price of the trade
     */
    public void getNotifiedOfFilledQuote(long closingPrice)
    {
        Preconditions.checkState(closingPrice>=0);
        Preconditions.checkState(active);
        numberOfFilledQuotes++;
        sumOfClosingPrices +=closingPrice;

        assert numberOfFilledQuotes >0;

    }

    /**
     * the average closing price of today's trades, or -1 if there were no trades
     */
    public float getTodayAverageClosingPrice()
    {
        if(numberOfFilledQuotes == 0)
            return -1;

        assert numberOfFilledQuotes > 0;
        return ((float)sumOfClosingPrices/(float)numberOfFilledQuotes);
    }


    public float getAveragedClosingPrice(){
        return averagedPrice.getSmoothedObservation();
    }
    @Override
    public void turnOff() {
        active = false;

    }
}
