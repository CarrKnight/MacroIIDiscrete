/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production;

import agents.*;
import agents.firm.Department;
import agents.firm.Firm;
import agents.firm.cost.PlantCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.technology.Machinery;
import ec.util.MersenneTwisterFast;
import financial.MarketEvents;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.PlantData;
import model.utilities.stats.collectors.enums.PlantDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> This is the class representing a manufacturing/mining department able to completeProductionRunNow a set of goods at a specific rate
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
public class Plant implements Department, Steppable, Deactivatable, InventoryListener {


    /**
     * Minimal constructor
     */
    public Plant(@Nonnull Blueprint blueprint,@Nonnull Firm owner) {
        this(blueprint,owner,1,100,0,100000);
    }


    public Plant(@Nonnull Blueprint blueprint,
                 @Nonnull Firm owner, int minWorkers, int maxWorkers, long buildingCosts, long usefulLife) {
        this.blueprint = blueprint;
        this.owner = owner;
        this.minWorkers = minWorkers;
        this.maxWorkers = maxWorkers;
        this.buildingCosts = buildingCosts;
        this.usefulLife = usefulLife;
        this.listeners = new LinkedHashSet<>();
        this.model = owner.getModel();
        this.dataStorage = new PlantData();

        //add yourself as an inventory listener AND log
        owner.addInventoryListener(this);
        owner.addAgentToLog(this);



    }



    private final MacroII model;


    /**
     * The minimum number of workers needed to keep the plant working
     */
    protected int minWorkers = 1;


    /**
     * The max number of workers that can be employed in this firm
     */
    protected int maxWorkers = 100;


    /**
     * The owner of the plant. It should get notified of all productions
     */
    final protected Firm owner;

    /**
     * The blueprint explaining what is produced and what is needed.
     */
    protected Blueprint blueprint;

    /**
     * People working at the plant
     */
    final protected LinkedList<Person> workers = new LinkedList<>();

    /**
     * What's the status of the plant?
     */
    protected PlantStatus status = PlantStatus.READY;

    /**
     * The technology used by the firm to completeProductionRunNow whatever the blueprint says.
     */

    protected Machinery plantMachinery;

    /**
     * Fixed costs due to having built the plant
     */
    protected long buildingCosts=0;

    /**
     * How many weeks is this plant going to live, from creation
     */
    protected long usefulLife=10000;

    /**
     * How many weeks has this plant lived as of now
     */
    protected long age = 0;

    /**
     * How much money has been spent this week on inputs
     */
    private long thisWeekInputCosts = 0;
    /**
     * How much money was spent last week on inputs
     */
    private long lastWeekInputCosts = 0;

    /**
     * The accounting way of accounting for costs
     */
    protected PlantCostStrategy costStrategy;

    /**
     * The list of all objects listening to this plant logEvent
     */
    private final Set<PlantListener> listeners;

    /**
     * the time remaining until the next production run is over. 1 is a day
     */
    private float timeRemainingTillNextProductionRun = 0f;

    /**
     * the data storage object. It starts with the start() which is itself called by the HR object when it is started
     */
    private PlantData dataStorage;


    /**
     * Production counter
     */
    private int thisWeekProductionRate[] = new int[GoodType.values().length];

    /**
     * Last week production counter
     */
    private int lastWeekProductionRate[] = new int[GoodType.values().length];

    /**
     * Get the simulation randomizer from the owner.
     */
    public MersenneTwisterFast getRandom(){
        return owner.getRandom();
    }

    /**
     * get the simulation object from the owner
     */
    public MacroII getModel(){
        return owner.getModel();
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
        //this is like a change in machinery
        //tell the listeners
        for(PlantListener l : listeners)
            l.changeInMachineryEvent(this,plantMachinery);

    }

    /**
     * start for the plant simply starts collecting data. Nothing more
     */
    public void start()
    {
        dataStorage.start(getModel(),this);
    }

