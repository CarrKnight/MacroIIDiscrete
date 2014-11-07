package model.experiments.other;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.experiments.tuningRuns.MarginalMaximizerPIDTuning;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Very simple plotting of a learning monopolist and learning competitive
 * markets. Needed for a 3 pages presentation
 * Created by carrknight on 11/7/14.
 */
public class SFI30 {


    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("docs", "SFI30","rawdata"));

        monopolistRun();
        competitiveRun();


    }

    private static void competitiveRun() {
        //create the run
        MacroII macroII = new MacroII(0);
        TripolistScenario scenario = new TripolistScenario(macroII);
        macroII.setScenario(scenario);
        //5 competitors
        scenario.setAdditionalCompetitors(4);
        //use inventory buffers
        scenario.setAskPricingStrategy(InventoryBufferSalesControl.class);
        //increase production as long as MB>MC
        scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

        //start it and have one step
        macroII.start();
        macroII.schedule.step(macroII);

        //make sure the correct predictor strategy is used
        for(Firm firm : scenario.getCompetitors() )
        {
            final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            //learning
            assert salesDepartment.getPredictorStrategy() instanceof
                    ErrorCorrectingSalesPredictor;
        }


        //run the model for 5000 steps
        for(int j=0; j<5000; j++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, j, 100);
        }

        //write data to file
        macroII.getMarket(UndifferentiatedGoodType.GENERIC).
                getData().writeToCSVFile(Paths.get("docs", "SFI30","rawdata",
                "competitive.csv").toFile());
    }


    private static void monopolistRun() {
        //create the run
        MacroII macroII = new MacroII(0);
        //monopolist
        MonopolistScenario scenario = new MonopolistScenario(macroII);
        macroII.setScenario(scenario);
        //use inventory buffers
        scenario.setAskPricingStrategy(InventoryBufferSalesControl.class);
        //increase production as long as MB>MC
        scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

        //start it and have one step
        macroII.start();
        macroII.schedule.step(macroII);

        //make sure the correct predictor strategy is used
        Firm firm =scenario.getMonopolist();

        final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
        //learning
        assert salesDepartment.getPredictorStrategy() instanceof
                ErrorCorrectingSalesPredictor;



        //run the model for 5000 steps
        for(int j=0; j<5000; j++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, j, 100);
        }

        //write data to file
        macroII.getMarket(UndifferentiatedGoodType.GENERIC).
                getData().writeToCSVFile(Paths.get("docs", "SFI30","rawdata",
                "monopolist.csv").toFile());
    }



}
