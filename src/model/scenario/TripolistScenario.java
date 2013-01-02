package model.scenario;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import goods.GoodType;
import goods.production.Blueprint;
import goods.production.Plant;
import goods.production.control.DiscreteMatcherPlantControl;
import goods.production.control.DumbClimberControl;
import goods.production.control.ParticleControl;
import goods.production.technology.LinearConstantMachinery;
import model.MacroII;
import sim.engine.Schedule;
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
            getModel().schedule.scheduleOnce(Schedule.EPOCH + getModel().random.nextFloat(),new Steppable() {
                @Override
                public void step(SimState simState) {
                    //sales department
                    SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(seller,goodMarket,
                            new SimpleBuyerSearch(goodMarket,seller),new SimpleSellerSearch(goodMarket,seller));
                    seller.registerSaleDepartment(dept, GoodType.GENERIC);
                    dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                    //add the plant
                    Blueprint blueprint = new Blueprint.Builder().output(GoodType.GENERIC,1).build();
                    Plant plant = new Plant(blueprint,seller);
                    plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
                    plant.setCostStrategy(new InputCostStrategy(plant));
                    seller.addPlant(plant);


                    //human resources
                    HumanResources hr;
                    if(isParticle())
                        hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                                laborMarket, plant, ParticleControl.class, null, null);
                    else
                    if(!isAlwaysMoving())
                        hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                                laborMarket, plant, DiscreteMatcherPlantControl.class, null, null);
                    else
                        hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                                laborMarket, plant, DumbClimberControl.class, null, null);
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
