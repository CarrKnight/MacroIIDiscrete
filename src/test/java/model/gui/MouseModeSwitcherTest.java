/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import financial.market.GeographicalMarket;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import model.gui.market.GeographicalMarketPresentation;
import model.scenario.ControllableGeographicalScenario;
import model.utilities.geography.Location;
import org.junit.Assert;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import static org.mockito.Mockito.*;

/**
 * First javafx test, hope it is worth it
 * Created by carrknight on 4/27/14.
 */
public class MouseModeSwitcherTest extends GuiTest{


    AddAgentsToMapTitledPane underTest;

    private GeographicalMarketPresentation geographicalMarketPresentation;

    private Pane geographicalPane;

    private ControllableGeographicalScenario scenario;

    @Override
    protected Parent getRootNode() {
        geographicalMarketPresentation = mock(GeographicalMarketPresentation.class);
        geographicalPane = new Pane();
        when(geographicalMarketPresentation.getGeographicalMap()).thenReturn(geographicalPane);
        scenario = mock(ControllableGeographicalScenario.class);
        //todo try to put this in @Before
        //border pane with titled pane in the bottom
        BorderPane root = new BorderPane();
        root.setCenter(geographicalPane);
        underTest = new AddAgentsToMapTitledPane(geographicalMarketPresentation,scenario);
        root.setBottom(underTest);

        geographicalPane.setMinSize(100,100);
        geographicalPane.setMaxSize(100, 100);

        return root;


    }

    @Test
    public void testByTouchingButonsDirectly() throws Exception {

        click(underTest.getAddOilPump());
        Assert.assertEquals(Cursor.CROSSHAIR,geographicalPane.getCursor()); //should have switched to crosshair
        //if I click on the map, I should be able to run the scenario command for adding firms!
        click(geographicalPane.localToScene(90, 10));
        verify(scenario,times(1)).createNewProducer(any(Location.class), any(GeographicalMarket.class), anyString());
        verify(scenario,never()).createNewConsumer(any(Location.class), any(GeographicalMarket.class), anyLong());

        //click on selection
        click(underTest.getNormalSelection());
        Assert.assertEquals(Cursor.DEFAULT,geographicalPane.getCursor()); //back to normal
        //clicks do nothing
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,never()).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong());

        //click on add consumer
        click(underTest.getAddHousehold());
        Assert.assertEquals(Cursor.CROSSHAIR,geographicalPane.getCursor()); //back to crosshair
        //clicks to add consumer
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,times(1)).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong()); //this happened

        //once more back to selection
        click(underTest.getNormalSelection());
        Assert.assertEquals(Cursor.DEFAULT,geographicalPane.getCursor()); //back to normal
        //clicks do nothing
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,times(1)).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong()); //this happened

    }
    @Test
    public void testByUsingNames() throws Exception {
        click(AddAgentsToMapTitledPane.ADD_FIRM_BUTTON_STRING);
        Assert.assertEquals(Cursor.CROSSHAIR,geographicalPane.getCursor()); //should have switched to crosshair
        //if I click on the map, I should be able to run the scenario command for adding firms!
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,never()).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong());

        //click on selection
        click(AddAgentsToMapTitledPane.SELECT_BUTTON_STRING);
        Assert.assertEquals(Cursor.DEFAULT,geographicalPane.getCursor()); //back to normal
        //clicks do nothing
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,never()).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong());

        //click on add consumer
        click(AddAgentsToMapTitledPane.ADD_HOUSEHOLD_BUTTON_STRING);
        Assert.assertEquals(Cursor.CROSSHAIR,geographicalPane.getCursor()); //back to crosshair
        //clicks to add consumer
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,times(1)).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong()); //this happened

        //once more back to selection
        click(AddAgentsToMapTitledPane.SELECT_BUTTON_STRING);
        Assert.assertEquals(Cursor.DEFAULT,geographicalPane.getCursor()); //back to normal
        //clicks do nothing
        click(geographicalPane.localToScene(90,10));
        verify(scenario,times(1)).createNewProducer(any(Location.class),any(GeographicalMarket.class),anyString());
        verify(scenario,times(1)).createNewConsumer(any(Location.class),any(GeographicalMarket.class),anyLong()); //this happened
    }
}
