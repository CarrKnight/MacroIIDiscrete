/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import au.com.bytecode.opencsv.CSVWriter;
import model.MacroII;
import model.scenario.FarmersAndWorkersScenarioTest;
import model.scenario.MonopolistScenario;
import model.scenario.OneLinkSupplyChainResult;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <h4>Description</h4>
 * <p> Writing down a csv with how much time it takes to run simple models. This should finally provide some sort of time series of how fast the program is going.
 * <p> Monopolist scenario is the simplest, has a bit of everything but really only one firm. Suplly chain has a lot going on, learning is off but you have many firms which is a pain.
 * Macro is easy on firms but has lots of customers trading with one another, utility functions and all that.
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-11
 * @see
 */
public class SpeedTest {


    private CSVWriter monopolistWriter;

    private CSVWriter supplyChainWriter;

    private CSVWriter macroWriter;

    private String date;

    private String computerName;

    @Before
    public void setUp() throws Exception {

        monopolistWriter = new CSVWriter(new FileWriter(Paths.get("testresources", "src/test/resources/timing","monopolistTime.csv").toFile(),true));
        supplyChainWriter = new CSVWriter(new FileWriter(Paths.get("testresources", "src/test/resources/timing","supplyChainTime.csv").toFile(),true));
        macroWriter = new CSVWriter(new FileWriter(Paths.get("testresources", "src/test/resources/timing","macroTime.csv").toFile(),true));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date currentDay = new Date();
        date = dateFormat.format(currentDay);

        computerName = InetAddress.getLocalHost().getHostName();

    }


    @Test
    public void takeTime() throws Exception {

        //monopolist
        double startTime = System.currentTimeMillis();
        runMonopolistScenario();
        double endTime = System.currentTimeMillis();
        double timeInSeconds = (endTime-startTime)/1000d;
        monopolistWriter.writeNext(new String[]{date,computerName, String.valueOf(timeInSeconds)});
        monopolistWriter.close();

        //supplychain
        startTime = System.currentTimeMillis();
        OneLinkSupplyChainResult.beefMonopolistOneRun(0,100,0,true,true,null, null, null);
        endTime = System.currentTimeMillis();
        timeInSeconds = (endTime-startTime)/1000d;
        supplyChainWriter.writeNext(new String[]{date,computerName, String.valueOf(timeInSeconds)});
        supplyChainWriter.close();

        //farmersAndWorkers
        startTime = System.currentTimeMillis();
        FarmersAndWorkersScenarioTest.runForcedCompetitiveFarmersAndWorkersScenario(10, 1, 200, System.currentTimeMillis(), null, null, 3000);
        endTime = System.currentTimeMillis();
        timeInSeconds = (endTime-startTime)/1000d;
        macroWriter.writeNext(new String[]{date,computerName, String.valueOf(timeInSeconds)});
        macroWriter.close();


    }

    private void runMonopolistScenario() {
        final MacroII macroII = new MacroII(0);
        MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
        scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        macroII.start();
        while(macroII.schedule.getTime()<5000)
            macroII.schedule.step(macroII);
        macroII.finish();
    }
}
