/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns.processes;

/**
 * <h4>Description</h4>
 * <p> Can "step" forward, producing a new y
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-25
 * @see
 */
public interface DynamicProcess {


    public double newStep(double todayInput);


}
