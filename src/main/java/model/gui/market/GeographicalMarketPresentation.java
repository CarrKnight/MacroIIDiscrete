/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import financial.market.GeographicalMarket;
import goods.GoodType;
import javafx.application.Platform;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.MacroII;
import model.gui.utilities.GUINode;
import model.gui.utilities.GUINodeSimple;
import model.gui.utilities.SelectionEvent;
import model.gui.utilities.TabEvent;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.geography.HasLocation;
import model.utilities.geography.GeographicalCustomerPortrait;
import model.utilities.geography.GeographicalFirmPortrait;
import model.utilities.geography.HasLocationPortrait;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 *
 * Collecting data "map" to show who's selling to whom on top of the usual market presentation.
 * The step updates the color of all buyers.
 * Created by carrknight on 4/5/14.
 */
public class GeographicalMarketPresentation implements Steppable, Deactivatable, GUINode{


    /**
     * The map of geographical firms to their color
     */
    private final SellingFirmToColorMap colorMap;

    private boolean isActive = true;

    /**
     * normal zoom
     */
    private final static int defaultOneUnitInModelEqualsHowManyPixels = 50;



    /**
     * the zoom/conversion between model space and pixels!
     */
    private IntegerProperty oneUnitInModelEqualsHowManyPixels = new SimpleIntegerProperty(defaultOneUnitInModelEqualsHowManyPixels);

    /**
     * the default size of each agent portrait when.
     */
    private final static int defaultPortraitSizeInPixels = 30;


    private final NumberBinding portraitSize = (new SimpleIntegerProperty(defaultPortraitSizeInPixels)
            .multiply(oneUnitInModelEqualsHowManyPixels).divide(defaultOneUnitInModelEqualsHowManyPixels));

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
    private final GeographicalMarket market;

    /**
     * a reference to the market is needed to turn off properly
     */
    private final MacroII model;

    /**
     * the map where all the agents are
     */
    private final Pane geographicalMap;

    /**
     * the delegate that pushes up all the gui events
     *
     */
    GUINodeSimple delegate;

    private final SetChangeListener<agents.EconomicAgent> buyerSetListener = change -> {
        if (change.wasAdded()) {
            agents.EconomicAgent agent = change.getElementAdded();
            if (agent instanceof GeographicalCustomer)
                addBuyerToMap((GeographicalCustomer) agent);
        }
        if (change.wasRemoved()) {
            agents.EconomicAgent agent = change.getElementAdded();
            if (agent instanceof GeographicalCustomer) {
                removeBuyerFromMap((GeographicalCustomer) agent);
            }
        }
    };

    private final MapChangeListener<GeographicalFirm, Color> sellerListener;


    //todo make this have some sense!
    private NumberBinding pixelOffsetToSimulateNegativeXCoordinate =  minimumModelX.negate().multiply(oneUnitInModelEqualsHowManyPixels);

    private NumberBinding pixelOffsetToSimulateNegativeYCoordinate = minimumModelY.negate().multiply(oneUnitInModelEqualsHowManyPixels);



    /**
     * A map connecting all agents with a location to their portrait.
     */
    private final Map<HasLocation,HasLocationPortrait> portraitList; //could switch HasLocationPortrait to ImageView if needed

    /**
     * A map  exclusively for customers, handy for updating fast
     */
    private final Map<GeographicalCustomer,GeographicalCustomerPortrait> buyerPortraits; //could switch HasLocationPortrait to ImageView if needed

