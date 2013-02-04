package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import model.MacroII;
import model.utilities.DelayException;
import model.utilities.pid.PIDController;

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

    public MarginalAndPIDMaximizer(HumanResources hr, PlantControl control) {
        super(hr, control);
        MacroII model = hr.getFirm().getModel();
        //the pid controller
        pid = new PIDController(model.drawProportionalGain()/100f,model.drawIntegrativeGain()/100f,
                model.drawDerivativeGain()/100f,model.getRandom());
        pid.setOffset(hr.getPlant().workerSize());
    }

    /**
     * Simply checks profit in the 2 directions around the currentWorkerTarget. Choose the highest of the three
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    @Override
    protected int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {

        //check increasing by one:

        try{
            float profitsIfWeIncrease = currentWorkerTarget < getP().maximumWorkersPossible() ? //if we can increase production
                    computeMarginalProfits(currentWorkerTarget,currentWorkerTarget+1) //check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise don't go there!


            //compute profits if we decrease
            float profitsIfWeDecrease = currentWorkerTarget > getP().minimumWorkersNeeded() ? //can we decrease production?
                    computeMarginalProfits(currentWorkerTarget, currentWorkerTarget-1) //if so check marginal profits
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
