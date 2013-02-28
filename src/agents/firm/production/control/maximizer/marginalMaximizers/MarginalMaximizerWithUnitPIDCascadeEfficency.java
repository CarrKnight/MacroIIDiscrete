package agents.firm.production.control.maximizer.marginalMaximizers;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import financial.Market;
import financial.utilities.changeLooker.ChangeLookupMAMarket;
import model.utilities.pid.PIDController;

/**
 * <h4>Description</h4>
 * <p/> This is a cascade maximizer where the slave is just MarginalMaximizerWithUnitPID and the master deviates the efficiency
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-27
 * @see
 */
public class MarginalMaximizerWithUnitPIDCascadeEfficency extends MarginalMaximizerWithUnitPID {


    /**
     * pid controller that is going to be master?
     */
    PIDController lookAhead;

    /**
     * the object we use to store price change
     */
    ChangeLookupMAMarket changeLookup;

    /**
     * Very simple marginal maximizer that chooses worker by targeting MB/MC = 1
     * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1.
     * If the other controller is not set up AFTER this controller is called, then this object is just a MarginalMaximizerWithUnitPID
     * @param hr      the human resources
     * @param control the plant control.
     */
    public MarginalMaximizerWithUnitPIDCascadeEfficency(HumanResources hr, PlantControl control) {
        super(hr, control);
    }

    public void setupLookup(int daysToAverage,float proportional, float integrative, float derivative, Market market)
    {
        changeLookup = new ChangeLookupMAMarket(market,daysToAverage,getHr().getFirm().getModel());
        lookAhead
    }

}
