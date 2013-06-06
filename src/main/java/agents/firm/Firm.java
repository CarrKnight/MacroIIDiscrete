/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.owner.DividendStrategy;
import agents.firm.owner.NoDividendStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ec.util.MersenneTwisterFast;
import financial.MarketEvents;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import financial.utilities.TimelineManager;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.PlantStatus;
import lifelines.LifelinesPanel;
import lifelines.data.DataManager;
import lifelines.data.GlobalEventData;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/12/12
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Firm extends EconomicAgent {


    private final EnumMap<GoodType,SalesDepartment> salesDepartments;

    /**
     * The name, if any, of the firm
     */
    private String name = null;

    private final EnumMap<GoodType,PurchasesDepartment> purchaseDepartments;

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
    private long bestWageOffer;

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

        salesDepartments = new EnumMap<>(GoodType.class);
        purchaseDepartments = new EnumMap<>(GoodType.class);
        humanResources = HashBiMap.create(1); //factory method for this
        numberOfPlantsListeners = new HashSet<>(); //prepare for listeners
        profitReport = new ProfitReport(this);
        //create the timeline manager

        if(MacroII.hasGUI()){
            //create the panel holding the timeline
            GlobalEventData.getInstance().reset();

            //the record panel where we will hold the gui
            recordPanel = new LifelinesPanel(null,new Dimension(500,500));
            //the records where will hold the lifelines data
            records = new TimelineManager(recordPanel);
            //build the inspector
            buildInspector();

            //register profit report
            addAgentToLog(profitReport);
        }

    }

    /**
     * alternative constructor, NEVER builds panel/inspector
     * @param model
     */
    public Firm(MacroII model,boolean ignored) {
        super(model);

        salesDepartments = new EnumMap<>(GoodType.class);
        purchaseDepartments = new EnumMap<>(GoodType.class);
        humanResources = HashBiMap.create(1); //factory method for this
        numberOfPlantsListeners = new HashSet<>(); //prepare for listeners
        profitReport = new ProfitReport(this);
        //create the timeline manager

        firmInspector = null;

    }


    /**
     * Add a new plant creation listener to the list of people to be notified when a new plant opens or closes
     */
    public void addPlantCreationListener(@Nonnull NumberOfPlantsListener listener)
    {
        //addSalesDepartmentListener to the list
        numberOfPlantsListeners.add(listener);

    }

    /**
     * Assign a specific human resources object to the plant. If an old hr was working, turn it off first
     * @param p the plant to be controlled by HR
     * @param hr the human resources object
     */
    public void registerHumanResources(Plant p, HumanResources hr){
        assert plants.contains(p);
        HumanResources oldHR = humanResources.remove(p);
        assert hr !=oldHR : "you are registering the same hr department twice";

        //turn off the old HR if necessary
        if(oldHR != null)
            oldHR.turnOff();
        //log it
        addAgentToLog(hr);

        //put it in.
        humanResources.put(p,hr);


    }

    /**
     * Remove the creation listener to the list of people to be notified when a new plant opens or closes
     */
    public boolean removePlantCreationListener(@Nonnull NumberOfPlantsListener listener)
    {
        //addSalesDepartmentListener to the list
        return numberOfPlantsListeners.remove(listener);
    }

    /**
     * Add a new plant to this firm's management
     * @param p the new plant
     */
    public void addPlant(@Nonnull Plant p)
    {
        plants.add(p);
        for(NumberOfPlantsListener l : numberOfPlantsListeners)
            l.plantCreatedEvent(this,p);

        //log it
        addAgentToLog(p);

        //if it's ready start it!
        if(p.getStatus() == PlantStatus.READY)
            //schedule yourself for production
            model.scheduleSoon(ActionOrder.PRODUCTION, p);

    }

    /**
     * Remove the plant from this firm's management
     * @param p the old plant
     */
    public void removePlant(@Nonnull Plant p)
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
    public List<Plant> getListOfPlantsUsingSpecificInput(@Nonnull GoodType type)
    {

        if(plants.isEmpty())
            return Collections.emptyList();
        else
        {
            List<Plant> toReturn = new LinkedList<>();
            for(Plant p : plants)
            {
                Integer i = p.getBlueprint().getInputs().get(type);
                if(i != null && i > 0)
                    toReturn.add(p);
            }


            return toReturn;
        }

    }

    /**
     * Returns a list of all plants producing a specific output
     * @param type the type of good the plants use as inputs. (if none found it returns an empty list)
     */
    public List<Plant> getListOfPlantsProducingSpecificOutput(@Nonnull GoodType type)
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
     * Whenever the plant produces a good, it asks the firm what to do about it!
     * @param g  the good that was produced!
     */
    public void reactToPlantProduction(Good g){
        salesDepartments.get(g.getType()).sellThis(g);
    }


    @Override
    public MersenneTwisterFast getRandom() {
        return model.random;
    }

    @Override
    public void consumeAll() {
        throw new UnsupportedOperationException("Still to do"); //TODO do this
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
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        assert !g.getType().isLabor(); //hopefull you aren't selling labor, or this is weird.
        assert salesDepartments.containsKey(g.getType());       //you should have a sales department for it
        salesDepartments.get(g.getType()).reactToFilledQuote(g,price,buyer);  //tell the sales department that the deed is done!
    }

    @Override
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
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
    public long maximumOffer(Good g) {

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
                long offer = hr.maximumOffer(g);
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
    public long expectedInputPrice(GoodType g) {
        //get the right department
        PurchasesDepartment department = purchaseDepartments.get(g);
        assert department != null;

        //ask the department how much you are willing to pay for that!
        return department.maxPrice(g,department.getMarket());
    }



    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long askedForABuyOffer(GoodType t) {
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
    @Nonnull
    public SalesDepartment getSalesDepartment(@Nonnull GoodType good) {
        return salesDepartments.get(good);
    }

    /**
     * This is called to addSalesDepartmentListener a sales department as part of this firm. Makes sure there is only one for each market
     */
    public void registerSaleDepartment(SalesDepartment newSales, GoodType type){
        assert !salesDepartments.containsKey(type);
        //log it
        addAgentToLog(newSales);
        //add it
        salesDepartments.put(type,newSales);



    }


    /**
     * This is called to addSalesDepartmentListener a sales department as part of this firm. Makes sure there is only one for each market
     */
    public void registerPurchasesDepartment(PurchasesDepartment newPurchases, GoodType type){
        assert !purchaseDepartments.containsKey(type);
        //log it
        addAgentToLog(newPurchases);
        //add it
        purchaseDepartments.put(type,newPurchases);



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
            pDept.weekEnd();


        //weekend for sales departments
        for(SalesDepartment sDept : salesDepartments.values())
            sDept.weekEnd();

        //weekend for human resources
        for(HumanResources hr : humanResources.values())
            hr.weekEnd();

        //profit report!
        profitReport.weekEnd();

        //pay out your enormous dividends
        if(owner != null) //if there is an owner pay dividends, otherwise who cares
            dividendStrategy.payDividends(profitReport.getAggregateProfits(),this,owner);

        //if you have GUI do update it
        if(MacroII.hasGUI() && records != null) //the non-null check is there for dummy buyers/sellers
            records.weekEnd();

    }

    /**
     * It'll probably have to be assigned to a plant by react to quote
     * @param p
     */
    public void hire(@Nonnull Person p, @Nullable Department department){


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
        if(!workerFound)
            throw new IllegalArgumentException("Worker not found!");
    }


    /**
     * The firm turns off all its departments and plants and clears all its data structures
     */
    public void turnOff(){
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
    public void start(){
        for(PurchasesDepartment dept : purchaseDepartments.values())
            dept.start();
        for(HumanResources hr : humanResources.values())
            hr.start();

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



    /**
     * the JPanel containing the lifeline and all its utilities.
     */
    private LifelinesPanel recordPanel;

    /**
     * the manager containing timeline data.
     */
    private TimelineManager records;

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



        /****************************************************
         * Timeline
         ***************************************************/


        // marketRecord = new Record(toString());
        Inspector firmTimelineInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
            }
        };
        firmTimelineInspector.setLayout(new BorderLayout());
        //register the events globally


        //get the data manager for records so we can build a jpanel that displays it
        DataManager recordManager = records.getRecordManager();
        assert recordManager != null;
        //register all possible events names
        GlobalEventData.getInstance().reset();
        for(MarketEvents event : MarketEvents.values()){
            GlobalEventData.getInstance().registerEventName(event.name());
        }
        for(MarketEvents event : MarketEvents.values()){
            recordManager.addEventName(event.ordinal());
        }



        //initialize teh panel
        recordPanel.openData(recordManager);
        //add the panel to the inspector
        firmTimelineInspector.add(recordPanel);
        //add it as a tab!
        firmInspector.addInspector(firmTimelineInspector,"Timeline");



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
        if(MacroII.hasGUI())
            records.event(agent, action, time, annotations);
    }

    /**
     * If an logEvent worth to be put in the timeline occurred, call this!
     * @param agent the one who performed the logEvent
     * @param action the action that has occurred
     * @param time the "real" time when this occurred
     */
    public void logEvent(Object agent, MarketEvents action, long time) {
        if(MacroII.hasGUI())
            records.event(agent, action, time);
    }

    /**
     * When a (possibly) new agent enters the market call this to create a record for him.
     * It's not acceptable to add an logEvent for an agent that wasn't added
     */
    public void addAgentToLog(Object newAgent) {
        if(MacroII.hasGUI())
            records.addAgent(newAgent);
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
    public PurchasesDepartment getPurchaseDepartment(@Nonnull GoodType goodType) {
        return purchaseDepartments.get(goodType);
    }


    /**
     * An horrible utility method, returns a plant owned by this firm that produces a specific good. It throws an IllegalArgument exception if there is no plant producing this
     */
    @Nonnull
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
     * @param profitReport the new profit report.
     */
    public void setProfitReport(ProfitReport profitReport) {
        this.profitReport = profitReport;
    }

    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    public PurchaseResult shopHere(@Nonnull Quote buyerQuote,@Nonnull Quote sellerQuote)
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
            totalWorkers += p.workerSize();
        return totalWorkers;
    }




    /**
     * Count all the workers at plants that produce a specific output
     * @param goodType the type of output
     * @return the total number of workers
     */
    public int getTotalWorkersWhoProduceThisGood(GoodType goodType)
    {
        int totalWorkers = 0;
        for(Plant p : plants){
            Integer outputProduced = p.getBlueprint().getOutputs().get(goodType);
            if(outputProduced != null && outputProduced > 0)
                totalWorkers += p.workerSize();
        }
        return totalWorkers;


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

}
