/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import static com.google.common.base.Preconditions.checkNotNull;

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
 * @version %I%, %G%
 * @see
 */
public class Person extends EconomicAgent {


    final private String name;


    private int minimumDailyWagesRequired;

    private Firm employer = null;

    /**
     * This flag is set to true when the agent is about to quit his job because the new wage is below the minimum he needs to work with
     */
    private boolean aboutToQuit = false;


    private BuyerSearchAlgorithm employerSearch;

    private Market laborMarket;

    private int wage=-1;

    /***
     *       ______           __           _
     *      / __/ /________ _/ /____ ___ _(_)__ ___
     *     _\ \/ __/ __/ _ `/ __/ -_) _ `/ / -_|_-<
     *    /___/\__/_/  \_,_/\__/\__/\_, /_/\__/___/
     *                             /___/
     */
    /**
     * what to do when consuming (production phase)
     */
    private ConsumptionStrategy consumptionStrategy;

    /**
     * whether and what to personally produce (production phase)
     */
    private PersonalProductionStrategy productionStrategy;

    /**
     * what should the worker do at the end of its work day (prepare_to_trade phase)
     */
    private AfterWorkStrategy afterWorkStrategy;

    /**
     * the utility function of the person, if used at all.
     */
    private UtilityFunction utilityFunction;


    /**
     * the idea is that unless i specify either a consumption or a personal production strategy,
     * this worker has no reason to step on PRODUCTION which speeds up the model.
     * This is a link to the production steppable which is not null if activated
     */
    private ProductionStep productionSteppable = null;

    /**
     * this flag is set to true if a production or consumption strategy is used.
     */
    private boolean productionOrConsumptionStrategiesSet = false;


    //so this default means no consumption. If the default isn't changed for either consumption or production
    //the production step simply doesn't happen.
    private final static Class<? extends ConsumptionStrategy> DEFAULT_NO_CONSUMPTION_STRATEGY = NoConsumptionStrategy.class;

    //so this default means no production. If the default isn't changed for either consumption or production
    //the production step simply doesn't happen.
    private final static Class<? extends PersonalProductionStrategy> DEFAULT_NO_PRODUCTION_STRATEGY = NoPersonalProductionStrategy.class;

    private final static Class<? extends AfterWorkStrategy> DEFAULT_AFTER_STRATEGY = DoNothingAfterWorkStrategy.class;

    private final static Class<? extends UtilityFunction> DEFAULT_UTILITY_FUNCTION = NoUtilityFunction.class;





    public Person(MacroII model) {
        this(model,0l,0,null);
    }

    public Person(MacroII model, long cash) {
        this(model,cash,0,null);
    }

    public Person( MacroII model, long cash, int minimumDailyWagesRequired, Market laborMarket) {
        super(model, cash);
        this.minimumDailyWagesRequired = minimumDailyWagesRequired;
        this.laborMarket = laborMarket;
        if(laborMarket != null){
            laborMarket.registerSeller(this);
            employerSearch = new SimpleBuyerSearch(laborMarket,this);
            assert laborMarket.getGoodType().isLabor() : "is it a real labor market?";
            //if there is a labor market, you are going to be turned on by start()
            setActive(false);
        }
        this.name = "Person " + minimumDailyWagesRequired;
        //create strategies!
        this.consumptionStrategy = ConsumptionStrategy.Factory.build(DEFAULT_NO_CONSUMPTION_STRATEGY);
        this.productionStrategy = PersonalProductionStrategy.Factory.build(DEFAULT_NO_PRODUCTION_STRATEGY);
        this.afterWorkStrategy = AfterWorkStrategy.Factory.build(DEFAULT_AFTER_STRATEGY);
        this.utilityFunction = UtilityFunction.Factory.build(DEFAULT_UTILITY_FUNCTION);
    }

    @Override
    public void consumeAll() {

        super.consumeAll();

    }

    @Override
    public MersenneTwisterFast getRandom() {
        return getModel().random;
    }


