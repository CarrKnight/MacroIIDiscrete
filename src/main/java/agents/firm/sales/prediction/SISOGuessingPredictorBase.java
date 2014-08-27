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
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This is the part of the SISO guessing predictor that runs the regression, schedules itself and so on.
 * Created by carrknight on 8/27/14.
 */
public class SISOGuessingPredictorBase<T extends Enum<T>> implements Deactivatable, Steppable{

    /**
     * how many observations before we start actually predicting
     */
    private int burnOut = 500;

    /**
     * how many steps into the future to simulate!
     */
    private int stepsIntoTheFutureToSimulate = 100;

    private final RegressionDataCollector<T> collector;

    /**
     * the set of regressions to use
     */
    private final SISOGuessingRegression regression;


    private BufferedWriter debugWriter;


    public SISOGuessingPredictorBase(MacroII model,
                                     RegressionDataCollector<T> collector) {
        this.collector =collector;
        this.collector.setDataValidator(collector.getDataValidator().and(Department::hasTradedAtLeastOnce));
        this.collector.setyValidator(price-> Double.isFinite(price) && price > 0); // we don't want -1 prices
        regression = new SISOGuessingRegression(0,1,10,20,50,100);
        regression.setRoundError(true);
        regression.setHowManyObservationsBeforeModelSelection(burnOut);
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
            //gather the quantity sold
            double quantity = collector.getLastObservedX();
            //gather price
            double price = collector.getLastObservedY();

            //check weight
            double gap = collector.getLastObservedGap();
            //regress
            assert price > 0;

            if (Math.abs(gap) > 100)
                regression.skipObservation(price, quantity);
            else {
                regression.addObservation(price, quantity);
            }
            if(debugWriter != null)
            {
                try {
                    debugWriter.write(price + ", " + quantity + "," + (gap != 0 ? 0 : 1));
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
        if(regression.hasEnoughObservations() && collector.isLastXValid()) {
            final float predictedPrice = (float) DynamicProcess.simulateManyStepsWithFixedInput(regression.generateDynamicProcessImpliedByRegression(),
                    stepsIntoTheFutureToSimulate, collector.getLastObservedX() + increaseStep);
            return Math.max(predictedPrice,0);
        }
        else return Float.NaN;
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
        regression.setHowManyObservationsBeforeModelSelection(burnOut);
    }

    public int getStepsIntoTheFutureToSimulate() {
        return stepsIntoTheFutureToSimulate;
    }

    public void setStepsIntoTheFutureToSimulate(int stepsIntoTheFutureToSimulate) {
        this.stepsIntoTheFutureToSimulate = stepsIntoTheFutureToSimulate;
    }

    public void setDebugWriter(Path pathToDebugFileToWrite) throws IOException {
        //pathToDebugFileToWrite.toFile().createNewFile();
        this.debugWriter = Files.newBufferedWriter(pathToDebugFileToWrite, StandardOpenOption.CREATE);
        debugWriter.write("y,x,skipped");
        debugWriter.newLine();
        debugWriter.flush();

    }

    @Override
    public String toString() {
        return regression.toString();
    }
}
