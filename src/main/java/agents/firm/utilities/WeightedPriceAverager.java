/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;
import com.google.common.base.Preconditions;
import model.utilities.filters.WeightedMovingAverage;

/**
 * <h4>Description</h4>
 * <p> Very simple adaptor for WeigtedMovingAverage. Weights prices by the outflow that day
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-14
 * @see
 */
public class WeightedPriceAverager implements PriceAverager {

    private final WeightedMovingAverage<Integer,Integer> priceAverage;

    public WeightedPriceAverager(int days) {
        Preconditions.checkArgument(days>0);
        priceAverage = new WeightedMovingAverage<>(days);
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {


        priceAverage.addObservation(department.getLastClosingPrice(),department.getTodayTrades());



    }

    @Override
    public float getAveragedPrice(Department department) {

        return priceAverage.getSmoothedObservation();


    }
}
