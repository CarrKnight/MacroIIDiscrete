package model.scenario;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.RobustMarginalMaximizer;
import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.purchases.pricing.CheaterPricing;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SamplingLearningDecreaseSalesPredictor;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.ProducersStatCollector;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> This is just like one link supply chain but the purchase department looks at the price rather than using PID.
 * In a way, only the sellers are price-makers, the buyers are price-takers!
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
    protected PurchasesDepartment createPurchaseDepartment(Blueprint blueprint, Firm firm) {

        PurchasesDepartment department = null;
        for(GoodType input : blueprint.getInputs().keySet()){
            FactoryProducedPurchaseDepartment<FixedInventoryControl,CheaterPricing,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                    factoryProducedPurchaseDepartment =
                    PurchasesDepartment.getPurchasesDepartment(Long.MAX_VALUE,firm,getMarkets().get(input),FixedInventoryControl.class,
                            CheaterPricing.class,null,null);

            factoryProducedPurchaseDepartment.getInventoryControl().setInventoryTarget(80);
            factoryProducedPurchaseDepartment.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(2f);


            department = factoryProducedPurchaseDepartment.getDepartment();
            firm.registerPurchasesDepartment(department, input);

            if(input.equals(GoodType.BEEF))
                buildFoodPurchasesPredictor(department);


        }
        return department;

    }


    //food learned, beef learning
    public static void main(String[] args)
    {

        final MacroII macroII = new MacroII(0);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII)
        {



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                department.setPredictor(new FixedIncreasePurchasesPredictor(0));

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);    //To change body of overridden methods use File | Settings | File Templates.
                if(goodmarket.getGoodType().equals(GoodType.FOOD))
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                else
                    department.setPredictorStrategy(new SamplingLearningDecreaseSalesPredictor(department.getModel()));
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);    //To change body of overridden methods use File | Settings | File Templates.
                if(!blueprint.getOutputs().containsKey(GoodType.BEEF))
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                return hr;
            }
        };
        scenario1.setControlType(RobustMarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(15f);
        scenario1.setDivideIntegrativeGainByThis(15f);
        //no delay
        scenario1.setBeefPricingSpeed(0);


        macroII.setScenario(scenario1);
        macroII.start();

        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/supplychai/cheater3.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
            //prices and quantities
            final CSVWriter prices = new CSVWriter(new FileWriter("runs/supplychai/cheaterprices.csv"));
            final CSVWriter quantities = new CSVWriter(new FileWriter("runs/supplychai/cheaterquantities.csv"));
            ProducersStatCollector collector2 = new ProducersStatCollector(macroII,GoodType.BEEF,prices,quantities);
            collector2.start();
            //offer prices as cheater
           /* final CSVWriter writer2 = new CSVWriter(new FileWriter("runs/supplychai/plantData.csv"));
            String[] header = new String[SalesDataType.values().length+1];
            for(int i=0; i < SalesDataType.values().length; i++)
            {
                header[i] = SalesDataType.values()[i].toString();

            }
            header[header.length-1]="shockday";
            writer2.writeNext(header);
            macroII.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, new Steppable() {
                @Override
                public void step(SimState state) {
                    try {
                        String[] line = new String[SalesDataType.values().length+1];
                        SalesDepartment dept = scenario1.strategy2.getDepartment();

                        for(int i=0; i < SalesDataType.values().length; i++)
                        {
                            line[i] = String.valueOf(dept.getLatestObservation(SalesDataType.values()[i]));

                        }

                        line[line.length-1] =
                                String.valueOf(dept.getFirm().getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(dept.getGoodType()));

                        writer2.writeNext(line);
                        writer2.flush();
                        ((MacroII) state).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }); */
            //computed slope
            final CSVWriter writer3 = new CSVWriter(new FileWriter("runs/supplychai/monopolistLearnedSlope.csv"));
            writer3.writeNext(new String[]{"slope"});
            macroII.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, new Steppable() {
                @Override
                public void step(SimState state) {
                    try {
                        Preconditions.checkState(scenario1.getMarkets().get(GoodType.BEEF).getSellers().size()==1);
                        Firm monopolist = (Firm) scenario1.getMarkets().get(GoodType.BEEF).getSellers().iterator().next();
                        SamplingLearningDecreaseSalesPredictor predictorStrategy =
                                (SamplingLearningDecreaseSalesPredictor) monopolist.getSalesDepartment(GoodType.BEEF).getPredictorStrategy();
                        writer3.writeNext(new String[]{
                                String.valueOf(-predictorStrategy.getDecrementDelta())});
                        writer3.flush();
                        ((MacroII) state).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });




        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }






        while(macroII.schedule.getTime()<45000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(45001,(int)macroII.schedule.getSteps(),100);
        }

    }





}
