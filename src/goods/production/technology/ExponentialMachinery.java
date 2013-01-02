package goods.production.technology;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import goods.production.Plant;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is the exponential technology plant: all we need to do to now is to implement the delta function to make it CRS, DRS or IRS
 *
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
public abstract class ExponentialMachinery extends Machinery {

    private float outputMultiplier = 1f;

    @Nonnull
    final private Plant plant;

    protected ExponentialMachinery(@Nonnull GoodType type, @Nonnull EconomicAgent producer, long costOfProduction, @Nonnull Plant plant, float outputMultiplier) {
        super(type, producer, costOfProduction);
        this.plant = plant;
        this.outputMultiplier = outputMultiplier;
    }

    protected ExponentialMachinery(@Nonnull GoodType type, @Nonnull EconomicAgent producer, long costOfProduction, @Nonnull Plant plant) {
        this(type,producer,costOfProduction,plant,1f);
    }


    public abstract float deltaFunction(int workers);


    /**
     * How much is the expected time of production?
     */
    @Override
    public float hypotheticalWaitingTime(int workers) {
        return 1f/(deltaFunction(workers));

    }

    /**
     * How many production runs we expect to perform in a week?
     */
    @Override
    public float hypotheticalWeeklyProductionRuns(int workers) {
        return deltaFunction(workers) * plant.getModel().getWeekLength();
    }


    /**
     * The multiplier increases the production of each good over the basic value of the blueprint
     */
    @Override
    public float getOutputMultiplier(GoodType outputType) {
        //make sure we are asking for something that makes sense
        if(!plant.getBlueprint().getOutputs().containsKey(outputType))
            throw new IllegalArgumentException("Asking for the multiplier of something we don't produce");

        else
            return outputMultiplier;

    }

    /**
     * How much do we expect our production time to be?
     * @return
     */
    @Override
    public float expectedWaitingTime() {
        return hypotheticalWaitingTime(plant.workerSize());
    }


    /**
     * How many times are we going to start production given the number of workers in this plant
     * @return expected weekly production
     */
    public float expectedWeeklyProductionRuns(){
        return hypotheticalWeeklyProductionRuns(plant.workerSize());
    }

    /**
     * How much are we going to produce in a week, on average?
     * @param outputType
     * @return
     */
    @Override
    public float weeklyThroughput(GoodType outputType) {
        Preconditions.checkArgument(plant.getBlueprint().getOutputs().containsKey(outputType),"Expecting production of the wrong good!");
        if(plant.workerSize() <minimumWorkersNeeded())
            return 0;
        else
        //expected weekly production is poisson times blueprint multipliers
            return getOutputMultiplier(outputType) * plant.getBlueprint().getOutputs().get(outputType) *
                    expectedWeeklyProductionRuns();



    }

    @Override
    public float marginalProductOfWorker(GoodType outputType) {
        //check it makes sense
        if(!plant.getBlueprint().getOutputs().containsKey(outputType))
            throw new IllegalArgumentException("Expecting production of the wrong good!");
        if(plant.workerSize() ==0)
            return getOutputMultiplier(outputType) * plant.getBlueprint().getOutputs().get(outputType) * hypotheticalWeeklyProductionRuns(1);
        else
        {

            assert plant.workerSize() > 0;

            return getOutputMultiplier(outputType) * plant.getBlueprint().getOutputs().get(outputType) * (
                    hypotheticalWeeklyProductionRuns(plant.workerSize() + 1) - hypotheticalWeeklyProductionRuns(plant.workerSize())) ;

        }
    }



    /**
     * This is the quantile draw for exponential
     */
    public static float drawExponential(MersenneTwisterFast random, double lambda){
        float draw=  (float) (-Math.log(1f-random.nextFloat())/lambda);

        //sometimes it goes crazy so try to rein in ridiculous numbers
        if(draw > 10000){
            System.err.println("what?" + draw + "---" + lambda);
            return drawExponential(random,lambda);
        }
        else
            return draw;


    }

    public float getOutputMultiplier() {
        return outputMultiplier;
    }

    public void setOutputMultiplier(float outputMultiplier) {
        this.outputMultiplier = outputMultiplier;
    }

    @Override
    public float nextWaitingTime() {
        assert plant.checkForWorkers() && plant.workerSize() > 0;
        return drawExponential(plant.getRandom(),deltaFunction(plant.workerSize()));

    }


    @Override
    public int minimumWorkersNeeded() {
        return 1;
    }

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
        return hypotheticalWeeklyProductionRuns(plant.workerSize() + 1) - hypotheticalWeeklyProductionRuns(plant.workerSize() + 1);

    }


    @Nonnull
    public Plant getPlant() {
        return plant;
    }
}

