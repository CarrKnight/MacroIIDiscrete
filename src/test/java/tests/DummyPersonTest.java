package tests;

import agents.DummyPerson;
import financial.market.DecentralizedMarket;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.ActionsAllowed;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.dummies.DummySeller;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Steppable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
 * @version 2012-09-20
 * @see
 */
public class DummyPersonTest {



    @Test
    public void testAddDemand() throws Exception {
        //make sure demand table is created as planned


        MacroII model = new MacroII(1l);

        Market laborMarket = mock(Market.class);
        when(laborMarket.getGoodType()).thenReturn(GoodType.LABOR);
        DummyPerson person = new DummyPerson(model,0l,100,laborMarket);


        OrderBookMarket beefMarket = mock(OrderBookMarket.class);  when(beefMarket.getGoodType()).thenReturn(GoodType.BEEF);
        when(beefMarket.getBuyerRole()).thenReturn( ActionsAllowed.QUOTE);
        person.addDemand(GoodType.BEEF,10l,5l,3l,beefMarket);

        Market leatherMarket = mock(Market.class); when(leatherMarket.getGoodType()).thenReturn(GoodType.LEATHER); when(leatherMarket.getBuyerRole()).thenReturn(ActionsAllowed.SEARCH);
        person.addDemand(GoodType.LEATHER,3l,100l,2l,leatherMarket);

        assertEquals((Object) person.getDemand(GoodType.BEEF, DummyPerson.DemandComponent.MAX_PRICE),10l);
        assertEquals((Object) person.getDemand(GoodType.LEATHER, DummyPerson.DemandComponent.CONSUMPTION_TIME),100l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_MONEY);
        assertEquals(person.getStatus(GoodType.LEATHER), DummyPerson.ShoppingStatus.SHOPPING);      //notice that shop() never goes to "waiting for money".








    }

