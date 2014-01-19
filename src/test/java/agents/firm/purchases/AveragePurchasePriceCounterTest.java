/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
 * @version 2013-08-21
 * @see
 */
public class AveragePurchasePriceCounterTest {

    //counting test
    @Test
    public void countingTest(){

        AveragePurchasePriceCounter counter = new AveragePurchasePriceCounter(mock(PurchasesDepartment.class));

        Assert.assertEquals(-1,counter.getTodayAverageClosingPrice(),.0001f);

        counter.getNotifiedOfFilledQuote(10);
        counter.getNotifiedOfFilledQuote(20);
        counter.getNotifiedOfFilledQuote(30);
        Assert.assertEquals(20,counter.getTodayAverageClosingPrice(),.0001f);

        //reset
        counter.step(mock(MacroII.class));

        Assert.assertEquals(-1,counter.getTodayAverageClosingPrice(),.0001f);


    }


    //reschedule test
    @Test
    public void rescheduleTest(){

        AveragePurchasePriceCounter counter = new AveragePurchasePriceCounter(mock(PurchasesDepartment.class));

        MacroII mocked = mock(MacroII.class);
        //start should start it
        counter.start(mocked);
        verify(mocked).scheduleSoon(ActionOrder.DAWN,counter);

        //step should reschedule it!
        for(int i=0; i<5; i++)
        {
            mocked = mock(MacroII.class);
            counter.step(mocked);
            verify(mocked).scheduleTomorrow(ActionOrder.DAWN,counter);

        }

        //turn off and it stops rescheduling!
        counter.turnOff();
        mocked = mock(MacroII.class);
        counter.step(mocked);
        verify(mocked,never()).scheduleTomorrow(ActionOrder.DAWN,counter);


    }

}
