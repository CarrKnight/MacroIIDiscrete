/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.plantcontrol;

import agents.people.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.cost.PlantCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.control.AbstractPlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.facades.DiscreteSlowPlantControl;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.PIDController;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
 * @version 2012-09-01
 * @see
 */
public class DiscreteSlowPlantControlTest {

    @Test
    public void testChangeTargetStep() throws Exception
    {



        MacroII model = new MacroII(10);
        Firm firm = mock(Firm.class);      when(firm.getRandom()).thenReturn(new MersenneTwisterFast());
        when(firm.getModel()).thenReturn(model);
        Plant plant = mock(Plant.class); when(plant.getNumberOfWorkers()).thenReturn(1); when(plant.getModel()).thenReturn(model);  when(plant.getBuildingCosts()).thenReturn(10000);
        when(plant.maximumWorkersPossible()).thenReturn(100);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,1, UndifferentiatedGoodType.GENERIC,1);
        when(plant.getBlueprint()).thenReturn(b);
        when(plant.getRandom()).thenReturn(model.random); when(firm.getRandom()).thenReturn(model.random);
        when(plant.removeListener(any(PlantListener.class))).thenReturn(true);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(10000);
        Market market = new OrderBookMarket(UndifferentiatedGoodType.LABOR);   //labor market
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        HumanResources hr = null;
        for(int i=0; i <100; i++)
        {
            hr = HumanResources.getHumanResourcesIntegrated(1000000,
                    firm,market,plant,null,null,null).getDepartment();
            assert market.getBuyers().contains(firm);
            hr.turnOff();
            assert !market.getBuyers().contains(firm);

        }
        hr = HumanResources.getHumanResourcesIntegrated(1000000,
                firm,market,plant,null,null,null).getDepartment();
        assert market.getBuyers().contains(firm);

        DiscreteSlowPlantControl control = new DiscreteSlowPlantControl(hr);
        //  control.setQuickFiring(false);
        hr.setControl(control);
        when(plant.getNumberOfWorkers()).thenReturn(0);
        control.start();
        when(plant.getNumberOfWorkers()).thenReturn(1);


        //force the control to have wage = 50;
        Field field  = DiscreteSlowPlantControl.class.getDeclaredField("control");
        field.setAccessible(true);
        TargetAndMaximizePlantControl realControl = (TargetAndMaximizePlantControl) field.get(control);


        field = AbstractPlantControl.class.getDeclaredField("currentWage");
        field.setAccessible(true);
        field.set(realControl, 50); //wage is set by the control OTHER steppable, so for now let's just control it
        //because I care about profit maximization

        //get the maximizer
        field = TargetAndMaximizePlantControl.class.getDeclaredField("maximizer");
        field.setAccessible(true);
        SetTargetThenTryAgainMaximizer maximizer = (SetTargetThenTryAgainMaximizer) field.get(realControl);





        //target should be 1
        assertEquals(control.getTarget(), 1);

        //adjust on it once, it should switch to 2
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(100f);
        maximizer.step(model);
        maximizer.step(model);
        assertEquals(control.getTarget(), 2);
        when(plant.getNumberOfWorkers()).thenReturn(2);

