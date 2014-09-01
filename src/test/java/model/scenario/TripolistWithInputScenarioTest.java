/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.purchases.pricing.CheaterPricing;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
 * @version 2013-01-13
 * @see
 */
public class TripolistWithInputScenarioTest {

    //these tests are all with SimpleFlowSeller

    @Test
    public void monopolistHillClimber()
    {

        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
            scenario1.setAdditionalCompetitors(0);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            while(macroII.schedule.getTime()<5500)
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 87,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);

        }





    }


    @Test
    public void monopolistMarginal()
    {
        for(int i=0; i<50; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            System.out.println(macroII.seed());
            TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
            scenario1.setAdditionalCompetitors(0);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            System.err.println(macroII.seed());
            while(macroII.schedule.getTime()<3500)
            {
         /*       System.out.println(scenario1.getMonopolist().getPurchaseDepartment(TripolistWithInputScenario.INPUT).getLastOfferedPrice() + " - " +
                scenario1.getMonopolist().getPurchaseDepartment(TripolistWithInputScenario.INPUT).maxPrice(TripolistWithInputScenario.INPUT,scenario1.getMonopolist().getPurchaseDepartment(TripolistWithInputScenario.INPUT).getMarket()));
                */
                macroII.schedule.step(macroII);
            }
            System.out.println("---------------------------------------------------------------------------------------------------------");


            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 87,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);






        }


    }




    //SalesControlWithFixedInventoryAndPID
    @Test
    public void monopolistSalesControlFixedInventory()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
            scenario1.setAdditionalCompetitors(0);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            System.out.println(macroII.seed());
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 87, 1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);






        }


    }


    @Test
    public void monopolistCheatingPrice()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII){

                protected void addPurchaseDepartmentToFirms(){
                    FactoryProducedPurchaseDepartment<FixedInventoryControl,CheaterPricing,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                            factoryProducedPurchaseDepartment =
                            PurchasesDepartment.getPurchasesDepartment(Integer.MAX_VALUE, monopolist, getMarkets().get(TripolistWithInputScenario.INPUT), FixedInventoryControl.class,
                                    CheaterPricing.class, null, null);

                    factoryProducedPurchaseDepartment.getInventoryControl().setInventoryTarget(100);
                    factoryProducedPurchaseDepartment.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(1.1f);
                    monopolist.registerPurchasesDepartment(factoryProducedPurchaseDepartment.getDepartment(), TripolistWithInputScenario.INPUT);
                }

            };
            scenario1.setAdditionalCompetitors(0);

            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            System.out.println(i + " ----- " + scenario1.monopolist.getTotalWorkers() + " ... " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice() + " ----> " + macroII.seed());
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 87,1);






        }


    }


    //SalesControlWithFixedInventoryAndPID
    @Test
    public void competitiveSalesControlFixedInventoryAlreadyLearned()
    {
        for(int competitors=4;competitors<9; competitors++){

            System.out.println("competitors : " + (competitors+1));
            for(int i=0; i<5; i++)
            {
                //we know the profit maximizing equilibrium is q=220, price = 72
                final MacroII macroII = new MacroII(System.currentTimeMillis());
                TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
                scenario1.setAdditionalCompetitors(competitors);
                macroII.setScenario(scenario1);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAskPricingStrategy(InventoryBufferSalesControl.class);
                if(macroII.random.nextBoolean())
                    scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
                else
                    scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);




                macroII.start();
                macroII.schedule.step(macroII);
                //force learning
                //sales:
                for(Firm f : scenario1.getCompetitors())
                {
                    f.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                    f.getPurchaseDepartment(TripolistWithInputScenario.INPUT).setPredictor(new FixedIncreasePurchasesPredictor(0));
                    f.getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(0));
                }

                while(macroII.schedule.getTime()<5000)
                    macroII.schedule.step(macroII);

                double price = 0;
                double quantity = 0;

                for(int averaging=0; averaging<1000; averaging++)
                {
                    macroII.schedule.step(macroII);
                    price += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                    quantity += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume();
                }
                price /=1000;
                quantity /= 1000;


                System.out.println("price : " + price + ", quantity: " + quantity);

                assertEquals(price, 73, 5);
                assertEquals(quantity, 28.5,4);






            }
            System.out.println("-------------------------------------------");


        }
    }



    //SalesControlWithFixedInventoryAndPID
    @Test
    public void competitiveSalesControlFixedInventoryLearning()
    {
        for(int competitors=4;competitors<9; competitors++){

            System.out.println("competitors : " + (competitors+1));
            for(int i=0; i<5; i++)
            {
                //we know the profit maximizing equilibrium is q=220, price = 72
                final MacroII macroII = new MacroII(System.currentTimeMillis());
                TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
                scenario1.setAdditionalCompetitors(competitors);
                macroII.setScenario(scenario1);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
                if(macroII.random.nextBoolean())
                    scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
                else
                    scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

                macroII.start();
                while(macroII.schedule.getTime()<5000)
                    macroII.schedule.step(macroII);

                double price = 0;
                double quantity = 0;

                for(int averaging=0; averaging<1000; averaging++)
                {
                    macroII.schedule.step(macroII);
                    price += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                    quantity += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume();
                }
                price /=1000;
                quantity /= 1000;


                System.out.println("price : " + price + ", quantity: " + quantity);
                assertEquals(price, 73, 5);
                assertEquals(quantity, 28.5,4);






            }
            System.out.println("-------------------------------------------");


        }
    }


    /**
     * see fiveLearnedMonopolists in MonopolistScenarioTest for an explanation
     */
