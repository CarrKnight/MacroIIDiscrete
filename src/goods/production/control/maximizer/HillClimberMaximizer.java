package goods.production.control.maximizer;

import agents.firm.personell.HumanResources;
import com.google.common.base.Preconditions;
import goods.production.Plant;
import goods.production.control.PlantControl;
import goods.production.technology.Machinery;

import java.util.HashMap;
import java.util.Map;

/**
 * <h4>Description</h4>
 * <p/> This is the plant control weekly workforce maximizer that acts as an hillclimber to choose its targets. It has a simple memory to lookup when to stop. It never forgets
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-24
 * @see
 */
public class HillClimberMaximizer extends weeklyWorkforceMaximizer {

    /**
     * Here we keep the memory of the MOST RECENT profit observation at a specific worker target
     */
    private Map<Integer,Float> profitMemory;

    /**
     * What was your direction last turn? (if profits are flat we continue in this direction)
     */
    private  int oldDirection;


    /**
     * Asks the subclass what the next worker target will be!
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {
        //put the new result in memory
        Float oldMemory = profitMemory.put(currentWorkerTarget,newProfits);
        boolean memoryHasChanged = oldMemory != null && oldMemory != newProfits;   //memory changed flag activates a memory search if futuretarget == currentTargetr
        if(memoryHasChanged)
            memoryNoiseEvent(oldMemory,newProfits); //notify subclasses, if needed

        //if you need more time to think, return -1
        if(needMoreTime())
            return -1;


        //get the new direction
        int direction = direction(currentWorkerTarget,newProfits,oldWorkerTarget,oldProfits);
        //direction can only have 3 values
        Preconditions.checkState(direction == 0 || direction == 1 || direction == -1);
        //remember it
        oldDirection = direction;

        //get new adjust-size
        int stepSize = stepSize(currentWorkerTarget,newProfits, oldWorkerTarget,oldProfits);
        //I used to make sure this wasn't negative, but why? I can think a negative stepsize would come useful.
        //Preconditions.checkState(stepSize >=0);

        //compute the new target
        int futureTarget = currentWorkerTarget + stepSize * direction;

        //if future target is just today's target
        if(futureTarget == currentWorkerTarget)
        {
            //then unless there have been weird variations in profit results just return the usual, it means you are at optimum
            if(memoryHasChanged || (oldWorkerTarget == currentWorkerTarget && oldProfits != newProfits))
            {
                //weird variation, make a check
                int memorySearch =  getBestWorkerTargetInMemory();
                //return memory search unless it's negative (which means that the memory search should be ignored)
                return memorySearch < 0 ? futureTarget : memorySearch;
            }
            return futureTarget;
        }
        //if target DID change:
        else{
            //bound it to positives
            futureTarget=Math.max(futureTarget,0);

            //bound it to technological constraints
            futureTarget = Math.min(futureTarget,getControl().getHr().getPlant().maximumWorkersPossible());
            if(futureTarget > 0)
                futureTarget = Math.max(futureTarget,getControl().getHr().getPlant().minimumWorkersNeeded());

            //check if memory says yes
            if(checkMemory(futureTarget,newProfits))
                return futureTarget;
            else
                return currentWorkerTarget; //otherwise stay where you are!!


        }
    }

    /**
     * Do I want more time to consider the results? Default is no
     * @return false
     */
    protected boolean needMoreTime(){
        return false;
    }

    /**
     * Should I increase or decrease worker targets?  For hillclimber that's just ( sign(newProfits - oldProfits) * sign(currentTarget - oldTarget)). <br>
     * @return +1 if I should increase, -1 if I should decrease or 0 if I should stay the same. Any other result throws an exception
     */
    protected int direction(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits){
        //( sign(newProfits - oldProfits) * sign(currentTarget - oldTarget))
        int  increasedProfits = (int) Math.signum(newProfits - oldProfits);
        //if profits are unchanged, go the same direction
        if(increasedProfits == 0 )
            return oldDirection;
        //otherwise continue with the equation
        int increasedWorkers = Integer.signum(currentWorkerTarget - oldWorkerTarget);
        return increasedProfits * increasedWorkers;
    }

    /**
     * This should return the absolute value of the difference between futureTarget - currentTarget.
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the return can't be negative or an exception is thrown!!!
     */
    protected int stepSize(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits)
    {
        return 1;

    }

    /**
     * Create the hillclimber maximizer
     * @param hr the human resources object
     * @param control the controller it is attached to
     */
    public HillClimberMaximizer(HumanResources hr, PlantControl control) {
        super(hr, control);
        //instantiate memory
        profitMemory = new HashMap<>();
        profitMemory.put(0, (float) -getHr().getPlant().weeklyFixedCosts());
        //you "increase" from 0 to 1
        oldDirection = 1;

    }

    /**
     * If we change machinery all our memory is useless
     * @param p the plant that did the change
     * @param machinery the new machinery
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {

        assert p == getControl().getHr().getPlant();

        profitMemory.clear();
        profitMemory.put(0,-(float) getControl().getHr().getPlant().weeklyFixedCosts()); //add the fixed cost as loss if you target workers to go to 0
        //start over
        this.start();

        super.changeInMachineryEvent(p, machinery);

    }

    /**
     * given the future target does our memory suggests that profits are better at the current target?
     * @return true if the future target is not in memory or the memory of it is BETTER than the current profits
     */
    public boolean checkMemory(int futureTarget, float currentProfits)
    {
        //do we have it in memory?
        if(!profitMemory.containsKey(futureTarget))
            return true;
        //we do, then allow the new target only if it makes equal or better profits
        return profitMemory.get(futureTarget) >= currentProfits;

    }


    /**
     * Memory search!
     * This is called when direction is 0: the future target is the same as today's target.
     * This makes sure that in memory we got nothing better.
     * @return  the worker target that achieved the highest profits
     */

    public int getBestWorkerTargetInMemory(){

        assert  !(profitMemory.isEmpty()); //can't be empty, at worst there is 0

        float maxProfits = Float.NEGATIVE_INFINITY; int max = -1;
        //search in your memory for the maximum
        for(Map.Entry<Integer,Float> entry : profitMemory.entrySet())
        {
            if(entry.getValue() > maxProfits)
            {
                //memorize this as new max
                maxProfits = entry.getValue();
                max = entry.getKey();
            }
        }
        //return it
        assert max >=0;
        assert maxProfits > Float.NEGATIVE_INFINITY;
        return max;

    }


    /**
     * Method to switch the strategy off. Irreversible
     */
    @Override
    public void turnOff() {
        //clear memory
        profitMemory.clear();

    }

    /**
     * This is called by the "chooseWorkerTarget()" function whenever the old memory is wrong.
     * This method does nothing for the hill-climber but it might be used by subclasses
     */
    protected void memoryNoiseEvent(float oldMemory, float newMemory)
    {

    }


    /**
     * Clears out the whole memory
     */
    public void cleanMemory()
    {
        profitMemory.clear();
        profitMemory.put(0,(float)-getHr().getPlant().weeklyFixedCosts());

    }
}

