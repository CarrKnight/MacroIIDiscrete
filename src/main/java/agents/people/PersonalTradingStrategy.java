/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.EconomicAgent;
import financial.utilities.Quote;
import goods.Good;
import model.MacroII;
import model.utilities.Deactivatable;

/**
 * The strategy used by a person to buy and sell goods (except his labor which is dealt with separately)
 * Created by carrknight on 5/20/14.
 */
public interface PersonalTradingStrategy extends Deactivatable
{

    /**
     * called at PREPARE_TO_TRADE this method is useful for a person to prepare and perhaps schedule itself for trading
     * @param p the person
     * @param model the model reference for easy scheduling
     */
    public void beginTradingDay(Person p, MacroII model);

    /**
     * Called at AFTER_TRADE, this method signal the person that the trade phase is over and it should go and do something about his life
     * @param p the person
     * @param model the model reference
     */
    public void endTradingDay(Person p, MacroII model);

    /**
     * Called by the market when a sell quote gets filled. By the time this method is called the trade has already occurred
     * @param quoteFilled the quote filled
     * @param g the good sold
     * @param price the final price
     * @param seller the person you are strategizing for that managed to sell
     * @param buyer the buyer of your good
     */
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, Person seller, EconomicAgent buyer);

    /**
     * Called by the market when a buy quote gets filled. By the time this method is called the trade has already occurred
     * @param quoteFilled the quote filled
     * @param g the good bought
     * @param price the final price
     * @param buyer the person (you are controlling)
     * @param seller the person we sold the good to.
     */
    public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, Person buyer, EconomicAgent seller);
}
