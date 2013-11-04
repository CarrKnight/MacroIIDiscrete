/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import org.junit.Test;

import static org.mockito.Mockito.*;

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
 * @version 2013-11-04
 * @see
 */
public class GeographicalClearLastMarketTest
{

    //make sure it schedule itself correctly
    @Test
    public void testMakeSureItSchedules() throws Exception
    {

        //make sure start() puts it on the schedule
        MacroII mocked = mock(MacroII.class);
        GeographicalClearLastMarket market = new GeographicalClearLastMarket(GoodType.OIL);

        market.start(mocked);
        verify(mocked,times(1)).scheduleSoon(ActionOrder.TRADE,market, Priority.FINAL);
        //now check that step makes it schedule again!
        market.step(mocked);
        verify(mocked,times(1)).scheduleTomorrow(ActionOrder.TRADE,market,Priority.FINAL);

        market.turnOff();
        mocked = mock(MacroII.class);
        market.step(mocked);
        verify(mocked,never()).scheduleTomorrow(ActionOrder.TRADE,market,Priority.FINAL); //shouldn't reschedule


    }

}
