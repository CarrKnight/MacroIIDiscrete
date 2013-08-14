package tests.salesPID;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import model.utilities.dummies.DummyBuyer;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

        Market.TESTING_MODE = true;
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        dept.setAskPricingStrategy(strategy);

        //register sale department
        firm.registerSaleDepartment(dept,GoodType.GENERIC);




        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.earn(1000l);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 4 goods!
            for(int i=0; i<4; i++){
                Good good = new Good(GoodType.GENERIC,firm,50l);
                firm.receive(good,null);
                dept.sellThis(good);
            }

            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
            model.getPhaseScheduler().step(model);
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

        Market.TESTING_MODE = true;
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        dept.setAskPricingStrategy(strategy);
        strategy.setInitialPrice(150l);


        firm.registerSaleDepartment(dept,GoodType.GENERIC);

        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.earn(1000l);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 4 goods!
            for(int i=0; i<4; i++){
                Good good = new Good(GoodType.GENERIC,firm,10l);
                firm.receive(good,null);
                dept.sellThis(good);
            }

            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
            model.getPhaseScheduler().step(model);
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
    public void scenario3() throws IllegalAccessException   //now it's 2 sellers, each with 2 goods to sell each
    {

        Market.TESTING_MODE = true;
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario3");

        MacroII model = new MacroII(1l);
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);

        Firm firm1 = new Firm(model);
        SalesDepartment dept1 = SalesDepartmentFactory.incompleteSalesDepartment(firm1, market, new SimpleBuyerSearch(market, firm1), new SimpleSellerSearch(market, firm1), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy1 = new SimpleFlowSellerPID(dept1);
        dept1.setAskPricingStrategy(strategy1);
        firm1.registerSaleDepartment(dept1,GoodType.GENERIC);


        Firm firm2 = new Firm(model);
        SalesDepartment dept2 = SalesDepartmentFactory.incompleteSalesDepartment(firm2, market, new SimpleBuyerSearch(market, firm2), new SimpleSellerSearch(market, firm2), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy2 = new SimpleFlowSellerPID(dept2);
        dept2.setAskPricingStrategy(strategy2);
        firm2.registerSaleDepartment(dept2,GoodType.GENERIC);





        List<Quote> quotes =new LinkedList<>();

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.earn(1000l);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 2 goods!
            for(int i=0; i<2; i++){
                Good good = new Good(GoodType.GENERIC,firm1,10l);
                firm1.receive(good,null);
                dept1.sellThis(good);
            }
            //sell 2 goods!
            for(int i=0; i<2; i++){
                Good good = new Good(GoodType.GENERIC,firm2,10l);
                firm2.receive(good,null);
                dept2.sellThis(good);
            }

            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy1);
            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy2);

            model.getPhaseScheduler().step(model);

            System.out.println("At time: " +j +" seller1 price :" + strategy1.getTargetPrice() + " ---  seller2 price :" + strategy2.getTargetPrice());
            if(j<99)
                for(Quote q : quotes)
                    try{
                        market.removeBuyQuote(q);
                    }
                    catch (IllegalArgumentException ignored){}

        }


        //this is not necessarilly true because of the order with which things are sold
        assertEquals(market.getBestBuyPrice(), 60l);
        assertTrue(strategy1.getTargetPrice() > 60 && strategy1.getTargetPrice() <=75);
        assertTrue(strategy2.getTargetPrice() > 60 && strategy2.getTargetPrice() <=75);





        System.out.println("-------------------------------------------------------------------------------------");

    }


    @Test
    public void scenario4()  //like scenario 2 but production costs are 80
    {

        Market.TESTING_MODE = true;
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario4");


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        strategy.setProductionCostOverride(true);
        dept.setAskPricingStrategy(strategy);

        strategy.setInitialPrice(150l);


        firm.registerSaleDepartment(dept,GoodType.GENERIC);

        List<Quote> quotes =new LinkedList<>();
        long price=0;

        for(int j=0;j<100;j++){ //do 100 times!



            //10 sellers
            //1 buyer
            for(int i=1; i<11; i++)
            {
                DummyBuyer buyer = new DummyBuyer(model,i*10,market);
                market.registerBuyer(buyer);
                buyer.earn(1000l);
                Quote q = market.submitBuyQuote(buyer,i*10);
                quotes.add(q);
            }

            //sell 4 goods!
            for(int i=0; i<4; i++){
                Good good = new Good(GoodType.GENERIC,firm,80l);
                firm.receive(good,null);
                price = dept.price(good);
                dept.sellThis(good);
            }

            model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
            model.getPhaseScheduler().step(model);

            System.out.println("seller price :" + price);
            for(Quote q : quotes)
                try{
                    market.removeBuyQuote(q);
                }
                catch (IllegalArgumentException ignored){}

        }



        assertTrue(price ==80);




        System.out.println("-------------------------------------------------------------------------------------");

    }


}
