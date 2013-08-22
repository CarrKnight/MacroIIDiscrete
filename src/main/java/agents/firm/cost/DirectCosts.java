/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.cost;

import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.technology.Machinery;

import java.util.Map;
import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> This strategy accounts for costs as following:
 * <ul>
 *     <li>
 *         Fixed costs are amortized plant costs
 *     </li>
 *     <li>
 *         unit costs are made by inputCosts/productionPerRun + weekly.wages/weekly.throughput
 *     </li>
 *     <li>
 *         Wage cost are (totalWages  ) / ( expectedProductionRuns in a week )
 *     </li>
 * </ul>
 * <p/> It's important to know that this computation(except for direct costs) is not done each time unitOutputCost is called. This strategy implements plant listener to update the components of the costs.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-23
 * @see
 */
public class DirectCosts implements PlantCostStrategy, PlantListener {

    private final Plant plant;

    private long wageCostsPerUnit=0;

    private int totalProductionPerRun = 0;




    public DirectCosts(Plant plant) {
        //this is the plant we are dealing with
        this.plant = plant;
        //listen to it!
        plant.addListener(this);

        //start computing!
        updateProductionPerRun();
        updateWageCosts();

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
        long unitInputCost = Math.round((float) totalCostOfInputs / ((float) totalProductionPerRun));
        //add wage direct costs!
        return unitInputCost + wageCostsPerUnit;
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

        long hypotheticalWageCost = Math.round( ((float)totalWages) / ( plant.hypotheticalTotalThroughput(workers)));
        long hypotheticalInputCosts =  Math.round((float) totalCostOfInputs  / plant.hypotheticalTotalThroughput(workers) );
        return hypotheticalInputCosts + hypotheticalWageCost;
    }

    /**
     * The fixed costs associated with having the plant. These could be the ammortized costs of building the plant, wages if considered quasi-fixed and so on.
     *
     * @return the costs of running the plant
     */
    @Override
    public long weeklyFixedCosts() {
        return Math.round((float) plant.getBuildingCosts() / (float) plant.getUsefulLife());
    }

    /**
     * Updates wage costs
     */
    public void updateWageCosts()
    {
        long totalWages = plant.getHr().getWagesPaid();

        //divide the wages by expected production runs
        wageCostsPerUnit =  Math.round((float)totalWages / plant.hypotheticalTotalThroughput(plant.getNumberOfWorkers()) );

        assert  totalProductionPerRun >= 0;
        assert wageCostsPerUnit >= 0;

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
        updateWageCosts();
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
        updateWageCosts();
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

    public long getWageCostsPerUnit() {
        return wageCostsPerUnit;
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
        updateWageCosts();

    }
}
