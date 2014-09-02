/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import agents.firm.Department;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.processes.StickinessDescent;
import model.utilities.stats.regression.SISORegression;

import java.util.function.Function;

/**
 * Instead of changing P and I parameters, this tuner just changes the stickiness/ the speed at which the controller is called..
 * Created by carrknight on 9/2/14.
 */
public class PIDStickinessTuner extends PIDAutotuner {
    public PIDStickinessTuner(PIDController toDecorate) {
        super(toDecorate);
    }

    /**
     * @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDStickinessTuner(PIDController toDecorate, Department department) {
        super(toDecorate, department);
    }

    /**
     * @param toDecorate        the PID controller to deal with
     * @param regressionBuilder
     * @param department        nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDStickinessTuner(PIDController toDecorate, Function<Integer, SISORegression> regressionBuilder, Department department) {
        super(toDecorate, regressionBuilder, department);
    }

    @Override
    protected void tune(ControllerInput input) {
        StickinessDescent descent = new StickinessDescent(regression,decoratedCasted,isControllingFlows() ? input.getFlowTarget() : input.getStockTarget(),
                100);

        final int newSpeed = descent.getNewSpeed();
        System.out.println("new speed: " + newSpeed );
        System.out.println(regression);
        decoratedCasted.setSpeed(newSpeed);
    }
}
