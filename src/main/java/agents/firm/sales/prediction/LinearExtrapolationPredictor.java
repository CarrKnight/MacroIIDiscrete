/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import model.utilities.filters.Filter;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> Memorizes the difference in prices that occurs in adding one more worker; predicts the same absolute change in the future.
 * <p/> It checks every day at THINK
 * <p/> It uses a simple Exponential moving average to avoid a bit of noise
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-09
 * @see
 */
public class LinearExtrapolationPredictor implements SalesPredictor, Steppable {

    /**
     * The department you are linked to
     */
    SalesDepartment department;



    public LinearExtrapolationPredictor(SalesDepartment department)
    {

        this(department,department.getFirm().getModel());




    }

    public LinearExtrapolationPredictor(SalesDepartment salesDepartment, MacroII state)
    {
        this.department = salesDepartment;



        state.scheduleSoon(ActionOrder.THINK, this);

    }


    /**
     * What was our observation point for the low number of workers? What was the price then?
     */
    private int lowWorkers = -1;

    /**
     * the price we saw with few workers
     */
    private Filter<Long> lowWorkersPrice;

    private int highWorkers = -1; //start as non number


    /**
     * the price we saw with high workers
     */
    private Filter<Long> highWorkersPrice;


    private boolean initialized = false;

    /**
     * reads in and stores data
     * @param state
     */
    @Override
    public void step(SimState state) {
        //if you aren't attached to departments, you are turned off!
        if(department == null)
            return;

        Preconditions.checkState(((MacroII)state).getCurrentPhase().equals(ActionOrder.THINK));

        int workers = totalNumberOfWorkers();
        assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up


        try{

            initializeLowWorkersIfNeeded(workers);

            //if we are at the lowWorkers level, observe
            if(lowWorkers == workers)
            {
                lowWorkersPrice.addObservation(observePrice()); //whatever was the last price
                assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up
                return; //done
            }
            else
            if(workers < lowWorkers)
            {
                //shift down
                highWorkers = lowWorkers;
                highWorkersPrice = lowWorkersPrice;

                lowWorkers = workers;
                lowWorkersPrice = new ExponentialFilter<>(.1f);
                lowWorkersPrice.addObservation(observePrice()); //whatever was the last price
                assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up


            }
            else
            {
                assert  workers > lowWorkers;
                initializeHighWorkersIfNeeded(workers);
                if(workers == highWorkers) //observe
                {
                    highWorkersPrice.addObservation(observePrice()); //whatever was the last price
                    assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up
                    return; //done
                }
                else
                if(workers > highWorkers)
                {
                    //shift up, then observe
                    lowWorkers = highWorkers;
                    lowWorkersPrice = highWorkersPrice;

                    highWorkers = workers;
                    highWorkersPrice = new ExponentialFilter<>(.1f);
                    highWorkersPrice.addObservation(observePrice());
                    assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up


                }
                else
                {
                    assert  lowWorkers < workers && workers < highWorkers;
                    lowWorkers = workers;
                    lowWorkersPrice = new ExponentialFilter<>(.1f);
                    lowWorkersPrice.addObservation(observePrice()); //whatever was the last price
                    assert highWorkers > lowWorkers || highWorkers == -1; //otherwise something is up
                }
            }


        }finally {
            //reschedule yourself
            ((MacroII)state).scheduleTomorrow(ActionOrder.THINK,this);

        }



    }

    private void initializeHighWorkersIfNeeded(int workers) {
        if(highWorkers == -1)
        {
            highWorkers = workers;
            highWorkersPrice = new ExponentialFilter<>(.1f);
        }
    }

    private long observePrice() {
        return Math.max(department.getLastClosingPrice(),0);
    }

    private void initializeLowWorkersIfNeeded(int workers) {
        if(lowWorkers == -1)
        {
            Preconditions.checkState(!initialized);
            //this can only happen the first time
            initialized = true;
            lowWorkers = workers;
            lowWorkersPrice = new ExponentialFilter<>(.1f);

        }

        assert initialized;
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to
     * (usually in order to guide production). <br>
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePrice(SalesDepartment dept, long expectedProductionCost) {

        //if we aren't ready, default to last closing price
        if(lowWorkers == -1 || highWorkers == -1 || !highWorkersPrice.isReady() || !lowWorkersPrice.isReady() || lowWorkers == 0) //when low workers are 0 the prediction will be wrong because of no data
        {
            //create a fake good, get it valued, return that value
            return dept.getLastClosingPrice();
        }



        float stepSize = highWorkers - lowWorkers;
        assert stepSize > 0;

        //delay if you can't make it
        int workers = totalNumberOfWorkers();
        if( workers != highWorkers && workers != lowWorkers)
            return -1; //delay

        long currentPrice = highWorkers==totalNumberOfWorkers() ?  Math.round(highWorkersPrice.getSmoothedObservation())
                : Math.round(lowWorkersPrice.getSmoothedObservation())  ;
        long priceDifference = Math.round((lowWorkersPrice.getSmoothedObservation()-highWorkersPrice.getSmoothedObservation())/stepSize);
        return Math.max(currentPrice - priceDifference,0);


    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        department = null;
    }

    /**
     * checks how many workers the plant currently has
     * @return
     */
    private int totalNumberOfWorkers()
    {

        int workers = 0;
        Iterable<? extends Plant> plantsProducingGoodOfInterest =
                department.getServicedPlants();
        for( Plant p : plantsProducingGoodOfInterest)
            workers += p.workerSize();

        return workers;
    }

    public float  getLowWorkersPrice() {
        return lowWorkersPrice.getSmoothedObservation();
    }

    public float getHighWorkersPrice() {
        return highWorkersPrice.getSmoothedObservation();
    }
}
