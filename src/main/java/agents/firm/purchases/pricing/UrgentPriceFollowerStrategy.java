/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.Level;
import goods.Good;
import goods.GoodType;


/**
 * <h4>Description</h4>
 * <p/> This pricing strategy tries to act like a price follower (that is asks for the last price in the market, when possible). It defaults to zero intelligence when there is no history
 * <p/> It is also steppable: it queries the inventory level from inventory control. It does that because it increases prices by 10% when is "danger" and decreases them by 10% when is at acceptable levels
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-06
 */
public class UrgentPriceFollowerStrategy implements BidPricingStrategy {



    private final PurchasesDepartment dept;

    public UrgentPriceFollowerStrategy(PurchasesDepartment dept) {
        this.dept = dept;




    }


    /**
     * This is used internally by urgent price follower to ask the inventory control what's the status of our inventory
     * @return
     */

    private Level getInventoryLevel(){

        Level level = dept.rateCurrentLevel();
        if(level == null)
            throw new IllegalStateException("There is an inactive inventory control in charge!");
        else
            return level;

    }

    /**
     * The price follower tries to buy the good at the last closing price with a modifier given by the current inventory levels!
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(GoodType type) {
        assert type == dept.getGoodType();


        long lastPrice = dept.getMarket().getLastPrice();
        if( lastPrice== -1){
            if(dept.getAvailableBudget() == 0)  //if there is no money, offer no money
                return 0;
            return dept.getRandom().nextLong(dept.getAvailableBudget()); //throw a number between 0 and the available budget!
            }

        Level level = getInventoryLevel();
        float multiplier;
        switch (level)
        {
            case TOOMUCH:
            default:
                multiplier = .5f;
                break;
            case ACCEPTABLE:
                multiplier = .8f;
                break;
            case BARELY:
                multiplier = 1f;
                break;
            case DANGER:
                multiplier = 1.2f;
                break;
        }
        return (long) Math.round(lastPrice * multiplier);





    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(Good good) {
        return maxPrice(good.getType());
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
    }
}
