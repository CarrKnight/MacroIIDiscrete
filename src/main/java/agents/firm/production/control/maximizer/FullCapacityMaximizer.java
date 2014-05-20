/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.technology.Machinery;

/**
 * <h4>Description</h4>
 * <p/> This maximizer is extremely simple: it sets the target to the maximum number of workers and NEVER changes it (except when machinery is changed)
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
public class FullCapacityMaximizer<ALG extends WorkerMaximizationAlgorithm> extends BaseWorkforceMaximizer<ALG> {


    /**
     * the plant control we are part of
     */
    private PlantControl control;

    /**
     * Create a new full capacity
     * @param hr
     * @param control
     * @param ignored not used.
     */
    public  FullCapacityMaximizer(HumanResources ignored, PlantControl control,
                                                                           Class<ALG> ignored2) {
        this.control = control;
    }



    /**
     * Method to start the workforce maximizer
     */
    @Override
    public void start() {
        //set target to max
        control.setTarget(control.getHr().getPlant().maximumWorkersPossible());

    }



    /**
     * This is called whenever a plant has changed the number of workers
     *
     * @param p          the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        assert control.getHr().getPlant() == p;

        //don't care
    }

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     *
     * @param wage       the new wage
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
        assert control.getHr().getPlant() == p;

        //don't care
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        assert control.getHr().getPlant() == p;
        //don't care
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        //set target to max (as the max may have shifted during takeoff)
        control.setTarget(control.getHr().getPlant().maximumWorkersPossible());

    }
}
