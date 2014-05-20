/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.sales.prediction.AbstractRecursivePredictor;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.logs.*;
import model.utilities.stats.regression.ExponentialForgettingRegressionDecorator;
import model.utilities.stats.regression.GunnarsonRegularizerDecorator;
import model.utilities.stats.regression.KalmanRecursiveRegression;
import model.utilities.stats.regression.RecursiveLinearRegression;
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
public class AbstractOpenLoopRecursivePredictor implements Steppable, Deactivatable, LogNode
{

    private final AbstractRecursivePredictor delegate;


    private double[] currentBeta;

    private final MacroII model;

    private int openLoopPeriod = 100;

    boolean isActive = true;

    /**
     * updated result of predictPrice(1)-predictPrice(0). Useful so we don't have to compute it every day
     */
    private float upwardSlope;
    /**
     * updated result of predictPrice(0)-predictPrice(-1). Useful so we don't have to compute it every day
     */
    private float downwardSlope;

    public AbstractOpenLoopRecursivePredictor(AbstractRecursivePredictor delegate, MacroII model) {
        this.delegate = delegate;
        this.model = model;
        delegate.setInitialOpenLoopLearningTime(openLoopPeriod + 1);
        delegate.setTimeDelay(0);
        int dimensions = delegate.getPriceLags() + delegate.getIndependentLags() + 1;
        setRegression(new GunnarsonRegularizerDecorator( new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(dimensions),.995d)));
        //setRegression(new GunnarsonRegularizerDecorator(new KalmanRecursiveRegression(dimensions),1d));



        model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {
                updateSlopes();
            }
        });
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
            updateSlopes();

            //log it!
            String kindOfPredictor = delegate instanceof RecursivePurchasesPredictor ? "purchases" : "sales";
            handleNewEvent(new LogEvent(this,LogLevel.DEBUG,"{}: learned downward slope: {}, upward slope: {} , trace: {}",
                    kindOfPredictor,upwardSlope,downwardSlope,delegate.getRegression().getTrace()));






        }

        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);

    }

    private void updateSlopes() {
        upwardSlope = predictPrice(1) - predictPrice(0);
        downwardSlope = predictPrice(0) - predictPrice(-1);
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
        delegate.turnOff();


    }

    public int modifyStepIfNeeded(int step) {
        return delegate.modifyStepIfNeeded(step);
    }

    public float getUpwardSlope() {
        return upwardSlope;
    }


    public float getDownwardSlope() {
        return downwardSlope;
    }


    public double defaultPriceWithNoObservations() {
        return delegate.defaultPriceWithNoObservations();
    }

    public void setRegression(RecursiveLinearRegression regression) {
        delegate.setRegression(regression);
    }

    public int getTimeDelay() {
        return delegate.getTimeDelay();
    }

    public void setTimeDelay(int timeDelay) {
        delegate.setTimeDelay(timeDelay);
    }



    /***
     *       __
     *      / /  ___   __ _ ___
     *     / /  / _ \ / _` / __|
     *    / /__| (_) | (_| \__ \
     *    \____/\___/ \__, |___/
     *                |___/
     */

    /**
     * use delegate
     */


    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return delegate.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return delegate.removeLogEventListener(toRemove);
    }

    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        delegate.handleNewEvent(logEvent);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return delegate.stopListeningTo(branch);
    }

    @Override
    public boolean listenTo(Loggable branch) {
        return delegate.listenTo(branch);
    }

}
