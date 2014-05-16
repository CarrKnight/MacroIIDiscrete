/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPID;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

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
 * @version 2013-02-26
 * @see
 */
public class MarginalMaximizerWithUnitPIDTuning {


    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main(String[] args)
    {

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter("tuningUnit.csv"));
            writer.writeNext(new String[]{"proportional","integrative","derivative","maxFound","deviance","variance"});

        } catch (IOException e) {


        }

        final float[] pidParameters = new float[3];
        for(pidParameters[0] = 0.01f; pidParameters[0] <=2f; pidParameters[0] += 0.10f ){
            for(pidParameters[1] = 0.01f; pidParameters[1] <=2f; pidParameters[1] += 0.10f ){
                for(pidParameters[2]= 0f; pidParameters[2]<=0.1f; pidParameters[2] += 0.001f ){
                    float futureTargetAverage =0f;
                    double deviation = 0;
                    double variance = 0;
                    for(int i=0; i< 5; i++){


                        final MacroII macroII = new MacroII(System.currentTimeMillis());
                        MonopolistScenario scenario1 = new MonopolistScenario(macroII)
                        {
                            //override the monopolist construction in order to sweep through the pid parameters
                            @Override
                            public Firm buildFirm() {
                                //only one seller
                                final Firm built = new Firm(getModel());
                                built.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
                                //set up the firm at time 1
                                getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
                                    @Override
                                    public void step(SimState simState) {
                                        //sales department
                                        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(built, goodMarket,
                                                new SimpleBuyerSearch(goodMarket, built), new SimpleSellerSearch(goodMarket, built),
                                                SalesDepartmentAllAtOnce.class);
                                        built.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
                                        dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                                        //add the plant
                                        Plant plant = new Plant(blueprint, built);
                                        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL, built, 0, plant));
                                        plant.setCostStrategy(new InputCostStrategy(plant));
                                        built.addPlant(plant);


                                        //human resources
                                        HumanResources hr;
                                        //set up!
                                        hr = HumanResources.getEmptyHumanResources(1000000000, built, laborMarket, plant);
                                        TargetAndMaximizePlantControl control = TargetAndMaximizePlantControl.emptyTargetAndMaximizePlantControl(hr);
                                        control.setTargeter(new PIDTargeterWithQuickFiring(hr,control));
                                        MarginalMaximizerWithUnitPID algorithm = new MarginalMaximizerWithUnitPID(hr,control,plant,plant.getOwner(),
                                                model.random, hr.getPlant().getNumberOfWorkers());
                                        SetTargetThenTryAgainMaximizer<MarginalMaximizerWithUnitPID> maximizer =
                                                new SetTargetThenTryAgainMaximizer<MarginalMaximizerWithUnitPID>
                                                (hr,control,algorithm);
                                        control.setMaximizer(maximizer);
                                        hr.setPricingStrategy(control);
                                        hr.setControl(control);

                                        //       seller.registerHumanResources(plant, hr);
                                        hr.setFixedPayStructure(isFixedPayStructure());
                                        hr.start(model);


                                    }
                                });

                                getAgents().add(built);
                                return built;
                            }
                        };





                        macroII.setScenario(scenario1);
                        macroII.start();

                        int oldWorkers = 0;
                        while(macroII.schedule.getTime()<3500)
                        {

                            macroII.schedule.step(macroII);
                            variance += Math.pow(scenario1.getMonopolist().getTotalWorkers()-oldWorkers,2);
                            deviation += Math.pow(scenario1.getMonopolist().getTotalWorkers()-22,2);

                        }


                        futureTargetAverage+= scenario1.getMonopolist().getTotalWorkers();
                    }
                    futureTargetAverage= futureTargetAverage/5f;
                    deviation = deviation/5f;
                    variance = variance/5f;
                    writer.writeNext(new String[]{Float.toString((pidParameters[0])),Float.toString((pidParameters[1]))
                            ,Float.toString((pidParameters[2])),Float.toString(futureTargetAverage),Double.toString(deviation)
                            ,Double.toString(variance)});
                    try {
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
        }

    }

}
