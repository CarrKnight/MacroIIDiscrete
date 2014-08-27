/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.Department;
import model.utilities.stats.collectors.DataStorage;

import java.util.function.Predicate;

/**
 * A very simple tool to collect data from a department that can then be used by predictors to collect data.
 * Created by carrknight on 8/27/14.
 */
public class RegressionDataCollector<T extends Enum<T>> {

    /**
     * what variable type is x?
     */
    final private T xDataType;

    /**
     * what variable type is y?
     */
    final private T yDataType;

    /**
     * what variable type is gap (usually a proxy for observation weight)?
     */
    final private T gapType;


    final private T[] covariantsType;

    private double lastObservedX = Double.NaN;

    private double lastObservedY = Double.NaN;

    private double lastObservedGap = Double.NaN;

    private final double[] lastObservedCovariants;

    private final Department<T> departmentToFollow;


    //the default validator accepts all data that isn't negative or not a number
    private static final Predicate<Double> DEFAULT_VALIDATOR = x -> Double.isFinite(x) && x >= 0;

    private static final Predicate<Double> ANY_NUMBER_VALIDATOR = Double::isFinite;

    //if any validator fails, the observation should be discarded
    private Predicate<Double> xValidator = DEFAULT_VALIDATOR;

    private Predicate<Double> yValidator = DEFAULT_VALIDATOR;

    private Predicate<Double> gapValidator = ANY_NUMBER_VALIDATOR;

    /**
     * when this is false, no information is gathered.
     */
    private Predicate<Department<T>> dataValidator = dep-> dep.getData().numberOfObservations() > 0;


    public RegressionDataCollector(Department<T> departmentToFollow,T xDataType, T yDataType, T gapType,  T... covariantsType ) {
        this.xDataType = xDataType;
        this.yDataType = yDataType;
        this.gapType = gapType;
        this.covariantsType = covariantsType;
        lastObservedCovariants = new double[covariantsType.length];
        this.departmentToFollow = departmentToFollow;
    }

    /**
     * collects data, if possible
     */
    public void collect()
    {
        //if there are no observations, don't bother!
        final DataStorage<T> data = departmentToFollow.getData();
        if(!dataValidator.test(departmentToFollow))
        {
            assert Double.isNaN(lastObservedX);
            assert Double.isNaN(lastObservedY);
            assert Double.isNaN(lastObservedGap);
            return;
        }

        //otherwiiiise:
        lastObservedX = data.getLatestObservation(xDataType);
        lastObservedY = data.getLatestObservation(yDataType);
        lastObservedGap = data.getLatestObservation(gapType);
        for(int i=0; i<lastObservedCovariants.length; i++)
            lastObservedCovariants[i]=data.getLatestObservation(covariantsType[i]);


        //done
    }

    public boolean isLatestObservationValid(){
        return isLastXValid() && isLastYValid() && isLastGapValid();
    }

    public boolean isLastGapValid() {
        return gapValidator.test(lastObservedGap);
    }

    public boolean isLastYValid() {
        return yValidator.test(lastObservedY);
    }

    public boolean isLastXValid() {
        return xValidator.test(lastObservedX);
    }

    public Predicate<Department<T>> getDataValidator() {
        return dataValidator;
    }

    public void setDataValidator(Predicate<Department<T>> dataValidator) {
        this.dataValidator = dataValidator;
    }

    public Predicate<Double> getGapValidator() {
        return gapValidator;
    }

    public void setGapValidator(Predicate<Double> gapValidator) {
        this.gapValidator = gapValidator;
    }

    public Predicate<Double> getyValidator() {
        return yValidator;
    }

    public void setyValidator(Predicate<Double> yValidator) {
        this.yValidator = yValidator;
    }

    public Predicate<Double> getxValidator() {
        return xValidator;
    }

    public void setxValidator(Predicate<Double> xValidator) {
        this.xValidator = xValidator;
    }


    public double getLastObservedX() {
        return lastObservedX;
    }

    public double getLastObservedY() {
        return lastObservedY;
    }

    public double getLastObservedGap() {
        return lastObservedGap;
    }

    public double[] getLastObservedCovariants() {
        return lastObservedCovariants;
    }
}
