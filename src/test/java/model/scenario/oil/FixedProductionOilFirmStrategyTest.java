/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.Firm;
import financial.market.GeographicalMarket;
import goods.GoodType;
import model.MacroII;
import model.utilities.geography.Location;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/23/14.
 */
public class FixedProductionOilFirmStrategyTest {


    @Test
    public void testOilProduction() throws Exception {

        FixedProductionOilFirmStrategy strategy = new FixedProductionOilFirmStrategy();
        MacroII model = new MacroII(1l);
        model.start();
        GeographicalMarket stub = mock(GeographicalMarket.class); when(stub.getGoodType()).thenReturn(GoodType.GENERIC);
        //create the firm
        Firm firm = strategy.createOilPump(new Location(0,0),stub,"test",mock(OilDistributorScenario.class),model);
        //should start with no inventory but the sales department should be initialized
        Assert.assertEquals(0,firm.hasHowMany(GoodType.GENERIC));
        Assert.assertNotNull(firm.getSalesDepartment(GoodType.GENERIC));
        Assert.assertEquals(0,firm.getHRs().size());
        Assert.assertEquals(0,firm.getPlants().size());

        model.schedule.step(model);
        model.schedule.step(model);
        model.schedule.step(model);

        //should have 30 now
        Assert.assertEquals(30,firm.hasHowMany(GoodType.GENERIC));
        Assert.assertNotNull(firm.getSalesDepartment(GoodType.GENERIC));
        Assert.assertEquals(0,firm.getHRs().size());
        Assert.assertEquals(0,firm.getPlants().size());


    }


}