    /**
     * How many workers are in the plant?
     */
    public int getNumberOfWorkers(){
        return workers.size();
    }


    public void addListener(PlantListener plantListener){
        listeners.add(plantListener);
    }


    public boolean removeListener(PlantListener plantListener){
        return listeners.remove(plantListener);
    }


    /**
     * Check that you have enough of all the inputs needed to make one more of something
     * @return true if you have enough
     */
    public boolean checkForInputs(){

        for(Map.Entry<GoodType,Integer> input : blueprint.getInputs().entrySet())
        {
            if(owner.hasHowMany(input.getKey()) < input.getValue())
            {
                //notify the firm
                owner.fireFailedToConsumeEvent(input.getKey(), input.getValue());
                return false;
            }

        }

        return true; //we got enough
    }

    /**
     * Check if there are enough workers to run the machines
     * @return true if the number of workers is valid
     */
    public boolean checkForWorkers(){

        if(workers.size() == 0 || workers.size() < plantMachinery.minimumWorkersNeeded())
            return false; //too few workers
        if(workers.size() > plantMachinery.maximumWorkersPossible())
            return false; //too many workers
        //if you are here, you are fine!
        return true;
    }


    /**
     * This is called in the adjust when the machines are done producing.
     */
    private void completeProductionRunNow(){
        MacroII state = getModel();
        assert  checkForInputs(); //inputs should still be valid
        //  assert checkForWorkers(); //workers should still be valid

        long totalCostOfInputs = 0l;


        /*************************************
         * Consume inputs!
         *************************************/
        for(Map.Entry<GoodType,Integer> input : blueprint.getInputs().entrySet())
        {
            for(int i=0; i < input.getValue(); i++)
            {
                Good consumed = owner.consume(input.getKey()); //consume it!
                totalCostOfInputs += consumed.getLastValidPrice(); //the price for which the input was bought is added to the sum of costs
            }

        }
        thisWeekInputCosts += totalCostOfInputs; //add the total cost of inputs up

        /*************************************
         * PRODUCE OUTPUTS
         *************************************/
        for(Map.Entry<GoodType,Integer> output : blueprint.getOutputs().entrySet())
        {
            //addSalesDepartmentListener the multiplier
            int totalOutput = (int)Math.floor(output.getValue() * plantMachinery.getOutputMultiplier(output.getKey()));
            assert totalOutput >= output.getValue(); //can't have REDUCED production!
            for(int i=0; i < totalOutput; i++)
            {

                Good newProduct = new Good(output.getKey(),owner,costStrategy.unitOutputCost(output.getKey(), totalCostOfInputs));  //BUILD!
                thisWeekProductionRate[output.getKey().ordinal()]++;
                owner.receive(newProduct, null);
                owner.reactToPlantProduction(newProduct); //tell the owner!
            }
            //tell the firm it is a new production!
            if(totalOutput > 0)
                owner.countNewProduction(output.getKey(),totalOutput);

        }

        status = PlantStatus.READY;   //ready!

    }


    /**
     * The adjust for the plant is fundamental. It keeps going until there are no inputs, no outputs.
     * <p>
     *  The steps it goes through are the following:
     *  <ul>
     *      <li>Make sure that:<ul>
     *          <li> the plant is not already at work (throws an illegal state exception!)</li>
     <li>Check if the plant is obsolete</li>
     *          <li>Check for inputs</li>
     *          <li>Check for workers</li>
     *      </ul></li>
     *
     *      <li>If everything checks out call copleteProductionRunNow</li>
     *      <li>Schedules itself for tomorrow's production phase</li>
     *  </ul>
     *  It returns false if one of the two checks fails, at which point it has to be restarted manually
     */
    public boolean startProductionRun() {




        //check age
        if(status == PlantStatus.OBSOLETE) //if you are too old to completeProductionRunNow
            return false; //don't

        //check for inputs
        boolean check = checkForInputs();
        if(!check) //if it failed
        {
            status = PlantStatus.WAITING_FOR_INPUT;
            getOwner().logEvent(this, MarketEvents.PRODUCTION_HALTED, getOwner().getModel().getCurrentSimulationTimeInMillis(),
                    "missing inputs");
            return false;
        }

        //check for workers
        check = checkForWorkers();
        if(!check) //if it failed
        {
            status = PlantStatus.WAITING_FOR_WORKERS;
            getOwner().logEvent(this, MarketEvents.PRODUCTION_HALTED, getOwner().getModel().getCurrentSimulationTimeInMillis(),
                    "lack of labor");
            return false;
        }

        //draw a new wait time
        status = PlantStatus.PRODUCING;


        //register production has started!
        getOwner().logEvent(this, MarketEvents.PRODUCTION_STARTED, getOwner().getModel().getCurrentSimulationTimeInMillis());
        completeProductionRunNow();     //BUILD the thing
        return true;



    }


