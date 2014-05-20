/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

/**
 * This is a simple struct class that holds the result of a marginalCost estimation. It holds both the marginal cost expected
 * and the total cost expected
 */
public class CostEstimate
{

    final private float marginalCost;

    final private float totalCost;

    public CostEstimate(float marginalCost, float totalCost) {
        this.marginalCost = marginalCost;
        this.totalCost = totalCost;
    }

    final public float getMarginalCost() {
        return marginalCost;
    }

    final public float getTotalCost() {
        return totalCost;
    }

    @Override
    public String toString() {
        return "CostEstimate{" +
                "marginalCost=" + marginalCost +
                ", totalCost=" + totalCost +
                '}';
    }
}
