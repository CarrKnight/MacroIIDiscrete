package agents.firm.production.technology;

import agents.EconomicAgent;
import goods.GoodType;
import agents.firm.production.Plant;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is the simplest kind of machinery. Production time is just (a tenth of a weeklenght)/numberOfWorkers. Unlike other machineries, production time is not random.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-18
 * @see
 */
public class LinearConstantMachinery extends Machinery {


    @Nonnull
    final private Plant plant;

    private float oneWorkerThroughput;

    private float oneWorkerProductionTime = 7f;

    public LinearConstantMachinery(@Nonnull GoodType type, @Nonnull EconomicAgent producer, long costOfProduction, @Nonnull Plant plant) {
        super(type, producer, costOfProduction);
        this.plant = plant;
        oneWorkerThroughput = ((float)plant.getModel().getWeekLength()) / oneWorkerProductionTime;
    }





    /**
     * Does this technology produces a multiple of the output quantity in the blueprint? Remember that it is ALWAYS floored
     *
     * @return the multiplier
     */
    @Override
    public float getOutputMultiplier(GoodType outputType) {
        return 1;
    }

    /**
     * How much time has to pass between the production of the last batch and the production of the new one?
     *
     * @return time, in float
     */
    @Override
    public float expectedWaitingTime() {
        return hypotheticalWaitingTime(plant.getWorkers().size());
    }

    /**
     * By how much will a new worker increase weekly production
     */
    @Override
    public float marginalProductOfWorker(GoodType outputType)
    {
        Integer blueprintProduction = plant.getBlueprint().getOutputs().get(outputType);
        if(blueprintProduction == null ||  blueprintProduction <= 0)
            throw new IllegalArgumentException("asked marginal product on a good we don't completeProductionRunNow");

        //current production:
        float current = expectedWeeklyProductionRuns();
        //with an additional worker:
        float future =  hypotheticalWeeklyProductionRuns(plant.getWorkers().size() + 1);

        assert future > current: "this production function is strictly monotonic.";
        return blueprintProduction.floatValue() * (future - current);

    }

    /**
     * Total amount of weekly production expected by this technology
     */
    @Override
    public float weeklyThroughput(GoodType outputType) {
        Integer blueprintProduction = plant.getBlueprint().getOutputs().get(outputType);
        if(blueprintProduction == null ||  blueprintProduction <= 0)
            throw new IllegalArgumentException("asked marginal product on a good we don't completeProductionRunNow");
        if(plant.workerSize() <=0)
            throw new IllegalStateException("Can't completeProductionRunNow with no workers!");

        return expectedWeeklyProductionRuns() * blueprintProduction.floatValue();


    }

    /**
     * What would be the waiting time if we had this many workers?
     */
    @Override
    public float hypotheticalWaitingTime(int workers) {
        return oneWorkerThroughput /(float)workers;
    }

    /**
     * How many production runs do we expect to carry out in a week, if we had this many workers?
     */
    @Override
    public float hypotheticalWeeklyProductionRuns(int workers) {
        if(workers == 0)
            return  0;
        else
            return plant.getModel().getWeekLength() / hypotheticalWaitingTime(workers);
    }

    /**
     * How many production runs we expect to carry out in a week given the workers we have in the plan?
     *
     * @return
     */
    @Override
    public float expectedWeeklyProductionRuns() {
        return hypotheticalWeeklyProductionRuns(plant.getWorkers().size());
    }

    /**
     * Production has started, tell me when it will be ready!
     *
     * @return
     */
    @Override
    public float nextWaitingTime() {
        return oneWorkerThroughput /(float)plant.getWorkers().size();
    }

    /**
     * How many workers are needed for this technology to even work?
     *
     * @return the number of workers below which the plant can't operate.
     */
    @Override
    public int minimumWorkersNeeded() {
        return 1;
    }

    /**
     * How many workers are needed for this technology to even work?
     *
     * @return the number of workers above which the plant can't operate.
     */
    @Override
    public int maximumWorkersPossible() {
        return 100;
    }

    /**
     * Returns how many additional production runs a new worker would accomplish
     *
     * @return the additional production runs adding a new worker would cause
     */
    @Override
    public float marginalProductionRuns() {
        return hypotheticalWeeklyProductionRuns(plant.workerSize() + 1) - hypotheticalWeeklyProductionRuns(plant.workerSize());

    }

    public float getOneWorkerThroughput() {
        return oneWorkerThroughput;
    }

    public void setOneWorkerThroughput(float oneWorkerThroughput) {
        this.oneWorkerThroughput = oneWorkerThroughput;
    }

    @Nonnull
    public Plant getPlant() {
        return plant;
    }

    public float getOneWorkerProductionTime() {
        return oneWorkerProductionTime;
    }

    public void setOneWorkerProductionTime(float oneWorkerProductionTime) {
        this.oneWorkerProductionTime = oneWorkerProductionTime;
        oneWorkerThroughput = ((float)plant.getModel().getWeekLength()) / oneWorkerProductionTime;
    }
}
