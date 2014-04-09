/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import financial.market.GeographicalMarket;
import financial.market.Market;
import javafx.application.Platform;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.scenario.oil.GeographicalCustomer;
import model.utilities.Deactivatable;
import model.utilities.geography.GeographicalCustomerPortrait;
import model.utilities.geography.GeographicalFirmPortrait;
import model.utilities.geography.HasLocation;
import model.utilities.geography.HasLocationPortrait;


import java.util.*;

/**
 *
 * Collecting data "map" to show who's selling to whom on top of the usual market presentation.
 * Created by carrknight on 4/5/14.
 */
public class GeographicalMarketPresentation implements Deactivatable{


    /**
     * The map of geographical firms to their color
     */
    private final SellingFirmToColorMap colorMap;


    /**
     * normal zoom
     */
    private final static int defaultOneUnitInModelEqualsHowManyPixels = 100;

    private final SetChangeListener<EconomicAgent> buyerSetListener = change -> {
        if (change.wasAdded()) {
            EconomicAgent agent = change.getElementAdded();
            if (agent instanceof GeographicalCustomer)
                addBuyerToMap((GeographicalCustomer) agent);
        }
        if (change.wasRemoved()) {
            EconomicAgent agent = change.getElementAdded();
            if (agent instanceof GeographicalCustomer)
                removeAgentFromMap((GeographicalCustomer) agent);
        }
    };

    private final MapChangeListener<GeographicalFirm, Color> sellerListener = change -> {
        if (change.wasAdded()) {
            GeographicalFirm seller = change.getKey();
            addSellerToMap(seller, change.getValueAdded());
        }
        if (change.wasRemoved()) {
            GeographicalFirm seller = change.getKey();
            removeAgentFromMap(seller);
        }

    };

    /**
     * the zoom/conversion between model space and pixels!
     */
    private IntegerProperty oneUnitInModelEqualsHowManyPixels = new SimpleIntegerProperty(defaultOneUnitInModelEqualsHowManyPixels);

    /**
     * the default size of each agent portrait when.
     */
    private final static int defaultPortraitSizeInPixels = 30;


    private final IntegerProperty portraitSize = new SimpleIntegerProperty(defaultPortraitSizeInPixels);

    /**
     * the lowest y in the model. Notice that the model coordinates are double, so here I'll need to feed rounded up
     * values.
     * Notice that this will always be negative or 0 (because it starts at 0)
     */
    private final IntegerProperty minimumModelY = new SimpleIntegerProperty(0);

    /**
     * the lowest x in the model. Notice that the model coordinates are double, so here I'll need to feed rounded up
     * values
     * Notice that this will always be negative or 0 (because it starts at 0)
     */
    private final IntegerProperty minimumModelX = new SimpleIntegerProperty(0);

    /**
     * a reference to the market is needed to turn off properly
     */
    private final Market market;


    /**
     * the map where all the agents are
     */
    private final Pane canvasMap;



    //todo make this have some sense!
    private NumberBinding pixelOffsetToSimulateNegativeXCoordinate =  minimumModelX.negate().multiply(oneUnitInModelEqualsHowManyPixels);

    private NumberBinding pixelOffsetToSimulateNegativeYCoordinate = minimumModelY.negate().multiply(oneUnitInModelEqualsHowManyPixels);



    /**
     * A map connecting all agents with a location to their portrait.
     */
    private final Map<HasLocation,HasLocationPortrait> portraitList; //could switch HasLocationPortrait to ImageView if needed


    public GeographicalMarketPresentation(SellingFirmToColorMap colorMap, GeographicalMarket market) {
        this.market = market;
        portraitList = new HashMap<>();
        canvasMap = new Pane();
        canvasMap.setBackground(new Background(new BackgroundFill(Color.GRAY,null,null)));
        this.colorMap = colorMap;


        //initialize buyer list
        for(EconomicAgent agent : market.getBuyers())
        {
            if(agent instanceof GeographicalCustomer)
                addBuyerToMap((GeographicalCustomer) agent);
        }
        //now start listening to the buyer list
        market.addListenerToBuyerSet(buyerSetListener);

        for(Map.Entry<GeographicalFirm,Color> entry : colorMap.getColorMap().entrySet())
        {
                 addSellerToMap(entry.getKey(),entry.getValue());
        }
        //initialize the seller list!
        colorMap.getColorMap().addListener(sellerListener);



    }

    private IntegerBinding convertXModelCoordinateToXPixelCoordinate(ObservableDoubleValue xModel)
    {

        return new IntegerBinding() {
            {
                this.bind(xModel,oneUnitInModelEqualsHowManyPixels,pixelOffsetToSimulateNegativeXCoordinate);
            }
            @Override
            protected int computeValue() {
                int newValue = (int) Math.round(xModel.get() * oneUnitInModelEqualsHowManyPixels.get() +
                        pixelOffsetToSimulateNegativeXCoordinate.doubleValue());
                assert newValue >= 0; //new value must always be positive!
                return newValue;
            }
        };
    }


