/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model;

import agents.Agent;
import agents.EconomicAgent;
import agents.HasInventory;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import goods.GoodType;
import goods.GoodTypeMasterList;
import model.scenario.Scenario;
import model.scenario.TestScenario;
import model.scenario.TripolistScenario;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.scheduler.PhaseScheduler;
import model.utilities.scheduler.Priority;
import model.utilities.scheduler.TrueRandomScheduler;
import org.jfree.data.time.Day;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Macro II, still have no clue what this is about.
 * User: Ernesto
 * Date: 7/5/12
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacroII extends SimState{


    /**
     * Add more diagnostics
     */
    final public static boolean SAFE_MODE = false;


    public MacroII(long seed) {
        super(seed);
        hasGUI = false;
        phaseScheduler = new TrueRandomScheduler(200000,random);
        scenario = new TestScenario(this);
        toTurnOffAtFinish = new HashSet<>();
        goodTypeMasterList = new GoodTypeMasterList();
    }

    /**
     * How many Mason time units are a week?
     */
    private float weekLength = 7;




    /**
     * How many weeks of data do sales departments store?
     */
    private static final int SALES_MEMORY_LENGTH = 5;

    /**
     *  The simple buyer search  default sample size
     */
    private int simpleBuyerSearchSize = 5;

    /**
     * The default sample size of an economic agent search for its opponents
     */
    private static final int OPPONENT_SEARCH_SIZE = 5;

    /**
     * How much do undercutters lower the best price they find?
     */
    private static final float UNDERCUT_REDUCTION = 0.01f;

    /**
     * How much do undercutters wait between one search and the next?
     */
    private static final  float UNDERCUT_SPEED = 20f;

    /**
     * When I have NO IDEA what the price should be, the price is just the cost + (1+cluelessDefaultMarkup).
     */
    private float cluelessDefaultMarkup = .20f;

    /**
     * How much should markups adapt in EverythingMustGoAdaptive?
     */
    private float markupIncreases = 0.01f;

    /**
     * What's the target success rate in EverythingMustGoAdaptive?
     */
    private static final  float MIN_SUCCESS_RATE = .95f;

    /**
     * The speed of peddling
     */
    private float peddlingSpeed = 20f;

    /**
     * The target inventory level for a fixed Inventory Control
     */
    private int fixedInventoryTarget = 6;

    /**
     * tells the world whether there is a gui or not
     */
    public static boolean hasGUI = false;

    private PhaseScheduler phaseScheduler;

    /**
     * any object in this set gets their turnOff called during finish()
     */
    private Set<Deactivatable> toTurnOffAtFinish;

    /**
     * This flag is set to true when hasStarted has been called. It will never be set to false
     */
    private boolean hasStarted = false;

    /**
     * the model's goodtype/sector masterlist. This way, as long as you know the code and a reference to the model you can get a reference to any goodtype
     */
    private final GoodTypeMasterList goodTypeMasterList;


    /********************
     * Parameters of ProfitCheckPlantControl
     *******************/
    private static final float PLANT_CONTROL_INITIAL_CAPACITY_RATIO_TARGETED_MEAN = .20f;

    private static final float PLANT_CONTROL_INITIAL_CAPACITY_RATIO_TARGETED_DEVIATION = .05f;

    private static final float PLANT_CONTROL_INITIAL_MARGINAL_RATIO_CHANGE_MEAN = .05f;

    private static final float PLANT_CONTROL_INITIAL_MARGINAL_RATIO_CHANGE_DEVIATION = .01f;

    private static final float PLANT_CONTROL_SPEED_MEAN = 20f;

    private static final float PLANT_CONTROL_SPEED_DEVIATION = 5f;

    private HashMap<GoodType,Market> markets;

    /**
     * The scenario to use
     */
    private Scenario scenario;

    /**
     * the list of ALL agents. Notice it's an arraylist so it's faster to shuffle
     */
    private final ArrayList<EconomicAgent> agents = new ArrayList<>();;


    public void registerCashDelivery(EconomicAgent economicAgent, EconomicAgent receiver, long money) {
    }

    public void registerInventoryDelivery(HasInventory sender, HasInventory economicAgent, GoodType type) {
    }

    public static AtomicLong counter = new AtomicLong();

    /**
     * @return the value for the field counter.
     */
    public static long getCounter() {
        return counter.incrementAndGet();
    }


    /**
     * @return the value for the field weekLength.
     */
    public float getWeekLength() {
        return weekLength;
    }

    /**
     * @return the value for the field SALES_MEMORY_LENGTH.
     */
    public int getSalesMemoryLength() {
        return SALES_MEMORY_LENGTH;
    }


    @Override
    public void start() {
        super.start();



        //if there is no scenario, create one!
        assert scenario != null;

        //let the scenario do all the initialization
        scenario.start();

        //get the agents set
        markets = scenario.getMarkets();
        //for all goodtypes that weren't registerd before, register them now
        goodTypeMasterList.addNewSectors(markets.keySet());


        agents.addAll(scenario.getAgents());
        scenario.getAgents().clear();

        //go through all the agents and call their start!
        for(Agent a : agents)
            a.start(this);

        for(Market m : markets.values())
            m.start(this);

        //schedule weekends for everyone
        scheduleAnotherDay(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState state) {
                weekEnd(); //everybody is on weekend!
            }
        }, 7);
        //schedule new days actions for everyone
        scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {
                newDay();
            }
        });

        //get the phase scheduler to start
        schedule.scheduleOnce(0,phaseScheduler);

        hasStarted=true;


    }


    /**
     * Calls new day statistics for all markets
     */
    public void newDay(){

        for(Market market : markets.values())
            market.collectDayStatistics();
        scheduleTomorrow(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {
                newDay();
            }
        });

    }

    /**
     * Use this to draw a new random simple search department search size
     */
    public int drawNewSimpleBuyerSearchSize(EconomicAgent f, Market market){
        return simpleBuyerSearchSize; //TODO make this random

    }


    public int drawNewSimpleSellerSearchSize(EconomicAgent f, Market market){
        return simpleBuyerSearchSize; //TODO make this random

    }


    /**
     * The target inventory level for a fixed Inventory Control
     */
    public int drawFixedInventoryTarget() {
        return fixedInventoryTarget;
    }

    /**
     * Use this to draw a new  sample size of an economic agent search for its opponents
     */
    public int drawNewOpponentSearchSize(EconomicAgent a, Market market)
    {
        return OPPONENT_SEARCH_SIZE;
    }

    /**
     * Use this to draw the price cut (in percentage) that an undercuttingPricing attempts to gather market share
     */
    public float drawNewUndercutReduction(EconomicAgent a, Market market)
    {
        return UNDERCUT_REDUCTION;
    }

    /**
     * Use this to draw the price cut (in percentage) that an undercuttingPricing attempts to gather market share
     */
    public float drawNewUndercutSpeed(EconomicAgent a, Market market)
    {
        return UNDERCUT_SPEED;
    }


    /**
     * @return the value for the field cluelessDefaultMarkup.
     */
    public float getCluelessDefaultMarkup() {
        return cluelessDefaultMarkup;
    }

    public static void main(String[] args){

        MacroII macroII = new MacroII(System.currentTimeMillis());
        TripolistScenario scenario1 = new TripolistScenario(macroII);
        scenario1.setAdditionalCompetitors(4);
        scenario1.setFixedPayStructure(true);
        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);

        macroII.start();
        while(macroII.schedule.getTime()<300000)
            macroII.schedule.step(macroII);

    }

    /**
     * Translate the "time" of the schedule into a Date object
     */
    public Date getCurrentSimulationTime(){


        return new Date( getCurrentSimulationTimeInMillis());



    }


    /**
     * Translate the "time" of the schedule into a Date object
     */
    public Day getCurrentSimulationDay(){


        return new Day(getCurrentSimulationTime());



    }

    /**
     * Translate the "time" of the schedule into a Date object
     */
    public long getCurrentSimulationTimeInMillis(){


        double time =  getMainScheduleTime();




        return  Math.round(time * (1000*60*60*24d));

    }


    public void weekEnd(){

        //    Collections.shuffle(agents,new Random(random.nextLong())); //todo make this shuffled by the MersenneTwisterFast

        //agents on weekend!
        for(EconomicAgent agent : agents)
            agent.weekEnd(schedule.getTime());
        for(Market market : markets.values())
            market.weekEnd(this);


        //   printOutWorkers();
        scheduleAnotherDay(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState state) {
                weekEnd();
            }
        }, 7);
    }
    /**
     * @return the value for the field MIN_SUCCESS_RATE.
     */
    public float getMinSuccessRate() {
        return MIN_SUCCESS_RATE;
    }

    /**
     * @return the value for the field markupIncreases.
     */
    public float getMarkupIncreases() {
        return markupIncreases;
    }

    public float getPeddlingSpeed() {
        return 0;
    }


    public void setCluelessDefaultMarkup(float cluelessDefaultMarkup) {
        this.cluelessDefaultMarkup = cluelessDefaultMarkup;
    }

    public void setMarkupIncreases(float markupIncreases) {
        this.markupIncreases = markupIncreases;
    }

    public float randomGain(){
        return (float) (random.nextGaussian() * .1 + 1);

    }

    public void setFixedInventoryTarget(int fixedInventoryTarget) {
        this.fixedInventoryTarget = fixedInventoryTarget;
    }


    public float drawPlantControlInitialMarginalRatioChange(){

        return (float) (PLANT_CONTROL_INITIAL_MARGINAL_RATIO_CHANGE_MEAN + random.nextGaussian()* PLANT_CONTROL_INITIAL_MARGINAL_RATIO_CHANGE_DEVIATION);
    }

    public float drawplantControlInitialCapacityRatioTargeted(){

        return (float) (PLANT_CONTROL_INITIAL_CAPACITY_RATIO_TARGETED_MEAN + random.nextGaussian()* PLANT_CONTROL_INITIAL_CAPACITY_RATIO_TARGETED_DEVIATION);
    }

    public int drawplantControlSpeed(){

        return 0;
    }


    public float drawProportionalGain(){
        return (float) (.5f + random.nextGaussian()*.01f);
    }


    public float drawIntegrativeGain(){
        return (float) (.5f + random.nextGaussian()*.05f);
    }


    public float drawDerivativeGain(){
        return (float) Math.abs((.0001f + random.nextGaussian()*.0005f));
    }

    public int drawPIDSpeed(){
        return 0;
    }

    /**
     * Returns the list of markets, that's all.
     */
    public Collection<Market> getMarkets() {
        return markets.values();
    }

    /**
     * Returns a specific market, if it exists!
     */
    public Market getMarket(GoodType goodType){
        return markets.get(goodType);

    }

    /**
     * Does this simulation run with gui?
     */
    public static boolean hasGUI(){
        return hasGUI;
    }

    /**
     * this is the GUI controller, it's a static reference which is used by the exchange network
     */
    private static MacroIIGUI gui;

    /**
     * register the gui and the controller
     * @param gui
     */
    public void registerGUI(MacroIIGUI gui){
        MacroII.gui = gui;
        hasGUI = true;

        //this is temporary so that the reviewers can duplicate correctly my results with GUI without compiling the code on their own
        //and calling all the various scattered mains
        if(phaseScheduler instanceof TrueRandomScheduler)
            ((TrueRandomScheduler)phaseScheduler).setSimulationDays(5000);
    }

    /**
     * get the GUI controller!
     */
    public static MacroIIGUI getGUI() {
        assert hasGUI; //we must have gui to even get called!
        return gui;
    }


    /**
     * Turn off method, clear all data and all registered objects to clear
     */
    @Override
    public void finish() {
        super.finish();

        for(Market m : markets.values())
            m.turnOff();

        markets.clear();
        markets = null;
        Collection<EconomicAgent> copy = new LinkedList<>(agents); //make a copy to avoid concurrent modification
        for(EconomicAgent a : copy)
        {
            a.turnOff();
        }
        agents.clear();

        phaseScheduler.clear();


        //turn off when needed
        for(Deactivatable d :toTurnOffAtFinish)
            d.turnOff();
        toTurnOffAtFinish.clear();

        goodTypeMasterList.turnOff();

        scenario = null;
        hasStarted = false;


    }


    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /**
     * Just draw random gaussian sd=1
     */
    public double drawRandomSchedulingNoise(){

        return random.nextGaussian();

    }

    /**
     * Will add this agent to the masterlist. It will also schedule a start for the agent next dawn it if the model has already started
     * @param a the new agent
     */
    public void addAgent(final EconomicAgent a)
    {

        agents.add(a);
        if(hasStarted)
        {
            final MacroII reference = this;
            scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
                    a.start(reference);
                }
            },Priority.BEFORE_STANDARD);
        }
    }


    /**
     * remove an agent from the masterlist
     */
    public void removeAgent(final EconomicAgent a)
    {

        agents.remove(a);

    }


    public void setSimpleSellerSearchSize(int simpleSellerSearchSize) {
    }

    public void setSimpleBuyerSearchSize(int simpleBuyerSearchSize) {
        this.simpleBuyerSearchSize = simpleBuyerSearchSize;
    }

    public MersenneTwisterFast getRandom() {
        return random;
    }

    /**
     * schedule the event to happen when the next specific phase comes up!
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    public void scheduleSoon(ActionOrder phase, Steppable action) {
        phaseScheduler.scheduleSoon(phase, action);
    }

    /**
     * schedule an action to take place in the current phase, regardless of what it is
     * @param action the action to be performed
     */
    public void scheduleASAP(Steppable action){
        phaseScheduler.scheduleSoon(phaseScheduler.getCurrentPhase(), action);

    }

    /**
     * Schedule tomorrow assuming the phase passed is EXACTLY the current phase
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param priority the action priority
     */
    public void scheduleTomorrow(ActionOrder phase, Steppable action, Priority priority) {
        phaseScheduler.scheduleTomorrow(phase, action, priority);
    }

    /**
     * Schedule as soon as this phase occurs
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param priority the action priority
     *
     */
    public void scheduleSoon( ActionOrder phase,  Steppable action, Priority priority) {
        phaseScheduler.scheduleSoon(phase, action, priority);
    }

    /**
     * Schedule in as many days as passed (at priority standard)
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param daysAway how many days into the future should this happen
     * @param priority the action priority
     */
    public void scheduleAnotherDay( ActionOrder phase,  Steppable action, int daysAway, Priority priority) {
        phaseScheduler.scheduleAnotherDay(phase, action, daysAway, priority);
    }

    /**
     * @param probability each day we check against this fixed probability to know if we will step on this action today
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param
     */
    public void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action, float probability, Priority priority) {
        phaseScheduler.scheduleAnotherDayWithFixedProbability(phase, action, probability, priority);
    }

    /**
     *
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param daysAway how many days from now should it be scheduled
     */
    public void scheduleAnotherDay( ActionOrder phase,  Steppable action, int daysAway) {
        phaseScheduler.scheduleAnotherDay(phase, action, daysAway);
    }



    /**
     * force the schedule to record this action to happen tomorrow. This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    public void scheduleTomorrow(ActionOrder phase, Steppable action) {
        phaseScheduler.scheduleTomorrow(phase, action);
    }

    /**
     * Getter mostly useful for testing. Use delegate methods instead
     * @return
     */
    public PhaseScheduler getPhaseScheduler() {
        return phaseScheduler;
    }

    public ActionOrder getCurrentPhase() {
        return phaseScheduler.getCurrentPhase();
    }


    public void setMarkets(HashMap<GoodType, Market> markets) {
        this.markets = markets;
    }

    public ArrayList<EconomicAgent> getAgents() {
        return agents;
    }

    public void setWeekLength(float weekLength) {
        this.weekLength = weekLength;
    }

    /**
     * This is similar to scheduleAnotherDay except that rather than passing a fixed number of days we pass the probability
     * of the event being scheduled each day after the first (days away is always at least one!)
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param probability the daily probability of this action happening. So if you pass 15% then each day has a probability of 15% of triggering this action
     */
    public void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action, float probability) {
        phaseScheduler.scheduleAnotherDayWithFixedProbability(phase, action, probability);
    }

    /**
     * change the scheduler of the model. Probably not a good idea unless testing
     * @param phaseScheduler
     */
    public void setPhaseScheduler(PhaseScheduler phaseScheduler) {
        this.phaseScheduler = phaseScheduler;
    }


    /**
     * when this is called, the argument goes in a set of Deactivable and when MacroII calls finish() then turnOff will be called for all elements in that list
     */
    public void registerDeactivable(Deactivatable d)
    {
        toTurnOffAtFinish.add(d);
    }




    /**
     * utility method for schedule.getTime()
     * @return
     */
    public double getMainScheduleTime() {
        return schedule.getTime();
    }

    public boolean hasStarted() {
        return hasStarted;
    }


    public GoodTypeMasterList getGoodTypeMasterList() {
        return goodTypeMasterList;
    }

    /**
     * call this only if the model has started (otherwise just add it to the scenario list)
     * @param market the new mark
     */
    public void addMarket(Market market) {
        Preconditions.checkState(hasStarted(), "only call this method after the model has started");
        Preconditions.checkState(!markets.containsKey(market.getGoodType()),"Already has a market for this good type!");
        markets.put(market.getGoodType(),market);
        scheduleSoon(ActionOrder.DAWN, state -> market.start(this),Priority.BEFORE_STANDARD);

    }
}
