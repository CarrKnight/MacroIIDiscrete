package agents.firm.purchases.prediction;

import agents.firm.sales.prediction.AbstractRecursivePredictor;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-20
 * @see
 */
public class AbstractOpenLoopRecursivePredictor implements Steppable, Deactivatable
{

    private final AbstractRecursivePredictor delegate;

    private double[] currentBeta;

    private final MacroII model;

    private int openLoopPeriod = 100;

    boolean isActive = true;

    public AbstractOpenLoopRecursivePredictor(AbstractRecursivePredictor delegate, MacroII model) {
        this.delegate = delegate;
        this.model = model;
        delegate.setInitialOpenLoopLearningTime(openLoopPeriod+1);
        delegate.getRegression().setForgettingFactor(.995);
        delegate.setTimeDelay(0);

        model.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }

    @Override
    public void step(SimState state) {

        if(!isActive)
            return;

        MacroII model = (MacroII) state;

        //if it's time to close the loop
        if(delegate.getNumberOfValidObservations() % openLoopPeriod == 0 && delegate.getNumberOfValidObservations() > 0)
        {

            //new beta!
            currentBeta = delegate.getRegression().getBeta().clone();
            if(delegate instanceof RecursivePurchasesPredictor)
            {
                System.out.print(model.getMainScheduleTime() +", purchases ");
            }
            else
                System.out.print(model.getMainScheduleTime() + ", sales: ");
            System.out.println("learned slope: " + (predictPrice(1) - predictPrice(0) ) +" ================ " + (predictPrice(0) - predictPrice(-1) ) + " Trace: "  + delegate.getRegression().getTrace());

            //delegate.getRegression().addNoise(1000);


        }

        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);

    }


    public float predictPrice(int step)
    {
        if(currentBeta == null)
            return (float) delegate.defaultPriceWithNoObservations();
        else
            return (float) AbstractRecursivePredictor.simulateFuturePrice(delegate.getData(), model, delegate.getPriceLags(), delegate.getIndependentLags(),
                    delegate.getTimeDelay(), delegate.getXVariableType(), delegate.getYVariableType(), delegate.getHowFarIntoTheFutureToPredict(),
                    currentBeta.clone(), delegate.modifyStepIfNeeded(step));
    }

    @Override
    public void turnOff() {
        isActive=false;

    }

    public int modifyStepIfNeeded(int step) {
        return delegate.modifyStepIfNeeded(step);
    }


    public double defaultPriceWithNoObservations() {
        return delegate.defaultPriceWithNoObservations();
    }
}
