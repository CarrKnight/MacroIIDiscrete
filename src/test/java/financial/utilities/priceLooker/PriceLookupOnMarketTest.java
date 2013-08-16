package financial.utilities.priceLooker;

import financial.market.Market;
import static org.junit.Assert.*;import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 * @version 2013-02-25
 * @see
 */
public class PriceLookupOnMarketTest {

    //on non visible markets
    @Test
    public void nonVisibleMarkets()
    {
        //nothing visible.
        Market market = mock(Market.class);
        when(market.isBestSalePriceVisible()).thenReturn(false);
        when(market.isBestBuyPriceVisible()).thenReturn(false);

        PriceLookup lookup = new PriceLookupOnMarket(market);

        when(market.getLastPrice()).thenReturn(10l);
        assertEquals(lookup.getPrice(),10l);

        when(market.getLastPrice()).thenReturn(20l);
        assertEquals(lookup.getPrice(),20l);
        when(market.getLastPrice()).thenReturn(30l);
        assertEquals(lookup.getPrice(),30l);
        when(market.getLastPrice()).thenReturn(40l);
        assertEquals(lookup.getPrice(),40l);


        //something visible means non-visible
        market = mock(Market.class);
        when(market.isBestSalePriceVisible()).thenReturn(true);    //this is CHANGED TO TRUE
        when(market.isBestBuyPriceVisible()).thenReturn(false);

        lookup = new PriceLookupOnMarket(market);

        when(market.getLastPrice()).thenReturn(10l);
        assertEquals(lookup.getPrice(),10l);

        when(market.getLastPrice()).thenReturn(20l);
        assertEquals(lookup.getPrice(),20l);
        when(market.getLastPrice()).thenReturn(30l);
        assertEquals(lookup.getPrice(),30l);
        when(market.getLastPrice()).thenReturn(40l);
        assertEquals(lookup.getPrice(),40l);
    }

    //on non visible markets
    @Test
    public void visibleMarkets() throws IllegalAccessException {
        //nothing visible.
        Market market = mock(Market.class);
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.isBestBuyPriceVisible()).thenReturn(true);

        PriceLookup lookup = new PriceLookupOnMarket(market);

        when(market.getLastPrice()).thenReturn(10l); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(-1l); //no best sell price
        when(market.getBestBuyPrice()).thenReturn(-1l); //no best ask price
        assertEquals(lookup.getPrice(),10l);


        when(market.getLastPrice()).thenReturn(10l); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(20l); //best sell is available
        when(market.getBestBuyPrice()).thenReturn(-1l); //no best ask price
        assertEquals(lookup.getPrice(),20l);

        when(market.getLastPrice()).thenReturn(10l); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(20l); //best sell is available
        when(market.getBestBuyPrice()).thenReturn(40l); //best ask is available
        assertEquals(lookup.getPrice(),30l); //average



    }


}
