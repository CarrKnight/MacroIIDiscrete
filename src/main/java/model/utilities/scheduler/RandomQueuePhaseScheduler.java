/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import com.google.common.base.Preconditions;
import com.sun.javafx.beans.annotations.NonNull;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> I try to maintain an iron grip over the schedule calls. I prefer my objects registering actions here and then just stepping this class instead
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-20
 * @see
 */
public class RandomQueuePhaseScheduler implements Steppable, PhaseScheduler {

    /**
     * where we store every possible action!
     */
    private EnumMap<ActionOrder,RandomQueue<Steppable>> steppablesByPhase;

    private MersenneTwisterFast randomizer;




    private final int simulationDays;

    private ActionOrder currentPhase = ActionOrder.DAWN;

    final private ArrayList<Steppable> tomorrowSamePhase;

    final private ArrayList<FutureAction> futureActions;


    public RandomQueuePhaseScheduler(int simulationDays, MersenneTwisterFast randomizer) {
        this.simulationDays = simulationDays;
        this.randomizer = randomizer;


        //initialize the enums
        steppablesByPhase = new EnumMap<>(ActionOrder.class);
        for(ActionOrder order :ActionOrder.values())
        {
            steppablesByPhase.put(order,new RandomQueue<Steppable>(randomizer));
        }

        //initialize tomorrow schedule
        tomorrowSamePhase = new ArrayList<>();

        futureActions = new ArrayList<>();

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
            currentPhase = phase; //currentPhase!

            RandomQueue<Steppable> steppables = steppablesByPhase.get(phase);
            assert steppables != null;

            //while there are actions to take this phase, take them
            while(!steppables.isEmpty())
            {
                Steppable steppable = steppables.poll();
                assert steppable != null;
                //act nau!!!
                steppable.step(simState);
            }
            //schedule stuff to happen tomorrow
            steppables.addAll(tomorrowSamePhase);
            tomorrowSamePhase.clear(); //here we kept all steppables that are called to act the same phase tomorrow!


            //go to the next phase!

        }

        //prepare for tomorrow
        prepareForTomorrow();

        //see you tomorrow
        if(simState.schedule.getTime() <= simulationDays)
            simState.schedule.scheduleOnceIn(1,this);



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
                scheduleSoon(futureAction.getPhase(),futureAction.getAction());
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
    public void scheduleSoon(@NonNull ActionOrder phase, @NonNull Steppable action){

        //put it among the steppables of that phase
        steppablesByPhase.get(phase).add(action);

    }

    /**
     * force the schedule to record this action to happen tomorrow.
     * This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    @Override
    public void scheduleTomorrow(ActionOrder phase, Steppable action){

        Preconditions.checkArgument(phase.equals(currentPhase));
        //put it among the steppables of that phase
        tomorrowSamePhase.add(action);

    }

    /**
     *
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param daysAway how many days from now should it be scheduled
     */
    @Override
    public void scheduleAnotherDay(@Nonnull ActionOrder phase, @Nonnull Steppable action,
                                   int daysAway)
    {
        Preconditions.checkArgument(daysAway > 0, "Days away must be positive");
        futureActions.add(new FutureAction(phase,action,daysAway));

    }

    /**
     * This is similar to scheduleAnotherDay except that rather than passing a fixed number of days we pass the probability
     * of the event being scheduled each day after the first (days away is always at least one!)
     * @param phase The action order at which this action should be scheduled
     * @param action the action to schedule
     * @param probability the daily probability of this action happening. So if you pass 15% then each day has a probability of 15% of triggering this action
     */
    @Override
    public void scheduleAnotherDayWithFixedProbability(@Nonnull ActionOrder phase, @Nonnull Steppable action,
                                                       float probability)
    {
        Preconditions.checkArgument(probability > 0f && probability <=1f, "probability has to be in (0,1]");
        int daysAway = 0;
        do{
            daysAway++;
        }
        while (!randomizer.nextBoolean(probability));

        scheduleAnotherDay(phase,action,daysAway);



    }



    @Override
    public ActionOrder getCurrentPhase() {
        return currentPhase;
    }


    public void clear()
    {
        steppablesByPhase.clear();
        steppablesByPhase = new EnumMap<>(ActionOrder.class);
        for(ActionOrder order :ActionOrder.values())
        {
            steppablesByPhase.put(order,new RandomQueue<Steppable>(randomizer));
        }

        tomorrowSamePhase.clear();
        futureActions.clear();
        currentPhase = ActionOrder.DAWN;
    }




}
