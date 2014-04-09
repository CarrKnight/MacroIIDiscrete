package model.scenario.oil;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import financial.market.GeographicalMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @version 2013-10-27
 * @see
 */
public class GeographicalCustomerTest {


    private final GeographicalMarket market = mock(GeographicalMarket.class);

    @Before
    public void setUp() throws Exception {
        when(market.getGoodType()).thenReturn(GoodType.OIL);
        when(market.submitBuyQuote(any(EconomicAgent.class),anyLong())).thenReturn(mock(Quote.class));

    }

    @Test
    public void testThatTheCustomerEatsOilEveryDay()
    {
        //create the model
        MacroII macroII = new MacroII(1l);
        //create the customer
        GeographicalCustomer customer = new GeographicalCustomer(macroII,10,2,2, market);
        //give it two units of oil
        customer.receive(new Good(GoodType.OIL,null,0),null);
        customer.receive(new Good(GoodType.OIL,null,0),null);
        Assert.assertEquals(customer.hasHowMany(GoodType.OIL),2);

        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.hasHowMany(GoodType.OIL),0);




    }

    @Test
    public void testThatTheCustomerKeepTheSameAmountOfMoney()
    {
        //target higher than what you have

        //create the model
        MacroII macroII = new MacroII(1l);
        //create the customer
        GeographicalCustomer customer = new GeographicalCustomer(macroII,10,2,2, market);
        long targetCash = customer.getResetCashTo() + 100;
        customer.setResetCashTo(targetCash);
        Assert.assertFalse(targetCash == customer.getCash());
        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.getCash(),targetCash);



        //target lower than what you have
        macroII = new MacroII(1l);
        //create the customer
        customer = new GeographicalCustomer(macroII,10,2,2, market);
        targetCash = customer.getResetCashTo() -1;
        customer.setResetCashTo(targetCash);
        Assert.assertFalse(targetCash == customer.getCash());
        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.getCash(),targetCash);


    }


    @Test
    public void testChoosingSuppliersByPrice(){

        GeographicalCustomer customer = new GeographicalCustomer(mock(MacroII.class),100,0,0, market);

        //firm 1, location 1,1 price 10
        GeographicalFirm firm1 = mock(GeographicalFirm.class);
        when(firm1.getxLocation()).thenReturn(1d);
        when(firm1.getyLocation()).thenReturn(1d);
        Quote quote1 = Quote.newSellerQuote(firm1, 10, mock(Good.class));


        //firm 2, location -1,1 price 11
        GeographicalFirm firm2 = mock(GeographicalFirm.class);
        when(firm2.getxLocation()).thenReturn(1d);
        when(firm2.getyLocation()).thenReturn(1d);
        Quote quote2 = Quote.newSellerQuote(firm2, 11, mock(Good.class));

        Multimap<GeographicalFirm,Quote> firms = HashMultimap.create(); firms.put(firm1,quote1); firms.put(firm2,quote2);

        Assert.assertEquals(firm1,customer.chooseSupplier(firms));

    }


    @Test
    public void testChoosingSuppliersByLocation(){

        GeographicalCustomer customer = new GeographicalCustomer(mock(MacroII.class),100,0,0, market);

        //firm 1, location 1,1 price 10
        GeographicalFirm firm1 = mock(GeographicalFirm.class);
        when(firm1.getxLocation()).thenReturn(1d);
        when(firm1.getyLocation()).thenReturn(1d);
        Quote quote1 = Quote.newSellerQuote(firm1, 10, mock(Good.class));

        //firm 2, location 2,2 price 10
        GeographicalFirm firm2 = mock(GeographicalFirm.class);
        when(firm2.getxLocation()).thenReturn(2d);
        when(firm2.getyLocation()).thenReturn(2d);
        Quote quote2 = Quote.newSellerQuote(firm2, 10, mock(Good.class));

        Multimap<GeographicalFirm,Quote> firms = HashMultimap.create(); firms.put(firm1,quote1); firms.put(firm2,quote2);

        Assert.assertEquals(firm1,customer.chooseSupplier(firms));

    }

    @Test
    public void testChoosingSuppliersByChoosingNone(){

        GeographicalCustomer customer = new GeographicalCustomer(mock(MacroII.class),100,0,0, market);
        Multimap<GeographicalFirm,Quote> firms = HashMultimap.create();

        //firm 1, location 1,1 price 1000
        GeographicalFirm firm1 = mock(GeographicalFirm.class);
        when(firm1.getxLocation()).thenReturn(1d);
        when(firm1.getyLocation()).thenReturn(1d);
        Quote quote = Quote.newSellerQuote(firm1, 1000, mock(Good.class));
        firms.put(firm1,quote);

        //firm 2, location 1,1 price 1000
        GeographicalFirm firm2 = mock(GeographicalFirm.class);
        when(firm2.getxLocation()).thenReturn(1d);
        when(firm2.getyLocation()).thenReturn(1d);
        quote = Quote.newSellerQuote(firm2, 1000, mock(Good.class));
        when(firm2.askedForASaleQuote(customer, GoodType.OIL)).thenReturn(quote);
        firms.put(firm2,quote);

        Assert.assertEquals(null,customer.chooseSupplier(firms));

    }

    @Test
    public void placingQuotes()
    {
        //daily demand 2, with empty inventory that's 0
        GeographicalMarket geographicalMarket = market;
        MacroII model = new MacroII(1l);
        final GeographicalCustomer customer = new GeographicalCustomer(model,100,0,0, geographicalMarket);
        customer.setDailyDemand(2);
        customer.start(model);
        model.start();
        model.schedule.step(model);

        //should have placed two quotes
        verify(geographicalMarket,times(2)).submitBuyQuote(customer,100);
        //should have called the clear all too
        verify(geographicalMarket,times(1)).removeAllBuyQuoteByBuyer(customer);


        //step again
        model.schedule.step(model);
        //two more quotes, total 4
        verify(geographicalMarket,times(4)).submitBuyQuote(customer,100);
        //and should have removed the quotes twice
        verify(geographicalMarket,times(2)).removeAllBuyQuoteByBuyer(customer);

        //now give the customer one unit of good between trade and production
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                customer.receive(new Good(GoodType.OIL,mock(EconomicAgent.class),100l),null);

            }
        });

        //now it should only add one more quote!
        model.schedule.step(model);
        //two more quotes, total 4
        verify(geographicalMarket,times(5)).submitBuyQuote(customer,100);
        //and should have removed the quotes twice
        verify(geographicalMarket,times(3)).removeAllBuyQuoteByBuyer(customer);


    }


}
