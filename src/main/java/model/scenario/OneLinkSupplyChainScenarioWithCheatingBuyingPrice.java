package model.scenario;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.control.facades.MarginalPlantControlWithPIDUnit;
import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.pricing.CheaterPricing;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import au.com.bytecode.opencsv.CSVWriter;
import financial.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.DailyStatCollector;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

import static model.experiments.tuningRuns.MarginalMaximizerWithUnitPIDTuningMultiThreaded.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> This is just like one link supply chain but the purchase department looks at the price rather than using PID.
 * In a way, this is just a boring
 *
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-06-10
 * @see
 */
public class OneLinkSupplyChainScenarioWithCheatingBuyingPrice extends OneLinkSupplyChainScenario {
    public OneLinkSupplyChainScenarioWithCheatingBuyingPrice(MacroII model) {
        super(model);
    }


    @Override
    protected void createPurchaseDepartment(Blueprint blueprint, Firm firm) {

        for(GoodType input : blueprint.getInputs().keySet()){
            FactoryProducedPurchaseDepartment<FixedInventoryControl,CheaterPricing,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                    factoryProducedPurchaseDepartment =
                    PurchasesDepartment.getPurchasesDepartment(Long.MAX_VALUE,firm,getMarkets().get(input),FixedInventoryControl.class,
                            CheaterPricing.class,null,null);
            Market market = model.getMarket(input);

            factoryProducedPurchaseDepartment.getInventoryControl().setInventoryTarget(200);
            factoryProducedPurchaseDepartment.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(2f);

            PurchasesDepartment department = factoryProducedPurchaseDepartment.getDepartment();
            firm.registerPurchasesDepartment(department, input);
            department.start();

        }
    }


    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main(String[] args)
    {


        final MacroII macroII = new MacroII(0);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);
        scenario1.setControlType(MarginalPlantControlWithPIDUnit.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(5);
  //      scenario1.setNumberOfFoodProducers(5);





        macroII.setScenario(scenario1);
        macroII.start();

        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/supplychai/cheater3.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        //create the CSVWriter  for purchases prices
        try {
            final CSVWriter writer2 = new CSVWriter(new FileWriter("runs/supplychai/cheaterOfferPricesWithCompetition.csv"));
            writer2.writeNext(new String[]{"buyer offer price","target","filtered Outflow"});
            macroII.scheduleSoon(ActionOrder.CLEANUP, new Steppable() {
                @Override
                public void step(SimState state) {
                    try {
                        writer2.writeNext(new String[]{String.valueOf(
                                macroII.getMarket(GoodType.BEEF).getBestBuyPrice()),
                                String.valueOf(scenario1.strategy2.getTarget()),
                                String.valueOf(scenario1.strategy2.getFilteredOutflow())});
                        writer2.flush();
                        ((MacroII) state).scheduleTomorrow(ActionOrder.CLEANUP, this);
                    } catch (IllegalAccessException | IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            });

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }





        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }


    }

}
