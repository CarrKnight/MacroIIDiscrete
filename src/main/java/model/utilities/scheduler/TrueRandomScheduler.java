/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> This is the re-implementation of the random scheduler using arraylists. It is more random than the random queue
 * because each steppable is chosen uniformly at random every time (while the random queue gives each steppable a random "ordering"
 * which might let one chain of events happen very late.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-15
 * @see
 */
public class TrueRandomScheduler implements Steppable, PhaseScheduler
{


    /**
     * where we store every possible action!
     * Each action order has an array list for every possible priority value
     */
    private EnumMap<ActionOrder,List<Steppable>[]> steppablesByPhase;


    private EnumMap<ActionOrder,Long> timePerPhase;

    /**
     * The randomizer
     */
    private final MersenneTwisterFast randomizer;


    /**
     * the maximum number of simulation days
     */
    private int simulationDays;

    /**
     * which phase are we in?
     */
    private ActionOrder currentPhase = ActionOrder.DAWN;

    final private ArrayList<PrioritySteppablePair> tomorrowSamePhase;

    final private ArrayList<FutureAction> futureActions;


    public TrueRandomScheduler(int simulationDays, MersenneTwisterFast randomizer) {
        this.randomizer =  randomizer;
        this.simulationDays = simulationDays;

        timePerPhase = new EnumMap<>(ActionOrder.class);
        for(ActionOrder o : ActionOrder.values())
                timePerPhase.put(o,0l);


        //initialize the enums
        resetMap();

        //initialize tomorrow schedule
        tomorrowSamePhase = new ArrayList<>();

        futureActions = new ArrayList<>();

    }

    private void resetMap() {
        steppablesByPhase = new EnumMap<>(ActionOrder.class);
        for(ActionOrder order :ActionOrder.values())
        {
            //put the array
            List<Steppable>[] array = order.isToRandomize() ? new LinkedList[Priority.values().length] : new ArrayList[Priority.values().length];
            steppablesByPhase.put(order,array);

            //populate the array
            for(Priority p : Priority.values())
                array[p.ordinal()]= order.isToRandomize()? new LinkedList<>() : new ArrayList<Steppable>();

        }
    }


    /**
     * Go through a "day" in the model. It self schedules to do it again tomorrow
     */
    @Override
    public void step(SimState simState) {
        assert simState instanceof MacroII;
        MersenneTwisterFast random = simState.random; //grab the random from the simstate
        assert tomorrowSamePhase.isEmpty() : "things shouldn't be scheduled here";



        //for each phase
        for(ActionOrder phase : ActionOrder.values())
        {
            long timeAtStart = System.currentTimeMillis();

            currentPhase = phase; //currentPhase!




            int highestPriority = getHighestPriority(phase);
            while(highestPriority != -1) //as long as there are still things to do at any priority
            {
                //get the highest priority steppables
                List<Steppable> steppables = steppablesByPhase.get(phase)[highestPriority];


                assert steppables != null;
                assert !steppables.isEmpty();

                //take a random highest priority action and do it.
                //but don't bother randomizing for gui and clean up data
                final int index = currentPhase.isToRandomize() ?
                        0 :randomizer.nextInt(steppables.size());
                Steppable steppable = steppables.remove(index);
                assert steppable != null;
                //act nau!!!
                steppable.step(simState);

                //update priority (low priority can still schedule stuff to happen at high priority so we need to keep checking)
                highestPriority = getHighestPriority(phase);

            }


            //add all the steppables that reserved a spot for tomorrow, same phase
            allocateTomorrowSamePhaseActions(phase);


            long duration = System.currentTimeMillis() - timeAtStart;
            timePerPhase.put(phase, timePerPhase.get(phase) + duration);
            //go to the next phase!

        }

        //prepare for tomorrow
        prepareForTomorrow();

        //see you tomorrow
        if(((MacroII)simState).getMainScheduleTime() <= simulationDays)
            simState.schedule.scheduleOnceIn(1,this);



    }

    private void allocateTomorrowSamePhaseActions(ActionOrder phase) {
        List<Steppable>[] current= steppablesByPhase.get(phase);
        for(PrioritySteppablePair pair : tomorrowSamePhase)
        {
            current[pair.getPriority().ordinal()].add(pair.getSteppable());
        }
        tomorrowSamePhase.clear(); //here we kept all steppables that are called to act the same phase tomorrow!
    }

    /**
     * gets the index of the steppable with the highest priority in a given action order
     * @param phase the action phase
     * @return the highest priority or -1 if there are none.
     */
    public int getHighestPriority(ActionOrder phase) {
        List<Steppable> steppablesByPriority[] =  steppablesByPhase.get(phase);
        for(int i=0; i<steppablesByPriority.length;i++)
        {
            assert steppablesByPriority[i] !=null; //because they are created once and never destroyed, all the arraylists should not be null

            if(!steppablesByPriority[i].isEmpty())
                return i;
        }
        return -1;
    }

    private void prepareForTomorrow() {
        assert tomorrowSamePhase.isEmpty() : "things shouldn't be scheduled here";
        //set phase to dawn
        currentPhase = ActionOrder.DAWN;
        //check for delayed actions
        LinkedList<FutureAction> toRemove = new LinkedList<>();
        for(FutureAction futureAction : futureActions)
        {
            boolean ready = futureAction.spendOneDay();
            if(ready)
            {
                scheduleSoon(futureAction.getPhase(),futureAction.getAction(),futureAction.getPriority());
                toRemove.add(futureAction);
            }
        }
        futureActions.removeAll(toRemove);
    }

    /**
     * schedule the event to happen when the next specific phase comes up!
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    @Override
    public void scheduleSoon( ActionOrder phase,  Steppable action){

        scheduleSoon(phase, action,Priority.STANDARD);

    }


    /**
     * Schedule as soon as this phase occurs
     *
     * @param phase    the phase i want the action to occur in
     * @param action   the steppable that should be called
     * @param priority the action priority
     */
    @Override
    public void scheduleSoon( ActionOrder phase,  Steppable action, Priority priority) {

        steppablesByPhase.get(phase)[priority.ordinal()].add(action);

    }

    /**
     * force the schedule to record this action to happen tomorrow.
     * This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    @Override
    public void scheduleTomorrow(ActionOrder phase, Steppable action){

        this.scheduleTomorrow(phase, action,Priority.STANDARD);

    }


    /**
     * Schedule tomorrow assuming the phase passed is EXACTLY the current phase
     * This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase    the phase i want the action to occur in
     * @param action   the steppable that should be called
     * @param priority the action priority
     */
    @Override
    public void scheduleTomorrow(ActionOrder phase, Steppable action, Priority priority) {

        assert phase.equals(currentPhase);
        //put it among the steppables of that phase
        tomorrowSamePhase.add(new PrioritySteppablePair(action,priority));

    }

    /**
     *
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param daysAway how many days from now should it be scheduled
     */
    @Override
    public void scheduleAnotherDay( ActionOrder phase,  Steppable action,
                                   int daysAway)
    {
        scheduleAnotherDay(phase, action, daysAway,Priority.STANDARD);

    }


    /**
     * Schedule in as many days as passed (at priority standard)
     *
     * @param phase    the phase i want the action to occur in
     * @param action   the steppable that should be called
     * @param daysAway how many days into the future should this happen
     * @param priority the action priority
     */
    @Override
    public void scheduleAnotherDay( ActionOrder phase,  Steppable action, int daysAway, Priority priority) {
        Preconditions.checkArgument(daysAway > 0, "Days away must be positive");
        futureActions.add(new FutureAction(phase,action,priority,daysAway));

    }

    /**
     * This is similar to scheduleAnotherDay except that rather than passing a fixed number of days we pass the probability
     * of the event being scheduled each day after the first (days away is always at least one!)
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param probability the daily probability of this action happening. So if you pass 15% then each day has a probability of 15% of triggering this action
     */
    @Override
    public void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action,
                                                       float probability)
    {

        scheduleAnotherDayWithFixedProbability(phase, action, probability,Priority.STANDARD);



    }

    /**
     * @param probability each day we check against this fixed probability to know if we will step on this action today
     * @param phase       the phase i want the action to occur in
     * @param action      the steppable that should be called
     * @param
     */
    @Override
    public void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action,
                                                       float probability, Priority priority) {
        Preconditions.checkArgument(probability > 0f && probability <=1f, "probability has to be in (0,1]");
        int daysAway = 0;
        do{
            daysAway++;
        }
        while (!randomizer.nextBoolean(probability));

        scheduleAnotherDay(phase,action,daysAway,priority);
    }

    @Override
    public ActionOrder getCurrentPhase() {
        return currentPhase;
    }


    public void clear()
    {
        steppablesByPhase.clear();
        resetMap();


        tomorrowSamePhase.clear();
        futureActions.clear();
        currentPhase = ActionOrder.DAWN;
    }


    public void setSimulationDays(int simulationDays) {
        this.simulationDays = simulationDays;
    }

    public EnumMap<ActionOrder, List<Steppable>[]> getSteppablesByPhase() {
        return steppablesByPhase;
    }

    public EnumMap<ActionOrder, Long> getTimePerPhase() {
        return timePerPhase;
    }
}
