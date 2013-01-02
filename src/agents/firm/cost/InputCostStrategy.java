package agents.firm.cost;

import com.google.common.base.Preconditions;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.technology.Machinery;

import java.util.Map;
import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/>  This costing strategy consider wages fixed, the unit cost of each good then is:  input/ProductionPerRun
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-01
 * @see
 */
public class InputCostStrategy implements PlantCostStrategy, PlantListener{

    private final Plant plant;

    private int totalProductionPerRun = 0;




    public InputCostStrategy(Plant plant) {
        //this is the plant we are dealing with
        this.plant = plant;
        //listen to it!
        plant.addListener(this);

        //start computing!
        updateProductionPerRun();
    }

    @Override
    public long unitOutputCost(GoodType t, long totalCostOfInputs) {
        //throw an exception if the goodtype is not produced!
        if(!plant.getBlueprint().getOutputs().containsKey(t))
            throw new IllegalArgumentException("Can't assign cost to something we don't completeProductionRunNow!");
        //also throw an exception if totalCostOfInputs is positive in spite of having no inputs
        if(totalCostOfInputs > 0 && plant.getBlueprint().getInputs().isEmpty())
            throw new IllegalArgumentException("Can't have positive input costs when the blueprint has no input!");



        //divide direct costs by type.
        return Math.round((float) totalCostOfInputs / ((float) totalProductionPerRun));
    }


    /**
     * The unit costs of goods if we change the worker number is going to be how much?
     *
     * @param t                 the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @param workers           the new number of workers
     * @param totalWages        the new wages being paid if we have these workers.
     * @return the cost we assign to this good.
     */
    @Override
    public long hypotheticalUnitOutputCost(GoodType t, long totalCostOfInputs, int workers, long totalWages) {
        Preconditions.checkArgument(plant.getBlueprint().getOutputs().containsKey(t),
                "Can't assign cost to something we don't completeProductionRunNow!" );
        Preconditions.checkArgument(totalCostOfInputs >= 0 && plant.getBlueprint().getInputs().isEmpty(),
                "Can't have positive input costs when the blueprint has no input!");


        return Math.round((float) totalCostOfInputs / ((float) plant.totalProductionPerRun()));

    }

    /**
     * For this strategy fixed prices is the amortized fixed costs of the plant PLUS wages
     *
     * @return the costs of running the plant
     */
    @Override
    public long weeklyFixedCosts() {
        return Math.round((float) plant.getBuildingCosts() / (float) plant.getUsefulLife()) + plant.getHr().getWagesPaid();

    }

    /**
     * Updates wage costs
     */
    public void updateProductionPerRun()
    {
        totalProductionPerRun = 0;
        Set<Map.Entry<GoodType,Integer>> outputs = plant.getBlueprint().getOutputs().entrySet();
        for(Map.Entry<GoodType,Integer> output : outputs){
            totalProductionPerRun += Math.round(output.getValue() * plant.getOutputMultiplier(output.getKey()));
        }

        //done
        assert  totalProductionPerRun >= 0;


    }


    /**
     * This is called whenever a plant has changed the number of workers
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        updateProductionPerRun();
    }

    /**
     * Turning off the strategy
     */
    @Override
    public void turnOff() {
        plant.removeListener(this); //stop listening.

    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        //get ready you are going to be turned off soon.

    }


    public int getTotalProductionPerRun() {
        return totalProductionPerRun;
    }

    /**
     * This is called whenever a plant has changed the wages it pays to workers
     *
     * @param wage       the new wage
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {

    }



}