    /**
     * Called each week at production phase this method does the following:
     * <ul>
     *     <li>
     *         Checks for obsolescence and preconditions
     *     </li>
     *     <li>
     *         Compute how many production runs are possible today (time-wise)
     *     </li>
     *     <li>
     *         Calls startProductionRun() as many times as feasible or until the status stops being ready
     *     </li>
     * </ul>
     * @param state
     */
    @Override
    public void step(SimState state) {
        assert model.getCurrentPhase().equals(ActionOrder.PRODUCTION) : "plants should only be stepped on production phase";

        if(status == PlantStatus.PRODUCING)
            throw new IllegalStateException("The plant is already producing, it can't be stepped");

        //make sure you have technology
        if(plantMachinery == null)
            throw new IllegalStateException("Plant can't work until you set their technology");

        //check how much you produce
        int howManyProductionRunsToday;
        if(checkForWorkers())
        {
            howManyProductionRunsToday = howManyProductionRunsToday();
            status = status.equals(PlantStatus.WAITING_FOR_WORKERS) ? PlantStatus.READY : status; //you aren't waiting for workers anymore

        }
        else{
            howManyProductionRunsToday = 0;
            status = PlantStatus.WAITING_FOR_WORKERS;

        }


        //statistics
        int  howManyProductionRunsWereSuccessful = 0;
        int inventoryPreProductionOfOutput1=  getOwner().hasHowMany(blueprint.getOutputs().keySet().iterator().next());



        for(int i=0; i < howManyProductionRunsToday; i++)
        {
            boolean productionSuccessful = startProductionRun();
            if(productionSuccessful)
                howManyProductionRunsWereSuccessful++;
            //something came up, we are broken
            if(!status.equals(PlantStatus.READY))
            {
                assert checkForWorkers() : "this can't have failed";
                assert !productionSuccessful;
                break;
            }

        }
        if(howManyProductionRunsWereSuccessful>0)
            getOwner().logEvent(this, MarketEvents.PRODUCTION_COMPLETE, getOwner().getModel().getCurrentSimulationTimeInMillis(),
                    " Planned productions: " + howManyProductionRunsToday + " , actual production "
                            + howManyProductionRunsWereSuccessful + '\n' + "new inventory Output1: " +
                            getOwner().hasHowMany(blueprint.getOutputs().keySet().iterator().next()) + " , old inventory: "+
                            inventoryPreProductionOfOutput1);


        model.scheduleTomorrow(ActionOrder.PRODUCTION,this);
    }

