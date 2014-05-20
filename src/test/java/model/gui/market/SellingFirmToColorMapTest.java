/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import agents.EconomicAgent;
import agents.people.Person;
import agents.firm.Firm;
import agents.firm.GeographicalFirm;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.UndifferentiatedGoodType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 4/6/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MersenneTwisterFast.class)
public class SellingFirmToColorMapTest
{

    //make sure it initializes and adapts, notice additions and deletions

    @Test
    public void testControlSellerSet() throws Exception {

        //these are in before the creation of the map
        GeographicalFirm f1 = mock(GeographicalFirm.class);
        GeographicalFirm f2 = mock(GeographicalFirm.class);
        EconomicAgent p1 = mock(Person.class);
        //initialize market
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.registerSeller(f1);
        market.registerSeller(f2);
        market.registerSeller(p1);

        //create map
        SellingFirmToColorMap colorMap = new SellingFirmToColorMap(market,new MersenneTwisterFast());
        Assert.assertEquals(colorMap.getColorMap().size(), 2); //should have ignored p1!
        Assert.assertNotNull(colorMap.getFirmColor(f1));
        Assert.assertNotNull(colorMap.getFirmColor(f2));
        Assert.assertNull(colorMap.getFirmColor(mock(GeographicalFirm.class))); //random firms shouldn't be in!

        //add f3, remove f1
        Firm f3 = mock(GeographicalFirm.class);
        market.registerSeller(f3);
        market.deregisterSeller(f2);
        Assert.assertEquals(colorMap.getColorMap().size(), 2); //f2 gone, f3 came
        Assert.assertNotNull(colorMap.getFirmColor(f1));
        Assert.assertNull(colorMap.getFirmColor(f2));
        Assert.assertNotNull(colorMap.getFirmColor(f3));
        Assert.assertEquals(colorMap.getFirmColor(f3),SellingFirmToColorMap.getDefaultColors().get(2)); //not recycling colors!



    }


    //make sure the randomizer works.
    @Test
    public void testRandomizesColor() throws Exception {

        int defaultColorSize = SellingFirmToColorMap.getDefaultColors().size();
        //initialize market
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);

        //loaded dice
        MersenneTwisterFast loadedRandomizer = PowerMockito.mock(MersenneTwisterFast.class);
        PowerMockito.when(loadedRandomizer.nextDouble()).thenReturn(.1d);


        //create map
        SellingFirmToColorMap colorMap = new SellingFirmToColorMap(market,loadedRandomizer);

        for(int i=0; i<defaultColorSize; i++)
            market.registerSeller(mock(GeographicalFirm.class));
        //one more!
        Firm f1 = mock(GeographicalFirm.class);
        market.registerSeller(f1);

        Assert.assertEquals(colorMap.getFirmColor(f1).getBlue(),.1d,.001d);
        Assert.assertEquals(colorMap.getFirmColor(f1).getRed(),.1d,.001d);
        Assert.assertEquals(colorMap.getFirmColor(f1).getGreen(),.1d,.001d);




    }


}
