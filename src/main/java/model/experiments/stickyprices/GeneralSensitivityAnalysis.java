package model.experiments.stickyprices;

import static model.experiments.stickyprices.StickyPricesCSVPrinter.runWithDelay;

/**
 * Here to show the sensitivity analysis in the conclusion phase
 * Created by carrknight on 5/14/15.
 */
public class GeneralSensitivityAnalysis
{




    public static void main(String[] args) {
        //try different orders of magnitude

        //input MA
        runWithDelay(0, 0, 1, true, false, 100, 0, 0, "standard.csv");
        runWithDelay(0, 0, .5f, true, false, 100, 0, 0, "times2.csv");
        runWithDelay(0, 0, .2f, true, false, 100, 0, 0, "times5.csv");
        runWithDelay(0, 0, .15f, true, false, 100, 0, 0, "times6something.csv");
        runWithDelay(0, 0, 10f, true, false, 100, 0, 0, "divided10.csv");
        runWithDelay(0, 0, 100f, true, false, 100, 0, 0, "divided100.csv");
        runWithDelay(0, 0, 1000f, true, false, 100, 0, 0, "divided1000.csv");




    }
}