    /**
     * Simulates a "day" to see how much get produced in it
     * @return the number of production runs completed within a day!
     */
    private int howManyProductionRunsToday() {
        int howMany=0;
        float dayTime = 1f;

        //any production queued?
        if(timeRemainingTillNextProductionRun > 0)
        {
            if(timeRemainingTillNextProductionRun > dayTime)
            {
                timeRemainingTillNextProductionRun-= dayTime;
                assert timeRemainingTillNextProductionRun>0;
                return 0;
            }
            else
            {
                howMany++;
                dayTime -=timeRemainingTillNextProductionRun;
                timeRemainingTillNextProductionRun = 0;
                assert dayTime >=0;
            }



        }
        //go through the day, see how much you produce
        while(dayTime>0){
            float newProductionRunTime = plantMachinery.nextWaitingTime();
            assert dayTime>0 && newProductionRunTime >=0;
            if(dayTime >= newProductionRunTime)
            {
                dayTime = dayTime - newProductionRunTime;
                howMany++;
            }
            else{
                timeRemainingTillNextProductionRun = newProductionRunTime - dayTime;
                dayTime=0;
            }

        }

        return howMany;





    }


    /**
     * This returns an IMMUTABLE list of all workers.
     * @return the list of workers
     */
    public List<Person> getWorkers() {
        return Collections.unmodifiableList(workers);

    }

    /**
     * Remove the last hired worker.
     * @return the worker fired
     */
    public Person removeLastWorker(){
        return removeWorker(workers.getLast());

    }

    /**
     * Remove a specific worker
     * @return the worker fired
     */
    public Person removeWorker(@Nonnull Person w){
        if(getNumberOfWorkers() <=0)
            throw new IllegalStateException("Trying to fire a worker from an empty plant!");

        int originalNumberOfWorkers = getNumberOfWorkers();


        //did I remove him successfully?
        boolean succeeded = workers.remove(w);
        if(succeeded){

            //notify the listeners
            for(PlantListener l : listeners)
                l.changeInWorkforceEvent(this, getNumberOfWorkers(),originalNumberOfWorkers );

            //log it
            getOwner().logEvent(this, MarketEvents.LOST_WORKER, getOwner().getModel().getCurrentSimulationTimeInMillis(),
                    " Total workers: " + getNumberOfWorkers());
            //tell the market about it
            if(MacroII.hasGUI()){
                if(getHr() != null) //it might be null if we are turning off
                    getHr().getMarket().registerFiring(owner,w);
            }


            return w;
        }
        else
            throw new IllegalArgumentException("The worker you are looking for isn't here");

    }

    /**
     * Add a new worker to the plant.
     * @param p The new person to hire
     */
    public void addWorker(Person p){
        addWorkers(p); //call the full method



    }


    /**
     * Add a set of workers to the plant. This is a utility method to addSalesDepartmentListener multiple workers at once (so as to avoid firing the listeners a million times)
     * @param newHires the workers to hire
     */
    public void addWorkers(Person... newHires)
    {

        int originalNumberOfWorkers = getNumberOfWorkers();

        if(getNumberOfWorkers() + newHires.length > maxWorkers)
            throw new IllegalStateException("Trying to too many workers to the firm " + getNumberOfWorkers() + newHires.length);


        for(Person p : newHires){
            //check this guy isn't already one of us
            if(workers.contains(p))
                throw new IllegalArgumentException("Trying to the same worker twice!!");
            workers.add(p); //addSalesDepartmentListener it to the roster
        }

        //now fire the listeners
        //tell the listener
        for(PlantListener l : listeners)
            l.changeInWorkforceEvent(this, getNumberOfWorkers(), originalNumberOfWorkers);
        //tell the gui, if needed
        getOwner().logEvent(this, MarketEvents.HIRED_WORKER,getModel().getCurrentSimulationTimeInMillis(),
                "Hired " + newHires.length + ", new total: " + getNumberOfWorkers());


        //if you were waiting for a worker, try again
        //   if(status == PlantStatus.WAITING_FOR_WORKERS && checkForWorkers())
        //       this.step(owner.getModel());

    }

