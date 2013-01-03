package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummyBuyer;

/**
 * <h4>Description</h4>
 * <p/> This is actually a copy of scenario1 for the test class SimpleFlowSellerPIDTest. Very simple matching of supply and demand
 * <p/> Basically we periodically create new buyers (after removing the old ones) to give the impression of a constant demand and let the flow seller get to the right price
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-10
 * @see
 */
public class SimpleSellerScenario extends Scenario {



    int period = 10;

    boolean demandShifts = false;

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create and record a new market!
        final OrderBookMarket market= new OrderBookMarket(GoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter

        getMarkets().put(GoodType.GENERIC,market);


        //create 10 buyers
        for(int i=0;i<10;i++){



            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),i*10){
                @Override
                public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anynimous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(ActionOrder.TRADE,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            earn(1000l);
                            //put another quote

                            market.submitBuyQuote(reference,getFixedPrice());



                        }
                    });

                }
            };


            //make it adjust once to register and submit the first quote

            getModel().scheduleSoon(ActionOrder.TRADE,new Steppable() {
                @Override
                public void step(SimState simState) {
                    market.registerBuyer(buyer);
                    buyer.earn(1000l);
                    //make the buyer submit a quote soon.
                    market.submitBuyQuote(buyer,buyer.getFixedPrice());

                }
            }  );




            getAgents().add(buyer);



        }

        //only one seller
        final Firm seller = new Firm(getModel());
        //give it a seller department at time 1
        getModel().scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState simState) {
                SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(seller,market,new SimpleBuyerSearch(market,seller),new SimpleSellerSearch(market,seller));
                seller.registerSaleDepartment(dept,GoodType.GENERIC);
                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID
                getAgents().add(seller);
            }
        });




        //arrange for goods to drop periodically in the firm
        getModel().scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
            @Override
            public void step(SimState simState) {
                //sell 4 goods!
                for(int i=0; i<4; i++){
                    Good good = new Good(GoodType.GENERIC,seller,10l);
                    seller.receive(good,null);
                    seller.reactToPlantProduction(good);
                }
                //every day
                getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });

        //if demands shifts, add 10 more buyers after adjust 2000
        if(demandShifts)
        {
            //create 10 buyers
            for(int i=0;i<10;i++){



                /**
                 * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
                 */
                final DummyBuyer buyer = new DummyBuyer(getModel(),(i+10)*10){
                    @Override
                    public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                        //trick to get the steppable to recognize the anynimous me!
                        final DummyBuyer reference = this;
                        //schedule a new quote in period!
                        this.getModel().schedule.scheduleOnceIn(period,new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                earn(1000l);
                                //put another quote

                                market.submitBuyQuote(reference,getFixedPrice());



                            }
                        });

                    }
                };


                //make it adjust once to register and submit the first quote

                getModel().scheduleAnotherDay(ActionOrder.TRADE,new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        market.registerBuyer(buyer);
                        buyer.earn(1000l);
                        //make the buyer submit a quote soon.
                        market.submitBuyQuote(buyer,buyer.getFixedPrice());

                    }
                },2000  );




                getAgents().add(buyer);



            }
        }


        //hopefully that's that!






    }

    /**
     * Creates the scenario object, so that it links to the model.
     * =
     */
    public SimpleSellerScenario(MacroII model) {
        super(model);
    }


    public boolean isDemandShifts() {
        return demandShifts;
    }

    public void setDemandShifts(boolean demandShifts) {
        this.demandShifts = demandShifts;
    }
}
