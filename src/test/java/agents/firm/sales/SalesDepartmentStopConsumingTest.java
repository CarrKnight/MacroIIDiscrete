package agents.firm.sales;

import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.LinearExtrapolationPredictor;
import agents.firm.sales.pricing.MarkupFollower;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

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
        for(long i=0; i<50; i++)
        {
            testStopSellingThisGoodBecauseItWasConsumed_WithAllAtOnceDepartment(i);
            testStopSellingThisGoodBecauseItWasConsumed_WithOneAtATimeDepartment(i);
        }
    }



    public void testStopSellingThisGoodBecauseItWasConsumed_WithAllAtOnceDepartment(long seed) throws Exception {

        //create the model
        MacroII macroII = new MacroII(seed);
        //add a special phase scheduler that ignores everything scheduled not at trade time
        OrderBookMarket orderBookMarket = new OrderBookMarket(GoodType.GENERIC);
        //create a simple firm
        Firm firm = new Firm(macroII);
        //now create the sales department
        FactoryProducedSalesDepartment
                <BuyerSearchAlgorithm,SellerSearchAlgorithm,MarkupFollower,LinearExtrapolationPredictor> factoryMade =
                SalesDepartmentFactory.
                newSalesDepartment(firm, orderBookMarket, null, null, MarkupFollower.class, LinearExtrapolationPredictor.class,
                        SalesDepartmentAllAtOnce.class);
        SalesDepartment department = factoryMade.getSalesDepartment();

        //give three goods to the firm
        Good[] produced = new Good[3];
        department.start();
        macroII.start();


        for(int i=0; i <3 ; i++)
        {
            produced[i] = new Good(GoodType.GENERIC,firm,1l);
            firm.receive(produced[i],null);
            firm.reactToPlantProduction(produced[i]);
        }
        //step the model so that the orders are placed

        macroII.schedule.step(macroII);


        //they should all be quoted in the market
        Assert.assertEquals(orderBookMarket.numberOfAsks(),3);
        //the sales department should have them all in its master list
        for(Good g : produced)
            Assert.assertTrue(department.isSelling(g));
        //and the firm should have not sold any
        for(Good g : produced)
            Assert.assertTrue(firm.has(g));
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),3);




        //now consume 2 goods
        firm.consume(GoodType.GENERIC);
        firm.consume(GoodType.GENERIC);



        //only one good in inventory
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),1);
        //only one good being sold
        int sum = 0 ;
        for(Good g : produced)
            if(department.isSelling(g))
                sum++;
        Assert.assertEquals(sum,1);
        //there should be only one quote in the market now
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);


        //consume the last!
        firm.consume(GoodType.GENERIC);
        //should be empty now
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),0);
        for(Good g : produced)
            Assert.assertTrue(!department.isSelling(g));
        Assert.assertEquals(orderBookMarket.numberOfAsks(),0);




    }


    //this is the same as above, except it is using OneAtATime departments
    public void testStopSellingThisGoodBecauseItWasConsumed_WithOneAtATimeDepartment(long seed) throws Exception {

        //create the model
        MacroII macroII = new MacroII(seed);
        //create a simple market
        OrderBookMarket orderBookMarket = new OrderBookMarket(GoodType.GENERIC);
        //create a simple firm
        Firm firm = new Firm(macroII);
        //now create the sales department
        SalesDepartment department = SalesDepartmentFactory.
                newSalesDepartment(firm,orderBookMarket,null,null,MarkupFollower.class,null,SalesDepartmentOneAtATime.class).getSalesDepartment();


        department.start();
        macroII.start();

        //give three goods to the firm
        Good[] produced = new Good[3];
        for(int i=0; i <3 ; i++)
        {
            produced[i] = new Good(GoodType.GENERIC,firm,1l);
            firm.receive(produced[i],null);
            firm.reactToPlantProduction(produced[i]);
        }
        //step the model so that the orders are placed
        macroII.schedule.step(macroII);


        //only one of them should be quoted in the market
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);
        //the sales department should have them all in its master list
        for(Good g : produced)
            Assert.assertTrue(department.isSelling(g));
        //and the firm should have not sold any
        for(Good g : produced)
            Assert.assertTrue(firm.has(g));
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),3);




        //now consume 2 goods
        firm.consume(GoodType.GENERIC);
        firm.consume(GoodType.GENERIC);


        //step the model again to make sure that the queued good is being quoted
        macroII.getPhaseScheduler().step(macroII);

        //only one good in inventory
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),1);
        //only one good being sold
        int sum = 0 ;
        for(Good g : produced)
            if(department.isSelling(g))
                sum++;
        Assert.assertEquals(sum,1);
        //there should be only one quote in the market now
        Assert.assertEquals(orderBookMarket.numberOfAsks(),1);


        //consume the last!
        firm.consume(GoodType.GENERIC);
        //should be empty now
        Assert.assertEquals(firm.hasHowMany(GoodType.GENERIC),0);
        for(Good g : produced)
            Assert.assertTrue(!department.isSelling(g));
        Assert.assertEquals(orderBookMarket.numberOfAsks(),0);




    }


}
