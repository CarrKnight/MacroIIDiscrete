package agents.firm.purchases;

import agents.firm.Firm;
import agents.firm.purchases.inventoryControl.InventoryControl;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
 * @version 2013-02-14
 * @see
 */
public class InflowOutflowCounterTest {

    //raw, test the listener by calling its step and so on
    @Test
    public void rawCountTest()
    {

        MacroII model = mock(MacroII.class);
        Firm firm = new Firm(model);
        InflowOutflowCounter toTest = new InflowOutflowCounter(model,firm, GoodType.GENERIC); toTest.start();
        assertEquals(toTest.getTodayInflow(), 0);
        assertEquals(toTest.getTodayInflow(),0);


        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.LEATHER,mock(Firm.class),1l),null); //this is NOT counted
        firm.deliver(GoodType.GENERIC,mock(Firm.class),2l); //counted
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.LEATHER); //not
        assertEquals(toTest.getTodayInflow(),2);
        assertEquals(toTest.getTodayOutflow(),2);

        //reset
        when(model.getCurrentPhase()).thenReturn(ActionOrder.DAWN);
        toTest.step(model);
        assertEquals(toTest.getTodayInflow(),0);
        assertEquals(toTest.getTodayInflow(),0);

        //should still work
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        assertEquals(toTest.getTodayInflow(),1);
        assertEquals(toTest.getTodayOutflow(),0);





    }

    //same as raw, but steps the model instead of calling step directly
    @Test
    public void lessrawCountTest()
    {

        MacroII model =  new MacroII(1l);
        Firm firm = new Firm(model);
        InflowOutflowCounter toTest = new InflowOutflowCounter(model,firm, GoodType.GENERIC); toTest.start();
        assertEquals(toTest.getTodayInflow(), 0);
        assertEquals(toTest.getTodayInflow(),0);


        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.LEATHER,mock(Firm.class),1l),null); //this is NOT counted
        firm.deliver(GoodType.GENERIC,mock(Firm.class),2l); //counted
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.LEATHER); //not
        assertEquals(toTest.getTodayInflow(),2);
        assertEquals(toTest.getTodayOutflow(),2);

        //reset
        model.getPhaseScheduler().step(model);
        assertEquals(toTest.getTodayInflow(),0);
        assertEquals(toTest.getTodayInflow(),0);

        //should still work
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        assertEquals(toTest.getTodayInflow(),3);
        assertEquals(toTest.getTodayOutflow(),0);



        //reset
        model.getPhaseScheduler().step(model);

        //netouflow = 1
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.GENERIC); //yesss
        assertEquals(toTest.getTodayInflow(),2);
        assertEquals(toTest.getTodayOutflow(),3);
        assertEquals(firm.hasHowMany(GoodType.GENERIC),2);
        assertEquals(toTest.currentDaysOfInventory(),2f,.001f); //2 days of inventory left (2 goods, outflow of 1)






    }


    //check that it works when embedded in a purchase department
    //same as raw, but steps the model instead of calling step directly
    @Test
    public void embeddedTest()
    {

        MacroII model =  new MacroII(1l);
        Firm firm = new Firm(model);
        Market market = new OrderBookMarket(GoodType.GENERIC);
        PurchasesDepartment toTest = new PurchasesDepartment(10000000,firm,market,model); toTest.setControl(mock(InventoryControl.class));
        firm.registerPurchasesDepartment(toTest,GoodType.GENERIC);
        firm.start(model);

        assertEquals(toTest.getTodayInflow(), 0);
        assertEquals(toTest.getTodayInflow(),0);


        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.LEATHER,mock(Firm.class),1l),null); //this is NOT counted
        firm.deliver(GoodType.GENERIC,mock(Firm.class),2l); //counted
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.LEATHER); //not
        assertEquals(toTest.getTodayInflow(),2);
        assertEquals(toTest.getTodayOutflow(),2);

        //reset
        model.start();
        model.schedule.step(model);
        assertEquals(toTest.getTodayInflow(),0);
        assertEquals(toTest.getTodayInflow(),0);

        //should still work
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        assertEquals(toTest.getTodayInflow(),3);
        assertEquals(toTest.getTodayOutflow(),0);



        //reset
        model.schedule.step(model);

        //netouflow = 1
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.receive(new Good(GoodType.GENERIC,mock(Firm.class),1l),null); //this is counted
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.GENERIC); //yesss
        firm.consume(GoodType.GENERIC); //yesss
        assertEquals(toTest.getTodayInflow(),2);
        assertEquals(toTest.getTodayOutflow(),3);
        assertEquals(firm.hasHowMany(GoodType.GENERIC),2);
        assertEquals(toTest.currentDaysOfInventory(),2f,.001f); //2 days of inventory left (2 goods, outflow of 1)









    }




}
