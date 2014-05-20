/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.decorators.MatchBestControlDecorator;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import org.junit.Assert;
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
 * @author Ernesto
 * @version 2012-10-23
 * @see
 */
public class MatchBestControlDecoratorTest {


    @Test
    public void simpleTest(){
        Firm firm = mock(Firm.class);
        Market market = mock(Market.class);  when(market.getGoodType()).thenReturn(UndifferentiatedGoodType.LABOR);
        when(market.isBestBuyPriceVisible()).thenReturn(true);
        HumanResources hr = mock(HumanResources.class); when(hr.getMarket()).thenReturn(market);
        Plant plant = mock(Plant.class);
        when(hr.isFixedPayStructure()).thenReturn(true); when(hr.getFirm()).thenReturn(mock(Firm.class));
        PlantControl control = mock(PlantControl.class);
        when(control.canBuy()).thenReturn(true);
        when(control.getHr()).thenReturn(hr);


        MatchBestControlDecorator decorator = new MatchBestControlDecorator(control);

        try {
            when(market.getBestBuyPrice()).thenReturn(100);
            decorator.tradeEvent(null,null,null,0,null,null);
        } catch (IllegalAccessException e) {
            Assert.fail();
        }


        for(int i=0; i < 100; i++)
        {
            //notify the decorator
            decorator.setCurrentWage(i);
            verify(control,times((int)i+1)).setCurrentWage(100);
            verify(control,times((int)i+1)).setCanBuy(false);

        }
        for(int i=100; i < 200; i++)
        {
            //notify the decorator
            decorator.setCurrentWage(i);
            verify(control,times(i==100 ? 101 : 1)).setCurrentWage(i);
            verify(control,times((int)i+1 -100)).setCanBuy(true);
        }


    }


    //do a properly dressed test


}
