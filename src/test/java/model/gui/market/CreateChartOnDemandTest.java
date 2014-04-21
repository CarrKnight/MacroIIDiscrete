/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.LineChart;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Created by carrknight on 4/9/14.
 */
public class CreateChartOnDemandTest {

    final static private GoodType OIL = new GoodType("oiltest","oil");


    @Before
    public void setUp() throws Exception {
        //should start the Platform
        JFXPanel panel = new JFXPanel();

    }

    //usual simple geographical test
    @Test
    public void testChartCorrect() throws Exception {
        //the whole setup is stolen from: testSellsToTheHighestCustomer in dressedSimpleSellerTest
        //here i just want to make sure the color of the buyers is changed, while the buyer that doesn't buy remains white

        MacroII macroII = new MacroII(1l);
        final GeographicalCustomer customers[] = new GeographicalCustomer[100];
        final GeographicalCustomer farCustomers[] = new GeographicalCustomer[100];
        final GeographicalFirm[] seller = new GeographicalFirm[1];
        Scenario simpleSellerOilScenario = new Scenario(macroII) {
            @Override
            public void start() {

                //geographical market for oil!
                final GeographicalMarket market = new GeographicalMarket(OIL);
                market.setPricePolicy(new ShopSetPricePolicy());
                getMarkets().put(OIL,market);

                //create the seller
                seller[0] =new GeographicalFirm(getModel(),0,0);
                seller[0].setName("Seller");
                getAgents().add(seller[0]);
                //add the sales department to the seller
                SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(seller[0], market,
                        new SimpleBuyerSearch(market, seller[0]), new SimpleSellerSearch(market, seller[0]), SalesDepartmentOneAtATime.class);
                //give the sale department a simple PID
                salesDepartment.setAskPricingStrategy(new SalesControlWithFixedInventoryAndPID(salesDepartment,50));
                //finally register it!
                seller[0].registerSaleDepartment(salesDepartment, OIL);

                //receive 10 units a day!

                //schedule the seller to sell two goods
                getModel().scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
                    @Override
                    public void step(SimState state) {
                        for (int i = 0; i < 10; i++) {
                            Good toSell = new Good(OIL, seller[0], 0);
                            seller[0].receive(toSell, null);
                            seller[0].reactToPlantProduction(toSell);

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

        for(int i=0; i< 1000; i++)
        {
            macroII.schedule.step(macroII); //make 3000 step pass
            System.out.println(market.getLatestObservation(MarketDataType.CLOSING_PRICE));
        }
        //last price should be 90
        Assert.assertEquals(10, market.getLatestObservation(MarketDataType.VOLUME_TRADED), .0001d);
        Assert.assertEquals(91d, market.getLatestObservation(MarketDataType.CLOSING_PRICE), .0001d);


        CreateChartOnDemand task = new CreateChartOnDemand(map,OIL,SalesDataType.CLOSING_PRICES);
        LineChart<Number, Number> chart = task.call();
        Assert.assertEquals(1,chart.getData().size()); //one seller
        Assert.assertEquals("Seller",chart.getData().get(0).getName()); //correct name
        Assert.assertEquals(seller[0].getSalesDepartment(OIL).numberOfObservations(),chart.getData().get(0).getData().size()); //correct name
        double[] correctObservations = seller[0].getSalesDepartment(OIL).getAllRecordedObservations(SalesDataType.CLOSING_PRICES);
        for(int i=0; i<correctObservations.length; i++)
            Assert.assertEquals(correctObservations[i],chart.getData().get(0).getData().get(i).getYValue().doubleValue(),.00001d); //correct name



    }

}
