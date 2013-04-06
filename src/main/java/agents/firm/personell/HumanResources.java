/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.personell;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.Firm;
import agents.firm.production.Plant;
import agents.firm.production.control.FactoryProducedTargetAndMaximizePlantControl;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.targeter.WorkforceTargeter;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.Market;
import goods.Good;
import goods.GoodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> Human resources is just a special type of purchases department. Its task is to hire workers, supply them to a specific plant and pay wages at the end of the week.
 * <p/> One human resource object for each plant. sets its targets independently.
 * <p/> The main difference remains that we have only one wage so whenever we change the wage we need to change it to every worker.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-21
 * @see
 */
public class HumanResources extends PurchasesDepartment {

    /**
     * The plant to manage.
     */
    private Plant plant;

    /**
     * The wages paid by the hr last weekEnd
     */
    private long wagesPaid = 0;

    /**
     * When the pay structure is true, there is one single wage over all the firm. Otherwise each agent works for the wage he is hired for
     */
    private boolean fixedPayStructure = true;


    /**
     * This is the empty constructor for purchase department. We need to implement factories
     * @param budgetGiven the amount of money give to the department
     * @param firm the firm owning the department
     * @param market the labor market
     */
    private HumanResources(long budgetGiven, @Nonnull Firm firm, @Nonnull Market market,
                           @Nonnull Plant plant) {
        super(budgetGiven, firm, market);
        assert market.getGoodType().isLabor(); //must be a labor market!
        this.plant = plant; //record the plant to supply.
    }

    /**
     * This is the empty constructor for purchase department. It will not work if you don't set the control and price
     * @param budgetGiven the amount of money give to the department
     * @param firm the firm owning the department
     * @param market the labor market
     */
    public static HumanResources getEmptyHumanResources(long budgetGiven, @Nonnull Firm firm, @Nonnull Market market,
                                                        @Nonnull Plant plant)
    {
        HumanResources hr =  new HumanResources(budgetGiven, firm, market, plant);
        firm.registerHumanResources(plant, hr);
        return hr;
    }


    /**
     * This factory for human resources is used when we want the department to follow an integrated rule: plant control and pricing rule are the same object. <br>
     * Leaving any of the type arguments null will make the constructor generate a rule at random
     * @param budgetGiven the budget given to the department by the firm
     * @param firm the firm owning the department
     * @param market the market the department dabbles in
     * @param integratedControl the type of rule that'll be both BidPricing and PlantControl
     * @param buyerSearchAlgorithmType the algorithm the buyer follows to search for competitors
     * @param sellerSearchAlgorithmType the algorithm the buyer follows to search for suppliers
     * @return a new instance of PurchasesDepartment
     */
    public static <PC extends PlantControl, BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm>
    FactoryProducedHumanResources<PC,BS,SS> getHumanResourcesIntegrated(long budgetGiven, @Nonnull Firm firm,
                                                                        @Nonnull Market market, @Nonnull Plant plant,
                                                             @Nullable Class<PC> integratedControl,
                                                             @Nullable Class<BS> buyerSearchAlgorithmType,
                                                             @Nullable Class<SS> sellerSearchAlgorithmType )
    {

        //create the new human resources
        HumanResources hr = new HumanResources(budgetGiven,firm,market,plant); //call the constructor
        //register yourself
        firm.registerHumanResources(plant, hr);

        //create inventory control and assign it
        PC bidPricingStrategy;
        if(integratedControl == null) //if null randomize
            bidPricingStrategy = (PC) PersonellRuleFactory.randomPlantControl(hr);
        else //otherwise instantiate the specified one
            bidPricingStrategy= PersonellRuleFactory.newPlantControl(integratedControl, hr);
        hr.setPricingStrategy(bidPricingStrategy);
        assert bidPricingStrategy instanceof PlantControl; //if you are really integrated that's true
        hr.setControl(bidPricingStrategy);



        //create a buyer search algorithm and assign it
        BS buyerSearchAlgorithm;
        if(buyerSearchAlgorithmType == null)
            buyerSearchAlgorithm = (BS) BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearchAlgorithmType,market,firm);
        hr.setOpponentSearch(buyerSearchAlgorithm);


