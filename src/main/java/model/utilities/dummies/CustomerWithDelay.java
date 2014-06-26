/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.DelayBin;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

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
     * price asked until the delay is over (-1 means no offer)
     */
    private int defaultPrice=-1;

    /**
     * what is the real price of the customer
     */
    final private int originalMaxPrice;

    private final DelayBin<Integer> delayQueue;



    public CustomerWithDelay(final MacroII model, int price, final int delay, final Market market) {
        super(model, price,market);
        originalMaxPrice = price;
        maxPrice = defaultPrice;
        Preconditions.checkArgument(delay>=1);
        delayQueue = new DelayBin<>(delay-1,defaultPrice);



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

        Integer oldPrice;
        if(market.getBestSellPrice()==-1)
            oldPrice = delayQueue.addAndRetrieve(market.getLastFilledAsk());
        else
            oldPrice = delayQueue.addAndRetrieve(market.getBestSellPrice());

        if(oldPrice<= originalMaxPrice && oldPrice != -1 )
        {
            maxPrice = hasHowMany(UndifferentiatedGoodType.MONEY)-1;
        }
        else
            maxPrice = defaultPrice;


    }





}