    @Override
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent buyer) {

        if(g.getType().isLabor())
        {
            assert price >= minimumDailyWagesRequired;
            assert buyer instanceof Firm;
            assert employer == buyer; //this guy should have hired you!
        }
        else
        {
            throw new RuntimeException("Error: Not programmed yet!");
        }
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public int maximumOffer(Good g) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public int askedForABuyOffer(GoodType t) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        throw new RuntimeException("To make");

    }

    @Override
    public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {
        throw new RuntimeException("Not programmed in yet");
    }


    public void hired(Firm buyer, int wage){
        employer = buyer;
        this.wage = wage;
    }


    public void changeInWageOrReservationWage(int newWage, EconomicAgent employer1){

        wage = newWage;
        assert this.employer != null;
        assert this.employer == employer1; //make sure the one changing your wage is your owner, not somebody else (which is creepy).
        assert wage >=0;

        if(newWage< minimumDailyWagesRequired) {
            aboutToQuit=true;

            //quit at next prepare to trade
            getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE, simState -> {
                if(employer == null){
                    assert wage == -1; //if the employer changed the wage TWICE (or fired you) then you scheduled yourself twice which is silly, ignore it
                    return;
                }

                //If the wage is still low I QUIT!
                if(wage < minimumDailyWagesRequired)
                    quitWork();

            });
        }


    }

    /**
     * Utility method to call when the person quits his job!
     */
    public void quitWork(){

        assert this.employer != null;
        assert wage >=0;
        //you quit, you are surely not about to anymore.
        aboutToQuit=false;

        employer.workerQuit(Person.this);
        employer = null;  wage = -1;
        lookForWorkSoon();

    }

    /**
     * Returns the wage or -1 if unemployed
     * @return -1
     */
    public int getWage(){

        if(employer == null){
            assert wage == -1;
            return  -1;
        }
        else
            return wage;
    }

    /**
     * Call this when the firm lays the person off.
     * @param employer  the firm that hired this worker.
     */
    public void fired( final EconomicAgent employer){
        checkNotNull(employer); //make sure it's not null
        assert this.employer == employer;
        this.employer = null;
        wage = -1;

        //look for work soon.

        assert Person.this.employer == null;
        assert wage == -1;

        lookForWorkSoon();



    }


    /**
     * If we do have a labor market and we do want to work this will submit a quote or look for a job
     */
    public void lookForWorkSoon(){


        model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {
                if(!isActive())  //if you have been turned off, don't bother
                    return;

                //you can't have a job to look for a job, use lookForBetterOffersNow()
                assert employer == null;
                assert wage == -1;


                if(laborMarket == null)
                    return; //no luck
                if(laborMarket.getSellerRole() == ActionsAllowed.QUOTE)
                {

                    //if we can quote: great!
                    laborMarket.submitSellQuote(Person.this,
                            minimumDailyWagesRequired, Good.getInstanceOfUndifferentiatedGood(
                                    laborMarket.getGoodType()));
                }
                else{
                    //if we can't quote we have to peddle
                    throw new RuntimeException("Ernesto lazily didn't implement peddling.");
                }            }
        }, Priority.AFTER_STANDARD);




    }



    public Firm getEmployer() {
        return employer;
    }

    public int getMinimumDailyWagesRequired() {
        return minimumDailyWagesRequired;
    }


    public void setMinimumDailyWagesRequired(int minimumDailyWagesRequired) {
        this.minimumDailyWagesRequired = minimumDailyWagesRequired;

        //when set and you already have a job, call
        if(employer!= null)
            changeInWageOrReservationWage(wage,employer);
    }

    /**
     * call this to start the person and let it move a bit
     */
    public void start(MacroII state){
        super.start(state);
        //you have been activated!
        setActive(true);

        //look for work
        lookForWorkSoon();

        //start consumption/production step (only if there is at least one strategy set!)
        if(productionOrConsumptionStrategiesSet) {
            startProductionStep();
        }



        //after work routine
        getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState simState) {
                if(!isActive())
                    return;
                UndifferentiatedGoodType money = laborMarket == null? null : laborMarket.getMoney();
                afterWorkStrategy.endOfWorkDay(Person.this,model,getEmployer(),wage,money);

                //reschedule yourself
                getModel().scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, this);

            }
        });


    }

    private void startProductionStep() {
        final ProductionStep action = new ProductionStep();
        getModel().scheduleSoon(ActionOrder.PRODUCTION, action);
        productionSteppable = action;
    }





    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */

    @Override
    public PurchaseResult shopHere( Quote buyerQuote,  Quote sellerQuote) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * The market where the person look for employers
     */
    public void setLaborMarket(Market laborMarket) {
        this.laborMarket = laborMarket;
        assert laborMarket.getGoodType().isLabor();

        //update the searcher
        if(employerSearch == null)
            employerSearch =  new SimpleBuyerSearch(laborMarket,this);
        else
            employerSearch.setMarket(laborMarket);

    }


    public float computesUtility() {
        return utilityFunction.computesUtility(this);
    }

    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf, GoodType typeOfGoodToGiveAway) {
        return utilityFunction.howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(typeYouGainOneUnitOf, typeOfGoodToGiveAway, this);
    }

    public float howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(GoodType typeLost, GoodType typeGained) {
        return utilityFunction.howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(typeLost, typeGained, this);
    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        return 0;

    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public float estimateSupplyGap(GoodType type) {
        return 0;
    }

    public boolean isAboutToQuit() {
        return aboutToQuit;
    }

    @Override
    public void turnOff() {
        super.turnOff();
        if(this.employer != null)
            quitWork();
        laborMarket.removeAllSellQuoteBySeller(this);
        laborMarket.deregisterSeller(this);
        consumptionStrategy = null;
        productionStrategy = null;
    }

    @Override
    public String toString() {
        return name;
    }


    public ConsumptionStrategy getConsumptionStrategy() {
        return consumptionStrategy;
    }

    public void setConsumptionStrategy(ConsumptionStrategy consumptionStrategy) {
        Preconditions.checkNotNull(consumptionStrategy);
        this.consumptionStrategy = consumptionStrategy;
        productionOrConsumptionStrategiesSet = true;


        if(productionSteppable == null && startWasCalled) {
            startProductionStep();
            assert productionSteppable != null;
        }


    }

    public PersonalProductionStrategy getProductionStrategy() {
        return productionStrategy;
    }

    public void setProductionStrategy(PersonalProductionStrategy productionStrategy) {
        Preconditions.checkArgument(productionStrategy!=null);
        this.productionStrategy = productionStrategy;
        productionOrConsumptionStrategiesSet = true;

        if(productionSteppable == null && startWasCalled) {
            startProductionStep();
            assert productionSteppable != null;
        }

    }


    public AfterWorkStrategy getAfterWorkStrategy() {
        return afterWorkStrategy;
    }

    public void setAfterWorkStrategy(AfterWorkStrategy afterWorkStrategy) {
        this.afterWorkStrategy = afterWorkStrategy;
    }

    public UtilityFunction getUtilityFunction() {
        return utilityFunction;
    }

    public void setUtilityFunction(UtilityFunction utilityFunction) {
        this.utilityFunction = utilityFunction;
    }

    public boolean isSteppingOnProduction() {
        return productionSteppable != null;
    }

    public Market getLaborMarket() {
        return laborMarket;
    }

    private class ProductionStep implements Steppable
    {



        /**
         * this method is called by the person itself every PRODUCTION.
         *
         */
        private void productionStep() {
            consumptionStrategy.consume(Person.this,model); //first consume
            productionStrategy.produce(Person.this,model); //then produce!
        }

        public void step(SimState state) {
            if (!isActive())
                return;
            productionStep();
            getModel().scheduleTomorrow(ActionOrder.PRODUCTION, this);
        }

    }



}
