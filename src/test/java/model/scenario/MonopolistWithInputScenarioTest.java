package model.scenario;

import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.pricing.CheaterPricing;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;

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
public class MonopolistWithInputScenarioTest {

    //these tests are all with SimpleFlowSeller

    @Test
    public void rightPriceAndQuantityTestAsHillClimber()
    {

        //run the test 5 times
        for(int i=0; i<25; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistWithInputScenario(macroII);
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


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 87,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);

        }





    }


    @Test
    public void rightPriceAndQuantityTestAsMarginal()
    {
        for(int i=0; i<15; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            System.out.println(macroII.seed());
            MonopolistScenario scenario1 = new MonopolistWithInputScenario(macroII);
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
         /*       System.out.println(scenario1.getMonopolist().getPurchaseDepartment(GoodType.LEATHER).getLastOfferedPrice() + " - " +
                scenario1.getMonopolist().getPurchaseDepartment(GoodType.LEATHER).maxPrice(GoodType.LEATHER,scenario1.getMonopolist().getPurchaseDepartment(GoodType.LEATHER).getMarket()));
                */
                macroII.schedule.step(macroII);
            }
            System.out.println("---------------------------------------------------------------------------------------------------------");


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 87,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);






        }


    }




    //SalesControlWithFixedInventoryAndPID
    @Test
    public void rightPriceAndQuantityTestAsMarginalWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<15; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistWithInputScenario(macroII);
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


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 87,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);






        }


    }


//SalesControlWithFixedInventoryAndPID
    @Test
    public void rightPriceAndQuantityTestAsMarginalWithCheatingPrices()
    {
        for(int i=0; i<15; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistWithInputScenario(macroII){

                protected void addPurchaseDepartmentToMonopolist(){
                    FactoryProducedPurchaseDepartment<FixedInventoryControl,CheaterPricing,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                            factoryProducedPurchaseDepartment =
                            PurchasesDepartment.getPurchasesDepartment(Long.MAX_VALUE, monopolist, getMarkets().get(GoodType.LEATHER), FixedInventoryControl.class,
                                    CheaterPricing.class, null, null);

                    factoryProducedPurchaseDepartment.getInventoryControl().setInventoryTarget(100);
                    factoryProducedPurchaseDepartment.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(1.1f);
                    monopolist.registerPurchasesDepartment(factoryProducedPurchaseDepartment.getDepartment(), GoodType.LEATHER);
                }

            };
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


            System.out.println(i + " ----- " + scenario1.monopolist.getTotalWorkers());
            System.out.println(macroII.getMarket(GoodType.GENERIC).getLastPrice());
            assertEquals(scenario1.monopolist.getTotalWorkers(), 14,1);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 87,1);






        }


    }




}
