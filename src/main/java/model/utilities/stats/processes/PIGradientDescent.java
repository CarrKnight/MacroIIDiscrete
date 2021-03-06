/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import com.google.common.base.Objects;
import model.utilities.ActionOrder;
import model.utilities.pid.PIDController;
import model.utilities.stats.regression.SISORegression;

import java.math.RoundingMode;

/**
 * <h4>Description</h4>
 * <p> This really represents 1 step of the gradient descend. We try to minimize the
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-10
 * @see
 */
public class PIGradientDescent
{

    private final SISORegression systemRegression;

    private final PIDController originalController;

    private final double desiredTarget;

    private final int howManyStepsToSimulate;

    private final double derivativeStepSize; //.01?

    private final double maximizationStepSize;

    private final static double STEP_SIZE_LIMIT = .05;

    private final double covariants[];


    public PIGradientDescent(SISORegression systemRegression, PIDController originalController, double desiredTarget, double... covariants) {
        this(systemRegression,originalController,desiredTarget,100,.01,.0001,covariants);
    }

    public PIGradientDescent(SISORegression systemRegression, PIDController originalController, double desiredTarget, int howManyStepsToSimulate,
                             double derivativeStepSize, double maximizationStepSize, double... covariants) {
        this.systemRegression = systemRegression;
        this.originalController = originalController;
        this.desiredTarget = desiredTarget;
        this.howManyStepsToSimulate = howManyStepsToSimulate;
        this.derivativeStepSize = derivativeStepSize;
        this.maximizationStepSize = maximizationStepSize;
        this.covariants = covariants;
    }


    /**
     * computes ITAE of this pid controller with this process. The PID controller gets stepped on and is modified so supply a clone, not the original. Same thing for the dynamic process.
     * @param pid the pid controller to use
     * @param process the process that governs the outside world
     * @param desiredTarget the pid target
     * @param howManyStepsToSimulate how far into the future to simulate
     * @param covariants additional variable to feed the dynamic process  @return the integral time absolute error of this pid with this process
     */
    public static double computeITAE(PIDController pid, DynamicProcess process, float desiredTarget, int howManyStepsToSimulate,
                                     double... covariants)
    {
        double errorSum =0;
        for(int t=1; t<=howManyStepsToSimulate; t++)
        {
            float output = (float) Math.max(process.newStep(pid.getCurrentMV(),covariants),0);
            int speed = pid.getSpeed() + 1;
            if(t % (speed) == 0 )
                pid.adjust(desiredTarget,output,true,null,null, ActionOrder.DAWN);
            errorSum += t * Math.abs(output-desiredTarget);
        }

        return errorSum;

    }


    public PIDGains getNewGains()
    {

        double originalITAE = computeITAE(new PIDController(originalController),systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate, covariants);
        //   System.out.println(originalITAE);

        /***
         *     .----------------.
         *    | .--------------. |
         *    | |   ______     | |
         *    | |  |_   __ \   | |
         *    | |    | |__) |  | |
         *    | |    |  ___/   | |
         *    | |   _| |_      | |
         *    | |  |_____|     | |
         *    | |              | |
         *    | '--------------' |
         *     '----------------'
         */
        PIDController pIncreasedPID = new PIDController(originalController); pIncreasedPID.setGains((float) (originalController.getProportionalGain()+derivativeStepSize),originalController.getIntegralGain(),originalController.getDerivativeGain());
        double pIncreasedITAE = computeITAE(pIncreasedPID,systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate, covariants);
        PIDController pDecreasedPID = new PIDController(originalController); pDecreasedPID.setGains((float) (originalController.getProportionalGain()-derivativeStepSize),originalController.getIntegralGain(),originalController.getDerivativeGain());
        double pDecreasedITAE = computeITAE(pDecreasedPID,systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate, covariants);
        double pDerivative = (pIncreasedITAE-pDecreasedITAE)/(2*derivativeStepSize);
        if(!Double.isFinite(pDerivative))
            pDerivative = 0;
        /***
         *     .----------------.
         *    | .--------------. |
         *    | |     _____    | |
         *    | |    |_   _|   | |
         *    | |      | |     | |
         *    | |      | |     | |
         *    | |     _| |_    | |
         *    | |    |_____|   | |
         *    | |              | |
         *    | '--------------' |
         *     '----------------'
         */
        PIDController iIncreasedPID = new PIDController(originalController); iIncreasedPID.setGains(originalController.getProportionalGain(),(float) (originalController.getIntegralGain()+derivativeStepSize),originalController.getDerivativeGain());
        double iIncreasedITAE = computeITAE(iIncreasedPID,systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate, covariants);
        PIDController iDecreasedPID = new PIDController(originalController); iDecreasedPID.setGains(originalController.getProportionalGain(),(float) (originalController.getIntegralGain()-derivativeStepSize),originalController.getDerivativeGain());
        double iDecreasedITAE = computeITAE(iDecreasedPID,systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate, covariants);
        double iDerivative = (iIncreasedITAE-iDecreasedITAE)/(2*derivativeStepSize);
        if(!Double.isFinite(iDerivative))
            iDerivative = 0;



        //return x + alpha * nablaX
        boolean tuneP = Math.abs(pDerivative) >= Math.abs(iDerivative);
        final double pStep = tuneP ?  Math.signum(pDerivative) * Math.min(Math.abs(maximizationStepSize * pDerivative), STEP_SIZE_LIMIT) : 0;
        final double iStep = tuneP ? 0 : Math.signum(iDerivative) * Math.min(Math.abs(maximizationStepSize * iDerivative), STEP_SIZE_LIMIT);
        return new PIDGains( (float)(originalController.getProportionalGain() - pStep),
                (float) (originalController.getIntegralGain() - iStep),
                originalController.getDerivativeGain());






    }


    public static class PIDGains{

        private final static RoundingMode rounder = RoundingMode.HALF_UP;

        private final float proportional;

        private final float integral;

        private final float derivative;

        public PIDGains(float proportional, float integral, float derivative) {
            this.proportional =  Math.max(proportional,0);
            this.integral = Math.max(integral,0);
            this.derivative = Math.max(derivative,0);
        }


        public float getProportional() {
            return proportional;
        }

        public float getIntegral() {
            return integral;
        }

        public float getDerivative() {
            return derivative;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("proportional", proportional)
                    .add("integral", integral)
                    .add("derivative", derivative)
                    .toString();
        }
    }








}
