/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.firm.Firm;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * This is more or less legacy code at this point. Basically one abandoned experiment had workers always quitting if better offers were available.
 * This resulted in fun dynamics because workers had perfect knowledge of the labor market while employers had none.
 * If both sides match you get very quickly to perfect competition, but that's sort of obvious given perfect knowledge.
 * Still, here it is.
 * Created by carrknight on 5/18/14.
 */
public class LookForBetterOffersAfterWorkStrategy implements AfterWorkStrategy {

    @Override
    public void endOfWorkDay(Person p, MacroII model, Firm employer, int wage, UndifferentiatedGoodType wageKind)
    {
        //don't look any further if you don't have a job or a market
        //also don't go forward if you are about to quit for some other reason
        final Market laborMarket = p.getLaborMarket();
        if(employer == null || laborMarket == null || p.isAboutToQuit())
            return;


        //make sure you are correctly employed
        assert wage >=0;
        //wage ought to be above what your minimum or you'd be quitting right now
        assert (p.getMinimumDailyWagesRequired() <= wage) || p.isAboutToQuit() ;


        //can you see the best offer now?
        if(laborMarket.isBestBuyPriceVisible())
        {
            //if there is work advertised ABOVE the wage we are being paid: quit!
            try {
                if(laborMarket.getBestBuyPrice() > wage) //notice that this also takes care of when there is no offer at all (best price then is -1)
                {
                    assert p.getMinimumDailyWagesRequired() < laborMarket.getBestBuyPrice(); //transitive property don't abandon me now!
                    p.quitWork(); //so long, suckers
                    //this might be false with better markets:
                }
            } catch (IllegalAccessException e) {
                assert false;
                throw new RuntimeException("The market said the best price was visible, but it wasn't");

            }

        }
        else
        {

            throw new RuntimeException("Not implemented yet");


        }
    }
}
