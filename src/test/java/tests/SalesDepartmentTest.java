package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SurveySalesPredictor;
import agents.firm.sales.pricing.CostAskPricing;
import agents.firm.sales.pricing.PriceFollower;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <h4>Description</h4>
 * <p/> 10 buyers, sales department is alone doing stuff.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-28
 * @see
 */
public class SalesDepartmentTest {

    SalesDepartment dept;
    MacroII model;
    OrderBookMarket market;


    @Before
    public void simpleScenarioSetup(){
        model = new MacroII(0);
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        Firm firm = new Firm(model);
        dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, 
                new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(dept,market.getGoodType());

        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummyBuyer buyer = new DummyBuyer(model,10+i*10,market){

                @Override
                public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {
                    super.reactToFilledBidQuote(quoteFilled, g, price, seller);
                    market.deregisterBuyer(this);
                }
            };  //these dummies are modified so that if they do trade once, they quit the market entirely
            buyer.receiveMany(UndifferentiatedGoodType.MONEY, 100);
            market.registerBuyer(buyer);
            market.submitBuyQuote(buyer,buyer.quotedPrice);
        }

        //set strategies for the department
        dept.setPredictorStrategy(new SurveySalesPredictor());
        dept.setAskPricingStrategy(new CostAskPricing(dept));



    }




    @Test
    public void testPrice() throws Exception {

        for(int i=0; i < 10; i++)
        {
            Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept.getFirm(),10*i);
            dept.getFirm().receive(good, null);
            assertEquals(10 * i, dept.price(good));     //cost pricing is easy
        }

    }

    @Test
    public void testSellThis() throws Exception {

        Market.TESTING_MODE = true;


        assertEquals(dept.getMarket().getBestBuyPrice(), 100l); //all the others should have been taken
        dept.start(model);

        for(int i=0; i<10; i++){
            Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept.getFirm(),30);
            dept.getFirm().receive(good,null);
            dept.sellThis(good); //sell this, now!
        }
        model.start();

        model.schedule.step(model);


        //when the dust settles...
        assertEquals(20l, dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        assertEquals(30l, dept.getMarket().getBestSellPrice());
        assertEquals(30l, dept.getMarket().getLastPrice());

        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(380, dept.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY));




        assertEquals(2, dept.getHowManyToSell()); //2 things left to sell




        assertEquals(2, dept.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL));




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30,market);
        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //when the dust settles...
        assertEquals(20l, dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        assertEquals(30l, dept.getMarket().getBestSellPrice());
        assertEquals(30l, dept.getMarket().getLastPrice());

        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(410, dept.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY));



        assertEquals(1, dept.getHowManyToSell()); //2 things left to sell




        assertEquals(1, dept.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL));




    }

    @Test
    public void testReactToFilledQuote() throws Exception {

        Market.TESTING_MODE = true;



        //make sure it throws errors when it's not set up correctly
        Good toQuote = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept.getFirm(),10);
        dept.getFirm().receive(toQuote,null);
        //force it in the toSell list
        dept.setAskPricingStrategy(new PriceFollower(dept));
        dept.start(model);
        dept.sellThis(toQuote);
        model.start();
        model.schedule.step(model);





        assertEquals(dept.getLastClosingPrice(), 56);
        assertEquals(toQuote.getLastValidPrice(), 56);
        assertEquals(toQuote.getCostOfProduction(), 10);





    }




    @Test
    public void testUpdateQuotes() throws IllegalAccessException {

        Good g =Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept.getFirm(),200);
        dept.getFirm().receive(g,null);
        dept.start(model);
        dept.sellThis(g);
        model.start();
        model.schedule.step(model);

        //should lay unsold
        assertEquals(dept.getMarket().getBestSellPrice(), 200);
        dept.setAskPricingStrategy(new UndercuttingAskPricing(dept));
        dept.updateQuotes();
        model.getPhaseScheduler().step(model);

        assertEquals(dept.getMarket().getBestSellPrice(), 240);


    }

}