    /**
     * Week end is a time of joy for the plant where the age increases by one.
     * @param time
     */
    public void weekEnd(double time) {
        age++;
        if(age == usefulLife)
        {//if the plant is too old
            status = PlantStatus.OBSOLETE; //the plant stops
            //tell listeners
            for(PlantListener l : listeners)
                l.plantShutdownEvent(this);
            owner.removeInventoryListener(this);

        }
        //reset counters!
        lastWeekProductionRate = thisWeekProductionRate;
        thisWeekProductionRate = new int[GoodType.values().length];
        lastWeekInputCosts = thisWeekInputCosts;
        thisWeekInputCosts = 0;

        //make sure it stays obsolete
        assert age < usefulLife || status == PlantStatus.OBSOLETE;
    }

    /**
     * This is the technology used for production at the plant
     * @return  the technology
     */
    public Machinery getPlantMachinery() {
        return plantMachinery;
    }

    /**
     * This is the technology used for production at the plant
     * @param plantMachinery the technology used for production at the plant
     */
    public void setPlantMachinery(Machinery plantMachinery) {
        this.plantMachinery = plantMachinery;
        //tell the listeners
        for(PlantListener l : listeners)
            l.changeInMachineryEvent(this,plantMachinery);

        //machinery has changed
        getOwner().logEvent(this, MarketEvents.MACHINERY_CHANGE, getOwner().getModel().getCurrentSimulationTimeInMillis(), plantMachinery.toString());
    }

    public Firm getOwner() {
        return owner;
    }

    public PlantStatus getStatus() {
        return status;
    }

    public long getUsefulLife() {
        return usefulLife;
    }

    public void setUsefulLife(long usefulLife) {
        this.usefulLife = usefulLife;
    }

    public long getAge() {
        return age;
    }

    /*
   * ***********************************************
   * DELEGATE METHODS FROM THE TECHNOLOGY
    ***********************************************
    */


    /**
     * Does this technology produces a multiple of the output quantity in the blueprint? Remember that it is ALWAYS floored
     * @return the multiplier
     */
    public float getOutputMultiplier(GoodType outputType) {
        return plantMachinery.getOutputMultiplier(outputType);
    }

    /**
     * How much time has to pass between the production of the last batch and the production of the new one?
     * @return time, in float
     */
    public float expectedWaitingTime() {
        return plantMachinery.expectedWaitingTime();
    }

    /**
     * By how much will a new worker increase weekly production
     */
    public float marginalProductOfWorker(GoodType outputType) {
        return plantMachinery.marginalProductOfWorker(outputType);
    }

    /**
     * Total amount of weekly production expected by this technology
     */
    public float expectedWeeklyProduction(GoodType outputType) {
        return plantMachinery.weeklyThroughput(outputType);
    }

    /**
     * What would be the waiting time if we had this many workers?
     */
    public float hypotheticalWaitingTime(int workers) {
        return plantMachinery.hypotheticalWaitingTime(workers);
    }

    /**
     * How many production runs do we expect to carry out in a week, if we had this many workers?
     */
    public float hypotheticalWeeklyProductionRuns(int workers) {
        return plantMachinery.hypotheticalWeeklyProductionRuns(workers);
    }

    /**
     * Production has started, tell me when it will be ready!
     * @return
     */
    public float nextWaitingTime() {
        return plantMachinery.nextWaitingTime();
    }

    /**
     * How many workers are needed for this technology to even work?
     * @return the number of workers below which the plant can't operate.
     */
    public int minimumWorkersNeeded() {
        return plantMachinery.minimumWorkersNeeded();
    }

    /**
     * How many workers are needed for this technology to even work?
     * @return the number of workers above which the plant can't operate.
     */
    public int maximumWorkersPossible() {
        return plantMachinery.maximumWorkersPossible();
    }

    /**
     * How many production runs we expect to carry out in a week given the workers we have in the plan?
     * @return
     */
    public float expectedWeeklyProductionRuns() {
        if(plantMachinery != null)
            return plantMachinery.expectedWeeklyProductionRuns();
        else
            return 0;
    }

