package tests.plantcontrol;

import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.MemorySalesPredictor;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.MarginalPlantControl;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.MarginalMaximizer;
import agents.firm.production.technology.LinearConstantMachinery;
import junit.framework.Assert;
import model.MacroII;
import model.utilities.DelayException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

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
 * @version 2012-10-05
 * @see
 */
public class MarginalMaximizerTest {

    @Test
    public void testDelay(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.workerSize()).thenReturn(10);
        when(hr.predictPurchasePrice()).thenReturn(-1l); //tell the hr to fail at predicting
        PlantControl control = mock(PlantControl.class);
        //it should immediately fail
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control);
        boolean exceptionThrown = false;
        try {
            float marginalProfits = maximizer.computeMarginalProfits(10,11);
            Assert.fail();
        } catch (DelayException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);


    }

    @Test
    public void testMarginalProfits(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class); when(p.minimumWorkersNeeded()).thenReturn(0); when(p.maximumWorkersPossible()).thenReturn(1000);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        when(p.getBlueprint()).thenReturn(b);
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.workerSize()).thenReturn(10);
        when(hr.predictPurchasePrice()).thenReturn(-1l); //tell the hr to fail at predicting
        SalesDepartment sales = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(sales);
        when(p.hypotheticalThroughput(anyInt(),any(GoodType.class))).thenAnswer(new Answer<Object>() {     //production is just number of workers
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });



        //say that wages are always 50, sell prices are always 100
        when(hr.predictPurchasePrice()).thenReturn(50l); when(hr.hypotheticalWageAtThisLevel(anyInt())).thenReturn(50l);
        when(hr.getWagesPaid()).thenReturn(10*50l);
        when(sales.predictSalePrice(anyLong())).thenReturn(100l); when(sales.getLastClosingPrice()).thenReturn(100l);



        PlantControl control = mock(PlantControl.class);
        //it should immediately fail
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control);
        boolean exceptionThrown = false;
        float marginalProfits = -1;
        try {
            marginalProfits = maximizer.computeMarginalProfits(10,11);
            Assert.assertEquals(marginalProfits,50f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,9);
            Assert.assertEquals(marginalProfits,-50f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,8);
            Assert.assertEquals(marginalProfits,-100f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,12);
            Assert.assertEquals(marginalProfits,100f,.0001f);
        } catch (DelayException e) {
            Assert.fail();
        }
        Assert.assertTrue(!exceptionThrown);




        //now make sure it's used properly in the maximizer method.
        //the method is protected so we need to open it from afar
        //chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits)
        try {
            Method chooseTarget = MarginalMaximizer.class.getDeclaredMethod("chooseWorkerTarget", int.class, float.class, int.class, float.class);
            chooseTarget.setAccessible(true);
            Integer newtarget = (Integer) chooseTarget.invoke(maximizer,10,10000,11,1000000);
            Assert.assertEquals(11,newtarget.intValue());
            //same should happen at target 100
            when(p.workerSize()).thenReturn(100);  when(hr.getWagesPaid()).thenReturn(50*100l);
            newtarget = (Integer) chooseTarget.invoke(maximizer,100,10000,11,1000000);
            Assert.assertEquals(101,newtarget.intValue());


            //do it again, but this time it pays to cut back
            when(hr.predictPurchasePrice()).thenReturn(100l);
            when(hr.getWagesPaid()).thenReturn(10*50l);
            when(sales.predictSalePrice(anyLong())).thenReturn(100l); when(sales.getLastClosingPrice()).thenReturn(100l);



        } catch (NoSuchMethodException  | InvocationTargetException | IllegalAccessException e) {
            Assert.fail();
        }


    }




    //todo needs better predict sales to be accurate, so far it is always preferring to buy
    public void monopolist() throws Exception
    {


        Market.TESTING_MODE = true;
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        final MacroII model = new MacroII(1l);
        final Firm firm = new Firm(model); firm.earn(100000000l);
        final OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy());
        OrderBookMarket labor = new OrderBookMarket(GoodType.LABOR);

        SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(firm,market,new SimpleBuyerSearch(market,firm), new SimpleSellerSearch(market,firm));
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept,.3f,.16f,.01f,10);
        dept.setAskPricingStrategy(strategy);   dept.setPredictorStrategy(new MemorySalesPredictor());
        //  dept.setAskPricingStrategy(new EverythingMustGoAdaptive(dept));
        firm.registerSaleDepartment(dept, GoodType.GENERIC);
        Blueprint blueprint = new Blueprint.Builder().output(GoodType.GENERIC,1).build();
        Plant plant = new Plant(blueprint,firm);
        LinearConstantMachinery machinery = new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),100000,plant);
        machinery.setOneWorkerProductionTime(100f);
        plant.setPlantMachinery(machinery);
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);
        HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000,firm,labor,plant,MarginalPlantControl.class,null,null);
//        firm.registerHumanResources(plant, hr);
        hr.start();


        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {

                firm.weekEnd(model.schedule.getTime());         //manual weekend, might want to change this later
                //    System.out.println("weekend!");



            }
        },1,model.getWeekLength());

        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                final List<Quote> quotes =new LinkedList<>();
                DiscreteSlowPlantControlTest.fillMarket(market, quotes, model);
                simState.schedule.scheduleOnceIn(model.getWeekLength()/100f,
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                DiscreteSlowPlantControlTest.emptyMarket(market, quotes);
                            }
                        });

            }
        },2,model.getWeekLength()/100f);

        //add the four workers
        for(int i=0; i<4; i++)
        {
            Person p = new Person(model,0,30+i*10,labor);
            labor.submitSellQuote(p, 30+i*10 , new Good(GoodType.LABOR,p,30+i*10));



        }



        do{
            if (!model.schedule.step(model)) break;
            //             System.out.println("the time is " + model.schedule.getTime() +
            //                    "the sales department is selling at: " + strategy.getTargetPrice() + "plant has this many workers: " + plant.workerSize() + " offering wage: " + hr.maxPrice(GoodType.GENERIC,labor));
            //   System.out.println(model.schedule.getTime()+","+strategy.getTargetPrice() +","+ pid.getCurrentMV() +","+plant.workerSize()  + "," + hr.maxPrice(GoodType.LABOR,labor) + "," + market.getBestBuyPrice());
            //  System.out.println("market best bid: " + market.getBestBuyPrice());
        }
        while(model.schedule.getSteps() < 100000);

        Assert.assertEquals(plant.workerSize(),2);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");





    }



}