        //create a random seller search algorithm and assign it
        SS sellerSearchAlgorithm;
        if(sellerSearchAlgorithmType == null)
            sellerSearchAlgorithm = (SS)SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearchAlgorithmType,market,firm);
        hr.setSupplierSearch(sellerSearchAlgorithm);

        //finally: return it!
        FactoryProducedHumanResources<PC,BS,SS> container =
                new FactoryProducedHumanResources<PC, BS, SS>(hr,bidPricingStrategy,buyerSearchAlgorithm,sellerSearchAlgorithm);
        //check it's correct
        assert container.getDepartment() == hr;
        assert container.getPlantControl() == hr.getPricingStrategy();        //unfortunately, this being a subclass, I can't be sure.


        //finally: return it!
        return container;

    }


    /**
     * This factory for human resources is used when we want the department to use a separate Targeter and Maximizer for its control. <br>
     * Leaving any of the type arguments null will make the constructor generate a rule at random
     * @param budgetGiven the budget given to the department by the firm
     * @param firm the firm owning the department
     * @param market the market the department dabbles in
     * @param targeter the plant control targeter algorithm to use
     * @param maximizer the plant control maximizer algorithm to use
     * @param buyerSearchAlgorithmType the algorithm the buyer follows to search for competitors
     * @param sellerSearchAlgorithmType the algorithm the buyer follows to search for suppliers
     * @return a new instance of PurchasesDepartment
     */
    public static
    <PC extends PlantControl,
            BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm, WT extends WorkforceTargeter,
            WM extends WorkforceMaximizer<ALG>, ALG extends WorkerMaximizationAlgorithm>
    FactoryProducedHumanResourcesWithMaximizerAndTargeter getHumanResourcesIntegrated(long budgetGiven, @Nonnull Firm firm,
                                                                                      @Nonnull Market market, @Nonnull Plant plant,
                                                             @Nullable Class<WT> targeter,
                                                             @Nullable Class<WM> maximizer,
                                                             @Nullable Class<ALG> maximizationAlgorithm,
                                                             @Nullable Class<BS> buyerSearchAlgorithmType,
                                                             @Nullable Class<SS> sellerSearchAlgorithmType )
    {

        //create the new human resources
        HumanResources hr = new HumanResources(budgetGiven,firm,market,plant); //call the constructor
        //register yourself
        firm.registerHumanResources(plant, hr);



        //create inventory control and assign it
        FactoryProducedTargetAndMaximizePlantControl<WT,WM> control =
                TargetAndMaximizePlantControl.PlantControlFactory(hr, targeter, maximizer,maximizationAlgorithm) ;
        BidPricingStrategy bidPricingStrategy =  control.getControl();
        hr.setPricingStrategy(bidPricingStrategy);
        assert bidPricingStrategy instanceof PlantControl; //if you are really integrated that's true
        hr.setControl((PlantControl) bidPricingStrategy);



        //create a buyer search algorithm and assign it
        BS buyerSearchAlgorithm;
        if(buyerSearchAlgorithmType == null)
            buyerSearchAlgorithm =  (BS)BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearchAlgorithmType,market,firm);
        hr.setOpponentSearch(buyerSearchAlgorithm);


        //create a random seller search algorithm and assign it
        SS sellerSearchAlgorithm;
        if(sellerSearchAlgorithmType == null)
            sellerSearchAlgorithm = (SS)SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearchAlgorithmType,market,firm);
        hr.setSupplierSearch(sellerSearchAlgorithm);


        FactoryProducedHumanResourcesWithMaximizerAndTargeter<TargetAndMaximizePlantControl,BS,SS,WT,WM,ALG>
                container = new FactoryProducedHumanResourcesWithMaximizerAndTargeter<>(hr,control.getControl(),buyerSearchAlgorithm,
                sellerSearchAlgorithm,control.getWorkforceTargeter(),control.getWorkforceMaximizer());

        assert hr == container.getDepartment();
        assert hr.getPricingStrategy() == container.getPlantControl();
        assert container.getWorkforceMaximizer() == container.getWorkforceMaximizer();
        assert container.getWorkforceTargeter() == container.getWorkforceTargeter();


        //finally: return it!
        return container;

    }

    public Plant getPlant() {
        return plant;
    }


    /**
     * Max price is overriden to immediately call the inventory control rather than playing with the market visibility and budget.
     * This is because we are forced to keep a single wage at the end of the day.
     *
     * @param type   the type of good we are going to buy
     * @param market the market we are buying it from
     * @return the max price we are willing to pay!
     */
    @Override
    public long maxPrice(GoodType type, Market market) {
        return getPricingStrategy().maxPrice(type);
    }

    /**
     * This is an additional method exclusive for HR. What it does is that it receives a new wage from its control and sets it for all the employees currently working at the plant.
     * It complements updateOfferPrices(), as in this method updates the wages of people we already have while the updateOfferPrices() update the wage offered to new employees.
     */
    public void updateEmployeeWages(){



        //new wage!
        long newWage = maxPrice(getGoodType(),getMarket()); //this is the new wage

        //change the wage to everyone
        if(fixedPayStructure)
            for(Person p : plant.getWorkers()){
                //max price calls should NOT change over time
                assert maxPrice(getGoodType(),getMarket()) == newWage;
                //change the wage to the worker
                p.changeInWage(newWage,getFirm());
            }

        //tell the plant to tell the others
        getPlant().fireWageEvent(newWage);

    }

    /**
     * In addition to checking the budget, hr also pays everybody's wages.
     */
    @Override
    public void weekEnd() {
        wagesPaid = 0;
        List<Person> roster = plant.getWorkers();
        //make sure the employees are paid the right wage
        updateEmployeeWages();

        for(Person p : roster)
        {
            long wage = p.getWage(); //get the wage promised to pay
            //either the pay is the same as what we are offering now or we aren't using a fixed pay structure.
            //it is not XOR because it is probably true for the last employee
            assert wage == maxPrice(getGoodType(),getMarket()) || !fixedPayStructure :
                    "this is the wage: " + wage + " this is how much we are willing to pay: " + maxPrice(GoodType.GENERIC,getMarket());
            getFirm().pay(wage,p,getMarket()); //pay wage to worker
            wagesPaid += wage;
            spendFromBudget(wage);
            assert getAvailableBudget() >=0;
        }
        super.weekEnd();
    }

    /**
     * React to a bid filled quote  (just calls super after asserting that we did in fact hire the guy)
     *
     * @param g     good bought
     * @param price price of the good.
     */
    @Override
    public void reactToFilledQuote(Good g, long price, @Nonnull EconomicAgent seller) {
        assert seller instanceof Person && plant.getWorkers().contains(seller);
        super.reactToFilledQuote(g, price, seller);
    }

    /**
     * Get the wages paid by the HR object last weekend
     */
    public long getWagesPaid() {
        return wagesPaid;
    }


    /**
     * if we need to fire people in order to get at this specific number of workers, what will the wage be if we fire all the most expensive ones?
     * @param workerTarget the new target of workers
     * @return the new wage that would ensue
     */
    public long hypotheticalWageAtThisLevel(int workerTarget)
    {
        assert workerTarget <= plant.workerSize();
        assert workerTarget >= plant.minimumWorkersNeeded();

        int currentWorkers = plant.workerSize();

        List<Person> workers = new ArrayList<>(getPlant().getWorkers());
        //sort them by their minimum wage
        Collections.sort(workers, new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return Long.compare(o1.getMinimumWageRequired(), o2.getMinimumWageRequired());

            }
        });

        assert workers.size() >0;
        int workersToFire = currentWorkers - workerTarget;
        assert workersToFire <= currentWorkers: workersToFire + "---" + currentWorkers;
        assert workersToFire > 0;
        //find out at what wage you can keep this many workers
        return workers.get(workers.size() - workersToFire -1).getMinimumWageRequired();

    }


    /**
     * Does everybody receive the same pay from this hr company?
     * @return true if there is a single wage for every worker
     */
    public boolean isFixedPayStructure() {
        return fixedPayStructure;
    }

    /**
     * Does everybody receive the same pay from this hr company?
     * @param fixedPayStructure if true everybody will be paid the same amount of money.
     */
    public void setFixedPayStructure(boolean fixedPayStructure) {
        this.fixedPayStructure = fixedPayStructure;
    }

    /**
     * utility method to fire people when you can't lower their wages. Wow, that sounds evil. <br>
     * Basically start by firing everyone who asks more than newWage. then assert that the new number of workers is exactly the worker target
     * @param newWage the new wage set
     * @param workerTarget the new work target, used for diagnostics only
     */
    public void fireEveryoneAskingMoreThan(long newWage, int workerTarget)
    {
        Preconditions.checkArgument(workerTarget>=0, "worker target can't be negative");
        Preconditions.checkArgument(workerTarget<plant.workerSize(), "you are not actually asking to fire anyone");
        Preconditions.checkArgument(newWage>=0,"what do I do with negative wages?");



        List<Person> workersToFire = new LinkedList<>();
        //now go around spreading death
        for(Person p : plant.getWorkers())
        {
            //is your wage higher than the current wage?
            if(p.getWage() > newWage  )
                workersToFire.add(p); //if so, bye bye.
        }

        //you may fire when ready
        for(Person p : workersToFire)
        {
            //fire
            plant.removeWorker(p);
            //fired!
            p.fired(getFirm());     //todo test that the worker will actually apply for a job again!
        }
        assert workerTarget == plant.workerSize();
    }


    /**
     * Get the simulation randomizer from the owner.
     */
    @Override
    public MersenneTwisterFast getRandom() {
        return getPlant().getRandom();
    }


    /**
     * Asks the firm to ask the model to ask the schedule (!!!) what time is it in Mason time
     */
    public double getTime(){

        return getFirm().getModel().schedule.getTime();

    }

    /**
     * returns all the economic agents that hire in the same market as this hr. It includes this firm as well
     */
    public Set<EconomicAgent> getAllEmployers(){
        assert getMarket().getGoodType().isLabor();
        return getMarket().getBuyers();
    }


    /**
     * How many workers are needed for this technology to even work?
     * @return the number of workers above which the plant can't operate.
     */
    public int maximumWorkersPossible() {
        return getPlant().maximumWorkersPossible();
    }
}


