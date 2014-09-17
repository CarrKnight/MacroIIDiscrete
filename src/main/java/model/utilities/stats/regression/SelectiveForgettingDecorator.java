/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import com.google.common.base.Objects;

/**
 * <h4>Description</h4>
 * <p> This is taken from "avoiding windup in recursive parameter estimation" by Stenlund and Gustaffson.
 * Supposedly it's a fast approximation of selective forgetting. I'd like to judge it against selective forgetting but I can't because i don't have access to that journal
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-16
 * @see
 */
public class SelectiveForgettingDecorator extends RecursiveLinearRegressionDecorator {



    private final double centerEigenvector;

    private final double forgettingFactor;


    public SelectiveForgettingDecorator(KalmanBasedRecursiveRegression decorated,
                                        double centerEigenvector, double forgettingFactor) {
        super(decorated);
        this.centerEigenvector = centerEigenvector;
        this.forgettingFactor = forgettingFactor;
    }

    public SelectiveForgettingDecorator(KalmanBasedRecursiveRegression decorated) {
        this(decorated,1000,.999);
    }



    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        super.addObservation(observationWeight, y, observation);


        if(observationWeight<.001)
            return;

        Matrix originalP = new Matrix(getpCovariance());


        final EigenvalueDecomposition decomposition = originalP.eig();

        //divide the eigenvalues by the forgetting factor
        final double[] realEigenvalues = decomposition.getRealEigenvalues();
        //bind the eigenvalues

        for (int i = 0; i < realEigenvalues.length; i++) {
            realEigenvalues[i] = realEigenvalues[i] > centerEigenvector ?
                    realEigenvalues[i] * forgettingFactor :
                    realEigenvalues[i] / forgettingFactor;
        }
        //turn it into a matrix
        Matrix forgot = new Matrix(realEigenvalues.length, realEigenvalues.length);
        for (int i = 0; i < realEigenvalues.length; i++)
            forgot.set(i, i, realEigenvalues[i]);
        //by using transpose I am already assuming simmetricity, which I think it's fine, given that
        Matrix newP = decomposition.getV().times(forgot.times(decomposition.getV().transpose()));

        setPCovariance(newP.getArray());

   //     System.out.println(Arrays.toString(getBeta()));
  //      System.out.println(Arrays.deepToString(originalP.eig().getD().getArray()));
   //     System.out.println(Arrays.toString(originalP.eig().getRealEigenvalues()));
   //     System.out.println("========================================================");

        //done!
        //       setPCovariance(originalP.plus(numerator.times(1d/denominator)).getArray());



    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getBeta())
                .toString();
    }
}
