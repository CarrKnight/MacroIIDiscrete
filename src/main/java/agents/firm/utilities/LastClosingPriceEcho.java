/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;

/**
 * <h4>Description</h4>
 * <p> just echoes last closing price
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-17
 * @see
 */
public class LastClosingPriceEcho implements PriceAverager {

    private final Department department;


    public LastClosingPriceEcho(Department department) {
        this.department = department;
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {

    }

    @Override
    public float getAveragedPrice() {
        return department.getLastClosingPrice();

    }
}
