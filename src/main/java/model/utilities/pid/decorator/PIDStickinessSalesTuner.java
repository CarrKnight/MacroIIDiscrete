/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.prediction.RegressionDataCollector;
import agents.firm.sales.prediction.SISOPredictorBase;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.processes.StickinessDescent;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;
import model.utilities.stats.regression.MultipleModelRegressionWithSwitching;
import model.utilities.stats.regression.SISORegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Very special decorator/tuner. To use for the stickyprices paper. Instead of changing P and I it changes speed.
 * Now because speed affects how often the PID gets called this tuner needs an independent step to regress every day regardless of PID
 * speed. It is used only for sales because i am too lazy to make generic
 * Created by carrknight on 9/2/14.
 */
public class PIDStickinessSalesTuner extends ControllerDecorator implements Steppable, Deactivatable {


    private final SalesDepartment department;

    private final SISOPredictorBase<SalesDataType,SISORegression> regression;

    private int observationsBeforeTuning = 500;

    private final static int MAXIMIZATION_FREQUENCY = 1;

    private float currentTarget = 0;

    private final PIDController controller;

    private BufferedWriter log;


    public PIDStickinessSalesTuner(PIDController toDecorate, SalesDepartment department, MacroII model) {
        super(toDecorate);
        this.controller = toDecorate;
        this.department = department;
        RegressionDataCollector<SalesDataType> collector = new RegressionDataCollector<>(department, SalesDataType.LAST_ASKED_PRICE,
                SalesDataType.OUTFLOW, SalesDataType.SUPPLY_GAP);
        collector.setDataValidator(dept -> dept.hasTradedAtLeastOnce());
        collector.setxValidator(x -> Double.isFinite(x) && x >= 0);
        collector.setyValidator(y -> Double.isFinite(y) && y >= 0);

        final MultipleModelRegressionWithSwitching switching = new MultipleModelRegressionWithSwitching(
                (size) -> new AutoRegressiveWithInputRegression(size, size, .99f), 1, 2, 3, 4, 5, 6, 7, 8);
        switching.setExcludeLinearFallback(false);
        switching.setRoundError(true);
        regression = new SISOPredictorBase<>(model, collector,switching);

        model.registerDeactivable(this); //turn off when the model does

        model.scheduleSoon(ActionOrder.ADJUST_PRICES,this);

    }


    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input    the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase    at which phase should this controller be rescheduled
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user, ActionOrder phase) {
        currentTarget = controller.isControllingFlows() ? input.getFlowTarget() : input.getStockTarget();
        super.adjust(input, isActive, simState, user, phase);
    }

    private void tune() {



        StickinessDescent descent = new StickinessDescent(regression.getRegression(),controller,currentTarget,
                1000);

        final int newSpeed = descent.getNewSpeed();
        System.out.println("new speed: " + newSpeed );
        System.out.println(regression);
        department.handleNewEvent(new LogEvent(this, LogLevel.TRACE,"tuned with following gains and speed {},{},{},{}",
                controller.getProportionalGain(),controller.getIntegralGain(),controller.getDerivativeGain(),
                controller.getSpeed()));

        controller.setSpeed(newSpeed);
    }

    @Override
    public void turnOff() {
        regression.turnOff();
    }

    @Override
    public void step(SimState state) {
        if(!regression.isActive())
            return;

        final int numberOfObservations = regression.getRegression().getNumberOfObservations();

        if(log != null)
        {

            try {
                log.write(state.schedule.getTime() + "," + controller.getSpeed());
                log.newLine();
                log.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if(numberOfObservations >= observationsBeforeTuning &&
                (numberOfObservations - observationsBeforeTuning) % MAXIMIZATION_FREQUENCY == 0 )
            tune();

        ((MacroII)state).scheduleTomorrow(ActionOrder.ADJUST_PRICES, this);


    }

    public int getObservationsBeforeTuning() {
        return observationsBeforeTuning;
    }

    public void setObservationsBeforeTuning(int observationsBeforeTuning) {
        this.observationsBeforeTuning = observationsBeforeTuning;
    }


    public void setLogToWrite(Path logToWrite) {
        try {
            Files.deleteIfExists(logToWrite);
            Files.createFile(logToWrite);
            this.log = Files.newBufferedWriter(logToWrite);
            log.write("time,speed");
            log.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