        //adjust it again, it should switch to 3
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(180f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, it should switch to 4
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(240f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 4);
        when(plant.getNumberOfWorkers()).thenReturn(4);


        //adjust it again, it should switch back to 3
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(200f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, it should stay at 3
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(240f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, it should stay at 3
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(240f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, it should stay at 3
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(240f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, now it'll switch to 4!
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(150f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 4);
        when(plant.getNumberOfWorkers()).thenReturn(4);


        //adjust it again, now it'll switch to 3!
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(0f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 3);
        when(plant.getNumberOfWorkers()).thenReturn(3);


        //adjust it again, now it'll switch to 2!
        when(firm.getPlantProfits(any(Plant.class))).thenReturn(0f);
        assertTrue(!maximizer.isCheckWeek());
        maximizer.step(model);
        assertTrue(maximizer.isCheckWeek());
        maximizer.step(model);
        assertEquals(control.getTarget(), 2);
        when(plant.getNumberOfWorkers()).thenReturn(2);



    }


    /**
     * BIG FULLY DRESSED MONOPOLY SCENARIO!
     */
    //moved to running the test in the monopoly scenario
    public void monopolyScenario() throws IllegalAccessException, NoSuchFieldException {

        
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("SimpleFlowSeller scenario1");

        final MacroII model = new MacroII(2);
        final Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
        final OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy());
        OrderBookMarket labor = new OrderBookMarket(UndifferentiatedGoodType.LABOR);

        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept,.8f,.30f,.01f,10, dept.getMarket(), dept.getRandom().nextInt(100), dept.getFirm().getModel());
        strategy.setProductionCostOverride(true);
        dept.setAskPricingStrategy(strategy);
        //  dept.setAskPricingStrategy(new EverythingMustGoAdaptive(dept));
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant = new Plant(blueprint,firm);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);
        HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000,firm,labor,plant,DiscreteSlowPlantControl.class,null,null).getDepartment();
        //   firm.registerHumanResources(plant, hr);
        hr.start(model);

        model.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState state) {
                final List<Quote> quotes =new LinkedList<>();

                model.scheduleTomorrow(ActionOrder.DAWN, new Steppable() {
                    @Override
                    public void step(SimState state) {
                        emptyMarket(market,quotes);
                        fillMarket(market, quotes, model);
                        model.scheduleTomorrow(ActionOrder.DAWN,this);
                    }
                });





            }
        });


        //add the four workers
        for(int i=0; i<4; i++)
        {
            Person p = new Person(model,0,30+i*10,labor);
            labor.submitSellQuote(p, 30+i*10 , Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR));



        }


        Field field = SimpleFlowSellerPID.class.getDeclaredField("controller");
        field.setAccessible(true);
        PIDController pid = (PIDController) field.get(strategy);

        model.start();
        model.getAgents().clear(); model.getAgents().add(firm);
        do{
            if (!model.schedule.step(model)) break;
            System.out.println("the time is " + model.schedule.getTime() +
                    "the sales department is selling at: " + strategy.getTargetPrice() + "plant has this many workers: " + plant.getNumberOfWorkers() + " offering wage: " + hr.maxPrice(UndifferentiatedGoodType.GENERIC,labor));
            System.out.println(model.schedule.getTime()+","+strategy.getTargetPrice() +","+ pid.getCurrentMV() +","+plant.getNumberOfWorkers()  + "," + hr.maxPrice(UndifferentiatedGoodType.LABOR,labor) + "," + market.getBestBuyPrice());
            if(model.schedule.getSteps() > 50)
                System.out.println("market best bid: " + market.getBestBuyPrice() + ", profits: " + firm.getPlantProfits(plant));
        }
        while(model.schedule.getSteps() < 6000);

        assertEquals(plant.getNumberOfWorkers(), 2);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");




    }

    //duopoly scenario: like monopoly but with 2 suppliers!

    //  @Test
    public void duopolyScenario() throws IllegalAccessException, NoSuchFieldException {

        
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("Duopoly scenario1");

        final MacroII model = new MacroII(1);

        //1
        final Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
        final OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy());
        OrderBookMarket labor = new OrderBookMarket(UndifferentiatedGoodType.LABOR);

        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept,.3f,.16f,.01f,10, dept.getMarket(), dept.getRandom().nextInt(100), dept.getFirm().getModel());
        dept.setAskPricingStrategy(strategy);
        //  dept.setAskPricingStrategy(new EverythingMustGoAdaptive(dept));
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant = new Plant(blueprint,firm);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);
        HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000,firm,labor,plant,DiscreteSlowPlantControl.class,null,null).getDepartment();
        firm.registerHumanResources(plant, hr);
        hr.start(model);

        //2
        final Firm firm2 = new Firm(model); firm2.receiveMany(UndifferentiatedGoodType.MONEY,100000000);

        SalesDepartment dept2 = SalesDepartmentFactory.incompleteSalesDepartment(firm2, market, new SimpleBuyerSearch(market, firm2), new SimpleSellerSearch(market, firm2), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy2 = new SimpleFlowSellerPID(dept2,.3f,.16f,.01f,10, dept2.getMarket(), dept2.getRandom().nextInt(100), dept2.getFirm().getModel());
        dept2.setAskPricingStrategy(strategy2);
        //  dept2.setAskPricingStrategy(new EverythingMustGoAdaptive(dept2));
        firm2.registerSaleDepartment(dept2, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint2 = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant2 = new Plant(blueprint2,firm2);
        plant2.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant2));
        plant2.setCostStrategy(new InputCostStrategy(plant2));
        firm2.addPlant(plant2);
        HumanResources hr2 = HumanResources.getHumanResourcesIntegrated(100000000,firm2,labor,plant2,DiscreteSlowPlantControl.class,null,null).getDepartment();
        firm2.registerHumanResources(plant2, hr2);
        hr2.start(model);


        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {

                firm.weekEnd(model.schedule.getTime());         //manual weekend, might want to change this later
                firm2.weekEnd(model.schedule.getTime());
                //    System.out.println("weekend!");



            }
        },1,model.getWeekLength());

        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                final List<Quote> quotes =new LinkedList<>();
                fillMarket(market, quotes, model);
                simState.schedule.scheduleOnceIn(model.getWeekLength()/10f,
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                emptyMarket(market,quotes);
                            }
                        });

            }
        },2,model.getWeekLength()/10f);

        //add 10 workers (4 is the mr)
        for(int i=0; i<10; i++)
        {
            Person p = new Person(model,0,30+i*10,labor);
            labor.submitSellQuote(p, 30+i*10 , Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR));



        }



        do{
            if (!model.schedule.step(model)) break;

            System.out.println("FIRST FIRM : the time is " + model.schedule.getTime() +
                    "the sales department is selling at: " + strategy.getTargetPrice() + "plant has this many workers: " + plant.getNumberOfWorkers() + " offering wage: " + hr.maxPrice(UndifferentiatedGoodType.GENERIC,labor));
            System.out.println("SECOND FIRM: the time is " + model.schedule.getTime() +
                    "the sales department is selling at: " + strategy2.getTargetPrice() + "plant has this many workers: " + plant2.getNumberOfWorkers() + " offering wage: " + hr2.maxPrice(UndifferentiatedGoodType.GENERIC,labor));

        }
        while(model.schedule.getSteps() < 6000);

        assertEquals(plant.getNumberOfWorkers() + plant2.getNumberOfWorkers(), 4);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");




    }

    //    @Test
    public void tripolyScenario() throws IllegalAccessException, NoSuchFieldException {

        
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("Duopoly scenario1");

        final MacroII model = new MacroII(1);

        //1
        final Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
        final OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy());
        OrderBookMarket labor = new OrderBookMarket(UndifferentiatedGoodType.LABOR);

        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept,.3f,.16f,.01f,10, dept.getMarket(), dept.getRandom().nextInt(100), dept.getFirm().getModel());
        dept.setAskPricingStrategy(strategy);
        //  dept.setAskPricingStrategy(new EverythingMustGoAdaptive(dept));
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant = new Plant(blueprint,firm);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);
        HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000,firm,labor,plant,DiscreteSlowPlantControl.class,null,null).getDepartment();
        Field field = PurchasesDepartment.class.getDeclaredField("control"); field.setAccessible(true);
        DiscreteSlowPlantControl control = (DiscreteSlowPlantControl) field.get(hr);
        firm.registerHumanResources(plant, hr);
        //   control.setProbabilityForgetting(.15f);

        hr.start(model);

        //2
        final Firm firm2 = new Firm(model); firm2.receiveMany(UndifferentiatedGoodType.MONEY,100000000);

        SalesDepartment dept2 = SalesDepartmentFactory.incompleteSalesDepartment(firm2, market, new SimpleBuyerSearch(market, firm2), new SimpleSellerSearch(market, firm2), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy2 = new SimpleFlowSellerPID(dept2,.3f,.16f,.01f,10, dept2.getMarket(), dept2.getRandom().nextInt(100), dept2.getFirm().getModel());
        dept2.setAskPricingStrategy(strategy2);
        //  dept2.setAskPricingStrategy(new EverythingMustGoAdaptive(dept2));
        firm2.registerSaleDepartment(dept2, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint2 = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant2 = new Plant(blueprint2,firm2);
        plant2.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant2));
        plant2.setCostStrategy(new InputCostStrategy(plant2));
        firm2.addPlant(plant2);
        HumanResources hr2 = HumanResources.getHumanResourcesIntegrated(100000000,firm2,labor,plant2,DiscreteSlowPlantControl.class,null,null).getDepartment();
        field = PurchasesDepartment.class.getDeclaredField("control"); field.setAccessible(true);
        DiscreteSlowPlantControl control2 = (DiscreteSlowPlantControl) field.get(hr2);
        //   control2.setProbabilityForgetting(.15f);

        firm2.registerHumanResources(plant2, hr2);
        hr2.start(model);

        //2
        final Firm firm3 = new Firm(model); firm3.receiveMany(UndifferentiatedGoodType.MONEY,100000000);

        SalesDepartment dept3 = SalesDepartmentFactory.incompleteSalesDepartment(firm3, market, new SimpleBuyerSearch(market, firm3), new SimpleSellerSearch(market, firm3), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy3 = new SimpleFlowSellerPID(dept3,.3f,.16f,.01f,10, dept3.getMarket(), dept3.getRandom().nextInt(100), dept3.getFirm().getModel());
        dept3.setAskPricingStrategy(strategy3);
        //  dept3.setAskPricingStrategy(new EverythingMustGoAdaptive(dept3));
        firm3.registerSaleDepartment(dept3, UndifferentiatedGoodType.GENERIC);
        Blueprint blueprint3 = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build();
        Plant plant3 = new Plant(blueprint3,firm3);
        plant3.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),100000,plant3));
        plant3.setCostStrategy(new InputCostStrategy(plant3));
        firm3.addPlant(plant3);
        HumanResources hr3 = HumanResources.getHumanResourcesIntegrated(100000000,firm3,labor,plant3,DiscreteSlowPlantControl.class,null,null).getDepartment();
        field = PurchasesDepartment.class.getDeclaredField("control"); field.setAccessible(true);
        DiscreteSlowPlantControl control3 = (DiscreteSlowPlantControl) field.get(hr3);
        //       control3.setProbabilityForgetting(.15f);
        firm3.registerHumanResources(plant3, hr3);
        hr3.start(model);


        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {

                firm.weekEnd(model.schedule.getTime());         //manual weekend, might want to change this later
                firm2.weekEnd(model.schedule.getTime());
                firm3.weekEnd(model.schedule.getTime());
                //    System.out.println("weekend!");



            }
        },1,model.getWeekLength());

        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                final List<Quote> quotes =new LinkedList<>();
                fillMarket(market, quotes, model);
                simState.schedule.scheduleOnceIn(model.getWeekLength()/10f,
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                emptyMarket(market,quotes);
                            }
                        });

            }
        },2,model.getWeekLength()/10f);

        //add 10 workers (4 is the mr)
        for(int i=0; i<10; i++)
        {
            Person p = new Person(model,0,30+i*10,labor);
            labor.submitSellQuote(p, 30+i*10 , Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR));



        }



        do{
            if (!model.schedule.step(model)) break;
            //   System.out.println("FIRST FIRM : the time is " + model.schedule.getTime() +
            //           "the sales department is selling at: " + strategy.getTargetPrice() + "plant has this many workers: " + plant.getNumberOfWorkers() + " offering wage: " + hr.maxPrice(GoodType.GENERIC,labor));
            //   System.out.println("SECOND FIRM: the time is " + model.schedule.getTime() +
            //           "the sales department is selling at: " + strategy2.getTargetPrice() + "plant has this many workers: " + plant2.getNumberOfWorkers() + " offering wage: " + hr2.maxPrice(GoodType.GENERIC,labor));
            System.out.println(plant.getNumberOfWorkers() + " <>" + dept.getLastClosingPrice() + "<>" + hr.maxPrice(UndifferentiatedGoodType.GENERIC,labor) + "<>" + firm.getPlantProfits(plant) +
                    " , "
                    + plant2.getNumberOfWorkers() + " <>" + dept2.getLastClosingPrice() + "<>" + hr2.maxPrice(UndifferentiatedGoodType.GENERIC, labor) + "<>" +firm2.getPlantProfits(plant2) +
                    " , "
                    + plant3.getNumberOfWorkers() + " <>" + dept3.getLastClosingPrice() + "<>" + hr3.maxPrice(UndifferentiatedGoodType.GENERIC, labor) + "<>" + firm3.getPlantProfits(plant3) +
                    " ----- "
                    + (plant.getNumberOfWorkers() + plant2.getNumberOfWorkers()  + plant3.getNumberOfWorkers()));

        }
        while(model.schedule.getSteps() < 50000);   //todo why it takes so int and why sometimes it's 4 rather than 5?

        assertEquals(plant.getNumberOfWorkers() + plant2.getNumberOfWorkers() + plant3.getNumberOfWorkers(), 4);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");




    }



    public static void fillMarket(Market market, Collection<Quote> quotes, MacroII model){
        for(int i=9; i>2;i --)
        {

            DummyBuyer buyer = new DummyBuyer(model,i+1,market);
            market.registerBuyer(buyer);
            buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
            Quote q = market.submitBuyQuote(buyer,i+1);
            quotes.add(q);



        }

    }

    public static void emptyMarket(Market market, Iterable<Quote> quotes){
        for(Quote q : quotes)
            try{
                market.removeBuyQuote(q);
            }
            catch (IllegalArgumentException ignored){}

    }


}
