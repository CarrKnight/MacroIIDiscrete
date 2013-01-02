package agents.firm.production.technology;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Plant;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * <h4>Description</h4>
 * <p/> This is
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
public abstract class Machinery extends Good {


    protected Machinery(@Nonnull GoodType type,@Nonnull EconomicAgent producer, long costOfProduction) {
        super(type, producer, costOfProduction);
        assert type.isMachinery();
    }

    /**
     * Does this technology produces a multiple of the output quantity in the blueprint? Remember that it is ALWAYS floored
     * @return the multiplier
     */
    public abstract float getOutputMultiplier(GoodType outputType);

    /**
     * How much time has to pass between the production of the last batch and the production of the new one?
     * @return time, in float
     */
    public abstract float expectedWaitingTime();

    /**
     * By how much will a new worker increase weekly production
     */
    public abstract float marginalProductOfWorker(GoodType outputType);

    /**
     * Returns how many additional production runs a new worker would accomplish
     * @return the additional production runs adding a new worker would cause
     */
    public abstract float marginalProductionRuns();

    /**
     * Returns how many additional inputs of a specific kind we would need with a new worker
     *
     * @param inputType the input kind
     * @return how many additional inputs of a specific kind we would need with a new worker
     */
    public float marginalInputRequirements(GoodType inputType) {

        Preconditions.checkArgument(getPlant().getBlueprint().getInputs().containsKey(inputType));

        //get marginal production runs
        float marginalProductionRuns = marginalProductionRuns();
        return getPlant().getBlueprint().getInputs().get(inputType) * marginalProductionRuns;

    }

    /**
     * How much total output gets produced each production run (this is usually just the blueprint value times output multiplier)
     */
    public int totalProductionPerRun()
    {
        int totalProduction=0;
        //sum up all the output production
        for(Map.Entry<GoodType,Integer> output : getPlant().getBlueprint().getOutputs().entrySet())
        {
            totalProduction += (int)(output.getValue() *  getOutputMultiplier(output.getKey()));
        }

        return totalProduction;
    }

    /**
     * How much specific output gets produced each production run (this is usually just the blueprint value times output multiplier)
     *
     * @param outputType the specific good we want to know how much we completeProductionRunNow of
     */
    public int totalProductionPerRun(@Nonnull GoodType outputType) {
        Preconditions.checkArgument(getPlant().getBlueprint().getOutputs().containsKey(outputType), "can't have production for goods that aren't output");
        return (int) (getPlant().getBlueprint().getOutputs().get(outputType) * getOutputMultiplier(outputType));

    }

    /**
     * How many inputs of this specific kind will I have to buy/use given these many workers?
     * @param inputType the kind of input we are interested in
     * @param workerSize the hypothetical size of the workforce
     * @return the number of inputs we will consume in a week
     */
    public int hypotheticalWeeklyInputNeeds(@Nonnull GoodType inputType, int workerSize)
    {
        Preconditions.checkArgument(getPlant().getBlueprint().getInputs().containsKey(inputType), "the type provided is not even an input!");
        //this is easy: just number of production runs times the amount of input burned each run.
        return (int) (getPlant().getBlueprint().getInputs().get(inputType) * hypotheticalWeeklyProductionRuns(workerSize));

    }

    /**
     * Total amount of weekly production expected by this technology
     */
    public abstract float weeklyThroughput(@Nonnull GoodType outputType);

    /**
     * How much of a specific good will be produced in a week given this many workers
     * @param workers the number of workers
     * @param type the goodtype
     * @return
     */
    public float hypotheticalThroughput(int workers,@Nonnull GoodType type)
    {
        Preconditions.checkArgument(getPlant().getBlueprint().getOutputs().containsKey(type), "can't have production for goods that aren't output");
        return hypotheticalWeeklyProductionRuns(workers) * totalProductionPerRun(type);

    }

    /**
     * The sum of the number of goods being produced in a week (regadless of the kind of good) for a given number of workers
     *
     */
    public float hypotheticalTotalThroughput(int workers){
        /**
         * Just expected number of production runs times the production per run
         */
        return hypotheticalWeeklyProductionRuns(workers) * totalProductionPerRun();

    }

    /**
     * What would be the waiting time if we had this many workers?
     */
    public abstract float hypotheticalWaitingTime(int workers);

    /**
     * How many production runs do we expect to carry out in a week, if we had this many workers?
     */
    public abstract float hypotheticalWeeklyProductionRuns(int workers);

    /**
     * How many production runs we expect to carry out in a week given the workers we have in the plan?
     * @return
     */
    public abstract float expectedWeeklyProductionRuns();

    /**
     * Production has started, tell me when it will be ready!
     * @return
     */
    public abstract float nextWaitingTime();

    /**
     * How many workers are needed for this technology to even work?
     * @return the number of workers below which the plant can't operate.
     */
    public abstract int minimumWorkersNeeded();


    /**
     * How many workers are needed for this technology to even work?
     * @return the number of workers above which the plant can't operate.
     */
    public abstract int maximumWorkersPossible();

    /**
     * A getter for the plant using the machinery
     */
    public abstract Plant getPlant();

}
