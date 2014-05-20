/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.production.Plant;
import goods.GoodType;
import model.utilities.Deactivatable;

/**
 * <h4>Description</h4>
 * <p/>
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
public interface ProfitReport extends Deactivatable {
    void weekEnd();

    float getPlantProfits(Plant p);

    int getAggregateProfits();

    float getEfficiencyRatio(Plant p);

    float getNetProfitRatio(Plant p);

    float getPlantRevenues(Plant p);

    float getPlantCosts(Plant p);

    int getLastWeekProduction(GoodType type);
}
