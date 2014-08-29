/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.Department;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.regression.SISOGuessingRegression;
import model.utilities.stats.regression.SISORegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * A general structure for autoscheduling collecting and regressing to be used by any predictor that needs it.
 * Created by carrknight on 8/27/14.
 */
public class SISOPredictorBase<T extends Enum<T>,R extends SISORegression> implements Deactivatable, Steppable{

    private static final int DEFAULT_BURNOUT = 500;
    /**
     * how many observations before we start actually predicting
     */
    private int burnOut = DEFAULT_BURNOUT;

    /**
     * how many steps into the future to simulate!
     */
    private int stepsIntoTheFutureToSimulate = 100;

    private final RegressionDataCollector<T> collector;

    /**
     * the set of regressions to use
     */
    private final R regression;


    private BufferedWriter debugWriter;


    public static <K extends Enum<K>> SISOPredictorBase<K,SISOGuessingRegression> buildDefaultSISOGuessingRegression(MacroII model,
                                                                                                         RegressionDataCollector<K> collector){
        return new SISOPredictorBase<>(model,collector,new SISOGuessingRegression(0,1,10,20,50,100),
                regression1 -> {
                    regression1.setRoundError(true);
                    regression1.setHowManyObservationsBeforeModelSelection(DEFAULT_BURNOUT);
                });

    }




    public SISOPredictorBase(MacroII model, RegressionDataCollector<T> collector, R regression,
                             Consumer<R> regressionInitialization) {
        this.collector =collector;
        this.collector.setDataValidator(collector.getDataValidator().and(Department::hasTradedAtLeastOnce));
        this.collector.setyValidator(price-> Double.isFinite(price) && price > 0); // we don't want -1 prices
        this.regression = regression;
        if(regressionInitialization != null)
            regressionInitialization.accept(regression);

        //schedule yourself
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);
    }

    private boolean active = true;

    @Override
    public void step(SimState state) {
        if(!active)
            return;


        assert state instanceof MacroII;

        collector.collect();
        if(collector.isLatestObservationValid()) {
            //gather the independentVariable sold
            double independentVariable = collector.getLastObservedX();
            //gather dependentVariable
            double dependentVariable = collector.getLastObservedY();

            //check weight
            double gap = collector.getLastObservedGap();
            //regress
            assert dependentVariable > 0;

            if (Math.abs(gap) > 100)
                regression.skipObservation(dependentVariable, independentVariable);
            else {
                regression.addObservation(dependentVariable, independentVariable);
            }
            if(debugWriter != null)
            {
                try {
                    debugWriter.write(dependentVariable + ", " + independentVariable + ","
                            + (Math.abs(gap) > 100 ? 1 : 0));
                    debugWriter.newLine();
                    debugWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        //restep
        ((MacroII) state).scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this);

    }



    /**
     * predict (by simulation) what will be Y after X is changed by "increaseStep"
     * @param increaseStep can be negative or 0
     * @return the prediction or NaN if no prediction is available
     */
    public float predictYAfterChangingXBy(int increaseStep) {
        if(readyForPrediction() && collector.isLastXValid()) {
            final float predictedPrice = (float) DynamicProcess.simulateManyStepsWithFixedInput(regression.generateDynamicProcessImpliedByRegression(),
                    stepsIntoTheFutureToSimulate, collector.getLastObservedX() + increaseStep);
            return Math.max(predictedPrice,0);
        }
        else return Float.NaN;
    }

    /**
     * are there enough observations to make a call?
     */
    public boolean readyForPrediction() {
        return regression.getNumberOfObservations() > burnOut;
    }


    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        active = false;
    }


    public int getBurnOut() {
        return burnOut;
    }

    public void setBurnOut(int burnOut) {
        this.burnOut = burnOut;
    }

    public int getStepsIntoTheFutureToSimulate() {
        return stepsIntoTheFutureToSimulate;
    }

    public void setStepsIntoTheFutureToSimulate(int stepsIntoTheFutureToSimulate) {
        this.stepsIntoTheFutureToSimulate = stepsIntoTheFutureToSimulate;
    }

    public void setDebugWriter(Path pathToDebugFileToWrite) throws IOException {
        Files.deleteIfExists(pathToDebugFileToWrite);
        this.debugWriter = Files.newBufferedWriter(pathToDebugFileToWrite, StandardOpenOption.CREATE);
        debugWriter.write("y,x,skipped");
        debugWriter.newLine();
        debugWriter.flush();

    }

    @Override
    public String toString() {
        return regression.toString();
    }

    public R getRegression() {
        return regression;
    }
}
