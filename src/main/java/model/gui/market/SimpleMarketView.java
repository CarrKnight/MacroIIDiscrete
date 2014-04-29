/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import financial.market.Market;
import javafx.scene.CacheHint;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.MacroII;
import model.utilities.Deactivatable;

/**
 * The JavaFX view of the market. Works through the MarketPresentation
 * Created by carrknight on 4/2/14.
 */
public class SimpleMarketView extends TabPane implements Deactivatable
{

    private final LineChart<Number,Number> priceChart;

    private final LineChart<Number,Number> volumeChart;

    /**
     * where the data is stored
     */
    private final MarketPresentation presentation;


    public SimpleMarketView(Market market, MacroII model) {
        //create data collector!
        presentation = new MarketPresentation();
        //two tabs, price and volume
        Tab priceTab = new Tab("Price Tab");
        NumberAxis xAxis= new NumberAxis();
        xAxis.setAutoRanging(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);

        xAxis.setLabel("Day");
        yAxis.setLabel("Price");
        priceChart = new LineChart<>(xAxis,yAxis);
        priceChart.setCreateSymbols(false);
        priceChart.setCache(true);
        priceChart.setCacheHint(CacheHint.SPEED);
        priceChart.getData().add(presentation.getClosingPriceSeries());
        priceTab.setContent(priceChart);
        this.getTabs().add(priceTab);

        Tab quantityTab = new Tab("Quantity Tab");
        xAxis= new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Volume");
        volumeChart = new LineChart<>(xAxis,yAxis);
        volumeChart.setCreateSymbols(false);
        volumeChart.setCache(true);
        volumeChart.setCacheHint(CacheHint.SPEED);
        volumeChart.getData().add(presentation.getVolumeConsumed());
        volumeChart.getData().add(presentation.getVolumeProduced());
        volumeChart.getData().add(presentation.getVolumeTraded());
        quantityTab.setContent(volumeChart);
        this.getTabs().add(quantityTab);

        //start the presentation
        presentation.start(model,market.getData());

    }


    @Override
    public void turnOff() {
        presentation.turnOff();
    }
}
