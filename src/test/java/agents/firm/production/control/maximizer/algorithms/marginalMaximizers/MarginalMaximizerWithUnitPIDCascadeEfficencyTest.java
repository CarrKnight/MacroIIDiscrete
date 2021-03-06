/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import financial.market.Market;
import financial.utilities.changeLooker.ChangeLookupMAMarket;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.DelayException;
import model.utilities.pid.PIDController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
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
 * @version 2013-03-04
 * @see
 */
@PrepareForTest(MarginalMaximizerStatics.class)
@RunWith(PowerMockRunner.class)
public class MarginalMaximizerWithUnitPIDCascadeEfficencyTest
{

    //no change in price, price stays the same


    //when price downstream is increasing, expect more production, so target higher efficency!
    @Test
    public void priceGoesUpEfficencyGoesUp() throws IllegalAccessException, DelayException {
        PowerMockito.mockStatic(MarginalMaximizerStatics.class);

        //fake market, fake model
        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class); when(macroII.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        HumanResources hr = mock(HumanResources.class);
        PlantControl control = mock(PlantControl.class);
        Firm owner = mock(Firm.class); when(hr.getFirm()).thenReturn(owner);    when(owner.getModel()).thenReturn(macroII);
        Plant p = mock(Plant.class); when(hr.getPlant()).thenReturn(p);

        //create the maximizer
        MarginalMaximizerWithUnitPIDCascadeEfficency maximizer = new MarginalMaximizerWithUnitPIDCascadeEfficency(
                hr,control,hr.getPlant(),hr.getFirm(),hr.getRandom(),hr.getPlant().getNumberOfWorkers());
        //make sure before the setup they are not instantiated
        ChangeLookupMAMarket changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        PIDController lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNull(lookAhead); assertNull(changeLookup);





        maximizer.setupLookup(5,1,1,0,market); //the numbers are there just so that any change in efficency is strong enough to catch it
        //look at private methods to make sure it was set up
        changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNotNull(changeLookup); assertNotNull(changeLookup);

        //prepare the statics
        PowerMockito.when(MarginalMaximizerStatics.marginalProduction(any(Plant.class),anyInt(),anyInt())).thenReturn(1); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class),any(Plant.class),any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).
                thenReturn(mock(CostEstimate.class)); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class), any(PlantControl.class), anyInt(), anyInt(), any(MarginalMaximizer.RandomizationPolicy.class)
        )).
                thenReturn(mock(CostEstimate.class)); //numbers are not important


        //now convince the price change lookup that prices went up
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(2);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(3);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(4);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(5);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(6);
        changeLookup.step(macroII);
        assertTrue(changeLookup.getChange() > 0); //there should be a positive change in the air

        //before calling it, the target efficency should be 1
        assertEquals(maximizer.getTargetEfficiency(), 1f, .000001f);

        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() > 1f); //target efficency should be up

        //if you do it again, should increase further
        float efficiency =maximizer.getTargetEfficiency();
        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() > efficiency); //target efficency should be up







    }



    //when price downstream is constant, targeted efficency should remain at 1!!!
    @Test
    public void priceConstantEfficencyConstant() throws IllegalAccessException, DelayException {
        PowerMockito.mockStatic(MarginalMaximizerStatics.class);

        //fake market, fake model
        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class); when(macroII.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        HumanResources hr = mock(HumanResources.class);
        PlantControl control = mock(PlantControl.class);
        Firm owner = mock(Firm.class); when(hr.getFirm()).thenReturn(owner);    when(owner.getModel()).thenReturn(macroII);
        Plant p = mock(Plant.class); when(hr.getPlant()).thenReturn(p);

        //create the maximizer
        MarginalMaximizerWithUnitPIDCascadeEfficency maximizer = new MarginalMaximizerWithUnitPIDCascadeEfficency(
                hr,control,hr.getPlant(),hr.getFirm(),hr.getRandom(),hr.getPlant().getNumberOfWorkers());
        //make sure before the setup they are not instantiated
        ChangeLookupMAMarket changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        PIDController lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNull(lookAhead); assertNull(changeLookup);





        maximizer.setupLookup(5,1,1,0,market); //the numbers are there just so that any change in efficency is strong enough to catch it
        //look at private methods to make sure it was set up
        changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNotNull(changeLookup); assertNotNull(changeLookup);

        //prepare the statics
        PowerMockito.when(MarginalMaximizerStatics.marginalProduction(any(Plant.class),anyInt(),anyInt())).thenReturn(1); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class),any(Plant.class),any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).
                thenReturn(mock(CostEstimate.class)); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class), any(PlantControl.class), anyInt(), anyInt(), any(MarginalMaximizer.RandomizationPolicy.class)
        )).
                thenReturn(mock(CostEstimate.class)); //numbers are not important


        //now convince the price change lookup that prices went up
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        assertTrue(changeLookup.getChange() == 0); //there should be a positive change in the air

        //before calling it, the target efficency should be 1
        assertEquals(maximizer.getTargetEfficiency(), 1f, .000001f);

        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() == 1f); //target efficency should be up

        //if you do it again, should increase further
        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() == 1f); //target efficency should be up







    }

    //when price downstream is decreasing, expect more production, so target lower efficency!
    @Test
    public void priceGoesDownEfficencyGoesDown() throws IllegalAccessException, DelayException {
        PowerMockito.mockStatic(MarginalMaximizerStatics.class);

        //fake market, fake model
        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class); when(macroII.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        HumanResources hr = mock(HumanResources.class);
        PlantControl control = mock(PlantControl.class);
        Firm owner = mock(Firm.class); when(hr.getFirm()).thenReturn(owner);    when(owner.getModel()).thenReturn(macroII);
        Plant p = mock(Plant.class); when(hr.getPlant()).thenReturn(p);

        //create the maximizer
        MarginalMaximizerWithUnitPIDCascadeEfficency maximizer = new MarginalMaximizerWithUnitPIDCascadeEfficency(
                hr,control,hr.getPlant(),hr.getFirm(),hr.getRandom(),hr.getPlant().getNumberOfWorkers());
        //make sure before the setup they are not instantiated
        ChangeLookupMAMarket changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        PIDController lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNull(lookAhead); assertNull(changeLookup);





        maximizer.setupLookup(5,.1f,.1f,0,market); //the numbers are there just so that any change in efficency is strong enough to catch it
        //look at private methods to make sure it was set up
        changeLookup = (ChangeLookupMAMarket) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class,
                "changeLookup").get(maximizer);
        lookAhead = (PIDController) PowerMockito.field(MarginalMaximizerWithUnitPIDCascadeEfficency.class, "lookAhead").get(maximizer);
        assertNotNull(changeLookup); assertNotNull(changeLookup);

        //prepare the statics
        PowerMockito.when(MarginalMaximizerStatics.marginalProduction(any(Plant.class),anyInt(),anyInt())).thenReturn(1); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class),any(Plant.class),any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).
                thenReturn(mock(CostEstimate.class)); //numbers are not important
        PowerMockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class), any(PlantControl.class), anyInt(), anyInt(), any(MarginalMaximizer.RandomizationPolicy.class)
        )).
                thenReturn(mock(CostEstimate.class)); //numbers are not important


        //now convince the price change lookup that prices went up
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(6);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(5);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(4);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(3);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(2);
        changeLookup.step(macroII);
        when(market.getLastPrice()).thenReturn(1);
        changeLookup.step(macroII);
        assertTrue(changeLookup.getChange() < 0); //there should be a negative change in the air

        //before calling it, the target efficency should be 1
        assertEquals(maximizer.getTargetEfficiency(), 1f, .000001f);

        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() < 1f); //target efficency should go down

        //if you do it again, should increase further
        float efficiency =maximizer.getTargetEfficiency();
        maximizer.chooseWorkerTarget(2,0,0,0,0,0,1,0); //most of the given variables are useless.
        assertTrue(maximizer.getTargetEfficiency() < efficiency); //target efficency should be down







    }

    //when lookup is not set up, make sure it defaults correctly


}
