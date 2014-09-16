/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import agents.firm.GeographicalFirm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import ec.util.MersenneTwisterFast;
import financial.market.GeographicalMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import javafx.embed.swing.JFXPanel;
import javafx.scene.paint.Color;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 4/7/14.
 */
public class GeographicalMarketPresentationTest {

    final static private GoodType OIL = new UndifferentiatedGoodType("oiltest","oil");



    @Before
    public void setUp() throws Exception {
        //should start the Platform
        JFXPanel panel = new JFXPanel();

    }

    //make sure the pixels coordinates are correct
    @Test
    public void positioningWhenEverythingIsInitialized() throws Exception {
        GeographicalMarket market = new GeographicalMarket(UndifferentiatedGoodType.GENERIC);
        SellingFirmToColorMap map = new SellingFirmToColorMap(market,new MersenneTwisterFast());

        //add a seller at -5,-5 and 5,5 and a buyer at 0,0
        GeographicalFirm firm1 = new GeographicalFirm(mock(MacroII.class), -5, -5);
        GeographicalFirm firm2 = new GeographicalFirm(mock(MacroII.class), 5, 5);
        GeographicalCustomer buyer = new GeographicalCustomer(mock(MacroII.class),100,0,0,market); //buyer autoregisters


        market.registerSeller(firm1);
        market.registerSeller(firm2);


        GeographicalMarketPresentation presentation = new GeographicalMarketPresentation(map,market,mock(MacroII.class));


        isPositionCorrect(firm1, firm2, buyer, presentation);

    }

    //make sure the pixels coordinates are correct
    @Test
    public void positioningWhenListening() throws Exception {
        GeographicalMarket market = new GeographicalMarket(UndifferentiatedGoodType.GENERIC);
        SellingFirmToColorMap map = new SellingFirmToColorMap(market,new MersenneTwisterFast());
        GeographicalMarketPresentation presentation = new GeographicalMarketPresentation(map,market,mock(MacroII.class));

        //add a seller at -5,-5 and 5,5 and a buyer at 0,0
        GeographicalFirm firm1 = new GeographicalFirm(mock(MacroII.class), -5, -5);
        GeographicalFirm firm2 = new GeographicalFirm(mock(MacroII.class), 5, 5);
        GeographicalCustomer buyer = new GeographicalCustomer(mock(MacroII.class),100,0,0,market); //buyer autoregisters


        market.registerSeller(firm1);
        market.registerSeller(firm2);




        isPositionCorrect(firm1, firm2, buyer, presentation);

    }

