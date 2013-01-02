package agents.firm.production.control.maximizer;

import agents.firm.production.PlantListener;

/**
 * <h4>Description</h4>
 * <p/> The workforce maximizer is supposed to be the low-frequency strategy that checks whether to change workforce targets over time.
 * <p/>
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
public interface WorkforceMaximizer extends PlantListener {

    /**
     * Method to start the workforce maximizer
     */
    public void start();

    /**
     * Method to switch the strategy off. Irreversible
     */
    public void turnOff();



}
