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
import sim.util.Heap;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> The phase scheduler using the Mason HEAP to keep order
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-12
 * @see
 */
public class HeapPhaseScheduler implements PhaseScheduler {

    /**
     * where we store every possible action!
     */
    final private Heap heapScheduler;

    final private MersenneTwisterFast randomizer;

    final private MacroII state;





    private ActionOrder currentPhase = ActionOrder.DAWN;


    public HeapPhaseScheduler(MersenneTwisterFast randomizer, MacroII state) {
        this.randomizer = randomizer;
        this.state= state;

        heapScheduler = new Heap();

    }


    /**
     * Go through a "day" in the model. It self schedules to do it again tomorrow
     */
    @Override
    public void step(SimState simState) {

        //unfortunately HEAP has no peek()
        while(true){
            //check if the day is over!
            ScheduleEntry entry = (ScheduleEntry) heapScheduler.getMinKey();
            if(entry == null || entry.time !=simState.schedule.getTime() )
                break;


            //if not,get the first action on the schedule
            entry = (ScheduleEntry) heapScheduler.extractMin();
            assert entry.time ==  simState.schedule.getTime();
            currentPhase = entry.order; //set current phase!
            //action!
            entry.action.step(simState);
            //next!
        }

        //reschedule yourself for tomorrow
        simState.schedule.scheduleOnceIn(1,this);



    }


    /**
     * schedule the event to happen when the next specific phase comes up!
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    @Override
    public void scheduleSoon(@NonNull ActionOrder phase, @NonNull Steppable action){


        //if it's the same or later than the current phase, schedule it now
        ScheduleEntry entry;

        if(currentPhase.compareTo(phase) <= 0)
        {
            entry = new ScheduleEntry(randomizer.nextInt(),phase,Math.max(state.schedule.getTime(),0d),action);
        }
        else
        {
            //if the current phase has passed, schedule it tomorrow
            entry = new ScheduleEntry(randomizer.nextInt(),phase,Math.max(state.schedule.getTime(),0d)+1d,action);

        }
        heapScheduler.add(entry,entry);



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
        //if the current phase has passed, schedule it tomorrow
        ScheduleEntry entry = new ScheduleEntry(randomizer.nextInt(),phase,Math.max(state.schedule.getTime(),0d)+1d,action);
        heapScheduler.add(entry,entry);


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
        //if the current phase has passed, schedule it tomorrow
        ScheduleEntry entry = new ScheduleEntry(randomizer.nextInt(),phase,Math.max(state.schedule.getTime(),0d)+daysAway,action);
        heapScheduler.add(entry,entry);

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

        scheduleAnotherDay(phase, action, daysAway);



    }



    @Override
    public ActionOrder getCurrentPhase() {
        return currentPhase;
    }


    public void clear()
    {
        heapScheduler.clear();
    }




    final private class ScheduleEntry implements Comparable<ScheduleEntry>
    {
        final private Integer randomKey;

        final private ActionOrder order;

        final private Double time;

        final private Steppable action;


        private ScheduleEntry(Integer randomKey, ActionOrder order, Double time, Steppable action) {
            this.randomKey = randomKey;
            this.order = order;
            this.time = time;
            this.action = action;
        }

        /**
         * Compares by time and if they are equal by actionOrder and if they are equal by random key
         */
        @Override
        public int compareTo(ScheduleEntry o)
        {
            int timeCompare = Double.compare(this.time,o.time);
            if(timeCompare != 0)
                return timeCompare;
            else
            {
                int actionCompare = order.compareTo(o.order);
                if(actionCompare != 0)
                    return actionCompare;


                else
                    return Integer.compare(this.randomKey,o.randomKey);
            }

        }
    }
}