    private void isPositionCorrect(GeographicalFirm firm1, GeographicalFirm firm2, GeographicalCustomer buyer, GeographicalMarketPresentation presentation) {
        presentation.setOneUnitInModelEqualsHowManyPixels(100);
        //now get! (magic/binding should have taken care of everything)
        Assert.assertEquals(presentation.getMinimumModelX(), -6, .0001d); //the minimum always round way down
        Assert.assertEquals(presentation.getMinimumModelY(),-6,.0001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentXLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentYLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutYProperty().doubleValue(),100,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutXProperty().doubleValue(),100,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentXLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentYLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutYProperty().doubleValue(),600,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutXProperty().doubleValue(),600,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentXLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentYLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutYProperty().doubleValue(),1100,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutXProperty().doubleValue(),1100,.001d);

        //change zoom!
        presentation.setOneUnitInModelEqualsHowManyPixels(10);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentXLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).agentYLocationProperty().doubleValue(),-5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutYProperty().doubleValue(),10,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm1).layoutXProperty().doubleValue(),10,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentXLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).agentYLocationProperty().doubleValue(),0,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutYProperty().doubleValue(),60,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(buyer).layoutXProperty().doubleValue(),60,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentXLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).agentYLocationProperty().doubleValue(),5,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutYProperty().doubleValue(),110,.001d);
        Assert.assertEquals(presentation.getPortraitList().get(firm2).layoutXProperty().doubleValue(),110,.001d);
    }




    @Test
    public void testChangeColor() throws Exception {
        //the whole setup is stolen from: testSellsToTheHighestCustomer in dressedSimpleSellerTest
        //here i just want to make sure the color of the buyers is changed, while the buyer that doesn't buy remains white

        MacroII macroII = new MacroII(1);
        final GeographicalCustomer customers[] = new GeographicalCustomer[100];
        final GeographicalCustomer farCustomers[] = new GeographicalCustomer[100];

        Scenario simpleSellerOilScenario = new Scenario(macroII) {
            @Override
            public void start() {

                //geographical market for oil!
                final GeographicalMarket market = new GeographicalMarket(OIL);
                market.setPricePolicy(new ShopSetPricePolicy());
                getMarkets().put(OIL,market);

                //create the seller
                final GeographicalFirm seller =new GeographicalFirm(getModel(),0,0);
                getAgents().add(seller);
                //add the sales department to the seller
                SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(seller, market,
                        new SimpleBuyerSearch(market, seller), new SimpleSellerSearch(market, seller), SalesDepartmentOneAtATime.class);
                //give the sale department a simple PID
                salesDepartment.setAskPricingStrategy(new SalesControlWithFixedInventoryAndPID(salesDepartment,50));
                //finally register it!
                seller.registerSaleDepartment(salesDepartment, OIL);

                //receive 10 units a day!

                //schedule the seller to sell two goods
                getModel().scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
                    @Override
                    public void step(SimState state) {
                        for (int i = 0; i < 10; i++) {
                            Good toSell = Good.getInstanceOfUndifferentiatedGood(OIL);
                            seller.receive(toSell,null);
                            seller.reactToPlantProduction(toSell);

                        }
                        getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);

                    }



                });

                //create 100 fake buyers, with max price from 1 to 100
                //create and start the 3 buyers
                for(int i=0; i<customers.length; i++)
                {
                    customers[i] =new GeographicalCustomer(getModel(),i+1,0,0,market);
                    getAgents().add(customers[i]);
                }
                //create another 100 fake buyers that all want to pay 200 but they are very very far. They shouldn't be able to buy anything!
                for(int i=0; i<farCustomers.length; i++)
                {
                    farCustomers[i] =new GeographicalCustomer(getModel(),200,500,500,market);
                    getAgents().add(farCustomers[i]);
                }
            }
        };
        macroII.setScenario(simpleSellerOilScenario);
        macroII.start();
        GeographicalMarket market = (GeographicalMarket) macroII.getMarket(OIL);


        //CREATE GUI OBJECTS!
        SellingFirmToColorMap map = new SellingFirmToColorMap(market,new MersenneTwisterFast());
        GeographicalMarketPresentation presentation = new GeographicalMarketPresentation(map,market,macroII);

        for(int i=0; i< 1000; i++)
        {
            macroII.schedule.step(macroII); //make 3000 step pass
            System.out.println(market.getLatestObservation(MarketDataType.CLOSING_PRICE));
        }
        //last price should be 90
        Assert.assertEquals(10,market.getLatestObservation(MarketDataType.VOLUME_TRADED),.0001d);
        Assert.assertEquals(91d, market.getLatestObservation(MarketDataType.CLOSING_PRICE), .0001d);


        //make sure the only firm is colored correctly
        Assert.assertEquals(market.getSellers().size(),1);
        GeographicalFirm seller = (GeographicalFirm) market.getSellers().iterator().next();
        Color firmExpectedColor = SellingFirmToColorMap.getDefaultColors().get(0);
        Assert.assertEquals(presentation.getPortraitList().get(seller).getColor(),
                firmExpectedColor); //should be the default color


        //all distant agents bought nothing SO THEIR COLOR IS WHITE!
        for(GeographicalCustomer farCustomer : farCustomers)
        {
            Assert.assertEquals(0, farCustomer.hasHowMany(OIL));
            Assert.assertNull(farCustomer.getLastSupplier());
            Assert.assertEquals(presentation.getPortraitList().get(farCustomer).getColor(), Color.BLACK);
        }
        //cheap people got nothing
        for(int i=0; i<90; i++)
        {
            Assert.assertEquals(0, customers[i].hasHowMany(OIL));
            Assert.assertNull(customers[i].getLastSupplier());
            Assert.assertEquals(presentation.getPortraitList().get(customers[i]).getColor(), Color.BLACK);

        }

        //rich people got everything
        for(int i=90; i<customers.length; i++)
        {
            Assert.assertEquals(1,customers[i].hasHowMany(OIL));
            Assert.assertEquals(customers[i].getLastSupplier(),seller);
            MarketPresentationTest.waitForRunLater();

            Assert.assertEquals(presentation.getPortraitList().get(customers[i]).getColor(), firmExpectedColor);

        }


    }
}
