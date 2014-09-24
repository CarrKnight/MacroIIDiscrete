/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.MarketData;
import model.utilities.stats.collectors.enums.MarketDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Basically a presentation object with all the data for the view
 * Created by carrknight on 4/1/14.
 */
public class MarketPresentation implements Steppable, Deactivatable {



    private boolean active = true;

    public static boolean addTooltip = false;


    private final XYChart.Series<Number,Number> closingPriceSeries;

    private final XYChart.Series<Number,Number> volumeTraded;

    private final XYChart.Series<Number,Number> volumeProduced;

    private final XYChart.Series<Number,Number> volumeConsumed;


    private MarketData data;

    /**
     * creates the series
     */
    public MarketPresentation() {

        closingPriceSeries = new XYChart.Series<>();
        closingPriceSeries.setName("Closing Prices");

        volumeTraded = new XYChart.Series<>();
        volumeTraded.setName("Volume Traded");

        volumeConsumed = new XYChart.Series<>();
        volumeConsumed.setName("Volume Consumed");

        volumeProduced = new XYChart.Series<>();
        volumeProduced.setName("Volume Produced");


    }


    /**
     * Schedule an updater, links to the market
     * @param model the model to schedule to
     * @param marketData the data of market to observe
     */
    public void start(MacroII model, MarketData marketData)
    {

        this.data = marketData;

        model.scheduleSoon(ActionOrder.GUI_PHASE,this);
        model.registerDeactivable(this);


    }

    /**
     * query the MarketData to update the series
     * @param simState model
     */
    @Override
    public void step(SimState simState)
    {
        if(!active)
            return;

        //nope
        Preconditions.checkState(!Platform.isFxApplicationThread());

        final Integer day = data.getLastObservedDay();
        XYChart.Data<Number, Number> priceDatum = new XYChart.Data<>(day,
                data.getLatestObservation(MarketDataType.CLOSING_PRICE));
        XYChart.Data<Number, Number> tradedDatum = new XYChart.Data<>(day,
                data.getLatestObservation(MarketDataType.VOLUME_TRADED));
        XYChart.Data<Number, Number> producedDatum = new XYChart.Data<>(day,
                data.getLatestObservation(MarketDataType.VOLUME_PRODUCED));
        XYChart.Data<Number, Number> consumedDatum = new XYChart.Data<>(day,
                data.getLatestObservation(MarketDataType.VOLUME_CONSUMED));

        Platform.runLater(() -> {

            if(addTooltip)
            {
                final Tooltip tooltip= new Tooltip("Day: "+ priceDatum.getXValue() +"Price: "
                    + priceDatum.getYValue().toString() +", traded: " +tradedDatum.getYValue().toString());

                Tooltip.install(priceDatum.getNode(),tooltip);
                Tooltip.install(tradedDatum.getNode(),tooltip);
                Tooltip.install(producedDatum.getNode(),tooltip);
                Tooltip.install(consumedDatum.getNode(),tooltip);

            }
            closingPriceSeries.getData().add(priceDatum);
            volumeTraded.getData().add(tradedDatum);
            volumeProduced.getData().add(producedDatum);
            volumeConsumed.getData().add(consumedDatum);

        });

        ((MacroII)simState).scheduleTomorrow(ActionOrder.GUI_PHASE,this);

    }



    @Override
    public void turnOff() {
        active = false;
    }


    public XYChart.Series<Number, Number> getClosingPriceSeries() {
        return closingPriceSeries;
    }

    public XYChart.Series<Number, Number> getVolumeTraded() {
        return volumeTraded;
    }

    public XYChart.Series<Number, Number> getVolumeProduced() {
        return volumeProduced;
    }

    public XYChart.Series<Number, Number> getVolumeConsumed() {
        return volumeConsumed;
    }
}
