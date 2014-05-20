/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.hillClimbers;

import ec.util.MersenneTwisterFast;


/**
 * <h4>Description</h4>
 * <p/> A modification of the annealing maximizer, whenever the memory is wrong by more than 15% (or whatever) it resets temperature to .5
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-24
 * @see
 */
public class AnnealingReactingMaximizer extends AnnealingMaximizer {

    private float noiseTreshold = .25f;


    public AnnealingReactingMaximizer(long weeklyFixedCosts, int minimumWorkers, int maximumWorkers,
                                       MersenneTwisterFast random) {
        super(weeklyFixedCosts, minimumWorkers, maximumWorkers, random);
    }

    /**
     * This is called by the "chooseWorkerTarget()" function whenever the old memory is wrong.
     * This method does nothing for the hill-climber but it might be used by subclasses
     */
    @Override
    protected void memoryNoiseEvent(float oldMemory, float newMemory) {

        //find percent change
        float changePercentage = Math.abs((newMemory-oldMemory)/oldMemory);
        //if the change percentage is too much: reset!
        if(changePercentage > noiseTreshold )
        {
            super.setTemperature(Math.min(getTemperature() + changePercentage,.75f));
            super.cleanMemory();
            super.setMisStep(true);
        }

    }

    public float getNoiseTreshold() {
        return noiseTreshold;
    }



    public void setNoiseTreshold(float noiseTreshold)
    {
        this.noiseTreshold = noiseTreshold;

    }
}
