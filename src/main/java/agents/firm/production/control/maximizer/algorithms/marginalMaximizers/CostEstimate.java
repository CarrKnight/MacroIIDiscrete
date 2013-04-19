/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

/**
 * This is a simple struct class that holds the result of a marginalCost estimation. It holds both the marginal cost expected
 * and the total cost expected
 */
public class CostEstimate
{

    final private long marginalCost;

    final private long totalCost;

    public CostEstimate(long marginalCost, long totalCost) {
        this.marginalCost = marginalCost;
        this.totalCost = totalCost;
    }

    final public long getMarginalCost() {
        return marginalCost;
    }

    final public long getTotalCost() {
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
