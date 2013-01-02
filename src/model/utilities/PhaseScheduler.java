package model.utilities;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.EnumMap;

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
public class PhaseScheduler implements Steppable {

    /**
     * where we store every possible action!
     */
    private EnumMap<ActionOrder,ArrayList<Steppable>> steppablesByPhase;




    private final int simulationDays;

    private ActionOrder currentPhase;

    final private ArrayList<Steppable> tomorrowSamePhase;


    public PhaseScheduler(int simulationDays) {
        this.simulationDays = simulationDays;


        //initialize the enums
        steppablesByPhase = new EnumMap<ActionOrder, ArrayList<Steppable>>(ActionOrder.class);
        for(ActionOrder order : steppablesByPhase.keySet())
        {
            steppablesByPhase.put(order,new ArrayList<Steppable>());
        }

        //initialize tomorrow schedule
        tomorrowSamePhase = new ArrayList<>();

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

            ArrayList<Steppable> steppables = steppablesByPhase.get(phase);
            assert steppables != null;

            //while there are actions to take this phase, take them
            while(!steppables.isEmpty())
            {
                Steppable steppable = steppables.remove(random.nextInt(steppables.size()));
                //act nau!!!
                steppable.step(simState);
            }
            //schedule stuff to happen tomorrow
            steppables.addAll(tomorrowSamePhase);
            tomorrowSamePhase.clear(); //here we kept all steppables that are called to act the same phase tomorrow!


            //go to the next phase!

        }


        assert tomorrowSamePhase.isEmpty() : "things shouldn't be scheduled here";

        //see you tomorrow
        if(simState.schedule.getTime() <= simulationDays)
            simState.schedule.scheduleOnceIn(1,this);



    }

    /**
     * schedule the event to happen when the next specific phase comes up!
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    public void scheduleSoon(ActionOrder phase, Steppable action){

        //put it among the steppables of that phase
        steppablesByPhase.get(phase).add(action);

    }

    /**
     * force the schedule to record this action to happen tomorrow.
     * This is allowed only if you are at a phase (say PRODUCTION) and you want the action to occur tomorrow at the same phase (PRODUCTION)
     * @param phase which phase the action should be performed?
     * @param action the action taken!
     */
    public void scheduleTomorrow(ActionOrder phase,Steppable action){

        Preconditions.checkArgument(phase.equals(currentPhase));
        //put it among the steppables of that phase
        tomorrowSamePhase.add(action);

    }


    public ActionOrder getCurrentPhase() {
        return currentPhase;
    }


}
