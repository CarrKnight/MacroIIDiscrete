/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> This is really a desperate attempt to get something out of this. Basically it's a marginalplantcontrol
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-01-21
 * @see
 */
public class HillClimberThroughPredictionControl implements PlantControl, PlantListener, Steppable
{

    /**
     * this is the standard marginal plant control that does all the work while we just hack at predictors from the back
     */
    final private MarginalPlantControl delegate;

    /**
     * how many days will pass between each attempt at maximization
     */
    private int daysBetweenEachTrial = 100;

    final private Firm owner;

    private LinkedList<SalesDepartment> salesDepartments;

    private LinkedList<PurchasesDepartment> purchasesDepartments;

    final private HumanResources hr;

    final private MacroII model;

    final private MersenneTwisterFast random;

    private HillClimberState maximizationState = new InitializationState();


    public HillClimberThroughPredictionControl(MarginalPlantControl delegate, Firm owner,
                                               HumanResources hr, MacroII model,
                                               MersenneTwisterFast random) {
        this.delegate = delegate;
        delegate.getMaximizer().setHowManyDaysBeforeEachCheck(10);

        this.owner = owner;

        this.hr = hr;
        this.model = model;
        this.random = random;
    }

    public HillClimberThroughPredictionControl(HumanResources hr) {
        this(new MarginalPlantControl(hr),hr.getFirm(),hr,hr.getModel(),hr.getRandom());
    }


    private int[] startingSlope;

    /**
     * the slopes/fixed predictors that we are currently running
     */
    private int[] currentSlopes;

    /**
     * all the permutations of current slope we need to check
     */
    private List<int[]> neighborsToCheck;

    /**
     * the best slope we found in the current loop
     */
    private int[] bestSlope;

    private long bestProfits = Long.MIN_VALUE;


    static final private int maximumSlope = 3;

    static final private int minimumSlope = 0;


    /**
     * how many fixed predictors we have to deal with
     * @return
     */
    private int getDimension()
    {
        return salesDepartments.size() + purchasesDepartments.size() + 1; //the 1 is hr
    }

    /**
     * start the delegate, but start yourself too!
     */
    @Override
    public void start() {
        delegate.start();
        this.salesDepartments = new LinkedList<>(owner.getSalesDepartments().values());
        this.purchasesDepartments = new LinkedList<>(owner.getPurchaseDepartments());
        //schedule yourself
        model.scheduleSoon(ActionOrder.THINK,this,Priority.BEFORE_STANDARD);
    }


    @Override
    public void step(SimState simState) {



        maximizationState.step();









        //always be rescheduling
        model.scheduleAnotherDay(ActionOrder.THINK,this,daysBetweenEachTrial, Priority.BEFORE_STANDARD);




    }

