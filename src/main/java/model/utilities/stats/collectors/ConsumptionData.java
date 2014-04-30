/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;

import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> like production plant data, but counting consumption instead
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-03
 * @see
 */
public class ConsumptionData extends ProductionData {


    @Override
    public void step(SimState state) {
        Preconditions.checkState(plant != null);
        if(!active)
            return;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        assert model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING);
        //set starting day if needed
        if(getStartingDay()==-1)
            setCorrectStartingDate(model);
        assert getStartingDay() >=0;


        //memorize
        //grab the production vector

        final Set<GoodType> sectorList = model.getGoodTypeMasterList().getListOfAllSectors();
        for(GoodType type : sectorList)  //record all consumption
        {
            if(data.get(type)== null) //this can happen if a new good sector has been created
                fillNewSectorObservationsWith0(model, type);

            data.get(type).add((double) plant.getConsumedToday(type));
        }
        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);




    }
}
