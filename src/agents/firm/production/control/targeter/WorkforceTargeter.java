/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.targeter;

import agents.firm.production.PlantListener;

/**
 * <h4>Description</h4>
 * <p/> One of the component of the improved PlantControl. This one is the "high frequency" part: it is told how many workers should be working at the plant and tell hr the wage to set
 * <p/> Although it's a plant listener it doesn't listen directly to the plant, rather it's the container that passes messages through.
 * <h4>Notes</h4>
 * <p/> All sub-classes MUST have a constructor with arguments HumanResources,TargetAndMaximizePlantControl!!!
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-22
 * @see
 */
public interface WorkforceTargeter extends PlantListener {

    /**
     * The strategy is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    public void setTarget(int workerSizeTargeted);

    /**
     * Ask the strategy what is the current worker target
     * @return the number of workers the strategy is targeted to find!
     */
    public int getTarget();


    /**
     * This is called by the plant control when it is started.
     */
    public void start();

    /**
     * This is called when the object stops being useful. Irreversible
     */
    public void turnOff();



}