    private IntegerBinding convertYModelCoordinateToYPixelCoordinate(ObservableDoubleValue yModel)
    {

        return new IntegerBinding() {
            {
                this.bind(yModel,oneUnitInModelEqualsHowManyPixels,pixelOffsetToSimulateNegativeYCoordinate);
            }
            @Override
            protected int computeValue() {
                int newValue = (int) Math.round(yModel.get() * oneUnitInModelEqualsHowManyPixels.get() +
                        pixelOffsetToSimulateNegativeYCoordinate.doubleValue());
                assert newValue >= 0; //new value must always be positive!
                return newValue;
            }
        };
    }


    /**
     * adds seller to map and list
     */
    private void addSellerToMap(GeographicalFirm seller, Color color)
    {

        //create proper portrait
        GeographicalFirmPortrait portrait = new GeographicalFirmPortrait(seller,color);
        addAgentToMap(seller, portrait);


    }

    private void addBuyerToMap(final GeographicalCustomer buyer)
    {
        GeographicalCustomerPortrait portrait = new GeographicalCustomerPortrait(buyer);
        buyer.lastSupplierProperty().addListener(new ChangeListener<GeographicalFirm>() {
            @Override
            public void changed(ObservableValue<? extends GeographicalFirm> observableValue, GeographicalFirm geographicalFirm, GeographicalFirm geographicalFirm2) {
                Color color;
                if(buyer.lastSupplierProperty().get()==null)
                    color= Color.WHITE;
                else
                    color = colorMap.getFirmColor(buyer.lastSupplierProperty().get());
                Platform.runLater(() -> {
                    portrait.colorProperty().setValue(color);
                });
            }
        });

        addAgentToMap(buyer, portrait);


    }

    private void addAgentToMap(HasLocation seller, HasLocationPortrait portrait) {
        //see if there is a need to change the minimums
        if(seller.getxLocation() < minimumModelX.get())
            minimumModelX.setValue(Math.floor(seller.getxLocation()-0.5d));
        if(seller.getyLocation() < minimumModelY.get())
            minimumModelY.setValue(Math.floor(seller.getyLocation()-0.5d));


        //set Coordinates
        portrait.layoutXProperty().bind(convertXModelCoordinateToXPixelCoordinate(seller.xLocationProperty()));
        portrait.layoutYProperty().bind(convertYModelCoordinateToYPixelCoordinate(seller.yLocationProperty()));
        //resize image
        portrait.fitHeightProperty().bind(portraitSize);
        portrait.fitWidthProperty().bind(portraitSize);

        //add it to the big list
        portraitList.put(seller,portrait);
        //add it to canvas!
        canvasMap.getChildren().add(portrait);
    }

    /**
     * i assume the seller has already been removed from the colorMap (in fact, that removal is what triggers this)
     */
    private void removeAgentFromMap(HasLocation seller)
    {
        //grab the portrait from the map
        HasLocationPortrait portrait = portraitList.remove(seller);
        //make sure it's not null
        Preconditions.checkNotNull(portrait);
        //make sure it's the right portrait
        assert portrait.getAgent().equals(seller);

        //remove it from the canvasMap
        canvasMap.getChildren().remove(portrait);
        portrait.layoutXProperty().unbind();
        portrait.layoutYProperty().unbind();
        portrait.fitHeightProperty().unbind();
        portrait.fitWidthProperty().unbind();

        assert !portraitList.containsKey(seller);
        assert !canvasMap.getChildren().contains(portrait);

    }



    public int getOneUnitInModelEqualsHowManyPixels() {
        return oneUnitInModelEqualsHowManyPixels.get();
    }

    public IntegerProperty oneUnitInModelEqualsHowManyPixelsProperty() {
        return oneUnitInModelEqualsHowManyPixels;
    }

    public void setOneUnitInModelEqualsHowManyPixels(int oneUnitInModelEqualsHowManyPixels) {
        this.oneUnitInModelEqualsHowManyPixels.set(oneUnitInModelEqualsHowManyPixels);
    }

    @Override
    public void turnOff() {

        market.removeListenerFromBuyerSet(buyerSetListener);
        colorMap.removeListener(sellerListener);

        List<HasLocation> agents = new LinkedList<>(portraitList.keySet());
        for(HasLocation agent : agents)
            removeAgentFromMap(agent);
        portraitList.clear();
        //they should all be gone!
        assert canvasMap.getChildren().isEmpty();


    }

    /**
     * a view of the map agent---> portrait
     */
    public Map<HasLocation, HasLocationPortrait> getPortraitList() {
        return Collections.unmodifiableMap(portraitList);
    }

    public Pane getCanvasMap() {
        return canvasMap;
    }


    public int getMinimumModelY() {
        return minimumModelY.get();
    }

    public IntegerProperty minimumModelYProperty() {
        return minimumModelY;
    }

    public int getMinimumModelX() {
        return minimumModelX.get();
    }

    public IntegerProperty minimumModelXProperty() {
        return minimumModelX;
    }
}
