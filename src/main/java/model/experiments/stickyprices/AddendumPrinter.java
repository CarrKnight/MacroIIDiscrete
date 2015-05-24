package model.experiments.stickyprices;

import static model.experiments.stickyprices.StickyPricesCSVPrinter.runWithDelay;

/**
 * Figures to print after the first round of reviews
 * Created by carrknight on 5/13/15.
 */
public class AddendumPrinter {




    public static void main(String[] args) {
        //with delay

        //input MA
        runWithDelay(20, 0, 1, true, false, 100, 20, 0, "inputMADelay.csv");
        System.out.println("done");
        //output MA
        runWithDelay(20, 0, 1, true, false, 100, 0, 20, "outputMADelay.csv");
        System.out.println("done");
        //no MA
        runWithDelay(20, 0, 1, true, false, 100, 0, 0, "noMADelay.csv");
        System.out.println("done");

        //without delay:
        //input MA
        runWithDelay(0, 0, 1, true, false, 100, 20, 0, "inputMAEasy.csv");
        System.out.println("done");
        //output MA
        runWithDelay(0, 0, 1, true, false, 100, 0, 20, "outputMAEasy.csv");
        System.out.println("done");
        //no MA
        runWithDelay(0, 0, 1, true, false, 100, 0, 0, "noMAEasy.csv");
        System.out.println("done");
    }

}