    @Test
    public void testPlaceQuote() throws Exception {


        MacroII model = new MacroII(1l);
        Schedule fakeSchedule = mock(Schedule.class);
        model.schedule = fakeSchedule;


        Market laborMarket = mock(Market.class);
        when(laborMarket.getGoodType()).thenReturn(GoodType.LABOR);
        final DummyPerson person = new DummyPerson(model,0l,100,laborMarket);
        final boolean[] rightPriceQuote = {false}; //this is turned true if the person bids correctly through mockito  (turned into an array to avoid "final" issue)


        OrderBookMarket beefMarket = mock(OrderBookMarket.class);  when(beefMarket.getGoodType()).thenReturn(GoodType.BEEF);
        when(beefMarket.getBuyerRole()).thenReturn( ActionsAllowed.QUOTE);
        when(beefMarket.submitBuyQuote(person,10l)).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                rightPriceQuote[0] = true;
                return Quote.newBuyerQuote(person,10l,GoodType.BEEF);

            }
        });
        person.addDemand(GoodType.BEEF,10l,5l,3l,beefMarket);

        assertEquals((Object) person.getDemand(GoodType.BEEF, DummyPerson.DemandComponent.MAX_PRICE),10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_MONEY);

        //nothing should be scheduled or bid/whatever
        verify(fakeSchedule,times(0)).scheduleOnceIn(any(Double.class),any(Steppable.class));
        verify(beefMarket,times(0)).submitBuyQuote(person,10l);
        verify(beefMarket,times(0)).removeBuyQuote(any(Quote.class));

        //give some money to the dude, but not enough
        person.earn(5l);                                                         //nothing should change
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_MONEY);
        verify(fakeSchedule,times(0)).scheduleOnceIn(any(Double.class),any(Steppable.class));
        verify(beefMarket,times(0)).submitBuyQuote(person,10l);
        verify(beefMarket,times(0)).removeBuyQuote(any(Quote.class));

        //give him some more
        person.earn(10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_QUOTE); //should have quoted!
        verify(fakeSchedule,times(0)).scheduleOnceIn(any(Double.class),any(Steppable.class));
        verify(beefMarket,times(1)).submitBuyQuote(person,10l); //should be there!
        verify(beefMarket,times(0)).removeBuyQuote(any(Quote.class));

        //take away some money!
        person.burnMoney(10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_MONEY); //should have removed the quote!!
        verify(fakeSchedule,times(0)).scheduleOnceIn(any(Double.class),any(Steppable.class));
        verify(beefMarket,times(1)).submitBuyQuote(person,10l); //should be there!
        verify(beefMarket,times(1)).removeBuyQuote(any(Quote.class));






    }

    @Test
    public void testBuyQuote() throws Exception {



        MacroII model = new MacroII(1l);
        Schedule fakeSchedule = mock(Schedule.class);
        model.schedule = fakeSchedule;


        Market laborMarket = mock(Market.class);
        when(laborMarket.getGoodType()).thenReturn(GoodType.LABOR);
        final DummyPerson person = new DummyPerson(model,0l,100,laborMarket);


        OrderBookMarket beefMarket = new OrderBookMarket(GoodType.BEEF);
        beefMarket.setOrderHandler(new ImmediateOrderHandler(),model);
        DummySeller seller = new DummySeller(model,5l);
        beefMarket.registerSeller(seller);
        Good toSell = new Good(GoodType.BEEF,seller,3l); seller.receive(toSell,null);
        beefMarket.submitSellQuote(seller,5l,toSell);


        beefMarket.registerBuyer(person);
        person.addDemand(GoodType.BEEF,10l,5l,3l,beefMarket);


        //he doesn't have money!
        assertEquals((Object) person.getDemand(GoodType.BEEF, DummyPerson.DemandComponent.MAX_PRICE), 10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.WAITING_FOR_MONEY);
        assertEquals(beefMarket.getBestSellPrice(),5l);
        verify(fakeSchedule, times(0)).scheduleOnceIn(any(Double.class),any(Steppable.class));

        //give him money
        person.earn(100l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.CONSUMING); //should have bought!
        verify(fakeSchedule,times(1)).scheduleOnceIn(any(Double.class),any(Steppable.class));
        assertEquals(beefMarket.getBestSellPrice(),-1l); //should have been taken




    }

    @Test
    public void testBuyShopping() throws Exception {



        MacroII model = new MacroII(1l);
        Schedule fakeSchedule = mock(Schedule.class);
        model.schedule = fakeSchedule;


        Market laborMarket = mock(Market.class);
        when(laborMarket.getGoodType()).thenReturn(GoodType.LABOR);
        final DummyPerson person = new DummyPerson(model,0l,100,laborMarket);


        DecentralizedMarket beefMarket = new DecentralizedMarket(GoodType.BEEF);
        DummySeller seller = new DummySeller(model,5l);
        beefMarket.registerSeller(seller);
        Good toSell = new Good(GoodType.BEEF,seller,3l); seller.receive(toSell, null);



        beefMarket.registerBuyer(person);
        person.earn(100l);
        person.addDemand(GoodType.BEEF,10l,5l,3l,beefMarket);


        //he should have found the guy and bought from him!
        assertEquals((Object) person.getDemand(GoodType.BEEF, DummyPerson.DemandComponent.MAX_PRICE),10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.CONSUMING);
        assertTrue(!seller.hasAny(GoodType.BEEF));
        verify(fakeSchedule,times(1)).scheduleOnceIn(any(Double.class),any(Steppable.class)); //should have called shopping!





    }



    @Test
    public void testEat() throws Exception {

        //starts like shopping test
        MacroII model = new MacroII(1l);
        Schedule fakeSchedule = mock(Schedule.class);
        model.schedule = fakeSchedule;


        Market laborMarket = mock(Market.class);
        when(laborMarket.getGoodType()).thenReturn(GoodType.LABOR);
        final DummyPerson person = new DummyPerson(model,0l,100,laborMarket);


        DecentralizedMarket beefMarket = new DecentralizedMarket(GoodType.BEEF);
        DummySeller seller = new DummySeller(model,5l);
        beefMarket.registerSeller(seller);
        Good toSell = new Good(GoodType.BEEF,seller,3l); seller.receive(toSell, null);



        beefMarket.registerBuyer(person);
        person.earn(100l);
        person.addDemand(GoodType.BEEF,10l,5l,3l,beefMarket);


        //he should have found the guy and bought from him!
        assertEquals((Object) person.getDemand(GoodType.BEEF, DummyPerson.DemandComponent.MAX_PRICE),10l);
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.CONSUMING);
        assertTrue(!seller.hasAny(GoodType.BEEF));
        verify(fakeSchedule,times(1)).scheduleOnceIn(any(Double.class),any(Steppable.class)); //should have called shopping!

        //and now call eat
        person.eat(GoodType.BEEF);
        //it should still be consuming
        assertEquals(person.getStatus(GoodType.BEEF), DummyPerson.ShoppingStatus.CONSUMING);
        //to shop should have been scheduled now!
        verify(fakeSchedule,times(2)).scheduleOnceIn(any(Double.class),any(Steppable.class)); //should have called shopping!






    }

    @Test
    public void testTryAgainNextTime() throws Exception {

    }
}
