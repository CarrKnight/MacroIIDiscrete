/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.production.Plant;
import goods.GoodType;

/**
 * <h4>Description</h4>
 * <p/> this profit report is class is useless and make no counting whatsoever.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-08
 * @see
 */
public class DummyProfitReport implements ProfitReport {
    @Override
    public void weekEnd() {
    }

    @Override
    public float getPlantProfits(Plant p) {
        return 0f;
    }

    @Override
    public int getAggregateProfits() {
        return 0;
    }

    @Override
    public float getEfficiencyRatio(Plant p) {
        return 0f;
    }

    @Override
    public float getNetProfitRatio(Plant p) {
        return 0f;
    }

    @Override
    public float getPlantRevenues(Plant p) {
        return 0f;
    }

    @Override
    public float getPlantCosts(Plant p) {
        return 0f;
    }

    @Override
    public int getLastWeekProduction(GoodType type) {
        return 0;
    }

    @Override
    public void turnOff() {
    }
}
