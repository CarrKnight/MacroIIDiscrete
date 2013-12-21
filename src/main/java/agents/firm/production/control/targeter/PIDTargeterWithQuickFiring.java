/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.targeter;

import agents.Person;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.technology.Machinery;
import com.google.common.base.Preconditions;
import financial.MarketEvents;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> The standard PID workforce targeter.
 * Works very much like a purchase department control: increase wages when it needs workers,
 * decreases wages when it needs to fire people.
 * <p/> Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
 * just fire and lower the wage to the maximum reservation wage
 * of the remaining workers
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
public class PIDTargeterWithQuickFiring implements WorkforceTargeter, Steppable {

    /**
     * Current worker target
     */
    private int workerTarget = 0 ;

    /**
     * PID Controller
     */
    private final PIDController pid;

    /**
     * link to the human resources objects (and plant and all)
     */
    final private HumanResources hr;

    /**
     * The control we are part of
     */
    private final PlantControl plantControl;

    /**
     * Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
     * just fire and lower the wage to the maximum reservation wage
     * of the remaining workers     */
    private boolean quickfiring = false;



    /**
     * is it activated?
     */
    boolean active = true;

    /**
     * This specify a threshold over which the PID targeter stops buying even though it could given the price it set for itself.
     * So for example, if this is 1.5 it means that the PID targeter will stop hiring once it exceeds by 50% its current target.
     * This is somewhat of a tradeoff because the less you let the PID be wrong, the slower it learns; on the other hand it reduces big swings in worker size.
     */
    private float maximumPercentageOverTargetOfWorkersToHire = 1.2f;


    /**
     * A new PID targeter, the pid controller is randomly generated by drawing parameters from the model object
     * @param hr the human resources the PID target works with
     */
    public PIDTargeterWithQuickFiring(HumanResources hr, PlantControl control) {
        this(hr,new PIDController(
                hr.getFirm().getModel().drawProportionalGain()/5,
                hr.getFirm().getModel().drawIntegrativeGain()/10,
                -hr.getFirm().getModel().drawProportionalGain()/10,
                hr.getRandom()),
                control);
    }

    /**
     * A new PID targeter, the controller will be created with the given parameters
     * @param hr the human resources the PID target works with
     */
    public PIDTargeterWithQuickFiring(HumanResources hr, PlantControl control,
                                      float proportionalGain, float integrativeGain, float derivativeGain, int controlSpeed){
        this(hr,new PIDController(
                proportionalGain,integrativeGain,derivativeGain,controlSpeed,hr.getRandom())

                ,control);
    }

    /**
     * A new PID targeter, with pre-specified controller
     * @param hr the human resources the PID target works with
     */
    public PIDTargeterWithQuickFiring(HumanResources hr, PIDController controller, PlantControl control) {
        this.hr = hr;
        this.pid = controller;
        this.plantControl =  control;
    }

    /**
     * The strategy is told that now we need to hire this many workers
     *
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        workerTarget = workerSizeTargeted;
    }


    /**
     * This is your standard PID controller adjust with a few  modifications: it tries to fix rounding problems, and calls set currentWage only when there is a real change
     * @param simState
     */
    @Override
    public void step(SimState simState) {
        //don't go on if you aren't active
        if(!active)
            return;

        //remember old wage
        final long oldWage = plantControl.maxPrice(hr.getGoodType());
        //if you are lowering wage, double check by ceiling (makes it sluggish to wage changes)


        //if firing: go into quickfiring mode:
        if(quickfiring && workerTarget < hr.getPlant().getNumberOfWorkers())
        {
            if(workerTarget != 0)
                quickfire();
            else
            {
                //set wages to 0
                setInitialWage(0); //set price to 0
                //fire directly everyone
                while(hr.getPlant().getNumberOfWorkers() > 0){
                    Person workerFired = hr.getPlant().removeLastWorker();
                    workerFired.fired(hr.getFirm());
                }
            }
        }

        //run the controller (it will reschedule us)
        ControllerInput input = ControllerInput.simplePIDTarget(workerTarget,hr.getPlant().getNumberOfWorkers());
        pid.adjust(input
                , active, hr.getPlant().getModel(), this,
                ActionOrder.ADJUST_PRICES);  //i made this before standard so it acts BEFORE the maximizer

        //initially round
        final long newWage = Math.round(pid.getCurrentMV());




        //if there was a REAL change call setCurrentWage of the control. Otherwise don't bother.
        if(oldWage != newWage && newWage >=0) //if pid says to change prices, change prices
        {

            //if we have the ovverride flag on, this is the time
            //              System.out.println(" setting new wage " + newWage + ", given old wage " + oldWage + " pid mv: " + pid.getCurrentMV() + "| workers : " + hr.getPlant().getNumberOfWorkers() + ", target: " + workerTarget  );
            plantControl.setCurrentWage(newWage); //set the new wage! that'll do it!

            //log it!
            if(MacroII.hasGUI())

                hr.getFirm().logEvent(hr,
                        MarketEvents.CHANGE_IN_POLICY,
                        hr.getFirm().getModel().getCurrentSimulationTimeInMillis(),
                        "target: " + workerTarget + ", #workers:" + hr.getPlant().getNumberOfWorkers() +
                                "; oldwage:" + oldWage + ", newWage:" + newWage);
        }



    }







