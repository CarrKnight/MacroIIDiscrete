/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.firm.GeographicalFirm;
import ec.util.MersenneTwisterFast;
import financial.market.GeographicalClearLastMarket;
import goods.GoodType;
import model.MacroII;
import model.scenario.oil.GeographicalCustomer;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 4/7/14.
 */
public class GeographicalMarketPresentationTest {



    //make sure the pixels coordinates are correct
    @Test
    public void positioningTest() throws Exception {
        GeographicalClearLastMarket market = new GeographicalClearLastMarket(GoodType.GENERIC);
        SellingFirmToColorMap map = new SellingFirmToColorMap(market,new MersenneTwisterFast());
        GeographicalMarketPresentation presentation = new GeographicalMarketPresentation(map,market);

        //add a seller at -5,-5 and 5,5 and a buyer at 0,0
        GeographicalFirm firm1 = new GeographicalFirm(mock(MacroII.class), -5, -5);
        GeographicalFirm firm2 = new GeographicalFirm(mock(MacroII.class), 5, 5);
        GeographicalCustomer buyer = new GeographicalCustomer(mock(MacroII.class),100l,0,0,market); //buyer autoregisters

        market.registerSeller(firm1);
        market.registerSeller(firm2);

        presentation.setOneUnitInModelEqualsHowManyPixels(100);
        //now get! (magic/binding should have taken care of everything)
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentXLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentYLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutYProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutXProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentXLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentYLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutYProperty().doubleValue(),500,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutXProperty().doubleValue(),500,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentXLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentYLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutYProperty().doubleValue(),1000,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutXProperty().doubleValue(),1000,.001d);

        //change zoom!
        presentation.setOneUnitInModelEqualsHowManyPixels(10);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentXLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentYLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutYProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutXProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentXLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentYLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutYProperty().doubleValue(),50,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutXProperty().doubleValue(),50,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentXLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentYLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutYProperty().doubleValue(),100,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutXProperty().doubleValue(),100,.001d);

    }
}
