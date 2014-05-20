/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.GeographicalFirm;
import financial.market.GeographicalMarket;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/22/14.
 */
public class IndipendentLaborMarketsForEachFirmStrategyTest {


    @Test
    public void sameMarketForEveryone() throws Exception {
        MacroII model = new MacroII(1);
        model.start();
        OilDistributorScenario scenario = mock(OilDistributorScenario.class);
        HashMap<GoodType, Market> marketMap = new HashMap<>();
        when(scenario.getMarkets()).thenReturn(marketMap);


        IndipendentLaborMarketsForEachFirmStrategy marketStrategy = new IndipendentLaborMarketsForEachFirmStrategy();
        marketStrategy.initializeLaborMarkets(scenario,
                mock(GeographicalMarket.class), model);

        Market market1 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);
        Market market2 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);
        Market market3 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);

        Assert.assertNotEquals(market1, market2);
        Assert.assertNotEquals(market3, market2);

        //one market only!
        Assert.assertEquals(model.getGoodTypeMasterList().size(),7); //3 built in this test plus the 4 default ones!
        Assert.assertTrue(model.getMarkets().contains(market1));
        Assert.assertTrue(model.getMarkets().contains(market2));
        Assert.assertTrue(model.getMarkets().contains(market3));
    }

}
