package model.utilities.stats.regression;

import Jama.Matrix;
import com.google.common.base.Preconditions;

/**
 * <h4>Description</h4>
 * <p/> Properly a recursive linear regression filter, useful because most applications all learning is done online and there
 * is no point (and a lot of computational cost) in doing everything in batch
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-10
 * @see
 */
public class KalmanRecursiveRegression implements RecursiveLinearRegression {

    final private int dimensions;

    /**
     * here we keep the gains
     */
    final private double[] kGains;

    /**
     * The prediction error covariance matrix
     */
    private double[][] pCovariance;

    /**
     * the coefficients proper
     */
    final private double[] beta;


    private double noiseVariance = 1;


    /**
     * Sets up an initial linear regression with betas = 0 and P = diag(1)
     * @param dimensions how many betas this regression has
     */
    public KalmanRecursiveRegression(int dimensions)
    {
        this(dimensions,new double[dimensions]);

    }

    /**
     * Sets up an initial linear regression with betas = 0 and P = diag(1)
     * @param dimensions how many betas this regression has
     * @param initialBetas The vector of initial betas to use
     */
    public KalmanRecursiveRegression(int dimensions, double... initialBetas)
    {
        this.dimensions = dimensions;
        assert initialBetas.length == dimensions;

        //create the arrays containing important info
        kGains = new  double[dimensions];
        pCovariance = new  double[dimensions][dimensions];//fill as a diagonal
        for(int i =0; i<dimensions; i++)
            pCovariance[i][i] = 1000000 ;

        beta = initialBetas;
    }


    /**
     * a new "vector row" of observations (include intercept if needed)
     * @param observationWeight the weight of this observation, the higher the more important
     * @param y the dependent variable observation
     * @param observation the array of indepedent variables observed
     */
    @Override
    public void addObservation(double observationWeight, double y, double... observation)
    {
        Preconditions.checkState(observation.length == dimensions);


        /****************************************************
         * compute K!
         ***************************************************/
        updateKGains(observation,observationWeight);
        /****************************************************
         * Update Beta!
         ***************************************************/
        updateBeta(y, observation);
        /****************************************************
         * Update P
         ***************************************************/
        updateCovarianceP(observation);

    }

    private void updateCovarianceP(double[] observation) {
        double[][] toMultiply = new double[dimensions][dimensions];
        for(int i=0; i< dimensions; i++)
            for(int j=0; j<dimensions; j++)
            {
                toMultiply[i][j]=-(kGains[i]*observation[j]);
                if(i==j)
                    toMultiply[i][j]+=1; //diagonal element needs to be summed to a diag(1)
            }





        double[][] newP = new double[dimensions][dimensions];
        for(int row=0; row<dimensions; row++)
        {
            for(int column=0; column<dimensions; column++)
            {
                for(int i=0; i<dimensions; i++)
                {
                    newP[row][column] +=toMultiply[row][i] * pCovariance[i][column];
                }
            }

        }

        //don't change if the eigenvalue is negative
        try{
            double[] eigenValues = new Matrix(pCovariance).eig().getRealEigenvalues();
            for(double e : eigenValues)
                if(e < 0)
                    return;
        }
        catch (ArrayIndexOutOfBoundsException e){
            return;
        }


        //copy the new result into the old matrix
        pCovariance = newP;





    }

    private void updateBeta(double y, double[] observation) {
        double predicted = 0;
        for(int i=0; i< dimensions; i++)
            predicted += beta[i] * observation[i];
        double residual = y - predicted;

        double weightedResidual[] = new double[dimensions];
        for(int i=0; i < dimensions; i++)
            weightedResidual[i] = residual * kGains[i];

        //update beta
        for(int i=0; i< dimensions; i++)
            beta[i] = beta[i] + weightedResidual[i];
    }

    private void updateKGains(double[] observation, double weight) {

        //compute error dispersion
        //P*x
        double px[] = new double[dimensions];
        for(int i=0; i<dimensions; i++)
        {
            for(int j=0; j<dimensions; j++)
                px[i] += pCovariance[i][j] * observation[j];
        }

        double denominator = 0;
        for(int i=0; i<dimensions; i++)
            denominator += observation[i] * px[i];
        denominator += noiseVariance / weight;

        if(denominator != 0){
            //divide, that's your K gain
            for(int i=0; i< px.length; i++)
            {
                kGains[i] = px[i]/denominator;
                //          assert kGains[i]>=0 : Arrays.toString(kGains) + " <---> " + Arrays.toString(px)  + " ==== " + denominator;
                assert !Double.isNaN(kGains[i]);
                assert !Double.isInfinite(kGains[i]) : px[i] + " --- " + denominator;
            }
        }
    }

    @Override
    public double[] getBeta() {
        return beta.clone();
    }

    @Override
    public double[] setBeta(int index, double newValue){
        beta[index] = newValue;
        return getBeta();

    }


    @Override
    public double getNoiseVariance() {
        return noiseVariance;
    }

    @Override
    public void setNoiseVariance(double noiseVariance) {
        this.noiseVariance = noiseVariance;
    }

    @Override
    public double[][] getpCovariance() {
        return pCovariance;
    }

    @Override
    public void setPCovariance(double[][] pCovariance) {
        this.pCovariance = pCovariance;
    }

    @Override
    public double getTrace()
    {
        int sum = 0;
        for(int i =0; i<dimensions; i++)
            sum += pCovariance[i][i];
        return sum;

    }
}
