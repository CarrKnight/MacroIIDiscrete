package model;

import agents.EconomicAgent;
import agents.HasInventory;
import agents.Person;
import agents.firm.Firm;
import ec.util.MersenneTwisterFast;
import financial.Market;
import goods.GoodType;
import model.scenario.Scenario;
import model.scenario.TestScenario;
import model.scenario.TripolistScenario;
import model.utilities.ActionOrder;
import model.utilities.PhaseScheduler;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.media.chart.HistogramGenerator;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    final public static boolean SAFE_MODE = true;


    public MacroII(long seed) {
        super(seed);
        hasGUI = false;
        phaseScheduler = new PhaseScheduler(20000);
        scenario = new TestScenario(this);
    }

    /**
     * How many Mason time units are a week?
     */
    private float weekLength = 100;

    /**
     * How many weeks passed since we started the model?
     */
    private int weeksPassed = 0;

    /**
     * This is just how many milliseconds are there in a week
     */
    private static long MILLISECONDS_IN_A_WEEK = 604800000l;


    /**
     * How many weeks of data do sales departments store?
     */
    private int salesMemoryLength = 5;

    /**
     *  The simple buyer search  default sample size
     */
    private int simpleBuyerSearchSize = 5;

    /**
     *  The simple seller search  default sample size
     */
    private int simpleSellerSearchSize = 5;

    /**
     * The default sample size of an economic agent search for its opponents
     */
    private int opponentSearchSize = 5;

    /**
     * How much do undercutters lower the best price they find?
     */
    private float undercutReduction = 0.01f;

    /**
     * How much do undercutters wait between one search and the next?
     */
    private float undercutSpeed = 20f;

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
    private float minSuccessRate = .95f;

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
    private static boolean hasGUI = false;

    private final PhaseScheduler phaseScheduler;


    /********************
     * Parameters of ProfitCheckPlantControl
     *******************/
    private float plantControlInitialCapacityRatioTargetedMean = .20f;

    private float plantControlInitialCapacityRatioTargetedDeviation = .05f;

    private float plantControlInitialMarginalRatioChangeMean = .05f;

    private float plantControlInitialMarginalRatioChangeDeviation = .01f;

    private float plantControlSpeedMean = 20f;

    private float plantControlSpeedDeviation = 5f;

    private EnumMap<GoodType,Market> markets;

    /**
     * The scenario to use
     */
    private Scenario scenario;

    /**
     * the list of ALL agents. Notice it's an arraylist so it's faster to shuffle
     */
    private ArrayList<EconomicAgent> agents;


    public void registerCashDelivery(EconomicAgent economicAgent, EconomicAgent receiver, long money) {
    }

    public void registerInventoryDelivery(HasInventory sender, HasInventory economicAgent, GoodType type) {
    }

    public static int counter = 0;

    /**
     * @return the value for the field counter.
     */
    public static int getCounter() {
        return counter++;
    }


    /**
     * @return the value for the field weekLength.
     */
    public float getWeekLength() {
        return weekLength;
    }

    /**
     * @return the value for the field salesMemoryLength.
     */
    public int getSalesMemoryLength() {
        return salesMemoryLength;
    }


    @Override
    public void start() {
        super.start();
        //make sure counters are at 0
        weeksPassed = 0;
        counter = 0;

        //if there is no scenario, create one!
        assert scenario != null;

        //let the scenario do all the initialization
        scenario.start();

        //get the agents set
        markets = scenario.getMarkets();
        agents = scenario.getAgents();

        //schedule weekends for everyone
        schedule.scheduleRepeating(weekLength,0,new Steppable() {
            @Override
            public void step(SimState simState) {
                weekEnd(); //everybody is on weekend!
            }
        },weekLength);

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
        return opponentSearchSize;
    }

    /**
     * Use this to draw the price cut (in percentage) that an undercuttingPricing attempts to gather market share
     */
    public float drawNewUndercutReduction(EconomicAgent a, Market market)
    {
        return undercutReduction;
    }

    /**
     * Use this to draw the price cut (in percentage) that an undercuttingPricing attempts to gather market share
     */
    public float drawNewUndercutSpeed(EconomicAgent a, Market market)
    {
        return undercutSpeed;
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
    public long getCurrentSimulationTimeInMillis(){


        long time = (long) schedule.getTime(); //get the time


        if(time <= 0 && Market.TESTING_MODE ) //in tests if schedule is not initiated it will be <0, so floor it
            time = 1;
//        assert time >= 0: time;

        long weeks = (long) (time / weekLength); //get how many weeks have passed
        assert weeks == weeksPassed || Market.TESTING_MODE ;

        float remainder =  (time % weekLength);
//        assert remainder>=0;
        long remainderInMilliseconds = (long) (((remainder)/weekLength) *   MILLISECONDS_IN_A_WEEK);
        assert remainderInMilliseconds <= MILLISECONDS_IN_A_WEEK;
//        assert remainderInMilliseconds >= 0;


        return  weeks * MILLISECONDS_IN_A_WEEK + remainderInMilliseconds;

    }


    public void weekEnd(){
        weeksPassed++;

        Collections.shuffle(agents); //todo make this shuffled by the MersenneTwisterFast

        //agents on weekend!
        for(EconomicAgent agent : agents)
            agent.weekEnd(schedule.getTime());
        for(Market market : markets.values())
            market.weekEnd(this);

        printOutWorkers();
    }

    /**
     * How many weeks have passed in the model?
     */
    public int getWeeksPassed() {
        return weeksPassed;
    }

    /**
     * @return the value for the field minSuccessRate.
     */
    public float getMinSuccessRate() {
        return minSuccessRate;
    }

    /**
     * @return the value for the field markupIncreases.
     */
    public float getMarkupIncreases() {
        return markupIncreases;
    }

    public float getPeddlingSpeed() {
        return (float) (peddlingSpeed *random.nextGaussian());
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

        return (float) (plantControlInitialMarginalRatioChangeMean + random.nextGaussian()*plantControlInitialMarginalRatioChangeDeviation);
    }

    public float drawplantControlInitialCapacityRatioTargeted(){

        return (float) (plantControlInitialCapacityRatioTargetedMean + random.nextGaussian()*plantControlInitialCapacityRatioTargetedDeviation);
    }

    public float drawplantControlSpeed(){

        return (float) (plantControlSpeedMean + random.nextGaussian()*plantControlSpeedDeviation);
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

    public float drawPIDSpeed(){
        return (float) (10 + random.nextGaussian()*.5f);
    }

    /**
     * Returns the list of markets, that's all.
     */
    protected Collection<Market> getMarkets() {
        return markets.values();
    }

    /**
     * Returns a specific market, if it exists!
     */
    @Nullable
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
    private static GUIState gui;

    /**
     * register the gui and the controller
     * @param gui
     */
    public void registerGUI(MacroIIGUI gui){
        MacroII.gui = gui;
        hasGUI = true;
    }

    /**
     * get the GUI controller!
     */
    public static GUIState getGUI() {
        assert hasGUI; //we must have gui to even get called!
        return gui;
    }


    /**
     * Turn off method, clear all data
     */
    @Override
    public void finish() {

        markets.clear();
        markets = null;
        for(EconomicAgent a : agents)
            a.setActive(false);
        agents.clear();
        agents = null;


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
     * An inspector with histograms of wealth and so on. It sets itself to adjust periodically
     * @return a
     */
    public Inspector buildDistributionInspector()
    {
        assert hasGUI();

        //this we will return
        TabbedInspector mainInspector = new TabbedInspector();

        //wealth holding non-firm cash holdings!
        final HistogramGenerator peopleWealth = new HistogramGenerator();

        //add the name to the series
        peopleWealth.addSeries(null, 50, "cash held by non-firms", null);
        //create the inspector holding it
        Inspector inspector = new Inspector() {
            @Override
            public void updateInspector() {
                peopleWealth.update();
            }
        };
        //set the new layout and put it in
        inspector.setLayout(new BorderLayout());
        inspector.add(peopleWealth.getChartPanel());
        //add the inspector as a tab!
        mainInspector.addInspector(inspector,"people's cash");




        //wealth holding everyone's cash holdings!
        final HistogramGenerator agentWealth = new HistogramGenerator();

        //add the name to the series
        agentWealth.addSeries(null, 50, "cash held by agents", null);
        //create the inspector holding it
        inspector = new Inspector() {
            @Override
            public void updateInspector() {
                agentWealth.update();
            }
        };
        //set the new layout and put it in
        inspector.setLayout(new BorderLayout());
        inspector.add(agentWealth.getChartPanel());
        //add the inspector as a tab!
        mainInspector.addInspector(inspector,"cash held by agents");


        schedule.scheduleOnceIn(weekLength/3,new Steppable() {
            @Override
            public void step(SimState simState) {
                List<Long> peopleCash = new ArrayList<>(agents.size());
                List<Long> agentCash = new ArrayList<>(agents.size());
                //loop through
                for(EconomicAgent agent : agents)
                {
                    if(agent instanceof Person)
                        peopleCash.add(agent.getCash());
                    agentCash.add(agent.getCash());
                }
                //now loop again to create arrays (stupid JFreeChart)
                final double[] people = new double[peopleCash.size()]; int i=0;
                for(Long l : peopleCash) //cycle
                {
                    people[i] = l;
                    i++;
                }

                final double[] agents = new double[peopleCash.size()]; i=0;
                for(Long l : agentCash) //cycle
                {
                    agents[i] = l;
                    i++;
                }

                //finally put them in. Make sure it's done in the swing thread
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        peopleWealth.updateSeries(0,people );
                        agentWealth.updateSeries(0,agents);
                    }
                });

                //and now reschedule
                schedule.scheduleOnceIn(weekLength/3,this);

            }
        });

        return mainInspector;




    }

    public void printOutWorkers()
    {
        StringBuilder builder = new StringBuilder();
        int sum=0;
        //sort it so they are always in the right order
        LinkedList<EconomicAgent> agents = new LinkedList(this.agents);
        Collections.sort(agents, new Comparator<EconomicAgent>() {
            @Override
            public int compare(EconomicAgent o1, EconomicAgent o2) {
                return Integer.compare(o1.hashCode(),o2.hashCode());

            }
        });
        for(EconomicAgent a : agents)
        {
            try{
            if( a instanceof Firm && ((Firm) a).hasPlants())
            {
                int workers =  ((Firm) a).getRandomPlantProducingThis(GoodType.GENERIC).workerSize();
                sum += workers;
                builder.append( Integer.toString(workers)).append(",");

            }
            }
            catch (Exception ignored){}
        }

        builder.append(sum);
        System.out.println(builder.toString());

    }


    public void setSimpleSellerSearchSize(int simpleSellerSearchSize) {
        this.simpleSellerSearchSize = simpleSellerSearchSize;
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
     * force the schedule to record this action to happen tomorrow. This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    public void scheduleTomorrow(ActionOrder phase, Steppable action) {
        phaseScheduler.scheduleTomorrow(phase, action);
    }

    public ActionOrder getCurrentPhase() {
        return phaseScheduler.getCurrentPhase();
    }
}