    /**
     * When the plant is not producing and a new input comes in, it checks if it's time to completeProductionRunNow!
     *
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    @Override
    public void inventoryIncreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {


    }

    /**
     * This is called by the inventory to notify the listener that the quantity in the inventory has decreased
     *
     * @param source   the agent with the inventory that is calling the listener
     * @param type     which type of good has increased/decreased in numbers
     * @param quantity how many goods do we have in the inventory now
     */
    @Override
    public void inventoryDecreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {



    }

    /**
     * Given the input(direct) costs how much do I assign as cost to a unit of this output?
     * @param t the type of good produced
     * @param totalCostOfInputs the sum of all inputs
     * @return the cost the accounting assings to this good.
     */
    public long costOfOutput(GoodType t, long totalCostOfInputs) {
        return costStrategy.unitOutputCost(t, totalCostOfInputs);
    }


    /**
     * Turn off the plant (includes firing all people)
     */
    public void turnOff()
    {
        //turn off the cost
        costStrategy.turnOff();
        //fire everyone.
        while(!workers.isEmpty())
            removeLastWorker();
        for(PlantListener listener : listeners)
            listener.plantShutdownEvent(this);

        //turn off the data storage
        dataStorage.turnOff();

    }

    public PlantCostStrategy getCostStrategy() {
        return costStrategy;
    }

    public void setCostStrategy(PlantCostStrategy costStrategy) {
        this.costStrategy = costStrategy;
    }




    /**
     * Returns the cost of building the machinery that makes out the plant
     * @return
     */
    public long getBuildingCosts(){
        return plantMachinery.getLastValidPrice();
    }

    /**
     * This is called by hr, it tells the plant to tell plant listeners that wages are being changed
     */
    public void fireWageEvent(long wage){
        for(PlantListener listener : listeners)
            listener.changeInWageEvent(this, getNumberOfWorkers(),wage);
    }

    public int[] getLastWeekThroughput() {
        return lastWeekProductionRate;
    }

    public int[] getThisWeekThroughput() {
        return thisWeekProductionRate;
    }

    /**
     * Get the human resources object associated with the plant (it asks the firm about it)
     */
    public HumanResources getHr()
    {

        return  owner.getHR(this);
    }


    /**
     * Returns how many additional inputs of a specific kind we would need with a new worker
     *
     * @param inputType the input kind
     * @return how many additional inputs of a specific kind we would need with a new worker
     */
    public float marginalInputRequirements(GoodType inputType) {
        return plantMachinery.marginalInputRequirements(inputType);
    }

    public void setLastValidPrice(long lastValidPrice) {
        plantMachinery.setLastValidPrice(lastValidPrice);
    }

    /**
     * How much specific output gets produced each production run (this is usually just the blueprint value times output multiplier)
     *
     * @param outputType the specific good we want to know how much we completeProductionRunNow of
     */
    public int totalProductionPerRun(GoodType outputType) {
        return plantMachinery.totalProductionPerRun(outputType);
    }

    /**
     * How much total output gets produced each production run (this is usually just the blueprint value times output multiplier)
     */
    public int totalProductionPerRun() {
        return plantMachinery.totalProductionPerRun();
    }

    /**
     * How many inputs of this specific kind will I have to buy/use given these many workers?
     * @param inputType the kind of input we are interested in
     * @param workerSize the hypothetical size of the workforce
     * @return the number of inputs we will consume in a week
     */
    public int hypotheticalWeeklyInputNeeds(GoodType inputType, int workerSize) {
        return plantMachinery.hypotheticalWeeklyInputNeeds(inputType, workerSize);
    }

    public long getSecondLastValidPrice() {
        return plantMachinery.getSecondLastValidPrice();
    }

    /**
     * Returns how many additional production runs a new worker would accomplish
     * @return the additional production runs adding a new worker would cause
     */
    public float marginalProductionRuns() {
        return plantMachinery.marginalProductionRuns();
    }

    public EconomicAgent getProducer() {
        return plantMachinery.getProducer();
    }

    public GoodType getType() {
        return plantMachinery.getType();
    }

