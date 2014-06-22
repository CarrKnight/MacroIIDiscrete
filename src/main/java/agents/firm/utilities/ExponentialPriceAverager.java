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
 * <p> Simple price averager using ExponentialFilter
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
public class ExponentialPriceAverager implements PriceAverager {


    private final ExponentialFilter<Integer> averagedPrice;

    /**
     * when we fail to trade, if this flag is true we feed a "0" price for that day, if this flag is false we ignore it.
     */
    private final NoTradingDayPolicy noTradingDayPolicy;


    public ExponentialPriceAverager(float weight, NoTradingDayPolicy policy) {
        this.averagedPrice = new ExponentialFilter<>(weight);
        this.noTradingDayPolicy = policy;
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {

        int lastClosingPrice = department.getLastClosingPrice();
        if(lastClosingPrice < 0)
            return;
        else if(department.getTodayTrades() > 0)
        {
            averagedPrice.addObservation(department.getLastClosingPrice());
        }


        switch (noTradingDayPolicy)
        {
            case COUNT_AS_0:
                averagedPrice.addObservation(0);
                break;
            case COUNT_AS_LAST_CLOSING_PRICE:
                averagedPrice.addObservation(department.getLastClosingPrice());
                break;
            default:
            case IGNORE:
                break;
        }



    }

    @Override
    public float getAveragedPrice(Department department) {
       return averagedPrice.getSmoothedObservation();
    }
}
