/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;
import model.utilities.filters.ExponentialFilter;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-16
 * @see
 */
public class AveragerOverSmallIntervalOnly implements PriceAverager {

    ExponentialFilter<Integer> averager;

    public AveragerOverSmallIntervalOnly(float weight) {
        this.averager = new ExponentialFilter<>(weight);
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {

        int price = department.getLastClosingPrice();
        //reset to closing price whenever there is no trade, no observation or the new observation is far away from the current price
        if(department.getTodayTrades()<=0 || Float.isNaN(averager.getSmoothedObservation()) || Math.abs(price-averager.getSmoothedObservation()) > 1 )
            averager = new ExponentialFilter<>(averager.getWeight());
        averager.addObservation(price);

    }

    @Override
    public float getAveragedPrice(){

        return averager.getSmoothedObservation();


    }
}
