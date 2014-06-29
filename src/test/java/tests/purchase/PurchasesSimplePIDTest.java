/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesSimplePID;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.Schedule;

import java.util.LinkedList;
import java.util.List;

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
 * @version 2012-08-15
 * @see
 */
public class PurchasesSimplePIDTest {


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestFromBelow(){

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);




        PurchasesSimplePID control = new PurchasesSimplePID(dept,.5f,2f,.05f);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0);
        assertEquals(control.getSingleProductionRunNeed(), 6);

        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            //    System.out.println(getCurrentInventory + " ---> " + pidPrice);

            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);


    }


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    //this time you start with initial price = 100
    @Test
    public void simpleStubTestFromAbove(){

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);




        PurchasesSimplePID control = new PurchasesSimplePID(dept,.5f,2f,.05f);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        control.setInitialPrice(100);
        assertEquals(pidPrice, 0l);
        assertEquals(control.getSingleProductionRunNeed(), 6);

        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE, control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
           //     System.out.println(getCurrentInventory + " ---> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);


    }


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubMidwayChange(){

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);




        PurchasesSimplePID control = new PurchasesSimplePID(dept,.5f,2f,.05f);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0l);
      assertEquals(control.getSingleProductionRunNeed(), 6);

        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,control);
        //for the first 50 turns is like the first test
        for(int i=0; i < 50; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
       //     System.out.println(getCurrentInventory + " ---> " + pidPrice);

            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);

        //and now you need half of the money!
        for(int i=0; i < 50; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/5f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
       //     System.out.println(getCurrentInventory + " ****> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 30 && pidPrice <= 40);


    }


}
