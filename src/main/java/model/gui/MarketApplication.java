/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import com.google.common.base.Preconditions;
import goods.GoodType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import model.MacroII;
import model.gui.market.MarketView;
import model.scenario.SimpleSellerScenario;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.MarketData;
import model.utilities.stats.collectors.enums.MarketDataType;
import sim.engine.SimState;
import sim.engine.Steppable;



/**
 * I'll try to migrate to JavaFX, and this is a stepping stone towards it
 * Created by carrknight on 4/1/14.
 */
public class MarketApplication extends Application implements Steppable, Deactivatable
{

    private final MacroII model;

    private boolean active = true;

    private XYChart.Series<Number,Number> priceSeries;


    public MarketApplication() {
        this.model = new MacroII(System.currentTimeMillis());

    }


    /**
     * creates a simple chart scene and schedule steppable to self update!
     * @param stage the stage containing the scene
     * @throws Exception all sorts of it
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        //////////////////
        SimpleSellerScenario scenario = new SimpleSellerScenario(model);
        scenario.setSellerStrategy(SimpleFlowSellerPID.class);
        scenario.setDemandShifts(false);
        scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        scenario.setDemandSlope(-10);
        model.setScenario(scenario);



        //////////////////////////////////
        model.start();
        MarketView marketView = new MarketView(model.getMarket(GoodType.GENERIC),model);

        //make a scene
        stage.setScene(new Scene(marketView));
        stage.show();


        ///////////////////////////////////////////
        Thread thread = new Thread(() -> {
            while(model.schedule.getTime()<5000) {
                model.schedule.step(model);
                System.out.println(model.getMarket(GoodType.GENERIC).getTodayVolume());
            }
        });
        thread.start();



    }

    @Override
    public void step(SimState simState) {

        if(!active)
            return;
        Preconditions.checkState(!Platform.isFxApplicationThread());

        MarketData marketData = model.getMarket(GoodType.GENERIC).getData();
        final Double newPrice = marketData.getLatestObservation(MarketDataType.CLOSING_PRICE);
        final Integer day = new Integer(marketData.getLastObservedDay());

        // if(newPrice >=0
        Platform.runLater(() -> {
            System.out.println(newPrice + "--- " + day + ", " + priceSeries.getData().size());
            priceSeries.getData().add(new XYChart.Data<>(day,
                    newPrice));


        });
        model.scheduleTomorrow(ActionOrder.GUI_PHASE, this);

        return;
    }

    @Override
    public void turnOff() {
        active =false;
        Platform.exit();
    }

    public static void main(String[] args)
    {
        Application.launch(MarketApplication.class,args);








    }

}
