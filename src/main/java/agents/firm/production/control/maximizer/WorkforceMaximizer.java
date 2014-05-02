/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.production.PlantListener;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import model.utilities.Deactivatable;
import model.utilities.logs.LogNode;

/**
 * <h4>Description</h4>
 * <p/> The workforce maximizer is supposed to be the low-frequency strategy that checks whether to change workforce targets over time.
 * <p/> The workforce maximizer itself should be used to decide WHEN to maximize, while it should delegate to WorkerMaximizationAlgorithm
 * when it comes to decide how!
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-23
 * @see
 */
public interface WorkforceMaximizer<ALG extends WorkerMaximizationAlgorithm> extends PlantListener, Deactivatable, LogNode {

    /**
     * Method to start the workforce maximizer
     */
    public void start();




}
