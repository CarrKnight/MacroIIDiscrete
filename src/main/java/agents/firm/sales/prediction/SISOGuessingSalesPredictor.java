/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.io.Files;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.regression.SISOGuessingRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

/**
 * A multiple model regression where one gets chosen to predict, possibly by simulating.
 * Created by carrknight on 8/26/14.
 */
public class SISOGuessingSalesPredictor extends BaseSalesPredictor implements Steppable, Deactivatable {


    private final SalesDataType xVariable = SalesDataType.WORKERS_PRODUCING_THIS_GOOD;
    private BufferedWriter debug;
    /**
     * how many observations before we start actually predicting
     */
    private int burnOut = 500;

    /**
     * how many steps into the future to simulate!
     */
    private int stepsIntoTheFutureToSimulate = 100;

    /**
     * the sales department to use
     */
    private final SalesDepartment toFollow;

    /**
     * the set of regressions to use
     */
    private final  SISOGuessingRegression regression;


    public SISOGuessingSalesPredictor(MacroII model, SalesDepartment toFollow) {
        this.toFollow = toFollow;
        regression = new SISOGuessingRegression(0,1,10,20,50,100);
        regression.setRoundError(true);
        regression.setHowManyObservationsBeforeModelSelection(burnOut);
        //schedule yourself
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);

        try {
            Paths.get("runs", "tmp2.csv").toFile().createNewFile();
            debug = Files.newWriter(Paths.get("runs", "tmp2.csv").toFile(), Charset.defaultCharset());
            debug.write("price,quantity");
            debug.newLine();
        }
        catch (IOException e){}
    }

    private boolean active = true;

    @Override
    public void step(SimState state) {
        if(!active)
            return;


        assert state instanceof MacroII;
        if(toFollow.hasTradedAtLeastOnce() && toFollow.getData().numberOfObservations() > 0) {
            //gather the quantity sold
            double quantity = toFollow.getData().getLatestObservation(xVariable);
            //gather price
            double price = toFollow.getData().getLatestObservation(SalesDataType.LAST_ASKED_PRICE);

            //check weight
            double gap = toFollow.getData().getLatestObservation(SalesDataType.SUPPLY_GAP);
            //regress
            if(price<0){}
            else if (gap != 0)
                regression.skipObservation(price, quantity);
            else {
                regression.addObservation(price, quantity);
                try {
                    debug.write(price + "," + quantity);
                    debug.newLine();
                    debug.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        //restep
        ((MacroII) state).scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this);

    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep) {

        if(regression.hasEnoughObservations()) {
            final float predictedPrice = (float) DynamicProcess.simulateManyStepsWithFixedInput(regression.generateDynamicProcessImpliedByRegression(),
                    stepsIntoTheFutureToSimulate, toFollow.getData().getLatestObservation(xVariable) + 1);
            System.out.println(regression);
            System.out.println("predicted slope: " + (predictedPrice - toFollow.getData().getLatestObservation(SalesDataType.LAST_ASKED_PRICE)) );
            return Math.max(predictedPrice,0);
        }
        else return (float) toFollow.getAveragedPrice();
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep           by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {
        if(regression.hasEnoughObservations()) {
            final float predictedPrice = (float) DynamicProcess.simulateManyStepsWithFixedInput(regression.generateDynamicProcessImpliedByRegression(),
                    stepsIntoTheFutureToSimulate, toFollow.getData().getLatestObservation(xVariable) - 1);
            System.out.println("predicted slope: " + (toFollow.getData().getLatestObservation(SalesDataType.LAST_ASKED_PRICE)-predictedPrice ) );
            return Math.max(predictedPrice,0);
        }
        else return (float) toFollow.getAveragedPrice();
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        if(regression.hasEnoughObservations()) {
            final float predictedPrice = (float) DynamicProcess.simulateManyStepsWithFixedInput(regression.generateDynamicProcessImpliedByRegression(),
                    stepsIntoTheFutureToSimulate, toFollow.getData().getLatestObservation(xVariable));
            return Math.max(predictedPrice,0);
        }
        else return (float) toFollow.getAveragedPrice();    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {

    }
}
