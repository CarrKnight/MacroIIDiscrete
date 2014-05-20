/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production;

import agents.firm.production.technology.Machinery;

/**
 * <h4>Description</h4>
 * <p/> This interface is there for objects to react to changes in worker size or technology from plants.
 * This is particularly useful for inventory control
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-05
 * @see
 */
public interface PlantListener {


    /**
     * This is called whenever a plant has changed the number of workers
     * @param p the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore);

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     * @param wage the new wage
     * @param p the plant that made the change
     * @param workerSize the new number of workers
     */
    public void changeInWageEvent(Plant p, int workerSize, long wage);


    /**
     * This is called whenever a plant has been shut down or just went obsolete
     * @param p the plant that made the change
     */
    public void plantShutdownEvent(Plant p);


    /**
     * This is called by the plant whenever the machinery used has been changed
     * @param p The plant p
     * @param machinery the machinery used.
     */
    public void changeInMachineryEvent(Plant p, Machinery machinery);






}
