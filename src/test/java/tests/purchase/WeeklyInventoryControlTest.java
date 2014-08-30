/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.people.Person;
import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.DailyInventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;

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
 * @author Ernesto
 * @version 2012-08-11
 * @see
 */
public class WeeklyInventoryControlTest {


    MacroII model;
    Market market;


    @Before
    public void setup(){
        model = new MacroII(1);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);

    }

    //with no plants, it is basically useless
    @Test
    public void stubInventoryRatingNoPlant()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(1);
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, DailyInventoryControl.class,
                null, null, null).getDepartment();

        //target inventory is going to be 0 because there is no plant

        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(10);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(5);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);

    }


    @Test
    public void demandGap()
    {
        model.setWeekLength(100);


        Firm f = new Firm(model);
        Plant plant = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        plant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(plant);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, DailyInventoryControl.class,
                null, null, null).getDepartment();

        DailyInventoryControl control = new DailyInventoryControl(dept);
        control.setHowManyDaysOfInventoryToHold(7);
        dept.setControl(control);


        //without workers and machinery the need is always 0

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        f.receiveMany(UndifferentiatedGoodType.GENERIC,9);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <100; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        //adding a new plant has no effect at all!
        Plant newPlant = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,100, DifferentiatedGoodType.CAPITAL,1),f);
        newPlant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(newPlant);
        //the new requirement should automatically be put to 100
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);


        //without workers the need is stil 0
        plant.setPlantMachinery(new CRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,f,0,plant,1));

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        f.receiveMany(UndifferentiatedGoodType.GENERIC,9);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        f.consumeMany(UndifferentiatedGoodType.GENERIC,5);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);

        f.receiveMany(UndifferentiatedGoodType.GENERIC,100);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), false);



        //now with workers the need is different
        plant.addWorker(new Person(model));     //we make 100 runs a week each requiring 6 ---> weekly needs = 600, danger level should still be 6 (single production run of non-empty firms)


        //216 goods as of now
        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(216-600,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(216+9-600,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <400; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        assertEquals(0,dept.estimateDemandGap());
        assertEquals(dept.canBuy(), true);

        while(f.hasAny(UndifferentiatedGoodType.GENERIC))
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        assertEquals(-600,dept.estimateDemandGap());

        assertEquals(dept.canBuy(), true);

    }


    //with no plants, it is basically useless

    @Test
    public void InventoryRatingNoPlant()
    {
        Firm f = new Firm(model);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, DailyInventoryControl.class,
                null, null, null).getDepartment();


        //target inventory is going to be 0 because there is no plant

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);


    }


    @Test
    public void stubInventoryRating() throws IllegalAccessException, NoSuchFieldException {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);

        Plant p = mock(Plant.class);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        when(p.getBlueprint()).thenReturn(b);
        LinkedList<Plant> list = new LinkedList<>();
        list.add(p);
        when(f.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(list);

        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, DailyInventoryControl.class,
                null, null, null).getDepartment();
        when(f.removePlantCreationListener(any())).thenReturn(true);

        DailyInventoryControl control = new DailyInventoryControl(dept);
        control.setHowManyDaysOfInventoryToHold(7);
        dept.setControl(control);


        //unfortunately weekly production is still 0!

        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(1);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(10);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(5);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);


        //simulate a faster production
        when(p.expectedWeeklyProductionRuns()).thenReturn(1f);
        when(p.expectedWeeklyProductionRuns()).thenReturn(1f);

        //now I have to update the weekly inventory control manually for this stub
        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        control = (DailyInventoryControl) field.get(dept);
        control.changeInWorkforceEvent(p,100, 99);  //the number of workers is ignored anyway!


        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(1);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(10);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH); //more than twice the weekly needs is too much
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(5);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);

    }

    @Test
    public void InventoryRating()
    {
        model.setWeekLength(100);


        Firm f = new Firm(model);
        Plant plant = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        plant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(plant);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, DailyInventoryControl.class,
                null, null, null).getDepartment();

        DailyInventoryControl control = new DailyInventoryControl(dept);
        control.setHowManyDaysOfInventoryToHold(7);
        dept.setControl(control);


        //without workers and machinery the need is always 0

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <100; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        //adding a new plant has no effect at all!
        Plant newPlant = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,100, DifferentiatedGoodType.CAPITAL,1),f);
        newPlant.setPlantMachinery(mock(Machinery.class));
        f.addPlant(newPlant);
        //the new requirement should automatically be put to 100
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);


        //without workers the need is stil 0
        plant.setPlantMachinery(new CRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,f,0,plant,1));

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <100; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), false);



        //now with workers the need is different
        plant.addWorker(new Person(model));     //we make 100 runs a week each requiring 6 ---> weekly needs = 600, danger level should still be 6 (single production run of non-empty firms)


        //215 goods as of now
        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <400; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);
        assertEquals(dept.canBuy(), true);

        while(f.hasAny(UndifferentiatedGoodType.GENERIC))
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        assertEquals(dept.canBuy(), true);







    }



}
