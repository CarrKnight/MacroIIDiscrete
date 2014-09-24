/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;

/**
 * <h4>Description</h4>
 * <p> A simple interface trying to keep a "moving" average of prices being charged by a department. Because there are many ways to do this, i let this interface
 * provide a common method
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
public interface PriceAverager {


    /**
     * call this at the end/beginning of the day to collect data to build your average
     * @param department the department being averaged
     */
    public void endOfTheDay(Department department);

    public float getAveragedPrice(Department department);


    /**
     * what to do when there is no trading that day
     */
    public enum NoTradingDayPolicy
    {

        //don't feed that day in the averager
        IGNORE,

        //count as a day with no price
        COUNT_AS_0,

        //use the last closing price.
        COUNT_AS_LAST_CLOSING_PRICE

    }


}
