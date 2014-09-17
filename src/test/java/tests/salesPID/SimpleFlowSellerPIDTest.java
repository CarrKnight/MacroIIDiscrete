/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.salesPID;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DummyBuyer;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-28
 * @see
 */
public class SimpleFlowSellerPIDTest {


    @Test
    public void scenario1()
    {

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        MacroII model = new MacroII(System.currentTimeMillis());
        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept,.1f,.1f,0f,0, dept.getMarket(), dept.getRandom().nextInt(100), dept.getFirm().getModel());
        dept.setAskPricingStrategy(strategy);

        //register sale department
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        dept.start(model);
        model.start();



        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 4 goods!
            model.scheduleSoon(ActionOrder.PRODUCTION, simState -> {
                for(int i=0; i<4; i++){
                    Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                    firm.receive(good,null);
                    dept.sellThis(good);
                }
            });

            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
            model.schedule.step(model);
            System.out.println("seller price :" + strategy.getTargetPrice());
            for(Quote q : quotes)
                try{
                    market.removeBuyQuote(q);
                }
                catch (IllegalArgumentException ignored){}


        }



        assertTrue(strategy.getTargetPrice() > 60 && strategy.getTargetPrice() <=70);




        System.out.println("-------------------------------------------------------------------------------------");

    }

    @Test
    public void scenario2()   //like scenario 1 but the initial price is set very high
    {

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        dept.setAskPricingStrategy(strategy);
        strategy.setInitialPrice(150);


        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        dept.start(model);
        model.start();

        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 4 goods!
            model.scheduleSoon(ActionOrder.PRODUCTION, simState -> {
                for(int i=0; i<4; i++){
                    Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                    firm.receive(good,null);
                    dept.sellThis(good);
                }
            });


            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
            model.schedule.step(model);
            System.out.println("seller price :" + strategy.getTargetPrice());
            for(Quote q : quotes)
                try{
                    market.removeBuyQuote(q);
                }
                catch (IllegalArgumentException ignored){}

        }



        assertTrue(strategy.getTargetPrice() > 60 && strategy.getTargetPrice() <=70);




        System.out.println("-------------------------------------------------------------------------------------");

    }



    @Test
    public void scenario4()  //like scenario 2 but production costs are 80
    {

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario4");


        MacroII model = new MacroII(1l);
        model.start();

        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm),
                new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        strategy.setProductionCostOverride(true);
        dept.setAskPricingStrategy(strategy);
        firm.registerSaleDepartment(dept, DifferentiatedGoodType.CAPITAL);
        strategy.setInitialPrice(150);


        model.addAgent(firm);
     //   dept.start(model);




        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<300;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }


            //sell 4 goods!
            model.scheduleSoon(ActionOrder.PRODUCTION, simState -> {
                for(int i=0; i<4; i++){
                    Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,firm,80);
                    firm.receive(good,null);
                    dept.sellThis(good);
                }
            });



            model.schedule.step(model);

            System.out.println("seller price :" + dept.hypotheticalSalePrice(80));
            for(Quote q : quotes)
                try{
                    market.removeBuyQuote(q);
                }
                catch (IllegalArgumentException ignored){}

        }



        assertTrue( dept.hypotheticalSalePrice(80) ==80);




        System.out.println("-------------------------------------------------------------------------------------");

    }


}
