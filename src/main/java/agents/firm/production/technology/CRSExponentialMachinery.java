/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.technology;

import agents.EconomicAgent;
import goods.GoodType;
import agents.firm.production.Plant;


/**
 * <h4>Description</h4>
 * <p/> CRS exponential has waiting time delta = alpha * number of workers
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
public class CRSExponentialMachinery extends ExponentialMachinery {

    private float alpha = 0.1f;

    public CRSExponentialMachinery( GoodType type,  EconomicAgent producer, int costOfProduction,  Plant plant, float outputMultiplier, float alpha) {
        super(type, producer, costOfProduction, plant, outputMultiplier);
        this.alpha = alpha;
    }

    public CRSExponentialMachinery( GoodType type,  EconomicAgent producer, int costOfProduction,  Plant plant, float alpha) {
        super(type, producer, costOfProduction, plant);
        this.alpha = alpha;
    }



    @Override
    public float deltaFunction(int workers) {
        return alpha * workers;
    }
}
