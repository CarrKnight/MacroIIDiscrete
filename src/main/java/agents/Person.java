/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import ec.util.MersenneTwisterFast;
import financial.Market;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.*;

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



    private int minimumWageRequired;

    private Firm employer = null;

    /**
     * This flag is set to true when the agent is about to quit his job because the new wage is below the minimum he needs to work with
     */
    private boolean aboutToQuit = false;

    /**
     * The person keeps searching for better work if he can whenver this is active
     */
    private boolean searchForBetterOffers = false;


    private BuyerSearchAlgorithm employerSearch;

    private Market laborMarket;

    private long wage=-1;






    public Person(MacroII model) {
        this(model,0l,0,null);
    }

    public Person(MacroII model, long cash) {
        this(model,cash,0,null);
    }

    public Person(@Nonnull MacroII model, long cash, int minimumWageRequired,@Nullable Market laborMarket) {
        super(model, cash);
        this.minimumWageRequired = minimumWageRequired;
        this.laborMarket = laborMarket;
        if(laborMarket != null){
            laborMarket.registerSeller(this);
            employerSearch = new SimpleBuyerSearch(laborMarket,this);
            assert laborMarket.getGoodType().isLabor() : "is it a real labor market?";
            //if there is a labor market, you are going to be turned on by start()
            setActive(false);
        }
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
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {

        if(g.getType().isLabor())
        {
            assert price >= minimumWageRequired;
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
    public long maximumOffer(Good g) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long askedForABuyOffer(GoodType t) {
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
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
        throw new RuntimeException("Not programmed in yet");
    }


    public void hired(Firm buyer, long wage){
        employer = buyer;
        this.wage = wage;
    }


    public void changeInWage(long newWage,@Nonnull EconomicAgent employer1){

        wage = newWage;
        assert this.employer != null;
        assert this.employer == employer1; //make sure the one changing your wage is your owner, not somebody else (which is creepy).
        assert wage >=0;

        if(newWage<minimumWageRequired) {
            aboutToQuit=true;

            getModel().scheduleASAP(new Steppable() {
                @Override
                public void step(SimState simState) {
                    if(employer == null){
                        assert wage == -1; //if the employer changed the wage TWICE (or fired you) then you scheduled yourself twice which is silly, ignore it
                        return;
                    }

                    //If the wage is still low I QUIT!
                    if(wage < minimumWageRequired )
                        quitWork();

                }
            });
        }


    }

    /**
     * Utility method to call when the person quits his job!
     */
    private void quitWork(){

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
    public long getWage(){

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
    public void fired(@Nonnull final EconomicAgent employer){
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
                            minimumWageRequired, new Good(laborMarket.getGoodType(),Person.this,minimumWageRequired));
                }
                else{
                    //if we can't quote we have to peddle
                    throw new RuntimeException("Ernesto lazily didn't implement peddling.");
                }            }
        });




    }

    /**
     * If there is a labor market and I am employed I can look for better jobs and leave my current one
     */
    public void lookForBetterOffersNow()
    {
        //if you have no employer, you got no point in being here
        try{
            if(!searchForBetterOffers || employer == null || laborMarket == null)
                return;

            //make sure you are correctly employed
            assert wage >=0;
            //this is not necessarilly true: you might be in the process of quitting!
            assert (getMinimumWageRequired() <= wage) || aboutToQuit : getMinimumWageRequired() + " ---- " + wage;

            if(aboutToQuit) //the quitting will take care of this!
                return;

            //can you see the best offer now?
            if(laborMarket.isBestBuyPriceVisible())
            {
                //if there is work advertised ABOVE the wage we are being paid: quit!
                try {
                    if(laborMarket.getBestBuyPrice() > wage) //notice that this also takes care of when there is no offer at all (best price then is -1)
                    {
                        assert getMinimumWageRequired() < laborMarket.getBestBuyPrice(); //transitive property don't abandon me now!
                        quitWork(); //so long, suckers
                        //this might be false with better markets:
                        assert employer != null && wage >=0 : "I am assuming if you quit when there is an offer you are willing to accept, it must be the case that you find that job immediately!";
                    }
                } catch (IllegalAccessException e) {
                    assert false;
                    throw new RuntimeException("The market said the best price was visible, but it wasn't");

                }

            }
            else
            {

                throw new RuntimeException("Not implemented yet");


            }

        }
        //there are too many branching returns to fix in any other way but by finally. Man I am a terrible programmer.
        finally {
            if(isActive())
                //no matter what, keep trying
                getModel().scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        Person.this.lookForBetterOffersNow();

                    }
                });
        }


    }

    public Firm getEmployer() {
        return employer;
    }

    public int getMinimumWageRequired() {
        return minimumWageRequired;
    }

    /**
     * Does the person keep searching for better work if he can?
     * @return true if he does
     */
    public boolean isSearchForBetterOffers() {
        return searchForBetterOffers;
    }

    /**
     * Does the person keep searching for better work if he can?
     * @param searchForBetterOffers true if he does
     */
    public void setSearchForBetterOffers(boolean searchForBetterOffers) {

        boolean  oldFlag =this.searchForBetterOffers;
        this.searchForBetterOffers = searchForBetterOffers;


        if(isActive())
        {
            //if you started already and you weren't searching before we have to start the stepper
            if( oldFlag == false)
            {

                //schedule yourself for a look
                getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        Person.this.lookForBetterOffersNow();

                    }
                });
            }


        }
        //normal setter regardless


    }


    /**
     * call this to start the person and let it move a bit
     */
    public void start(){
        //you have been activated!
        setActive(true);

        //look for work
        lookForWorkSoon();




        //if needed, start your routine of looking for better job (greedy)
        getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState simState) {
                Person.this.lookForBetterOffersNow();

            }
        });


    }


    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    @Override
    public PurchaseResult shopHere(@Nonnull Quote buyerQuote, @Nonnull Quote sellerQuote) {
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
}
