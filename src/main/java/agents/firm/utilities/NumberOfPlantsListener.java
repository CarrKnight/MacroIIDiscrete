/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Firm;
import agents.firm.production.Plant;

/**
 * <h4>Description</h4>
 * <p/> This simple logEvent listener registers itself with a Firm and is notified when a new plant is instantiated by the firm
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-06
 * @see
 */
public interface NumberOfPlantsListener {


    /**
     * React to a new plant having been built
     * @param firm the firm who built the plant
     * @param newPlant the new plant!
     */
    public void plantCreatedEvent(Firm firm, Plant newPlant);

    /**
     * React to an old plant having been closed/made obsolete
     * @param firm the firm who built the plant
     * @param newPlant the old plant
     */
    public void plantClosedEvent(Firm firm, Plant newPlant);

}
