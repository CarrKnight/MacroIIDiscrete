/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import com.google.common.base.Preconditions;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> This is just like a dummy buyer but instead of placing bids at its fixed price, it starts by placing bids at 0 and then
 * only places its real bid if the offer x days ago was favorable
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-23
 * @see
 */
public class DummyBuyerWithDelay extends DummyBuyer {

    /**
     * how many consecutive days the closingprice/bestoffer has to be below our
     * REAL reservation price in order for us to put the real order
     */
    final private int delay;

    /**
     * price asked until the delay is over (-1 means no offer)
     */
    private long defaultPrice=-1l;

    final private Market market;

    private final LinkedList<Long> delayQueue;


    /**
     * are we placing the real offer or the default price?
     */
    private boolean realOffer = false;

    public DummyBuyerWithDelay(final MacroII model, long price, final int delay, final Market market) {
        super(model, price,market);
        Preconditions.checkArgument(delay>=1);
        this.delay = delay;
        this.market = market;

        delayQueue = new LinkedList<>();




        //schedule yourself to check for delay
        model.scheduleSoon(ActionOrder.THINK,new Steppable() {
            @Override
            public void step(SimState state) {

                try {
                    checkPriceStep(market);

                    model.scheduleTomorrow(ActionOrder.THINK,this);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    System.exit(-1);

                }


            }
        });
    }

    private void checkPriceStep(Market market) throws IllegalAccessException {

        if(market.getBestSellPrice()==-1)
            delayQueue.addLast(market.getLastFilledAsk());
        else
            delayQueue.addLast(market.getBestSellPrice());

        checkDelay();
    }

    private void checkDelay()
    {

        if(delayQueue.size() < delay)
            realOffer = false;
        else
        {
            long oldPrice = delayQueue.poll();
            if(oldPrice<= super.quotedPrice && oldPrice != -1 )
                realOffer=true;
            else
                realOffer=false;
        }



    }


    /**
     * Dummy buyers have a fixed price with which they pretend to buy
     *
     * @return
     */
    @Override
    public long getFixedPrice() {
        if(realOffer)
            return super.getFixedPrice();
        else
            return defaultPrice;
    }


    /**
     * Gets price asked until the delay is over -1 means no offer.
     *
     * @return Value of price asked until the delay is over -1 means no offer.
     */
    public long getDefaultPrice() {
        return defaultPrice;
    }

    /**
     * Sets new price asked until the delay is over -1 means no offer.
     *
     * @param defaultPrice New value of price asked until the delay is over -1 means no offer.
     */
    public void setDefaultPrice(long defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public boolean isRealOffer() {
        return realOffer;
    }
}
