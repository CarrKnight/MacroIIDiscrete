/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.targeter;

import agents.Person;
import agents.firm.personell.HumanResources;
import com.google.common.base.Preconditions;
import financial.MarketEvents;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.technology.Machinery;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
 * @version 2012-09-22
 * @see
 */
public class MarketLookTargeter implements WorkforceTargeter {

    /**
     * Link to the human resources
     */
    private final HumanResources hr;

    /**
     * is active?
     */
    private boolean active = true;

    /**
     * link to the plant control
     */
    private PlantControl control;

    /**
     * The worker size targeted!
     */
    private int target = 0;

    /**
     * How many days unused pass between one attempt to hire and the next?
     */
    private int speed = 0;

    /**
     * Creates a new market-look targeter. Throws an state exception if the market doesn't allow for the best ask to be visible!
     * @param hr the human resources
     */
    public MarketLookTargeter(HumanResources hr,PlantControl control) {
        this(hr,control, 0);
    }

    /**
     * Creates a new market-look targeter. Throws an state exception if the market doesn't allow for the best ask to be visible!
     * @param hr the human resources
     */
    public MarketLookTargeter( HumanResources hr, PlantControl control, int speed ) {

        Preconditions.checkState(hr.getMarket().isBestSalePriceVisible()); //this can't work without looking at prices
        Preconditions.checkArgument(speed >= 0);
        this.control = control;
        this.hr = hr;
        this.speed = speed;
    }

    /**
     * Tells the hr to buy
     */
    private void hire(){

        //don't do anything if you are not active
        if(!active)
            return;

        if(target <= hr.getPlant().getNumberOfWorkers()){
            //we are on target, stop
            control.setCanBuy(false);
            return;
        }


        try {
            //compute the wage needed to get a new worker
            long lowestWage = hr.getMarket().getBestSellPrice();

            if(lowestWage == -1 ) //if there is no worker available check back in a while!
            {
                rescheduleHire();
                return;
            }

            assert lowestWage >=0 : "wages can't be negative";

            //if we are here there is a worker willing to work for us, set your new price
            control.setCurrentWage(lowestWage);
            if(!control.canBuy()) //if it had been deactivated..
            {
                control.setCanBuy(true);
                hr.buy();
            }
            //check back in a second
            rescheduleHire();


        } catch (IllegalAccessException e) {
            assert false;
            System.err.println("the best sell price wasn't visible! we were lied to!!!");
            System.exit(-1);
        }

    }

    private void rescheduleHire() {
        if(speed ==0)
            hr.getFirm().getModel().scheduleTomorrow(ActionOrder.TRADE
                    ,new Steppable() {
                @Override
                public void step(SimState simState) {
                    hire(); //try again soon!
                }
            }
            );
        else
            hr.getFirm().getModel().scheduleAnotherDay(ActionOrder.TRADE
                    , new Steppable() {
                @Override
                public void step(SimState simState) {
                    hire(); //try again soon!
                }
            }
                    , speed
            );
        return;
    }

    /**
     * The strategy is told that now we need to hire this many workers
     *
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        this.target = workerSizeTargeted;
        //if target is above what we have: buy!
        if(target > hr.getPlant().getNumberOfWorkers())
            hire();
        else if(target < hr.getPlant().getNumberOfWorkers())
            fire();

    }

    /**
     * Somebody will receive a very bad news...
     */
    private void fire(){
        //stop hiring
        control.setCanBuy(false);

        //if we have to fire EVERYONE:
        if(target == 0)
        {
            while(hr.getPlant().getNumberOfWorkers() > 0)
            {
                hr.getPlant().removeLastWorker();
                if(MacroII.hasGUI())

                    hr.getFirm().logEvent(hr,
                        MarketEvents.LOST_WORKER, hr.getFirm().getModel().getCurrentSimulationTimeInMillis(),
                        "quickfiring");
            }
            return;
        }

        //make a list of workers
        List<Person> workers = new ArrayList<>(hr.getPlant().getWorkers());
        //sort them by their minimum wage
        Collections.sort(workers, new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return Long.compare(o1.getMinimumDailyWagesRequired(), o2.getMinimumDailyWagesRequired());

            }
        });

        assert workers.size() >0;
        int workersToFire = hr.getPlant().getNumberOfWorkers() - target;
        assert workersToFire <= workers.size(): workersToFire + "---" + workers.size();



        //set the wage and fire the workers
        long newWage = workers.get(workers.size() - workersToFire -1).getMinimumDailyWagesRequired();
        control.setCurrentWage( newWage );
        //if it's not a fixed pay structure you have to remove them yourself
        if(!control.getHr().isFixedPayStructure())
            hr.fireEveryoneAskingMoreThan(newWage,target);


    }

    /**
     * This is called by the plant control when it is started.
     */
    @Override
    public void start() {
        //ignored
    }

    /**
     * This is called when the object stops being useful. Irreversible
     */
    @Override
    public void turnOff() {
        active = false;
    }

    /**
     * This is called whenever a plant has changed the number of workers
     *
     * @param p          the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        //if target is above what we have: buy!
        if(target > workerSizeNow)
            hire();
        else if(target < workerSizeNow)
            fire();
    }

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     *
     * @param wage       the new wage
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        //if target is above what we have: buy!
        if(target > hr.getPlant().getNumberOfWorkers())
            hire();
        else if(target <hr.getPlant().getNumberOfWorkers())
            fire();
    }


    public int getTarget() {
        return target;
    }
}
