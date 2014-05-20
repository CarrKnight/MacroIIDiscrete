/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.utilities.changeLooker;

import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.priceLooker.PriceLookupOnMarket;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.MovingAverage;
import sim.engine.SimState;


/**
 * <h4>Description</h4>
 * <p/> looks at the closing price after trading computes the rate of change and adds it to the moving average
 * <p/>  It schedules itself
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-27
 * @see
 */
public class ChangeLookupMAMarket implements ChangeLookup
{

    final private MovingAverage<Float> movingAverage;

    /**
     * the object we use to check the price (it takes care of using best bids and asks if they are visible)
     */
    final private PriceLookupOnMarket priceLookup;

    private long lastPrice = -1;

    private boolean active;

    /**
     * stored only to reschedule itself
     */
    private final MacroII model;

    /**
     * Create the object looking at a specific market with a given MA size. It registers itself to step
     * @param market the market to look prices at
     * @param movingAverageSize the size of the MA buffer
     * @param model a link to the model to schedule itself
     */
    public ChangeLookupMAMarket( Market market, int movingAverageSize, MacroII model) {
        Preconditions.checkNotNull(market);
        Preconditions.checkNotNull(model);
        this.active = true;

        this.priceLookup = new PriceLookupOnMarket(market);
        this.movingAverage = new MovingAverage<>(movingAverageSize);
        this.model = model;
        this.model.scheduleSoon(ActionOrder.THINK,this);


    }

    /**
     * Look up the price. If last price checked and this price are both valid, feed the % change to the MA
     * @param state
     */
    @Override
    public void step(SimState state) {

        assert this.model.getCurrentPhase().equals(ActionOrder.THINK); //should act only on think
        assert this.model == state;

        if(!active) //don't proceed if you have been turned off
            return;


        long currentPrice = priceLookup.getPrice();
        //if both observations are valid:
        if(lastPrice >0 && currentPrice >=0)
        {
            float percentChange = (currentPrice-lastPrice)/((float)lastPrice);
            movingAverage.addObservation(percentChange);
        }
        else
        if(lastPrice==0 && currentPrice ==0)
        {
            float percentChange = 0f;
            movingAverage.addObservation(percentChange);

        }
        else
        {
            //don't add observations that would screw up everything
        }
        lastPrice = currentPrice;

        //reschedule yourself
        assert active;
        this.model.scheduleTomorrow(ActionOrder.THINK,this);



    }

    /**
     * Turns itself off.
     */
    @Override
    public void turnOff() {
        active = false;

    }

    /**
     * Get the change rate this object is supposed to look up. Returns 0 if there is no observation
     *
     * @return the change
     */
    @Override
    public float getChange() {
        Float toReturn =  movingAverage.getSmoothedObservation();
        if(toReturn.isNaN())
            return 0;
        else
            return toReturn;

    }
}
