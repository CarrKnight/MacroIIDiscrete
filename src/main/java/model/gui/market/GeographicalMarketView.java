/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import financial.market.GeographicalMarket;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.MacroII;
import model.gui.utilities.InformationTab;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * An extension of Market View holding, adding a "map" tab
 * Created by carrknight on 4/8/14.
 */
public class GeographicalMarketView extends GroupOfInformationTabs implements Deactivatable {

    /**
     * the presentation holds most of the logic, keeping view paper-thin
     */
    private final GeographicalMarketPresentation presentation;
    /**
     * I figured this map would be independent.
     */
    private final SellingFirmToColorMap colors;
    /**
     * the tab holding the geographical map!
     */
    private final InformationTab mapTab;
    /**
     * the tab holding on to the price
     */
    private final InformationTab priceTab;

    private final ArrayList<InformationTab> tabsCreated;
    private final BooleanBinding turnedOff;


    @Override
    public Collection<InformationTab> getTabGroup() {
       return tabsCreated;

    }

    public GeographicalMarketView(GeographicalMarket market, MacroII model) {
        tabsCreated = new ArrayList<>(2);


        //    __  __
        //   |  \/  |__ _ _ __
        //   | |\/| / _` | '_ \
        //   |_|  |_\__,_| .__/
        //               |_|
        Tab mapView = new Tab("Geography");
        colors = new SellingFirmToColorMap(market,model.getRandom());
        presentation = new GeographicalMarketPresentation(colors,market,model);
        //create a scrollview to hold on to the map
        ScrollPane pane = new ScrollPane(presentation.getGeographicalMap());
        StackPane mapContainer = new StackPane(pane);
        mapView.setContent(mapContainer);
        //make it zoomable
        pane.setPannable(true);
        pane.setOnScroll(scrollEvent -> {
            System.out.println("scroll-event!");
            int zoom = presentation.getOneUnitInModelEqualsHowManyPixels();
            if (scrollEvent.getDeltaY() > 0)
                presentation.setOneUnitInModelEqualsHowManyPixels((int) Math.max(10,zoom+scrollEvent.getDeltaY()/5));
            if (scrollEvent.getDeltaY() < 0)
                presentation.setOneUnitInModelEqualsHowManyPixels((int) Math.max(10,(zoom+scrollEvent.getDeltaY()/5)));

        });
        //add no special control
        List<TitledPane> controllers = new LinkedList<>();
        mapTab = new InformationTab(controllers,mapView);
        //add date to the scrollpane, just for kicks
        Text date = new Text("time: " + 0);
        date.setFont(Font.font("Verdana", 20));
        date.setMouseTransparent(true);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {
                if (!mapTab.isActive().get())
                    return;
                int time = (int) ((MacroII) simState).getMainScheduleTime();
                Platform.runLater(() -> date.setText("date: " + time));
                model.scheduleTomorrow(ActionOrder.GUI_PHASE, this);
            }
        });
        mapContainer.getChildren().add(date);
        StackPane.setAlignment(date, Pos.TOP_LEFT);
        date.toFront();
        //we are going to let the map listen to the presentation.
        presentation.setGUINodeParent(mapTab);

        //finally add it
        tabsCreated.add(mapTab);


        //    ___     _
        //   | _ \_ _(_)__ ___ ___
        //   |  _/ '_| / _/ -_|_-<
        //   |_| |_| |_\__\___/__/
        //
        Tab prices = new Tab("Prices");
        prices.setContent(new FirmChartPresentation(colors,market.getGoodType(), SalesDataType.LAST_ASKED_PRICE));
        priceTab = new InformationTab(new LinkedList<>(),prices);
        tabsCreated.add(priceTab);


        //if both tabs get closed, then turn off.
        turnedOff = BooleanBinding.booleanExpression(mapTab.isActive()).or(priceTab.isActive()).not();
        assert !turnedOff.get(); //we just created it! it can't be already off
        turnedOff.addListener((observableValue, old, newValue) -> {
            if (newValue)
                turnOff();
        });


    }

    @Override
    public void turnOff() {
        assert turnedOff.get();
        presentation.turnOff();
        colors.turnOff();

    }

    protected GeographicalMarketPresentation getPresentation() {
        return presentation;
    }

    protected InformationTab getMapTab() {
        return mapTab;
    }

    public SellingFirmToColorMap getColors() {
        return colors;
    }


    public boolean turnedOff()
    {
       return turnedOff.get();
    }
}