    public GeographicalMarketPresentation(SellingFirmToColorMap colorMap, GeographicalMarket market, MacroII macroII) {
        this.market = market;
        this.model = macroII;
        portraitList = new HashMap<>();
        buyerPortraits = new HashMap<>();
        geographicalMap = new Pane();
        geographicalMap.setBackground(new Background(new BackgroundFill(Color.CYAN, null, null)));
        this.colorMap = colorMap;

        delegate = new GUINodeSimple();


        //create the seller listener (it needs to be created after getting references to MacroII and so on)
        sellerListener = change -> {
            if (change.wasAdded()) {
                GeographicalFirm seller = change.getKey();
                addSellerToMap(seller, change.getValueAdded(),model,market.getGoodType());
            }
            if (change.wasRemoved()) {
                GeographicalFirm seller = change.getKey();
                removeAgentFromMap(seller);
            }

        };


        //initialize buyer list
        for(agents.EconomicAgent agent : market.getBuyers())
        {
            if(agent instanceof GeographicalCustomer)
                addBuyerToMap((GeographicalCustomer) agent);
        }
        //now start listening to the buyer list
        market.addListenerToBuyerSet(buyerSetListener);

        for(Map.Entry<GeographicalFirm,Color> entry : colorMap.getColorMap().entrySet())
        {
            addSellerToMap(entry.getKey(),entry.getValue(),macroII,market.getGoodType());
        }
        //initialize the seller list!
        colorMap.addListener(sellerListener);


        //start the steppable, please
        macroII.scheduleSoon(ActionOrder.GUI_PHASE,this);
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

    public double convertXPixelCoordinateToXModelCoordinate(double pixelX)
    {
        return  (pixelX-pixelOffsetToSimulateNegativeXCoordinate.doubleValue())/oneUnitInModelEqualsHowManyPixels.get();
    }

    public double convertYPixelCoordinateToYModelCoordinate(double pixelY)
    {
        return  (pixelY-pixelOffsetToSimulateNegativeYCoordinate.doubleValue())/oneUnitInModelEqualsHowManyPixels.get();
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
    private void addSellerToMap(GeographicalFirm seller, Color color,MacroII model, GoodType goodSold)
    {

        //create proper portrait
        GeographicalFirmPortrait portrait = new GeographicalFirmPortrait(seller,color,goodSold,model);
        addAgentToMap(seller, portrait);


    }

    private void addBuyerToMap(final GeographicalCustomer buyer)
    {
        GeographicalCustomerPortrait portrait = new GeographicalCustomerPortrait(buyer);
        addAgentToMap(buyer, portrait);
        //second copy in specialized map
        buyerPortraits.put(buyer, portrait);



    }

    private void addAgentToMap(HasLocation agent, HasLocationPortrait portrait) {
        //see if there is a need to change the minimums
        if(agent.getxLocation() < minimumModelX.get())
            minimumModelX.setValue(Math.floor(agent.getxLocation()-0.5d));
        if(agent.getyLocation() < minimumModelY.get())
            minimumModelY.setValue(Math.floor(agent.getyLocation()-0.5d));


        //set Coordinates
        portrait.layoutXProperty().bind(convertXModelCoordinateToXPixelCoordinate(agent.xLocationProperty()));
        portrait.layoutYProperty().bind(convertYModelCoordinateToYPixelCoordinate(agent.yLocationProperty()));
        //resize image
        portrait.prefHeightProperty().bind(portraitSize);
        portrait.prefWidthProperty().bind(portraitSize);

        //add it to the big list
        portraitList.put(agent, portrait);
        //add it to canvas!
        geographicalMap.getChildren().add(portrait);

        //start listening to it
        portrait.setGUINodeParent(this);
    }


    private void removeBuyerFromMap(GeographicalCustomer buyer){
        removeAgentFromMap(buyer);
        assert buyerPortraits.containsKey(buyer);
        buyerPortraits.remove(buyer);
        assert !buyerPortraits.containsKey(buyer);

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
        assert portrait.getRepresentedAgent().equals(seller);

        //remove it from the geographicalMap
        geographicalMap.getChildren().remove(portrait);
        portrait.layoutXProperty().unbind();
        portrait.layoutYProperty().unbind();
        portrait.prefHeightProperty().unbind();
        portrait.prefWidthProperty().unbind();

        //stop listening it
        portrait.setGUINodeParent(null);
        assert !portraitList.containsKey(seller);
        assert !geographicalMap.getChildren().contains(portrait);

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
        isActive = false;
        delegate.setGUINodeParent(null);

        market.removeListenerFromBuyerSet(buyerSetListener);
        colorMap.removeListener(sellerListener);

        List<HasLocation> agents = new LinkedList<>(portraitList.keySet());
        for(HasLocation agent : agents)
            removeAgentFromMap(agent);
        portraitList.clear();
        //they should all be gone!
        assert geographicalMap.getChildren().isEmpty();


    }

    /**
     * update all colors of the buyers
     */
    public void step(SimState state){
        if(!isActive)
            return;
        Platform.runLater(() -> {
            for(Map.Entry<GeographicalCustomer,GeographicalCustomerPortrait> buyer : buyerPortraits.entrySet())
            {
                Color color = buyer.getKey().getLastSupplier() == null ? Color.BLACK : colorMap.getFirmColor(buyer.getKey().getLastSupplier());
                buyer.getValue().setColor(color);
            }
        });
        ((MacroII)state).scheduleTomorrow(ActionOrder.GUI_PHASE,this);
    }

    /**
     * a view of the map agent---> portrait
     */
    public Map<HasLocation, HasLocationPortrait> getPortraitList() {
        return Collections.unmodifiableMap(portraitList);
    }

    public Pane getGeographicalMap() {
        return geographicalMap;
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

    public GeographicalMarket getMarket() {
        return market;
    }

    @Override
    public void handleNewTab(TabEvent event) {
        delegate.handleNewTab(event);
    }

    @Override
    public void setGUINodeParent(GUINode parent) {
        delegate.setGUINodeParent(parent);
    }

    @Override
    public void representSelectedObject(SelectionEvent event) {
        delegate.representSelectedObject(event);
    }
}
