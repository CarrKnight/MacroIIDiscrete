/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import financial.market.GeographicalMarket;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;



/**
 * An extension of Market View holding, adding a "map" tab
 * Created by carrknight on 4/8/14.
 */
public class GeographicalMarketView extends TabPane implements Deactivatable {

    private final GeographicalMarketPresentation presentation;
    private final SellingFirmToColorMap colors;

    public GeographicalMarketView(GeographicalMarket market, MacroII model) {
        //create the map
        Tab mapTab = new Tab("Geography");
        colors = new SellingFirmToColorMap(market,model.getRandom());
        presentation = new GeographicalMarketPresentation(colors,market,model);
        //create a scrollview to hold on to the map
        ScrollPane pane = new ScrollPane(presentation.getGeographicalMap());
        StackPane mapContainer = new StackPane(pane);
        mapTab.setContent(mapContainer);
        this.getTabs().add(mapTab);
        this.getSelectionModel().select(mapTab);
        this.setHeight(800);
        this.setWidth(1024);

        pane.setPannable(true);

        pane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                System.out.println("scroll-event!");
                int zoom = presentation.getOneUnitInModelEqualsHowManyPixels();
                if (scrollEvent.getDeltaY() > 0)
                    presentation.setOneUnitInModelEqualsHowManyPixels((int) Math.max(10,zoom+scrollEvent.getDeltaY()/5));
                if (scrollEvent.getDeltaY() < 0)
                    presentation.setOneUnitInModelEqualsHowManyPixels((int) Math.max(10,(zoom+scrollEvent.getDeltaY()/5)));

            }
        });

        //add a canvas on top of the scrollpane telling you what step is it
        Text date = new Text("time: " + 0);
        date.setFont(Font.font("Verdana", 20));
        date.setMouseTransparent(true);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {
                int time = (int) ((MacroII) simState).getMainScheduleTime();
                Platform.runLater(() -> date.setText("date: " + time));
                model.scheduleTomorrow(ActionOrder.GUI_PHASE, this);
            }
        });
        mapContainer.getChildren().add(date);
        StackPane.setAlignment(date, Pos.TOP_LEFT);

        date.toFront();


        //create price-tab
        Tab prices = new Tab("Prices");
        prices.setContent(new FirmChartPresentation(colors,market.getGoodType(), SalesDataType.LAST_ASKED_PRICE));
        this.getTabs().add(prices);

    }

    @Override
    public void turnOff() {
        presentation.turnOff();
        colors.turnOff();

    }
}
