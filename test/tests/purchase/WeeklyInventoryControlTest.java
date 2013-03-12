package tests.purchase;

import agents.Person;
import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.inventoryControl.WeeklyInventoryControl;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;

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
 * @version 2012-08-11
 * @see
 */
public class WeeklyInventoryControlTest {


    MacroII model;
    Market market;


    @Before
    public void setup(){
        model = new MacroII(1l);
        market = new OrderBookMarket(GoodType.GENERIC);

    }

    //with no plants, it is basically useless
    @Test
    public void stubInventoryRatingNoPlant()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, WeeklyInventoryControl.class,
                null, null, null).getDepartment();

        //target inventory is going to be 0 because there is no plant

        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);

    }



    @Test
    public void InventoryRatingNoPlant()
    {
        Firm f = new Firm(model);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, WeeklyInventoryControl.class,
                null, null, null).getDepartment();


        //target inventory is going to be 0 because there is no plant

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);


    }


    @Test
    public void stubInventoryRating() throws IllegalAccessException, NoSuchFieldException {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);

        Plant p = mock(Plant.class);
        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        when(p.getBlueprint()).thenReturn(b);
        LinkedList<Plant> list = new LinkedList<>();
        list.add(p);
        when(f.getListOfPlantsUsingSpecificInput(GoodType.GENERIC)).thenReturn(list);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, WeeklyInventoryControl.class,
                null, null, null).getDepartment();

        //unfortunately weekly production is still 0!

        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);


        //simulate a faster production
        when(p.expectedWeeklyProductionRuns()).thenReturn(1f);
        when(p.expectedWeeklyProductionRuns()).thenReturn(1f);

        //now I have to update the weekly inventory control manually for this stub
        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        WeeklyInventoryControl control = (WeeklyInventoryControl) field.get(dept);
        control.changeInWorkforceEvent(p,100);  //the number of workers is ignored anyway!


        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH); //more than twice the weekly needs is too much
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);

    }

    @Test
    public void InventoryRating()
    {
        Market.TESTING_MODE = true;
        model.setWeekLength(100);


        Firm f = new Firm(model);
        Plant plant = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1),f);
        plant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(plant);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, WeeklyInventoryControl.class,
                null, null, null).getDepartment();


        //without workers and machinery the need is always 0

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <100; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        //adding a new plant has no effect at all!
        Plant newPlant = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,100,GoodType.CAPITAL,1),f);
        newPlant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(newPlant);
        //the new requirement should automatically be put to 100
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.receive(new Good(GoodType.GENERIC, f, 0l), null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);


        //without workers the need is stil 0
        plant.setPlantMachinery(new CRSExponentialMachinery(GoodType.CAPITAL,f,0l,plant,1));

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <100; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);



        //now with workers the need is different
        plant.addWorker(new Person(model));     //we make 100 runs a week each requiring 6 ---> weekly needs = 600, danger level should still be 6 (single production run of non-empty firms)


        //215 goods as of now
        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <400; i++)
            f.receive(new Good(GoodType.GENERIC, f, 0l), null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        Assert.assertEquals(dept.canBuy(), true);

        while(f.hasAny(GoodType.GENERIC))
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        Assert.assertEquals(dept.canBuy(), true);







    }



}
