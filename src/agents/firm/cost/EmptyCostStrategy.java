package agents.firm.cost;

import goods.GoodType;

/**
 * This is the "empty" class
 * User: carrknight
 * Date: 7/14/12
 * Time: 3:49 PM
 */
public class EmptyCostStrategy implements PlantCostStrategy{

    @Override
    public long unitOutputCost(GoodType t, long totalCostOfInputs) {
        return 0;
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
        return  0;
    }

    /**
     * The fixed costs associated with having the plant. These could be the ammortized costs of building the plant, wages if considered quasi-fixed and so on.
     *
     * @return the costs of running the plant
     */
    @Override
    public long weeklyFixedCosts() {
        return 0;
    }

    /**
     * Turning off the strategy
     */
    @Override
    public void turnOff() {

    }
}
