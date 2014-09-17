package model.scenario;

import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.regression.SISORegression;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Created by carrknight on 9/17/14.
 */
public class RegressionStatics {
    public static final double MAXIMUM_ABS_ERROR = 1d;
    public static final double FIXED_INPUT = 1d;

    public static boolean tracksAcceptably(SISORegression regression, DynamicProcess originalProcess,
                                     double maximumAbsError, int horizon, double fixedInput){

        DynamicProcess regressionProcess = regression.generateDynamicProcessImpliedByRegression();
        originalProcess.setRandomNoise(() -> 0d); //remove noise for testing


        SummaryStatistics absoluteDistance = new SummaryStatistics();
        for(int i=0; i<horizon; i++)
        {
            absoluteDistance.addValue(Math.abs(regressionProcess.newStep(fixedInput)-originalProcess.newStep(fixedInput)));
        }

        System.out.println("absolute tracking error: " + absoluteDistance.getMean());
        return absoluteDistance.getMean() < maximumAbsError;

    }
}
