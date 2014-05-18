/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm;

import agents.EconomicAgent;
import agents.people.Person;
import agents.firm.owner.DividendStrategy;
import agents.firm.owner.NoDividendStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.PlantStatus;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.utilities.DailyProfitReport;
import agents.firm.utilities.DummyProfitReport;
import agents.firm.utilities.NumberOfPlantsListener;
import agents.firm.utilities.ProfitReport;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ec.util.MersenneTwisterFast;
import financial.MarketEvents;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import javafx.beans.value.ObservableDoubleValue;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.PlantDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;


import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/12/12
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Firm extends EconomicAgent  {


    private final HashMap<GoodType,SalesDepartment> salesDepartments;

    /**
     * The name, if any, of the firm
     */
    private String name = null;

    private final HashMap<GoodType,PurchasesDepartment> purchaseDepartments;

    /**
     * This bilinear map (guava) links each plant to its human resources (and viceversa)
     */
    private final BiMap<Plant,HumanResources> humanResources;

    /**
     * If we are ever asked to rank our departments by the wages they offer we store here the one that pays the most
     * The computation occurs in maximumOffer()
     */
    private HumanResources bestPayingHR;

    /**
     * If we are ever asked to rank our departments by the wages they offer we store here the best wage offered
     * The computation occurs in maximumOffer()
     */
    private int bestWageOffer;

    /**
     * The object that computes profits for each plant
     */
    private ProfitReport profitReport;

    /**
     * The set of plants owned by the firm
     */
    private final LinkedHashSet<Plant> plants  = new LinkedHashSet<>(1);

    /**
     * This is a simple list of all the agents/objects/strategies that needs to be notified when a firm closes down or open a new plant
     */
    private final Set<NumberOfPlantsListener> numberOfPlantsListeners;

    /**
     * This reference is a cheap hack to make "reactToFilledQuote" work properly. For normal goods there is only one buyer so it's easy for the firm to tell the right department to react.
     * More than one HR could be hiring. If so, we need a way to tell the right hr to react to the right quote. Because hire() gets called BEFORE, we use that method to remember the hr that cause the hiring to occur
     * and call it when the reactQuoteTime occurs.
     */
    private HumanResources lastHiringDepartment = null;


    /**
     * The strategy with which the firm pays out weekly profits; by default it doesn't!
     */
    private DividendStrategy dividendStrategy = NoDividendStrategy.getInstance();

    /**
     * The owner of the firm, if null the firm doesn't pay dividends to anybody!
     */
    private EconomicAgent owner;

    /**
     * the usual constructor, checks for gui to build panel
     * @param model
     */
    public Firm(MacroII model) {
        super(model);

        salesDepartments = new HashMap<>();
        purchaseDepartments = new HashMap<>();
        humanResources = HashBiMap.create(1); //factory method for this
        numberOfPlantsListeners = new HashSet<>(); //prepare for listeners
        profitReport = new DailyProfitReport(this);
        //create the timeline manager

        if(MacroII.hasGUI()){
            //build the inspector
            buildInspector();
        }

    }

    /**
     * alternative constructor, NEVER builds panel/inspector
     * @param model
     */
    public Firm(MacroII model,boolean ignored) {
        super(model);

        salesDepartments = new HashMap<>();
        purchaseDepartments = new HashMap<>();
        humanResources = HashBiMap.create(1); //factory method for this
        numberOfPlantsListeners = new HashSet<>(); //prepare for listeners
        profitReport = new DummyProfitReport();
        //create the timeline manager

        firmInspector = null;

    }


    /**
     * Add a new plant creation listener to the list of people to be notified when a new plant opens or closes
     */
    public void addPlantCreationListener( NumberOfPlantsListener listener)
    {
        //addSalesDepartmentListener to the list
        numberOfPlantsListeners.add(listener);

    }

    /**
     * Assign a specific human resources object to the plant. If an old hr was working, turn it off first
     * @param p the plant to be controlled by HR
     * @param hr the human resources object
     */
    public void registerHumanResources(Plant p, final HumanResources hr){
        assert plants.contains(p);
        HumanResources oldHR = humanResources.remove(p);
        assert hr !=oldHR : "you are registering the same hr department twice";

        //turn off the old HR if necessary
        if(oldHR != null)
            oldHR.turnOff();
        //log it
        listenTo(hr);

        //put it in.
        humanResources.put(p,hr);

        //if start was already called, you need to start this too next dawn
        if(startWasCalled)
        {
            model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
                    hr.start(model);
                }
            });
        }



    }

    /**
     * Remove the creation listener to the list of people to be notified when a new plant opens or closes
     */
    public boolean removePlantCreationListener( NumberOfPlantsListener listener)
    {
        //addSalesDepartmentListener to the list
        return numberOfPlantsListeners.remove(listener);
    }

    /**
     * Add a new plant to this firm's management
     * @param p the new plant
     */
    public void addPlant( Plant p)
    {
        plants.add(p);
        for(NumberOfPlantsListener l : numberOfPlantsListeners)
            l.plantCreatedEvent(this,p);

        //log it
        listenTo(p);

        //if it's ready start it!
        if(p.getStatus() == PlantStatus.READY)
            //schedule yourself for production
            model.scheduleSoon(ActionOrder.PRODUCTION, p);

    }

    /**
     * Remove the plant from this firm's management
     * @param p the old plant
     */
    public void removePlant( Plant p)
    {
        boolean removed = plants.remove(p);
        assert removed;


        for(NumberOfPlantsListener l : numberOfPlantsListeners)
            l.plantClosedEvent(this,p);


        //remove it from the hr list
        HumanResources hr = humanResources.remove(p);
        if(hr != null)
            hr.turnOff();

        //turn it off
        p.turnOff();

    }



    /**
     * Returns a list containing all the plants using a specific good type as input
     * @param type the type of good the plants use as inputs. (if none found it returns an empty list)
     */
    public List<Plant> getListOfPlantsUsingSpecificInput( GoodType type)
    {

        if(plants.isEmpty())
            return Collections.emptyList();
        else
        {
            List<Plant> toReturn = new LinkedList<>();
            for(Plant p : plants)
            {
                if(!type.isLabor())
                {
                    Integer i = p.getBlueprint().getInputs().get(type);
                    if(i != null && i > 0)
                        toReturn.add(p);
                }
                else
                {
                    if(p.getHr().getMarket().getGoodType().equals(type))
                        toReturn.add(p);
                }

            }


            return toReturn;
        }

    }

    /**
     * Returns a list of all plants producing a specific output
     * @param type the type of good the plants use as inputs. (if none found it returns an empty list)
     */
    public List<Plant> getListOfPlantsProducingSpecificOutput( GoodType type)
    {

        if(plants.isEmpty())
            return Collections.emptyList();
        else
        {
            List<Plant> toReturn = new LinkedList<>();
            for(Plant p : plants)
            {
                Integer i = p.getBlueprint().getOutputs().get(type);
                if(i != null && i > 0)
                    toReturn.add(p);
            }


            return toReturn;
        }

    }



    /**
     * Whenever the plant produces goods, it asks the firm what to do about it!
     * @param g  the good that was produced!
     */
    public void reactToPlantProduction(Good g){
        salesDepartments.get(g.getType()).sellThis(g);
    }


    /**
     * Whenever the plant produces a number of goods, it asks the firm what to do about it!
     * @param g  the good that was produced!
     */
    public void reactToPlantProduction(UndifferentiatedGoodType undifferentiated, int amount)
    {
        salesDepartments.get(undifferentiated).sellThese(amount);
    }

    @Override
    public MersenneTwisterFast getRandom() {
        return model.random;
    }

    @Override
    public void consumeAll() {
        for(GoodType type : goodTypesEncountered())
            while(!type.isLabor() && this.hasAny(type))
                this.consume(type);

    }


    /**
     * This method burns inventory by 1. If there was a sales department selling that good, tell him
     *
     * @param g what good is consumed?
     * @return the good consumed
     */
    @Override
    public Good consume(GoodType g)
    {
        Good consumed =  super.consume(g);
        //get the seller associated to this kind of good
        SalesDepartment seller = salesDepartments.get(g);
        if(seller != null)
        {
            //if there is a sales department for this kind of good, it'll have be to told its good has been consumed
            assert seller.isSelling(consumed); //should be on sale there
            seller.stopSellingThisGoodBecauseItWasConsumed(consumed);

        }

        return consumed;
    }

    public MacroII getModel() {
        return model;
    }

    @Override
    public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent buyer) {
        assert !g.getType().isLabor(); //hopefull you aren't selling labor, or this is weird.
        assert salesDepartments.containsKey(g.getType());       //you should have a sales department for it
        salesDepartments.get(g.getType()).reactToFilledQuote(quoteFilled, g, price, buyer);  //tell the sales department that the deed is done!
    }

    @Override
    public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {
        checkNotNull(seller); //not null!

        if(!g.getType().isLabor()) //if it's not labor...
        {
            checkNotNull(g); //not null!

            assert purchaseDepartments.containsKey(g.getType());       //you should have a purchases department for it
            purchaseDepartments.get(g.getType()).reactToFilledQuote(g,price,seller);  //tell the sales department that the deed is done!
        }
        else{
            assert lastHiringDepartment != null;
            HumanResources hr = lastHiringDepartment;
            lastHiringDepartment = null; //clear it before making hr react or it might stack.
            hr.reactToFilledQuote(g,price,seller);
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

        if(!g.getType().isLabor()){
            //get the right department
            PurchasesDepartment department = purchaseDepartments.get(g.getType());
            if(department == null)  //if you are asking for an offer of something we don't want the result is -1
                return  -1;
            assert department != null;

            return department.maximumOffer(g);

        }
        else {
            //get all the hr departments
            Collection<HumanResources> hrs = humanResources.values();
            bestPayingHR = null;        //reset the variables
            bestWageOffer = -1;

            for(HumanResources hr: hrs){
                int offer = hr.maximumOffer(g);
                if(offer>bestWageOffer)
                {
                    bestWageOffer = offer;
                    bestPayingHR =hr;
                }
            }

            assert (bestWageOffer >= 0 && bestPayingHR != null) || (bestWageOffer == -1 && bestPayingHR == null);

            return bestWageOffer;
        }
    }

    /**
     * Basically asks the purchase department how much it is willing to pay for a good of a specific kind.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    public int expectedInputPrice(GoodType g) {
        //get the right department
        PurchasesDepartment department = purchaseDepartments.get(g);
        assert department != null;

        //ask the department how much you are willing to pay for that!
        return department.getLastOfferedPrice();
    }




    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public int askedForABuyOffer(GoodType t) {
        return 0;
    }


    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        if(salesDepartments.containsKey(type))
            return salesDepartments.get(type).askedForASalePrice(buyer);
        else
            throw new RuntimeException("To make "  + toString() + " --- " + this.getClass().getSimpleName() );
    }

    /**
     * Get the map of all sales department(unmodifiable)
     * @return
     */
    public Map<GoodType, SalesDepartment> getSalesDepartments() {
        return Collections.unmodifiableMap(salesDepartments);
    }


    /**
     * Get the sales department that sells a specific good
     * @return
     */
    
    public SalesDepartment getSalesDepartment( GoodType good) {
        return salesDepartments.get(good);
    }

    /**
     * This is called to addSalesDepartmentListener a sales department as part of this firm. Makes sure there is only one for each market
     */
    public void registerSaleDepartment(final SalesDepartment newSales, GoodType type){
        assert !salesDepartments.containsKey(type);
        //log it
        listenTo(newSales);
        //add it
        salesDepartments.put(type,newSales);

        //if start was already called, you need to start this too next dawn
        if(startWasCalled)
        {
            model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
                    newSales.start(model);
                }
            });
        }


    }


    /**
     * This is called to addSalesDepartmentListener a sales department as part of this firm. Makes sure there is only one for each market
     */
    public void registerPurchasesDepartment(final PurchasesDepartment newPurchases, GoodType type){
        assert !purchaseDepartments.containsKey(type);
        //log it
        listenTo(newPurchases);
        //add it
        purchaseDepartments.put(type,newPurchases);

        //if start was already called, you need to start this too next dawn
        if(startWasCalled)
        {
            model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
                    newPurchases.start(model);
                }
            });
        }



    }

    /**
     * It's the firm's responsbility to adjust the weekEnd methods of all its subcomponent. As of now the order is:
     * <ul>
     *     <li> Plants</li>
     *     <li> Purchase Departments</li>
     *     <li> Sales Department</li>
     * </ul>
     * After that is done the firm proceeds to:
     * <ul>
     *     <li> Count profits</li>
     *     <li> Pay out dividends</li>
     *     <li> Update GUI (if needed)</li>
     * </ul>
     * @param time the model time.
     */
    @Override
    public void weekEnd(double time) {
        super.weekEnd(time);
        Collection<Plant> obsoletePlants = new LinkedList<>();          //to store the obsolete plants we need to remove

        //weekend for plants
        for(Plant p : plants){
            p.weekEnd(time);
            if(p.getStatus() == PlantStatus.OBSOLETE) // if it became obsolete
                obsoletePlants.add(p);
        }

        //remove the obsolete plants
        for(Plant p : obsoletePlants)
            removePlant(p);

        //weekend for purchases departments
        for(PurchasesDepartment pDept : purchaseDepartments.values())
            pDept.weekEnd(time);


        //weekend for sales departments
        for(SalesDepartment sDept : salesDepartments.values())
            sDept.weekEnd(time);

        //weekend for human resources
        for(HumanResources hr : humanResources.values())
            hr.weekEnd(time);

        //profit report!
        profitReport.weekEnd();

        //pay out your enormous dividends
        if(owner != null) //if there is an owner pay dividends, otherwise who cares
            dividendStrategy.payDividends(profitReport.getAggregateProfits(),this,owner);

        //if you have GUI do update it
        //todo logtodo


    }

    /**
     * It'll probably have to be assigned to a plant by react to quote
     * @param p
     */
    public void hire( Person p,  Department department){


        if(department != null)
        {   //here we just have to assign it to the firm controlled by the human resources
            assert department instanceof HumanResources;
            humanResources.inverse().get(department).addWorker(p);
            lastHiringDepartment = (HumanResources) department;
        }
        else
        {
            //if we were here it means the employee was peddling and we got him
            assert bestPayingHR != null;
            assert bestWageOffer >=0;
            bestPayingHR.getPlant().addWorker(p);

            bestPayingHR = null;
            bestWageOffer = -1;


        }

    }

    /**
     * This is called by a worker when it quits the firm
     * @param p the person quitting
     */
    public void workerQuit(Person p){


        boolean workerFound = false;

        for(Plant plant : plants)
        {

            try{
                plant.removeWorker(p);
                workerFound=true;
            }
            catch (IllegalArgumentException ignored){
                assert !workerFound;
            }
            if(workerFound)
                break;

        }
        if(!workerFound && isActive())
            throw new IllegalArgumentException("Worker not found!");
    }


    /**
     * The firm turns off all its departments and plants and clears all its data structures
     */
    public void turnOff(){
        super.turnOff();
        for(SalesDepartment dept : salesDepartments.values())
            dept.turnOff();
        salesDepartments.clear();
        for(PurchasesDepartment dept : purchaseDepartments.values())
            dept.turnOff();
        for(HumanResources hr : humanResources.values())
            hr.turnOff();
        humanResources.clear();
        purchaseDepartments.clear();
        for(Plant p : plants)
            p.turnOff();
        plants.clear();


    }

    /**
     * Start all the subcomponents you have
     */
    @Override
    public void start(MacroII state){
        super.start(state);
        for(PurchasesDepartment dept : purchaseDepartments.values())
            dept.start(state);
        for(HumanResources hr : humanResources.values())
            hr.start(state);
        for(SalesDepartment sales : salesDepartments.values())
            sales.start(state);

    }


    public Set<Plant> getPlants() {
        return Collections.unmodifiableSet(plants);
    }

    /**
     * Get the human resources component
     * @param p The plant the human resources is controlling
     * @return the human resources object
     */
    public HumanResources getHR(Plant p){
        return humanResources.get(p);

    }

    /**
     * Returns LAST WEEK plant profits.
     * @param p The specific plant
     * @return the profits made
     */
    public float getPlantProfits(Plant p) {
        return profitReport.getPlantProfits(p);
    }

    /**
     * Efficiency ratio is just costs/revenues, the lower the better
     * @return the efficiency ratio
     */
    public float getEfficiencyRatio(Plant p) {
        return profitReport.getEfficiencyRatio(p);
    }

    /**
     * returns the total costs attributable to this plant
     * @param p the plant
     * @return the variable AND amortized fixed costs.
     */
    public float getPlantCosts(Plant p) {
        return profitReport.getPlantCosts(p);
    }

    /**
     * returns the total sales attributable to this plant
     * @param p the plant
     * @return the revenues of the plant
     */
    public float getPlantRevenues(Plant p) {
        return profitReport.getPlantRevenues(p);
    }

    /**
     * @return  the total profits of the week
     * */
    public long getAggregateProfits() {
        return profitReport.getAggregateProfits();
    }

    /**
     * This is just the ratio of profits to revenues
     */
    public float getNetProfitRatio(Plant p) {
        return profitReport.getNetProfitRatio(p);
    }

    /**
     * Turns on/off the firm
     * @param active true if it's to be on. It can't be turned on once it's off
     */
    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if(!active)
            turnOff();
    }




    private TabbedInspector firmInspector;

    /**
     * Used by the constructor to build the timeline. Only called if the GUI is active
     */
    public void buildInspector(){

        assert MacroII.hasGUI();




        assert MacroII.hasGUI();
        firmInspector = new TabbedInspector(false);


        /**************************************************
         * Simple inspector
         *************************************************/





    }

    /**
     * returns the inspector the firm built
     * @return
     */
    public TabbedInspector firmInspector() {
        return firmInspector;
    }

    /**
     * If an logEvent worth to be put in the timeline occurred, call this!
     * @param agent the one who performed the logEvent
     * @param action the action that has occurred
     * @param time the "real" time when this occurred
     * @param annotations additional information to display!
     */
    public void logEvent(Object agent, MarketEvents action, long time, String annotations) {
        //todo logtodo

    }

    /**
     * If an logEvent worth to be put in the timeline occurred, call this!
     * @param agent the one who performed the logEvent
     * @param action the action that has occurred
     * @param time the "real" time when this occurred
     */
    public void logEvent(Object agent, MarketEvents action, long time) {
        //todo logtodo

    }


    /**
     * The standard inspector is just a simpleInspector, but it can be overriden.
     *
     * @return
     */
    @Override
    public Inspector getInspector(GUIState gui) {
        return firmInspector();
    }

    /**
     * get the owner of the firm
     */
    public EconomicAgent getOwner() {
        return owner;
    }

    /**
     * Set the firm to have a new owner
     */
    public void setOwner(EconomicAgent owner) {
        this.owner = owner;
    }

    /**
     * get the dividend strategy
     */
    public DividendStrategy getDividendStrategy() {
        return dividendStrategy;
    }

    /**
     * set a new dividend. Throws an exception if there is no owner
     */
    public void setDividendStrategy(DividendStrategy dividendStrategy) {
        Preconditions.checkArgument(owner != null);
        this.dividendStrategy = dividendStrategy;
    }

    /**
     * Get the purchase department associated with buying this good
     * @param goodType the type of good
     * @return a purchase department
     */
    public PurchasesDepartment getPurchaseDepartment( GoodType goodType) {
        return purchaseDepartments.get(goodType);
    }


    /**
     * An horrible utility method, returns a plant owned by this firm that produces a specific good. It throws an IllegalArgument exception if there is no plant producing this
     */
    
    public Plant getRandomPlantProducingThis(GoodType g)
    {
        //dumps the Plants in a list
        List<Plant> listOfPlants = new ArrayList<>(plants);
        //shuffle it if needed
        if(listOfPlants.size()>1)
            Collections.shuffle(listOfPlants);
        //go through all the plants
        for(Plant p : listOfPlants)
        {
            //if you find a plant producing such good, return it
            if(p.getBlueprint().getOutputs().containsKey(g))
            {
                assert p.getBlueprint().getOutputs().get(g) > 0; //make sure it's not a weird 0 production
                return p;
            }
        }
        throw new IllegalArgumentException("There is no plant producing the specific good you asked for!");
    }

    /**
     * just checks if the firm owns any plants
     * @return true if it owns at least one plant
     */
    public boolean hasPlants(){

        return !plants.isEmpty();

    }

    /**
     * This is probably not used unless you want to inject a profit report object for testing reasons.
     * @param weeklyProfitReport the new profit report.
     */
    public void setProfitReport(ProfitReport weeklyProfitReport) {
        this.profitReport = weeklyProfitReport;
    }

    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    
    public PurchaseResult shopHere( Quote buyerQuote, Quote sellerQuote)
    {
        //make sure it's us
        assert sellerQuote.getAgent().equals(this);
        assert buyerQuote.getType().equals(sellerQuote.getType());
        //get the right sales department
        SalesDepartment dept = salesDepartments.get(buyerQuote.getType());
        //make sure it's the correct one
        assert sellerQuote.getOriginator().equals(dept);
        //delegate
        return dept.shopHere(buyerQuote,sellerQuote);

    }

    @Override
    public String toString() {
        if(name == null)
            return super.toString();
        else
            return name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected ProfitReport getProfitReport() {
        return profitReport;
    }


    /*-----------------------------------------
     * DELEGATES FROM PROFIT REPORTS
     -------------------------------------------*/

    /**
     * Get last recorded production for a specific good
     */
    public int getLastWeekProduction(GoodType type) {
        return profitReport.getLastWeekProduction(type);
    }

    /**
     * Count all workers at all plants
     * @return the total number of workers
     */
    public int getTotalWorkers()
    {
        int totalWorkers = 0;
        for(Plant p : plants)
            totalWorkers += p.getNumberOfWorkers();
        return totalWorkers;
    }


    /**
     * this is a "utility" method that should be used sparingly. What it does is it creates a mock object, passes it to the sales department
     * and ask for it for a price. It is no guarantee that the firm actually will charge such price when a real good gets created.
     * @param goodType the good type the sales department deals with
     * @return
     */
    public int hypotheticalSellPrice(GoodType goodType)
    {
        Preconditions.checkArgument(salesDepartments.containsKey(goodType));

        SalesDepartment dept = getSalesDepartment(goodType);

        return dept.hypotheticalSalePrice();

    }

    /**
     * this is a "utility" method to avoid tren wreck calls. Basically returns the outflow recorded by the sales department dealing with that good type
     * @param goodType the good type the sales department deals with
     * @return
     */
    public int getSalesDepartmentRecordedOutflow(GoodType goodType)
    {
        Preconditions.checkArgument(salesDepartments.containsKey(goodType));

        SalesDepartment dept = getSalesDepartment(goodType);
        return dept.getTodayOutflow();
    }
    /**
     * this is a "utility" method to avoid tren wreck calls. Basically returns the outflow recorded by the sales department dealing with that good type
     * @param goodType the good type the sales department deals with
     * @return
     */
    public int getSalesDepartmentRecordedInflow(GoodType goodType)
    {
        Preconditions.checkArgument(salesDepartments.containsKey(goodType));

        SalesDepartment dept = getSalesDepartment(goodType);
        return dept.getTodayInflow();
    }




    /**
     * Count all the workers at plants that produce a specific output
     * @param goodType the type of output
     * @return the total number of workers
     */
    public int getNumberOfWorkersWhoProduceThisGood(GoodType goodType)
    {
        int totalWorkers = 0;
        for(Plant p : plants){
            Integer outputProduced = p.getBlueprint().getOutputs().get(goodType);
            if(outputProduced != null && outputProduced > 0)
                totalWorkers += p.getNumberOfWorkers();
        }
        return totalWorkers;


    }

    /**
     * Count all the workers at plants that produce a specific output
     * @param goodType the type of output
     * @return the total number of workers
     */
    public int getNumberOfWorkersWhoProducedThisGoodThatDay(GoodType goodType, int day)
    {
        int totalWorkers = 0;
        for(Plant p : plants){
            Integer outputProduced = p.getBlueprint().getOutputs().get(goodType);
            if(outputProduced != null && outputProduced > 0)
                totalWorkers += p.getObservationRecordedThisDay(PlantDataType.TOTAL_WORKERS,day);
        }
        return totalWorkers;


    }


    /**
     * Count all the workers at plants that consume (as input) a specific output
     * @param goodType the type of output
     * @return the total number of workers
     */
    public int getNumberOfWorkersWhoConsumeThisGood(GoodType goodType)
    {
        int totalWorkers = 0;
        for(Plant p : plants){
            if(!goodType.isLabor())
            {
                Integer inputProduced = p.getBlueprint().getInputs().get(goodType);
                if(inputProduced != null && inputProduced > 0)
                    totalWorkers += p.getNumberOfWorkers();
            }
            else
            {
                if(p.getHr().getMarket().getGoodType().equals(goodType))
                    totalWorkers += p.getNumberOfWorkers();

            }
        }
        return totalWorkers;


    }

    /**
     * Count all the workers at plants that consume (as input) a specific output
     * @param goodType the type of output
     * @return the total number of workers
     */
    public int getNumberOfWorkersWhoConsumedThisGoodThatDay(GoodType goodType, int day)
    {
        int totalWorkers = 0;
        for(Plant p : plants){
            if(!goodType.isLabor())
            {
                Integer inputProduced = p.getBlueprint().getInputs().get(goodType);
                if(inputProduced != null && inputProduced > 0)
                    totalWorkers += p.getObservationRecordedThisDay(PlantDataType.TOTAL_WORKERS, day);
            }
            else
            {
                if(p.getHr().getMarket().getGoodType().equals(goodType))
                    totalWorkers += p.getObservationRecordedThisDay(PlantDataType.TOTAL_WORKERS, day);

            }
        }
        return totalWorkers;


    }

    /**
     * get the latest observation of a sales department datum that updates itself
     * @param goodType the good being sold
     * @param salesDataType the kind of datum you are looking form
     * @return an observable (and so listeneable) object updating
     */
    public ObservableDoubleValue getLatestObservableObservation( GoodType goodType, SalesDataType salesDataType)
    {
        SalesDepartment department = getSalesDepartment(goodType);
        Preconditions.checkState(department != null);
        return department.getLatestObservationObservable(salesDataType);

    }

    /**
     * Checks if at least one plant that was producing a specific good had its production halted today because of missing inputs
     * @param goodType the type of good the plant should be producing
     * @return true if AT LEAST one firm had its production halted
     */
    public boolean wereThereMissingInputsInAtLeastOnePlant(GoodType goodType)
    {
        for(Plant p : plants){
            Integer outputProduced = p.getBlueprint().getOutputs().get(goodType);
            if(outputProduced != null && outputProduced > 0)
                if(p.getStatus().equals(PlantStatus.WAITING_FOR_INPUT))
                    return true;

        }
        return false;
    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        PurchasesDepartment department = purchaseDepartments.get(type);
        if(department == null)
            return 0;
        else
            return department.estimateDemandGap();

    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public float estimateSupplyGap(GoodType type) {
        SalesDepartment department = salesDepartments.get(type);
        if(department == null)
            return 0;
        else
            return department.estimateSupplyGap();
    }

    /**
     * get a view of all the hrs in the firm
     */
    public Set<HumanResources> getHRs()
    {
        return humanResources.values();
    }

    /**
     * when was the last time a meaningful change in workers occurred
     *
     * @return the day or -1 if there are none
     */
    public int getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(GoodType goodType)
    {
        List<Plant> plants = getListOfPlantsProducingSpecificOutput(goodType);
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;
    }



    /**
     * when was the last time a meaningful change in workers occurred
     *
     * @return the day or -1 if there are none
     */
    public int getLatestDayWithMeaningfulWorkforceChangeInConsumingThisGood(GoodType goodType)
    {

        List<Plant> plants = getListOfPlantsUsingSpecificInput(goodType);
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;
    }

    /**
     * get all the days, sorted, of when each plant had meaningful changes in workers
     *
     * @return the day or -1 if there are none
     */
    public List<Integer> getAllDayWithMeaningfulWorkforceChangeInProducingThisGood(GoodType goodType)
    {
        Set<Integer> days = new HashSet<>();
        List<Plant> plants = getListOfPlantsProducingSpecificOutput(goodType);
        for(Plant p : plants)
        {
            days.addAll(p.getShockDays());
        }
        //now put it in a list and sort it
        List<Integer> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays);

        return sortedDays;
    }

    /**
     * get all the days, sorted, of when each plant had meaningful changes in workers
     *
     * @return the day or -1 if there are none
     */
    public List<Integer> getAllDayWithMeaningfulWorkforceChangeInConsumingThisGood(GoodType goodType)
    {
        Set<Integer> days = new HashSet<>();
        List<Plant> plants = getListOfPlantsUsingSpecificInput(goodType);
        for(Plant p : plants)
        {
            days.addAll(p.getShockDays());
        }
        //now put it in a list and sort it
        List<Integer> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays);

        return sortedDays;
    }


    public Collection<PurchasesDepartment> getPurchaseDepartments(){
        return Collections.unmodifiableCollection(purchaseDepartments.values());
    }

    /**
     * checks the model for what day it is
     * @return
     */
    public int getDay()
    {
        return (int) Math.round(model.getMainScheduleTime());
    }
}