    private void fillPredictor(int[] slopes) {
        int i=0;
        for(SalesDepartment department : salesDepartments)
            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(slopes[i++]));
        for(PurchasesDepartment department : purchasesDepartments)
            department.setPredictor(new FixedIncreasePurchasesPredictor(slopes[i++]));
        hr.setPredictor(new FixedIncreasePurchasesPredictor(slopes[i++]));
        assert i == slopes.length;
    }


    /****************************************
     * WE DELEGATE HERE EVERYTHING TO THE DELEGATE
     *****************************************/






    public boolean isActive() {
        return delegate.isActive();
    }

    /**
     * Returns the plant monitored by the HR
     */
    public Plant getPlant() {
        return delegate.getPlant();
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap() {
        return delegate.estimateDemandGap();
    }


    /**
     * pass the message down
     *
     * @param p          the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        delegate.changeInWorkforceEvent(p, workerSizeNow, workerSizeBefore);
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        delegate.changeInMachineryEvent(p, machinery);
    }

    /**
     * This method returns the control rating on current stock held <br>
     *
     * @return the rating on the current stock conditions or null if the department is not active.
     */

    @Override
    public Level rateCurrentLevel() {
        return delegate.rateCurrentLevel();
    }

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     * @param wage       the new wage
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
        delegate.changeInWageEvent(p, workerSize, wage);
    }

    /**
     * The targeter is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        delegate.setTarget(workerSizeTargeted);
    }

    /**
     * Ask the targeter what is the current worker target
     * @return the number of workers the strategy is targeted to find!
     */
    @Override
    public int getTarget() {
        return delegate.getTarget();
    }

    /**
     * Answer the question: how much am I willing to pay for this kind of labor?
     * Notice that NO UPDATING SHOULD TAKE PLACE in calling this method. Human Resources expects maxPrice() to be consistent from one call to the next.
     * To notify hr of inconsistencies call updateEmployeeWages(). <br>
     * In short,for plant control, <b>this should be a simple getter.</b>. If you are a subclass and want to change it, use the current wage setter.
     *
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {
        return delegate.maxPrice(type);
    }

    /**
     * Answer the question: how much am I willing to pay for this specific kind of labor?
     *
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        return delegate.maxPrice(good);
    }

    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    /**
     * Sets the wage, update offers and then updates wages of current workers
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(int newWage) {
        delegate.setCurrentWage(newWage);
    }

    /**
     * Returns the human resources object
     */
    @Override
    public HumanResources getHr() {
        return delegate.getHr();
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        delegate.plantShutdownEvent(p);
    }

    /**
     * Get the wage offered (for subclasses only)
     * @return the wage offered.
     */
    @Override
    public long getCurrentWage() {
        return delegate.getCurrentWage();
    }

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return delegate.canBuy();
    }

    /**
     * Set the flag to allow or ban the hr from hiring people
     * @param canBuy true if the hr can hire more people at this wage.
     */
    @Override
    public void setCanBuy(boolean canBuy) {
        delegate.setCanBuy(canBuy);
    }


    /**
     * a very simple state design pattern, for ease to read
     */
    private interface HillClimberState
    {

        public void step();
    }


    private class InitializationState implements HillClimberState
    {
        @Override
        public void step() {
            Preconditions.checkState(currentSlopes==null);
            Preconditions.checkState(startingSlope==null);
            startingSlope = new int[getDimension()];
            for(int i=0; i< startingSlope.length; i++)
                startingSlope[i] = random.nextInt(maximumSlope-minimumSlope) + minimumSlope;

            maximizationState = new CreateNeighbors();
            maximizationState.step();

        }
    }

    private class CreateNeighbors implements HillClimberState
    {


        @Override
        public void step() {

            Preconditions.checkState(neighborsToCheck == null);
            //if you have no neighbors, you need to create them

            neighborsToCheck = new LinkedList<>();
            //add current place too
            neighborsToCheck.add(startingSlope);
            for(int i=0; i<startingSlope.length; i++)
            {
                if(startingSlope[i]>minimumSlope)
                {
                    int[] neighbor = Arrays.copyOf(startingSlope,startingSlope.length);
                    neighbor[i]--;
                    assert neighbor[i] >= minimumSlope;
                    neighborsToCheck.add(neighbor);
                }
                if(startingSlope[i]<maximumSlope)
                {
                    int[] neighbor = Arrays.copyOf(startingSlope,startingSlope.length);
                    neighbor[i]++;
                    assert neighbor[i] <= maximumSlope;
                    neighborsToCheck.add(neighbor);
                }
            }

            Collections.shuffle(neighborsToCheck,new Random(random.nextLong()));
            maximizationState = new Loop();
            maximizationState.step();

        }
    }


    private class Loop implements HillClimberState
    {
        @Override
        public void step()
        {

            if(currentSlopes != null) //if you were running an experiment, check the results
            {
                //get the profits associated with this slope
                long currentProfits = owner.getAggregateProfits();
                System.out.println("we tried " + Arrays.toString(currentSlopes) + " , and we got: " + currentProfits + ",with workers: " + owner.getTotalWorkers() + ", and price: " + salesDepartments.getFirst().getLastClosingPrice());

                if(currentProfits > bestProfits)
                {
                    bestSlope = currentSlopes;
                    bestProfits = currentProfits;
                }
                if(!Arrays.equals(currentSlopes,startingSlope))
                {
                    neighborsToCheck.add(0,startingSlope);
                }
            }

            //if you still have neighbors to check, do so now
            if(!neighborsToCheck.isEmpty())
            {

                currentSlopes = neighborsToCheck.remove(0);
                fillPredictor(currentSlopes);
                System.out.println("going to try: " + Arrays.toString(currentSlopes));

            }
            else
            {
                System.out.println("we decided " + Arrays.toString(bestSlope) + " was the best choice ");


                //is the best our starting point? if so, stop trying.
                if(Arrays.equals(startingSlope, bestSlope)){
                    System.out.println("the starting slope was the best slope, we stop here");
                    fillPredictor(bestSlope);
                    maximizationState = new Over();
                    return;
                }


                //otherwise, start over from the best
                startingSlope = bestSlope;
                bestProfits = Long.MIN_VALUE;
                currentSlopes = null;
                neighborsToCheck=null;

                //restart
                maximizationState = new CreateNeighbors();
            }


        }
    }

    private class Over implements HillClimberState{

        @Override
        public void step() {

            long currentProfits = owner.getAggregateProfits();
            float percentDifference = (Math.abs((bestProfits-currentProfits)/(float)bestProfits));

            System.out.println("profits used to be: " + bestProfits +" , but now are: " + currentProfits +", a percentage change of : "  + percentDifference);

            //quit this loop if the current profits are a lot different from what we remember
            if((currentProfits>0 && bestProfits==0) || bestProfits != 0 && percentDifference>.05)
            {
                currentSlopes = null;
                bestProfits = Long.MIN_VALUE;
                neighborsToCheck=null;
                maximizationState = new CreateNeighbors();
                maximizationState.step();
            }

        }
    }


    public int getDaysBetweenEachTrial() {
        return daysBetweenEachTrial;
    }

    public void setDaysBetweenEachTrial(int daysBetweenEachTrial) {
        this.daysBetweenEachTrial = daysBetweenEachTrial;
    }
}
