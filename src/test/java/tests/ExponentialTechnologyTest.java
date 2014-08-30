/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.DRSExponentialMachinery;
import agents.firm.production.technology.ExponentialMachinery;
import agents.firm.production.technology.IRSExponentialMachinery;
import agents.people.Person;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/12/12
 * Time: 9:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExponentialTechnologyTest {

    Plant p;
    ExponentialMachinery crs;
    ExponentialMachinery irs;
    ExponentialMachinery drs;



    @Before
    public void setup(){

        


        Blueprint b = new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,2).build(); //create a simple output
        MacroII macro = new MacroII(1);
        Firm f = new Firm(macro); //used as the "producer of the capital goods"


        p = new Plant(b,new Firm(macro));
        crs = new CRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,f,0,p,1f,1f);
        irs = new IRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,f,0,p,1f,1f);
        drs = new DRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,f,0,p,1f,1f);


        //addSalesDepartmentListener two workers
        p.setPlantMachinery(crs);  //add any as default
        p.addWorker(new Person(macro));
        p.addWorker(new Person(macro));
        p.setBlueprint(b); //put it inside the plant



    }


    @Test
    public void testDeltaFunction() throws Exception {


        assertEquals(1.0f, crs.deltaFunction(1), .0001f);
        assertEquals(1.0f, irs.deltaFunction(1), .0001f);
        assertEquals(1.0f, drs.deltaFunction(1), .0001f);

        assertEquals(2.0f, crs.deltaFunction(2), .0001f);
        assertEquals(4.0f, irs.deltaFunction(2), .0001f);
        assertEquals(1.41421356f, drs.deltaFunction(2), .0001f);



    }

    @Test
    public void testHypotheticalExpectedWaitingTime() throws Exception {

        

        assertEquals(1.0f, crs.hypotheticalWaitingTime(1), .0001f);
        assertEquals(1.0f, irs.hypotheticalWaitingTime(1), .0001f);
        assertEquals(1.0f, drs.hypotheticalWaitingTime(1), .0001f);


        assertEquals(0.5f, crs.hypotheticalWaitingTime(2), .0001f);
        assertEquals(0.25f, irs.hypotheticalWaitingTime(2), .0001f);
        assertEquals(1f / 1.41421356f, drs.hypotheticalWaitingTime(2), .0001f);

    }

    @Test
    public void testHypotheticalExpectedWeeklyProductionRuns() throws Exception {


        assertEquals(1.0f * p.getModel().getWeekLength(), crs.hypotheticalWeeklyProductionRuns(1), .0001f);
        assertEquals(1.0f * p.getModel().getWeekLength(), irs.hypotheticalWeeklyProductionRuns(1), .0001f);
        assertEquals(1.0f * p.getModel().getWeekLength(), drs.hypotheticalWeeklyProductionRuns(1), .0001f);

        assertEquals(2.0f * p.getModel().getWeekLength(), crs.hypotheticalWeeklyProductionRuns(2), .0001f);
        assertEquals(4.0f * p.getModel().getWeekLength(), irs.hypotheticalWeeklyProductionRuns(2), .0001f);
        assertEquals(1.41421356f * p.getModel().getWeekLength(), drs.hypotheticalWeeklyProductionRuns(2), .0001f);


    }

    @Test
    public void testExpectedWaitingTime() throws Exception {

        


        assertEquals(0.5f, crs.expectedWaitingTime(), .0001f);
        assertEquals(0.25f, irs.expectedWaitingTime(), .0001f);
        assertEquals(1f / 1.41421356f, drs.expectedWaitingTime(), .0001f);


    }

    @Test
    public void testExpectedWeeklyProduction() throws Exception {
        

        assertEquals(2f * 2.0f * p.getModel().getWeekLength(), crs.weeklyThroughput(UndifferentiatedGoodType.GENERIC), .0001f);
        assertEquals(2f * 4.0f * p.getModel().getWeekLength(), irs.weeklyThroughput(UndifferentiatedGoodType.GENERIC), .0001f);
        assertEquals(2f * 1.41421356f * p.getModel().getWeekLength(), drs.weeklyThroughput(UndifferentiatedGoodType.GENERIC), .0001f);

    }

    @Test
    public void testMarginalProductOfWorker() throws Exception {

        

        assertEquals(2f * p.getModel().getWeekLength(), crs.marginalProductOfWorker(UndifferentiatedGoodType.GENERIC), .0001f);
        assertEquals(2f * 5.0f * p.getModel().getWeekLength(), irs.marginalProductOfWorker(UndifferentiatedGoodType.GENERIC), .0001f);
        assertEquals(2f * 0.317837245 * p.getModel().getWeekLength(), drs.marginalProductOfWorker(UndifferentiatedGoodType.GENERIC), .0001f);



    }



    public void testNextWaitTime() throws Exception {

        for(int i=0; i < 300; i++)
            System.out.println(crs.nextWaitingTime());



    }

}
