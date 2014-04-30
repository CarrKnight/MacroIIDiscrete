/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import model.gui.market.GeographicalMarketPresentation;
import model.scenario.ControllableGeographicalScenario;
import model.utilities.Deactivatable;
import model.utilities.geography.Location;

/**
 * a listener to use to actually give meaning to the mouse-mode
 */
public class MouseModeSwitcher implements Deactivatable, EventHandler<MouseEvent>, ChangeListener<MouseMode>
{

//the references are kept mostly because we need to remove us listening later

    private int id = 0;

    private final GeographicalMarketPresentation geographicalMarketPresentation;
    private final ObservableValue<MouseMode> mouseMode;

    private final ControllableGeographicalScenario scenario;

    public MouseModeSwitcher(ObservableValue<MouseMode> mouseMode,
                             GeographicalMarketPresentation geographicalMarketPresentation,
                             ControllableGeographicalScenario scenario)
    {
        //get a reference to the map and start listening for clicks
        this.geographicalMarketPresentation = geographicalMarketPresentation;
        this.geographicalMarketPresentation.getGeographicalMap().addEventFilter(MouseEvent.MOUSE_PRESSED,this);
        this.scenario = scenario;

        this.mouseMode = mouseMode;
        mouseMode.addListener(this);
    }

    /**
     * this is called whenever the mouse mode has changed for whatever reason; we just use it to switch the cursor over the map
     */
    @Override
    public void changed(ObservableValue<? extends MouseMode> observableValue, MouseMode oldMouseMode, MouseMode newMouseMode)
    {
        assert observableValue.getValue().equals(newMouseMode);

        if(newMouseMode.equals(MouseMode.ADD_FIRM) || newMouseMode.equals(MouseMode.ADD_CONSUMER))
        {
            geographicalMarketPresentation.getGeographicalMap().setCursor(Cursor.CROSSHAIR);
        }
        else
        {
            geographicalMarketPresentation.getGeographicalMap().setCursor(Cursor.DEFAULT);
        }


    }


    //stop listening
    @Override
    public void turnOff() {
        geographicalMarketPresentation.getGeographicalMap().removeEventFilter(MouseEvent.MOUSE_PRESSED,this);

    }

    @Override
    public void handle(MouseEvent mouseEvent)
    {
        //selection simply means to ignore
        MouseMode currentMode = mouseMode.getValue();
        if(currentMode.equals(MouseMode.SELECTION))
            return;
        else
        {
            //all the adds consume the event(don't allow nodes below to see this)
            mouseEvent.consume();
            //convert to model x-y
            double x = geographicalMarketPresentation.convertXPixelCoordinateToXModelCoordinate(mouseEvent.getX());
            double y = geographicalMarketPresentation.convertYPixelCoordinateToYModelCoordinate(mouseEvent.getY());
            if(currentMode.equals(MouseMode.ADD_FIRM))
                scenario.createNewProducer(new Location(x,y),geographicalMarketPresentation.getMarket(),"UserCreated"+id);
            else
            {
                assert currentMode.equals(MouseMode.ADD_CONSUMER);
                scenario.createNewConsumer(new Location(x,y),geographicalMarketPresentation.getMarket(),50);
            }



        }

    }
}
