package model.utilities.geography;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import ec.util.MersenneTwisterFast;
import financial.market.GeographicalClearLastMarket;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import model.scenario.oil.OilCustomer;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <h4>Description</h4>
 * <p/> Mostly a javafx object hidden within a simple
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-08
 * @see
 */
public class GeographicalClearLastMarketView extends TabbedInspector
{
    /**
     * A map that links the oil customer to the portrait used!
     */
    final private Map<OilCustomer,OilCustomerPortrait> customerToPortraitMap;

    /**
     * A map that connects each firm to its portrait (which also holds the firm's assigned color)!
     */
    final private Map<GeographicalFirm,GeographicalFirmPortrait> firmToPortraitColorMap;

    /**
     * a map linking each firm to its price-line
     */
    final private Map<GeographicalFirm, XYChart.Series<Number,Number>> firmToPriceLineMap;

    /**
     * a map linking each firm to the listener that keeps the chart updated
     */
    final private Map<GeographicalFirm,ChangeListener<Number>> firmToPriceListenerMap;

    /**
     * a list of all the price lines
     */
    final private ObservableList<XYChart.Series<Number,Number>> priceLines;

    public GeographicalClearLastMarketView(final GeographicalClearLastMarket market)
    {

        /*===========================================================
         Price-CHART!
        +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        //instantiate the map linking firms to price lines
        firmToPriceLineMap = new HashMap<>();
        firmToPriceListenerMap = new HashMap<>();
        //instantiate the list of all price lines
        priceLines = FXCollections.observableArrayList();
        LineChart <Number,Number> priceChart = new LineChart<>(xAxis,yAxis,priceLines);
        final JFXPanel priceChartPanel = new JFXPanel();
        priceChartPanel.setScene(new Scene(priceChart));
        Inspector priceInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
            }
        };
        priceInspector.add(priceChartPanel);
        addInspector(priceInspector, "prices");



         /*===========================================================
                        MAP
        +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

        final JFXPanel mapPanel = new JFXPanel();
        final ScrollPane mapView = new ScrollPane();
        mapView.setPrefSize(1024,800);

        final Group agents = new Group();
        mapView.setContent(agents);
        customerToPortraitMap = new HashMap<>();

        //BUYERS
        //go through all buyers to put them among the agents!
        ObservableSet<EconomicAgent > buyers = (ObservableSet<EconomicAgent>) market.getBuyers();


        for(EconomicAgent a : buyers)
        {
            newBuyer(agents, a);
        }
        //also add a listener that adds/removes customers as they come
        buyers.addListener(new SetChangeListener<EconomicAgent>() {
            @Override
            public void onChanged(final Change<? extends EconomicAgent> change) {
                if (change.wasAdded()) {
                    //add a new customer
                    assert !change.wasRemoved(); //can't be both!
                    OilCustomer newCustomer = (OilCustomer) change.getElementAdded();
                    newBuyer(agents, newCustomer);


                } else {
                    //find its drawing and remove it!
                    assert change.wasRemoved(); //one of the two!
                    OilCustomer oldCustomer = (OilCustomer) change.getElementRemoved();
                    removeBuyer(oldCustomer, agents);


                }

            }
        });
        //SELLERS
        //go through all buyers to put them among the agents!
        ObservableSet<EconomicAgent > sellers = (ObservableSet<EconomicAgent>) market.getSellers();
        firmToPortraitColorMap = new HashMap<>();
        //add the sellers that are already here
        for(EconomicAgent a : sellers)
        {
            assert a instanceof GeographicalFirm;
            final GeographicalFirm firm = (GeographicalFirm) a;
            newSeller(market, agents, firm);

        }
        //now add a listener to the seller set so that you can add/remove portraits and charts as they come!
        sellers.addListener(new SetChangeListener<EconomicAgent>() {
            @Override
            public void onChanged(Change<? extends EconomicAgent> change) {
                if (change.wasAdded()) {
                    //add a new customer
                    assert !change.wasRemoved(); //can't be both!
                    assert change.getElementAdded() instanceof GeographicalFirm;
                    GeographicalFirm firm = (GeographicalFirm) change.getElementAdded();
                    newSeller(market,agents,firm);


                } else {
                    //find its drawing and remove it!
                    assert !change.wasRemoved(); //can't be both!
                    assert change.getElementAdded() instanceof GeographicalFirm;
                    GeographicalFirm firm = (GeographicalFirm) change.getElementRemoved();
                    removeSeller(firm, agents, market);


                }

            }
        });



        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Scene map = new Scene(mapView);
                mapPanel.setScene(map);

            }
        });

        Inspector mapInspector = new Inspector() {
            @Override
            public void updateInspector() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
            }
        };
        mapInspector.add(mapPanel);


        addInspector(mapInspector, "map");






    }

    private void removeSeller(GeographicalFirm firm, final Group agents, GeographicalClearLastMarket market) {
        //first remove its drawing
        final GeographicalFirmPortrait portraitToRemove = firmToPortraitColorMap.remove(firm);
        assert portraitToRemove != null;
        assert !firmToPortraitColorMap.containsKey(firm);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                agents.getChildren().remove(portraitToRemove);
            }
        });

        //remove chart
        XYChart.Series<Number, Number> timeSeries = firmToPriceLineMap.get(firm);
        assert timeSeries != null;
        assert !firmToPriceLineMap.containsKey(firm);
        priceLines.remove(timeSeries);
        //stop listening
        ChangeListener<Number> updater = firmToPriceListenerMap.get(firm);
        assert updater != null;
        assert !firmToPriceListenerMap.containsKey(firm);
        firm.getLatestObservableObservation(market.getGoodType(), SalesDataType.CLOSING_PRICES).removeListener(updater);
        //done!
    }

    private void newSeller(GeographicalClearLastMarket market, final Group agents, final GeographicalFirm firm) {
        MersenneTwisterFast random = firm.getRandom();
        //create a firm portrait with random color
        final GeographicalFirmPortrait portrait = new GeographicalFirmPortrait(firm,this,
                Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        firmToPortraitColorMap.put(firm,portrait);
        //create the series chart
        final XYChart.Series<Number,Number> thisFirmPriceTimeSeries = new XYChart.Series<>();
        firm.setName(firm.getName());
        firmToPriceLineMap.put(firm,thisFirmPriceTimeSeries);
        //add the time series to plot AND portrait (using the same runnable)
        Platform.runLater(
                new Runnable() {
                    @Override
                    public void run() {
                        priceLines.add(thisFirmPriceTimeSeries);
                        agents.getChildren().add(portrait);
                    }
                }
        );
        //listen to the firm sales price so that you can update the plot regularly
        ChangeListener<Number> chartUpdater = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number old, final Number newNumber) {
                final Integer day = firm.getDay();

                thisFirmPriceTimeSeries.getData().add(new XYChart.Data<Number, Number>(day, newNumber));

            }
        };
        //record the listener, so that it can be removed later
        firmToPriceListenerMap.put(firm,chartUpdater);
        //attach it!
        firm.getLatestObservableObservation(market.getGoodType(), SalesDataType.CLOSING_PRICES).addListener(chartUpdater);
    }

    private void removeBuyer(OilCustomer oldCustomer, final Group agents) {
        final OilCustomerPortrait oldDrawing = customerToPortraitMap.remove(oldCustomer);
        assert oldDrawing != null;
        //the removal itself ought to be done on JAVAFX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                agents.getChildren().remove(oldDrawing);
            }
        });
    }

    private void newBuyer(final Group agents, EconomicAgent a) {
        assert a instanceof OilCustomer;
        final OilCustomerPortrait oilCustomerPortrait = buildPortraitForCustomer((OilCustomer) a);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                //this is the only part that interacts with the LIVE JavaFX
                agents.getChildren().add(oilCustomerPortrait);
            }                                                  });
        customerToPortraitMap.put((OilCustomer)a,oilCustomerPortrait);
    }


    private OilCustomerPortrait buildPortraitForCustomer(OilCustomer a) {
        return new OilCustomerPortrait(a,this);

    }
}
