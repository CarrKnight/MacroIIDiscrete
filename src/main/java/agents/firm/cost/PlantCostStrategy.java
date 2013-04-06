/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.cost;

import goods.GoodType;

/**
 * <h4>Description</h4>
 * <p/> This is the class represents how the plant decides on how to compute the cost of production for the goods
 * it makes
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
public interface PlantCostStrategy {

    /**
     * The production of ONE good of this specific type costs how much?
     * @param t the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @return the cost we assign to this good.
     */
    public long unitOutputCost(GoodType t, long totalCostOfInputs);

    /**
     * The unit costs of goods if we change the worker number is going to be how much?
     * @param t the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @param workers the new number of workers
     * @param totalWages the new wages being paid if we have these workers.
     * @return the cost we assign to this good.
     */
    public long hypotheticalUnitOutputCost(GoodType t, long totalCostOfInputs, int workers, long totalWages);

    /**
     * The fixed costs associated with having the plant. These could be the ammortized costs of building the plant, wages if considered quasi-fixed and so on.
     * @return the costs of running the plant
     */
    public long weeklyFixedCosts();

    /**
     * Turning off the strategy
     */
    public void turnOff();
}
