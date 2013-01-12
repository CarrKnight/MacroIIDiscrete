package agents.firm.production.control.targeter;

import agents.Person;
import agents.firm.personell.HumanResources;
import com.google.common.base.Preconditions;
import financial.MarketEvents;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.technology.Machinery;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
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
     * How much time passes between each call of hire()?
     */
    private float speed = 5;

    /**
     * Creates a new market-look targeter. Throws an state exception if the market doesn't allow for the best ask to be visible!
     * @param hr the human resources
     */
    public MarketLookTargeter(HumanResources hr,PlantControl control) {
       this(hr,control, 5f);
    }

    /**
     * Creates a new market-look targeter. Throws an state exception if the market doesn't allow for the best ask to be visible!
     * @param hr the human resources
     */
    public MarketLookTargeter(@Nonnull HumanResources hr,@Nonnull PlantControl control, float speed ) {

        Preconditions.checkState(hr.getMarket().isBestSalePriceVisible()); //this can't work without looking at prices
        Preconditions.checkArgument(speed > 0);
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

        if(target <= hr.getPlant().workerSize()){
            //we are on target, stop
            control.setCanBuy(false);
            return;
        }


        try {
            //compute the wage needed to get a new worker
            long lowestWage = hr.getMarket().getBestSellPrice();

            if(lowestWage == -1 ) //if there is no worker available check back in a while!
            {
                hr.getFirm().getModel().schedule.scheduleOnceIn(Math.max(speed*3 + hr.getFirm().getModel().random.nextGaussian()
                        ,0.01f),new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        hire(); //try again soon!
                    }
                });
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
            hr.getFirm().getModel().schedule.scheduleOnceIn(Math.max(speed + hr.getFirm().getModel().random.nextGaussian()
                    , 0.01f),new Steppable() {
                @Override
                public void step(SimState simState) {
                    hire(); //check if we need more!
                }
            });

        } catch (IllegalAccessException e) {
            assert false;
            System.err.println("the best sell price wasn't visible! we were lied to!!!");
            System.exit(-1);
        }

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
        if(target > hr.getPlant().workerSize())
            hire();
        else if(target < hr.getPlant().workerSize())
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
            while(hr.getPlant().workerSize() > 0)
            {
                hr.getPlant().removeLastWorker();
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
                return Long.compare(o1.getMinimumWageRequired(), o2.getMinimumWageRequired());

            }
        });

        assert workers.size() >0;
        int workersToFire = hr.getPlant().workerSize() - target;
        assert workersToFire <= workers.size(): workersToFire + "---" + workers.size();



        //set the wage and fire the workers
        long newWage = workers.get(workers.size() - workersToFire -1).getMinimumWageRequired();
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
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
        //if target is above what we have: buy!
        if(target > workerSize)
            hire();
        else if(target <workerSize)
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
        if(target > hr.getPlant().workerSize())
            hire();
        else if(target <hr.getPlant().workerSize())
            fire();
    }


    public int getTarget() {
        return target;
    }
}
