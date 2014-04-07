/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableDoubleValue;
import model.utilities.geography.HasLocation;
import model.utilities.geography.HasLocationPortrait;


import java.util.HashMap;
import java.util.Map;

/**
 *
 * Collecting data "map" to show who's selling to whom on top of the usual market presentation.
 * Created by carrknight on 4/5/14.
 */
public class GeographicalMarketPresentation {


    /**
     * The map of geographical firms to their color
     */
    private final SellingFirmToColorMap colorMap;


    /**
     * normal zoom
     */
    private final static int defaultOneUnitInModelEqualsHowManyPixels = 100;

    /**
     * the zoom/conversion between model space and pixels!
     */
    private IntegerProperty oneUnitInModelEqualsHowManyPixels = new SimpleIntegerProperty(defaultOneUnitInModelEqualsHowManyPixels);

    /**
     * the default size of each agent portrait when.
     */
    private final static int defaultPortraitSizeInPixels = 15;

    //todo make this have some sense!
    private IntegerProperty pixelOffsetToSimulateNegativeXCoordinate = new SimpleIntegerProperty(0);

    private IntegerProperty pixelOffsetToSimulateNegativeYCoordinate = new SimpleIntegerProperty(0);



    /**
     * A map connecting all agents with a location to their portrait.
     */
    private final Map<HasLocation,HasLocationPortrait> portraitList; //could switch HasLocationPortrait to ImageView if needed


    public GeographicalMarketPresentation(SellingFirmToColorMap colorMap) {
        portraitList = new HashMap<>();
        this.colorMap = colorMap;

    }

    private IntegerBinding getPixelXCoordinateWhenYouSupplyThisMethodXModelCoordinate(ObservableDoubleValue xCoordinate)
    {

        return new IntegerBinding() {
            {
                this.bind(xCoordinate,oneUnitInModelEqualsHowManyPixels,pixelOffsetToSimulateNegativeXCoordinate);
            }
            @Override
            protected int computeValue() {
                int newValue = (int) Math.round(xCoordinate.get() * oneUnitInModelEqualsHowManyPixels.get() +
                        pixelOffsetToSimulateNegativeXCoordinate.get());
                assert newValue >= 0; //new value must always be positive!
                return newValue;
            }
        };
    }


    private IntegerBinding getPixelYCoordinateWhenYouSupplyThisMethodYModelCoordinate(ObservableDoubleValue yCoordinate)
    {

        return new IntegerBinding() {
            {
                this.bind(yCoordinate,oneUnitInModelEqualsHowManyPixels,pixelOffsetToSimulateNegativeYCoordinate);
            }
            @Override
            protected int computeValue() {
                int newValue = (int) Math.round(yCoordinate.get() * oneUnitInModelEqualsHowManyPixels.get() +
                        pixelOffsetToSimulateNegativeYCoordinate.get());
                assert newValue >= 0; //new value must always be positive!
                return newValue;
            }
        };
    }










}
