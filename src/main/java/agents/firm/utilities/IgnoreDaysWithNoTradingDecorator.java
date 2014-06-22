/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;

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
 * @version 2014-06-22
 * @see
 */
public class IgnoreDaysWithNoTradingDecorator implements PriceAverager {

    private final PriceAverager decorated;

    public IgnoreDaysWithNoTradingDecorator(PriceAverager decorated) {
        this.decorated = decorated;
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {
        if(department.getTodayTrades() > 0)
            decorated.endOfTheDay(department);
        //otherwise the decorated doesn't learn about this


    }

    @Override
    public float getAveragedPrice(Department department) {

        return decorated.getAveragedPrice(department);


    }
}
