/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;

/**
 * <h4>Description</h4>
 * <p> A very simple decorator that overrides the usual average when the previous day the department failed to trade anything
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-15
 * @see
 */
public class NoTradingOverrideAveragerDecorator implements PriceAverager {

    private boolean overrideActive;

    private final PriceAverager decorated;



    public NoTradingOverrideAveragerDecorator(PriceAverager decorated) {
        this.decorated = decorated;
    }

    /**
     * call this at the end/beginning of the day to collect data to build your average
     *
     * @param department the department being averaged
     */
    @Override
    public void endOfTheDay(Department department) {

        overrideActive = department.getTodayTrades() <=0;
        decorated.endOfTheDay(department);


    }

    @Override
    public float getAveragedPrice() {

        if(!overrideActive)
            return  decorated.getAveragedPrice();
        else{
            return  -1;
        }



    }

    public PriceAverager getDecorated() {
        return decorated;
    }

    public boolean isOverrideActive() {
        return overrideActive;
    }
}
