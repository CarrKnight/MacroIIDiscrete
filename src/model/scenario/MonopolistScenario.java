package model.scenario;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import goods.production.Blueprint;
import goods.production.Plant;
import goods.production.control.DiscreteMatcherPlantControl;
import goods.production.control.DumbClimberControl;
import goods.production.control.ParticleControl;
import goods.production.technology.LinearConstantMachinery;
import model.MacroII;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummyBuyer;

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
 * @version 2012-09-14
 * @see
 */
public class MonopolistScenario extends Scenario {


    public MonopolistScenario(MacroII macroII) {
        super(macroII);
    }

    private boolean alwaysMoving = false;

    private boolean particle = false;

    /**
     * Does hr charge a single wage across the plant?
     */
    private boolean fixedPayStructure = true;

    /**
     * Do agents go around look for better offers all the time?
     */
    private boolean lookForBetterOffers = false;

    protected  OrderBookMarket goodMarket;

    protected OrderBookMarket laborMarket;

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create and record a new market!
        goodMarket= new OrderBookMarket(GoodType.GENERIC);
        goodMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.GENERIC,goodMarket);

        //create and record the labor market!
        laborMarket= new OrderBookMarket(GoodType.LABOR);
        laborMarket.setPricePolicy(new BuyerSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.LABOR,laborMarket);




        //buyers from 100 to 41 with increments of1
        for(int i=41; i<101; i++)
        {


            /************************************************
             * Add Good Buyers
             ************************************************/

            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),i){
                @Override
                public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().schedule.scheduleOnceIn(10,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            earn(1000l);
                            //put another quote
                            goodMarket.submitBuyQuote(reference,getFixedPrice());

                        }
                    });

                }
            };


            //make it adjust once to register and submit the first quote

            getModel().schedule.scheduleOnceIn(Math.max(5f + getModel().random.nextGaussian(),.01f),new Steppable() {
                @Override
                public void step(SimState simState) {
                    goodMarket.registerBuyer(buyer);
                    buyer.earn(1000l);
                    //make the buyer submit a quote soon.
                    goodMarket.submitBuyQuote(buyer,buyer.getFixedPrice());
                }
            }  );




            getAgents().add(buyer);

        }


        /************************************************
         * Add  Monopolist
         ************************************************/


        //only one seller
        final Firm seller = new Firm(getModel());
        seller.earn(1000000000l);
        //set up the firm at time 1
        getModel().schedule.scheduleOnce(new Steppable() {
            @Override
            public void step(SimState simState) {
                //sales department
                SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(seller,goodMarket,
                        new SimpleBuyerSearch(goodMarket,seller),new SimpleSellerSearch(goodMarket,seller));
                seller.registerSaleDepartment(dept,GoodType.GENERIC);
                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                //add the plant
                Blueprint blueprint = new Blueprint.Builder().output(GoodType.GENERIC,1).build();
                Plant plant = new Plant(blueprint,seller);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                seller.addPlant(plant);


                //human resources
                HumanResources hr;
                if(particle)
                    hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                            laborMarket, plant, ParticleControl.class, null, null);
                else
                if(!alwaysMoving)
                    hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                            laborMarket, plant, DiscreteMatcherPlantControl.class, null, null);
                else
                    hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, seller,
                            laborMarket, plant, DumbClimberControl.class, null, null);
                //       seller.registerHumanResources(plant, hr);
                hr.setFixedPayStructure(fixedPayStructure);
                hr.start();


            }
        });

        getAgents().add(seller);





        /************************************************
         * Add workers
         ************************************************/

        //with minimum wage from 15 to 65
        for(int i=0; i<120; i++)
        {
            //dummy worker, really
            final Person p = new Person(getModel(),0l,(15+i)*10,laborMarket);

            p.setSearchForBetterOffers(lookForBetterOffers);

            p.start();

            getAgents().add(p);

        }






    }


    public boolean isAlwaysMoving() {
        return alwaysMoving;
    }

    public void setAlwaysMoving(boolean alwaysMoving) {
        this.alwaysMoving = alwaysMoving;
    }

    public boolean isParticle() {
        return particle;
    }

    public void setParticle(boolean particle) {
        this.particle = particle;
    }

    public boolean isFixedPayStructure() {
        return fixedPayStructure;
    }

    public void setFixedPayStructure(boolean fixedPayStructure) {
        this.fixedPayStructure = fixedPayStructure;
    }


    public boolean isLookForBetterOffers() {
        return lookForBetterOffers;
    }

    public void setLookForBetterOffers(boolean lookForBetterOffers) {
        this.lookForBetterOffers = lookForBetterOffers;
    }
}