    /**
     * Total amount of weekly production expected by this technology
     */
    public float weeklyThroughput(GoodType outputType) {
        return plantMachinery.weeklyThroughput(outputType);
    }

    /**
     * The sum of the number of goods being produced in a week (regadless of the kind of good) for a given number of workers
     *
     */
    public float hypotheticalTotalThroughput(int workers) {
        return plantMachinery.hypotheticalTotalThroughput(workers);
    }


    /**
     * The production of ONE good of this specific type costs how much?
     * @param t the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @return the cost we assign to this good.
     */
    public long unitOutputCost(GoodType t, long totalCostOfInputs) {
        return costStrategy.unitOutputCost(t, totalCostOfInputs);
    }

    /**
     * The unit costs of goods if we change the worker number is going to be how much?
     * @param t the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @param workers the new number of workers
     * @param totalWages the new wages being paid if we have these workers.
     * @return the cost we assign to this good.
     */
    public long hypotheticalUnitOutputCost(GoodType t, long totalCostOfInputs, int workers, long totalWages) {
        return costStrategy.hypotheticalUnitOutputCost(t, totalCostOfInputs, workers, totalWages);
    }

    /**
     * The fixed costs associated with having the plant. These could be the ammortized costs of building the plant, wages if considered quasi-fixed and so on.
     * @return the costs of running the plant
     */
    public long weeklyFixedCosts() {
        return costStrategy.weeklyFixedCosts();
    }

    /**
     * How much of a specific good will be produced in a week given this many workers
     * @param workers the number of workers
     * @param type the goodtype
     * @return
     */
    public float hypotheticalThroughput(int workers, @Nonnull GoodType type) {
        return plantMachinery.hypotheticalThroughput(workers, type);
    }


    /**
     * Ignored
     *
     * @param source       the agent with the inventory
     * @param type         the good type demanded
     * @param numberNeeded how many goods were needed
     */
    @Override
    public void failedToConsumeEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int numberNeeded) {


    }


    public long getLastWeekInputCosts() {
        return lastWeekInputCosts;
    }

    public long getThisWeekInputCosts() {
        return thisWeekInputCosts;
    }

    /**
     * get a set with all the outputs produced at this plant from the machinery
     * @return
     */
    public Set<GoodType> getOutputs()
    {
       return blueprint.getOutputs().keySet();
    }

    /**
     * gets a set of all the type of inputs used by this plant during production
     * @return
     */
    public Set<GoodType> getInputs()
    {

        return blueprint.getInputs().keySet();
    }


    /**
     * return the latest price observed
     */
    public Double getLatestObservation(PlantDataType type) {
        return dataStorage.getLatestObservation(type);
    }

    /**
     * returns a copy of all the observed last prices so far!
     */
    public double[] getAllRecordedObservations(PlantDataType type) {
        return dataStorage.getAllRecordedObservations(type);
    }

    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(PlantDataType type, @Nonnull int[] days) {
        return dataStorage.getObservationsRecordedTheseDays(type, days);
    }

    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(PlantDataType type, int beginningDay, int lastDay) {
        return dataStorage.getObservationsRecordedTheseDays(type, beginningDay, lastDay);
    }

    /**
     * utility method to analyze only  a specific day
     */
    public double getObservationRecordedThisDay(PlantDataType type, int day) {
        return dataStorage.getObservationRecordedThisDay(type, day);
    }

    public int getLastObservedDay() {
        return dataStorage.getLastObservedDay();
    }

    /**
     * how many days worth of observations are here?
     */
    public int numberOfObservations() {
        return dataStorage.numberOfObservations();
    }

    public int getLastDayAMeaningfulChangeInWorkforceOccurred() {
        return dataStorage.getLastDayAMeaningfulChangeInWorkforceOccurred();
    }

    /**
     * Get the wages paid by the HR object last weekend
     */
    public long getWagesPaid() {
        return getHr().getWagesPaid();
    }
}
