package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.DelayException;
import model.utilities.pid.PIDController;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> this method looks up the marginal costs and so on but then uses a PID controller to choose the number of workers
 * that would maximize profits
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-02
 * @see
 */
public class MarginalAndPIDMaximizer extends MarginalMaximizer {


    PIDController pid;

    /**
     * constructor that generates PID parameters from the model
     */
    public MarginalAndPIDMaximizer(@Nonnull HumanResources hr, PlantControl control, Plant p, Firm owner, MacroII model) {
        this(hr, control, p, owner,
                model.drawProportionalGain()/100f,model.drawIntegrativeGain()/100f,
                model.drawDerivativeGain()/100f,
                model.getRandom());
    }

    /**
     * constructor that generates PID parameters from the model
     */
    public MarginalAndPIDMaximizer(@Nonnull HumanResources hr, @Nonnull PlantControl control, @Nonnull Plant p,
                                   @Nonnull Firm owner, float proportional, float integral, float derivative,
                                   MersenneTwisterFast random) {
        super(hr, control, p, owner);
        pid = new PIDController(proportional,integral,derivative,random);
        pid.setOffset(hr.getPlant().workerSize());
    }

    /**
     * Simply checks profit in the 2 directions around the currentWorkerTarget. Choose the highest of the three
     *
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param newRevenues
     *@param newCosts
     * @param oldRevenues
     * @param oldCosts
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {

        //check increasing by one:

        try{
            float profitsIfWeIncrease = currentWorkerTarget < getP().maximumWorkersPossible() ? //if we can increase production
                    MarginalMaximizerStatics.computeMarginalProfits(getOwner(), getP(), getHr(), getControl(), getPolicy(),
                            currentWorkerTarget, currentWorkerTarget + 1) //check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise don't go there!


            //compute profits if we decrease
            float profitsIfWeDecrease = currentWorkerTarget > getP().minimumWorkersNeeded() ? //can we decrease production?
                    MarginalMaximizerStatics.computeMarginalProfits(getOwner(), getP(), getHr(), getControl(), getPolicy(),
                            currentWorkerTarget, currentWorkerTarget - 1) //if so check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise ignore this choice!

            if(profitsIfWeDecrease < 0 && profitsIfWeIncrease < 0)

                //if profits decrease in both direction, stay where you are
                return currentWorkerTarget;
            else
            {
                pid.adjustOnce(profitsIfWeIncrease,true);
                return Math.min(Math.round(pid.getCurrentMV()),getHr().maximumWorkersPossible());


            }

        }catch (DelayException e)
        {
            //if an exception was thrown, it must be because we need more time!
            return -1;
        }



    }
}
