package agents.firm.sales;

import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.LinearExtrapolationPredictor;
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.MarkupFollower;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.market.OrderBookMarket;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 * @author carrknight
 * @version 2013-06-05
 * @see
 */
public class SalesDepartmentStopConsumingTest {

    //since these tests should be tested for mny different combinations of strategies, I run them 50 times each

    @Test
    public void testStopConsuming() throws Exception {
        for(int i=0; i<50; i++)
        {
            testStopSellingThisGoodBecauseItWasConsumed_WithAllAtOnceDepartment(i);
            testStopSellingThisGoodBecauseItWasConsumed_WithOneAtATimeDepartment(i);
        }
    }



    public void testStopSellingThisGoodBecauseItWasConsumed_WithAllAtOnceDepartment(int seed) throws Exception {

        //create the model
        MacroII macroII = new MacroII(seed);
        //add a special phase scheduler that ignores everything scheduled not at trade time
        OrderBookMarket orderBookMarket = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        //create a simple firm
        Firm firm = new Firm(macroII);
        //now create the sales department
        FactoryProducedSalesDepartment
                <BuyerSearchAlgorithm,SellerSearchAlgorithm,SimpleFlowSellerPID,LinearExtrapolationPredictor> factoryMade =
                SalesDepartmentFactory.
                newSalesDepartment(firm, orderBookMarket, null, null, SimpleFlowSellerPID.class, LinearExtrapolationPredictor.class,
                        SalesDepartmentAllAtOnce.class);
        SalesDepartment department = factoryMade.getSalesDepartment();
        AskPricingStrategy fixed = mock(AskPricingStrategy.class); when(fixed.price(any())).thenReturn(100);
        department.setAskPricingStrategy(fixed);
        department.setPredictorStrategy(new PricingSalesPredictor());


        //give three goods to the firm
        department.start(macroII);
        macroII.start();


        firm.receiveMany(UndifferentiatedGoodType.GENERIC,3);
        firm.reactToPlantProduction(UndifferentiatedGoodType.GENERIC,3);

        //step the model so that the orders are placed

        macroII.schedule.step(macroII);


        //they should all be quoted in the market
        Assert.assertEquals(orderBookMarket.numberOfAsks(),3);
        //the sales department should have them all in its master list
        Assert.assertEquals(3, department.numberOfGoodsToSell());
        Assert.assertEquals(3, department.numberOfQuotesPlaced());
        //and the firm should have not sold any

        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),3);




        //now consume 2 goods
        firm.consume(UndifferentiatedGoodType.GENERIC);
        firm.consume(UndifferentiatedGoodType.GENERIC);



        //only one good in inventory
        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),1);
        //only one good being sold
        Assert.assertEquals(1, department.numberOfGoodsToSell());
        Assert.assertEquals(1, department.numberOfQuotesPlaced());
        //there should be only one quote in the market now
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);


        //consume the last!
        firm.consume(UndifferentiatedGoodType.GENERIC);
        //should be empty now
        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(0, department.numberOfGoodsToSell());
        Assert.assertEquals(0, department.numberOfQuotesPlaced());
        Assert.assertEquals(orderBookMarket.numberOfAsks(),0);




    }


    //this is the same as above, except it is using OneAtATime departments
    public void testStopSellingThisGoodBecauseItWasConsumed_WithOneAtATimeDepartment(int seed) throws Exception {

        //create the model
        MacroII macroII = new MacroII(seed);
        //create a simple market
        OrderBookMarket orderBookMarket = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        //create a simple firm
        Firm firm = new Firm(macroII);
        //now create the sales department
        SalesDepartment department = SalesDepartmentFactory.
                newSalesDepartment(firm,orderBookMarket,null,null,MarkupFollower.class,null,SalesDepartmentOneAtATime.class).getSalesDepartment();

        department.setPredictorStrategy(new PricingSalesPredictor());

        macroII.start();
        department.start(macroII);

        //give three goods to the firm
        firm.receiveMany(UndifferentiatedGoodType.GENERIC,3);
        firm.reactToPlantProduction(UndifferentiatedGoodType.GENERIC,3);
        //step the model so that the orders are placed
        macroII.schedule.step(macroII);


        //only one of them should be quoted in the market
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);
        //the sales department should have them all in its master list
        Assert.assertEquals(1,department.numberOfQuotesPlaced());
        Assert.assertEquals(3,department.numberOfGoodsToSell());
        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),3);




        //now consume 2 goods
        firm.consume(UndifferentiatedGoodType.GENERIC);
        firm.consume(UndifferentiatedGoodType.GENERIC);


        //step the model again to make sure that the queued good is being quoted
        macroII.getPhaseScheduler().step(macroII);

        //only one good in inventory
        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),1);
        //only one good being sold
        Assert.assertEquals(1,department.numberOfGoodsToSell());
        Assert.assertEquals(1,department.numberOfQuotesPlaced());
        //there should be only one quote in the market now
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);


        //consume the last!
        firm.consume(UndifferentiatedGoodType.GENERIC);
        //should be empty now
        Assert.assertEquals(firm.hasHowMany(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(0,department.numberOfGoodsToSell());
        Assert.assertEquals(0,department.numberOfQuotesPlaced());
        Assert.assertEquals(orderBookMarket.numberOfAsks(),0);




    }


}