    /**
     * utility to fire workers without trial and attempt: just target how many workers you want to fire and that's it
     */
    private void quickfire() {

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
        int workersToFire = hr.getPlant().getNumberOfWorkers() - workerTarget;
        assert workersToFire <= workers.size(): workersToFire + "---" + workers.size();
        assert workersToFire > 0;

        //set the wage and fire the workers
        long newWage = workers.get(workers.size() - workersToFire -1).getMinimumDailyWagesRequired();
        assert newWage == workers.get(workers.size() - workersToFire -1).getMinimumDailyWagesRequired();
        plantControl.setCurrentWage(newWage ); //set new wage
        //in normal situation the new wage will be the variable newwage, but maybe there are frictions. At which point your best bet is just to be slightly below it
        //for the time being
        //      newWage = newWage < plantControl.getCurrentWage() ? plantControl.getCurrentWage()-1 : newWage;
        setInitialWage(newWage);     //reset PID


        //and now fire them
        if(!hr.isFixedPayStructure())
            //if you are discriminating on pay, this is quite easy.
            hr.fireEveryoneAskingMoreThan(newWage,workerTarget);
        else
        {
            //you could wait for people to leave their job because you lowered their wages, but since we have them already sorted might just as well kick them out ourselves
            for(int i=workers.size() - workersToFire; i  < workers.size(); i++)
            {
                Person toFire = workers.get(i);
                hr.getPlant().removeWorker(toFire);
                toFire.fired(hr.getFirm());
            }

            //on very rare occasions you lower wages enough to kick some people out but more people come in. When that happens, run it again
            if(hr.getPlant().getNumberOfWorkers() > workerTarget)
                quickfire();

            assert hr.getPlant().getNumberOfWorkers() == workerTarget : "workers: " + hr.getPlant().getNumberOfWorkers() + ", workerTarget: " + workerTarget;
        }

    }


    /**
     * This is called by the plant control when it is started.
     */
    @Override
    public void start() {
        //start stepping your PID!
        hr.getFirm().getModel().scheduleSoon(ActionOrder.ADJUST_PRICES,this, Priority.STANDARD); //i made this before standard so it acts BEFORE the maximizer

    }

    /**
     * always try to buy.
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        assert p == hr.getPlant();

        if(plantControl.canBuy() &&
                hr.getNumberOfWorkers() <= getTarget()* maximumPercentageOverTargetOfWorkersToHire)
            hr.buy();
    }

    /**
     * ignored
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }

    /**
     * ignored
     */
    @Override
    public void plantShutdownEvent(Plant p) {
    }

    /**
     * always try to buy.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        assert p == hr.getPlant();
        if(p.getNumberOfWorkers() < p.maximumWorkersPossible() && plantControl.canBuy())
            hr.buy();
    }


    /**
     * This is called when the object stops being useful. Irreversible
     */
    @Override
    public void turnOff() {
        active = false;
    }

    public int getTarget() {
        return workerTarget;
    }


    /**
     * Set the new "0" of the PID target
     */
    public void setInitialWage(long wage)
    {
        Preconditions.checkArgument(wage >=0);
        pid.setOffset(wage);
    }


    /**
     * Gets Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
     * just fire and lower the wage to the maximum reservation wage
     * of the remaining workers.
     *
     * @return Value of Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
     *         just fire and lower the wage to the maximum reservation wage
     *         of the remaining workers.
     */
    public boolean isQuickfiring() {
        return quickfiring;
    }

    /**
     * Sets new Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
     * just fire and lower the wage to the maximum reservation wage
     * of the remaining workers.
     *
     * @param quickfiring New value of Quickfiring means that it will not bother with PID adjustment when it needs to fire people: rather it will
     *                    just fire and lower the wage to the maximum reservation wage
     *                    of the remaining workers.
     */
    public void setQuickfiring(boolean quickfiring) {
        this.quickfiring = quickfiring;
    }



    /**
     * Gets This specify a threshold over which the PID targeter stops buying even though it could given the price it set for itself.
     * So for example, if this is 1.5 it means that the PID targeter will stop hiring once it exceeds by 50% its current target.
     * This is somewhat of a tradeoff because the less you let the PID be wrong, the slower it learns; on the other hand it reduces big swings in worker size..
     *
     * @return Value of This specify a threshold over which the PID targeter stops buying even though it could given the price it set for itself.
     *         So for example, if this is 1.5 it means that the PID targeter will stop hiring once it exceeds by 50% its current target.
     *         This is somewhat of a tradeoff because the less you let the PID be wrong, the slower it learns; on the other hand it reduces big swings in worker size..
     */
    public float getMaximumPercentageOverTargetOfWorkersToHire() {
        return maximumPercentageOverTargetOfWorkersToHire;
    }

    /**
     * Sets new This specify a threshold over which the PID targeter stops buying even though it could given the price it set for itself.
     * So for example, if this is 1.5 it means that the PID targeter will stop hiring once it exceeds by 50% its current target.
     * This is somewhat of a tradeoff because the less you let the PID be wrong, the slower it learns; on the other hand it reduces big swings in worker size..
     *
     * @param maximumPercentageOverTargetOfWorkersToHire
     *         New value of This specify a threshold over which the PID targeter stops buying even though it could given the price it set for itself.
     *         So for example, if this is 1.5 it means that the PID targeter will stop hiring once it exceeds by 50% its current target.
     *         This is somewhat of a tradeoff because the less you let the PID be wrong, the slower it learns; on the other hand it reduces big swings in worker size..
     */
    public void setMaximumPercentageOverTargetOfWorkersToHire(float maximumPercentageOverTargetOfWorkersToHire) {
        this.maximumPercentageOverTargetOfWorkersToHire = maximumPercentageOverTargetOfWorkersToHire;
    }
}
