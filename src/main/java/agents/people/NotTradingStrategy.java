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

/**
 * Simplest default strategy: do nothing. Always.
 * Created by carrknight on 5/20/14.
 */
public class NotTradingStrategy implements PersonalTradingStrategy {

    private static NotTradingStrategy instance = null;

    public static NotTradingStrategy getInstance(){
        if(instance == null)
            instance=new NotTradingStrategy();
        return instance;
    }


    @Override
    public void beginTradingDay(Person p, MacroII model) {
        //nothing!
    }

    @Override
    public void endTradingDay(Person p, MacroII model) {
        //nothing!
    }

    @Override
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, Person seller, EconomicAgent buyer) {
        throw  new RuntimeException("Never meant to be here! This strategy makes no sale offer!");
    }

    @Override
    public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, Person buyer, EconomicAgent seller) {
        throw  new RuntimeException("Never meant to be here! This strategy makes no buy offer!");

    }

    @Override
    public void turnOff() {

    }
}
