/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.GeographicalFirm;
import financial.market.GeographicalMarket;
import financial.market.Market;
import goods.GoodType;
import goods.GoodTypeMasterList;
import model.MacroII;
import model.scenario.OilDistributorScenario;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Make sure the unified labor market works properly
 * Created by carrknight on 4/22/14.
 */
public class UnifiedLaborMarketForOilScenarioTest {


    @Test
    public void sameMarketForEveryone() throws Exception {
        MacroII model = mock(MacroII.class);
        when(model.getGoodTypeMasterList()).thenReturn(mock(GoodTypeMasterList.class));
        OilDistributorScenario scenario = mock(OilDistributorScenario.class);
        HashMap<GoodType, Market> marketMap = new HashMap<>();
        when(scenario.getMarkets()).thenReturn(marketMap);


        UnifiedLaborMarketForOilScenario marketStrategy = new UnifiedLaborMarketForOilScenario();
        marketStrategy.initializeLaborMarkets(scenario,
                mock(GeographicalMarket.class), model);

        Market market1 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);
        Market market2 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);
        Market market3 =  marketStrategy.assignLaborMarketToFirm(mock(GeographicalFirm.class),
                scenario, model);

        Assert.assertEquals(market1, market2);
        Assert.assertEquals(market3, market2);

        //one market only!
        Assert.assertEquals(scenario.getMarkets().size(),1);
        Assert.assertEquals(scenario.getMarkets().get(GoodType.LABOR),market1);
    }
}
