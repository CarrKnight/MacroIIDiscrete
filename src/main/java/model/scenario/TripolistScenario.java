/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.DailyStatCollector;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

import java.util.LinkedList;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> Same situation as Monopoly Scenario, but on top of it I add two more identical firms
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-14
 * @see
 */
public class TripolistScenario extends MonopolistScenario{



    int additionalCompetitors = 2;

    /**
     * A linked list of all competiors, so that we can query them in constant ordering
     */
    private LinkedList<Firm> competitors;

    public TripolistScenario(MacroII macroII) {
        super(macroII);
        //make default maximizer as always climbing so reviewers can replicate the paper!
        super.setControlType(MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING);


        //instantiate the list
        competitors = new LinkedList<>();
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        //do all the monopolist stuff
        super.start();
        //add the monopolist to the list of competitors
        competitors.add(monopolist);
        monopolist.setName("competitor0");

        //sanity check
        //  assert !getAgents().isEmpty() : getAgents();
        assert getMarkets().size()==2;

        //now add n more agents
        for(int i=0; i < additionalCompetitors; i++)
        {

            Firm competitor = buildFirm();
            //the monopolist reference now points to the new guy
            competitor.setName("competitor" + i + 1);
            //also add it to the competitors' list
            competitors.add(competitor);

        }

    }





    public int getAdditionalCompetitors() {
        return additionalCompetitors;
    }

    public void setAdditionalCompetitors(int additionalCompetitors) {
        this.additionalCompetitors = additionalCompetitors;
    }





    public static void main2(String[] args)
    {
        //set up
        final MacroII macroII = new MacroII(0l);
        final TripolistScenario scenario1 = new TripolistScenario(macroII);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
        scenario1.setControlType(MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING);
        scenario1.setAdditionalCompetitors(2);
        scenario1.setWorkersToBeRehiredEveryDay(false);

        // scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
        scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
        //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);




        //assign scenario
        macroII.setScenario(scenario1);

        macroII.start();



        //CSV writer set up
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/monopolist/"+"tripolist0"+".csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }

        //set a different writer to check the prices quotes for each sales department
        try {
            final CSVWriter writer2 = new CSVWriter(new FileWriter("runs/monopolist/"+"tripolistPrices"+".csv"));
            final CSVWriter writer3 = new CSVWriter(new FileWriter("runs/monopolist/"+"tripolistQuantity"+".csv"));


            //write the title
            String[] title = new String[scenario1.competitors.size()];
            String[] title2 = new String[scenario1.competitors.size()];

            for(int i=0; i<scenario1.competitors.size(); i++)
            {
                title[i] = "price"+i;
                title2[i] = "quantity"+i;

            }



            writer2.writeNext(title);
            writer3.writeNext(title2);
            macroII.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, new Steppable() {
                @Override
                public void step(SimState state) {
                    try {
                        //find the prices/quantity and write them down
                        String[] priceline = new String[scenario1.competitors.size()];
                        String[] quantityline = new String[scenario1.competitors.size()];
                        //create a fake good to price

                        for(int i=0; i <scenario1.competitors.size(); i++)
                        {
                            Firm competitor = scenario1.competitors.get(i);
                            SalesDepartment dept = competitor.getSalesDepartment(GoodType.GENERIC);



                            quantityline[i]=Long.toString(dept.getTodayOutflow());
                            priceline[i] = Long.toString(competitor.hypotheticalSellPrice(GoodType.GENERIC));
                        }
                        writer2.writeNext(priceline);    writer3.writeNext(quantityline);
                        writer2.flush(); writer3.flush();
                        ((MacroII) state).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            });

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        //run!
        while(macroII.schedule.getTime()<150000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(150001,(int)macroII.schedule.getSteps(),100);
        }


    }

    public LinkedList<Firm> getCompetitors() {
        return competitors;
    }


}
