/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import static org.mockito.Mockito.*;

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

    public TripolistScenario(MacroII macroII) {
        super(macroII);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        //do all the monopolist stuff
        super.start();
        //sanity check
        assert !getAgents().isEmpty();
        assert getMarkets().size()==2;

        //now add two more agents
        for(int i=0; i < additionalCompetitors; i++)
        {

            //only one seller
            final Firm seller = new Firm(getModel());
            seller.earn(1000000000l);
            //set up the firm at time 1
            getModel().scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState simState) {
                    //sales department
                    SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, goodMarket,
                            new SimpleBuyerSearch(goodMarket, seller), new SimpleSellerSearch(goodMarket, seller),
                            SalesDepartmentAllAtOnce.class);
                    seller.registerSaleDepartment(dept, GoodType.GENERIC);
                    dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                    //add the plant
                    Blueprint blueprint = new Blueprint.Builder().output(GoodType.GENERIC,1).build();
                    Plant plant = new Plant(blueprint,seller);
                    plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
                    plant.setCostStrategy(new InputCostStrategy(plant));
                    seller.addPlant(plant);


                    //human resources
                    HumanResources hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                                laborMarket, plant, getControlType().getController(), null, null).getDepartment();
                //    seller.registerHumanResources(plant, hr);
                  //  hr.setProbabilityForgetting(.10f);
                    hr.setFixedPayStructure(isFixedPayStructure());
                    hr.start();


                }
            });

            getAgents().add(seller);

        }

    }


    public int getAdditionalCompetitors() {
        return additionalCompetitors;
    }

    public void setAdditionalCompetitors(int additionalCompetitors) {
        this.additionalCompetitors = additionalCompetitors;
    }
}
