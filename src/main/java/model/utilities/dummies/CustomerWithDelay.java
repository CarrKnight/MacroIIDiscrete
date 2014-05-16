/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
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
public class CustomerWithDelay extends Customer {

    /**
     * how many consecutive days the closingprice/bestoffer has to be below our
     * REAL reservation price in order for us to put the real order
     */
    final private int delay;

    /**
     * price asked until the delay is over (-1 means no offer)
     */
    private int defaultPrice=-1;

    /**
     * what is the real price of the customer
     */
    final private int originalMaxPrice;

    private final LinkedList<Integer> delayQueue;



    public CustomerWithDelay(final MacroII model, int price, final int delay, final Market market) {
        super(model, price,market);
        originalMaxPrice = price;
        maxPrice = defaultPrice;
        Preconditions.checkArgument(delay>=1);
        this.delay = delay;


        delayQueue = new LinkedList<>();




        //schedule yourself to check for delay
        model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {

                try {
                    checkPriceStep(market);

                    model.scheduleTomorrow(ActionOrder.TRADE,this, Priority.FINAL);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    System.exit(-1);

                }


            }
        }, Priority.FINAL);
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
            maxPrice = defaultPrice;

        else
        {
            long oldPrice = delayQueue.pop();
            if(oldPrice<= originalMaxPrice && oldPrice != -1 )
            {
                maxPrice = hasHowMany(UndifferentiatedGoodType.MONEY)-1;
            }
            else
                maxPrice = defaultPrice;
        }



    }



}