//    @Test
    public void fiveLearnedInputMonopolists() throws IOException {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(1);
            TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
            scenario1.setAdditionalCompetitors(4);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //create stat collector
       /*     CSVWriter writer = new CSVWriter(new FileWriter("runs/5LearnedMonopolists.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
            */



            macroII.start();
            macroII.schedule.step(macroII);
            //force learning
            //sales:
            for(Firm f : scenario1.getCompetitors())
            {
                f.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
                f.getPurchaseDepartment(TripolistWithInputScenario.INPUT).setPredictor(new FixedIncreasePurchasesPredictor(1));
                //without this overproduction
                f.getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));
            }

            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);

            double price = 0;
            double quantity = 0;

            for(int averaging=0; averaging<1000; averaging++)
            {
                macroII.schedule.step(macroII);
                price += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                quantity += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume();
            }
            price /=1000;
            quantity /= 1000;


            System.out.println("price : " + price + ", quantity: " + quantity);
         //not true!
            assertEquals(87,price,2);
            assertEquals(14,quantity,2);
        }
    }





    public void main(String[] args) throws IOException {
        //we know the profit maximizing equilibrium is q=220, price = 72
        final MacroII macroII = new MacroII(System.currentTimeMillis());
        TripolistWithInputScenario scenario1 = new TripolistWithInputScenario(macroII);
        scenario1.setAdditionalCompetitors(4);
        macroII.setScenario(scenario1);
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
        scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
        if(macroII.random.nextBoolean())
            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        else
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


        //create stat collector




        macroII.start();
        macroII.schedule.step(macroII);
        //force learning
        //sales:
        for(Firm f : scenario1.getCompetitors())
        {
            f.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
            f.getPurchaseDepartment(TripolistWithInputScenario.INPUT).setPredictor(new FixedIncreasePurchasesPredictor(1));
            //without this overproduction
            f.getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));
        }

        while(macroII.schedule.getTime()<5000)
            macroII.schedule.step(macroII);

        double price = 0;
        double quantity = 0;

        for(int averaging=0; averaging<1000; averaging++)
        {
            macroII.schedule.step(macroII);
            price += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
            quantity += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume();
        }
        price /=1000;
        quantity /= 1000;


        System.out.println("price : " + price + ", quantity: " + quantity);

    }



}
