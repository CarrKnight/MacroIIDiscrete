package model.utilities.pid;

import model.utilities.stats.collectors.DataStorage;

import java.util.function.Predicate;

/**
 * basically a very simple error counter. Every day checks target versus actual and adds it to the ITAE error.
 * Can be reset when needed. Needs to be turned off as it schedules itself every day
 *
 * Created by carrknight on 9/22/14.
 */
public class CounterITAE<DATA extends Enum<DATA>>{


    private final DataStorage<DATA> dataStorage;

    private final DATA outputDataType;


    /**
     * what's the error if you supply me a NaN or some other weird stuff
     */
    public final static double PENALTY_FOR_SKIP = 100;


    private double itae = 0;

    private int time = 1;

    private double target = 0;

    /**
     * whether to skip this datum or not.
     * By default only accepts finite positive (or 0) doubles
     */
    private Predicate<Double> outputValidator = datum -> Double.isFinite(datum) && datum >= 0;

    public CounterITAE(DataStorage<DATA> dataStorage, DATA outputDataType) {
        this.dataStorage = dataStorage;
        this.outputDataType = outputDataType;
    }

    /**
     * stops counter
     */
    public  void reset(){
        time = 1;
        itae = 0;
    }


    public void setTarget(double newTarget){

        target = newTarget;
    }


    public double getITAE(){

    return itae;

}


    public void collectData() {


        if(dataStorage.numberOfObservations() > 0) { //wait until there is something to judge!

            double output = dataStorage.getLatestObservation(outputDataType);

            //if invalid
            if(!outputValidator.test(output))
                itae +=time * PENALTY_FOR_SKIP; //penalty for dodging
            else
                itae += time * Math.abs(output - target); //itae formula

            time++;

        }

    }

    public Predicate<Double> getOutputValidator() {
        return outputValidator;
    }

    public void setOutputValidator(Predicate<Double> outputValidator) {

        this.outputValidator = outputValidator;
    }
}
