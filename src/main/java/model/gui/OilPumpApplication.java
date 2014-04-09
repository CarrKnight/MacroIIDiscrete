/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import goods.GoodType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.MacroII;
import model.scenario.OilDistributorScenario;

/**
 * A standalone test of javafx view of a geographical mdel
 * Created by carrknight on 4/8/14.
 */
public class OilPumpApplication extends Application
{


    @Override
    public void start(Stage stage) throws Exception {

        //create the model and start it
        MacroII model = new MacroII(System.currentTimeMillis());
        OilDistributorScenario scenario = new OilDistributorScenario(model);
        model.setScenario(scenario);
        model.start();



        //////////////////////////////////
        GeographicalMarketView view = new GeographicalMarketView((financial.market.GeographicalMarket) model.getMarket(GoodType.OIL),model);
        stage.setScene(new Scene(view));
        stage.setMaximized(true);
        stage.show();

        ///////////////////////////////////////////
        Thread thread = new Thread(() -> {
            while(model.schedule.getTime()<5000) {
                model.schedule.step(model);
                System.out.println(model.getMarket(GoodType.OIL).getTodayVolume());
            }
        });
        thread.start();

    }

    public static void main(String[] args)
    {
        Application.launch(OilPumpApplication.class,args);
    }
}
