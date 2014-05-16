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

        when(market.getLastPrice()).thenReturn(10);
        assertEquals(lookup.getPrice(),10);

        when(market.getLastPrice()).thenReturn(20);
        assertEquals(lookup.getPrice(),20);
        when(market.getLastPrice()).thenReturn(30);
        assertEquals(lookup.getPrice(),30);
        when(market.getLastPrice()).thenReturn(40);
        assertEquals(lookup.getPrice(),40);


        //something visible means non-visible
        market = mock(Market.class);
        when(market.isBestSalePriceVisible()).thenReturn(true);    //this is CHANGED TO TRUE
        when(market.isBestBuyPriceVisible()).thenReturn(false);

        lookup = new PriceLookupOnMarket(market);

        when(market.getLastPrice()).thenReturn(10);
        assertEquals(lookup.getPrice(),10);

        when(market.getLastPrice()).thenReturn(20);
        assertEquals(lookup.getPrice(),20);
        when(market.getLastPrice()).thenReturn(30);
        assertEquals(lookup.getPrice(),30);
        when(market.getLastPrice()).thenReturn(40);
        assertEquals(lookup.getPrice(),40);
    }

    //on non visible markets
    @Test
    public void visibleMarkets() throws IllegalAccessException {
        //nothing visible.
        Market market = mock(Market.class);
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.isBestBuyPriceVisible()).thenReturn(true);

        PriceLookup lookup = new PriceLookupOnMarket(market);

        when(market.getLastPrice()).thenReturn(10); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(-1); //no best sell price
        when(market.getBestBuyPrice()).thenReturn(-1); //no best ask price
        assertEquals(lookup.getPrice(),10);


        when(market.getLastPrice()).thenReturn(10); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(20); //best sell is available
        when(market.getBestBuyPrice()).thenReturn(-1); //no best ask price
        assertEquals(lookup.getPrice(),20);

        when(market.getLastPrice()).thenReturn(10); //last closing price is 10
        when(market.getBestSellPrice()).thenReturn(20); //best sell is available
        when(market.getBestBuyPrice()).thenReturn(40); //best ask is available
        assertEquals(lookup.getPrice(),30); //average



    }


}
