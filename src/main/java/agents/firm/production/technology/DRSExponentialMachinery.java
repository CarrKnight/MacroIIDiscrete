/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.technology;

import agents.EconomicAgent;
import goods.GoodType;
import agents.firm.production.Plant;


/**
 * <h4>Description</h4>
 * <p/> CRS exponential has waiting time delta = alpha * sqrt(number of workers)
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
public class DRSExponentialMachinery extends ExponentialMachinery {



        private float alpha = 0.1f;

    public DRSExponentialMachinery( GoodType type,  EconomicAgent producer, int costOfProduction,  Plant plant, float outputMultiplier, float alpha) {
        super(type, producer, costOfProduction, plant, outputMultiplier);
        this.alpha = alpha;
    }

    public DRSExponentialMachinery( GoodType type,  EconomicAgent producer, int costOfProduction,  Plant plant, float alpha) {
        super(type, producer, costOfProduction, plant);
        this.alpha = alpha;
    }

    @Override
        public float deltaFunction(int workers) {
            return alpha * (float) Math.sqrt(workers);

        }

}
