/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import agents.firm.Firm;
import com.google.common.base.Preconditions;
import goods.GoodType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.ColorInput;
import javafx.scene.shape.Path;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;

import java.util.LinkedList;
import java.util.Set;

/**
 * A different charting that is updated only when forced to
 * Created by carrknight on 4/9/14.
 */
public class CreateChartOnDemand extends Task<LineChart<Number,Number>> {

    private final SellingFirmToColorMap sellerMap;

    private final GoodType goodBeingTraded;

    private LineChart<Number,Number> chart;

    /**
     * the data type to chart
     */
    private final SalesDataType dataType;

    public CreateChartOnDemand(SellingFirmToColorMap map, GoodType goodBeingTraded,SalesDataType dataType)
    {
        this.sellerMap = map;
        this.goodBeingTraded = goodBeingTraded;
        this.dataType = dataType;
    }

    @Override
    protected LineChart<Number,Number> call() throws Exception {
        Preconditions.checkState(!Platform.isFxApplicationThread());

        LinkedList<Firm> sellers = new LinkedList<>(sellerMap.getColorMap().keySet());
        chart = new LineChart<>(new NumberAxis(),new NumberAxis());
        ObservableList<XYChart.Series<Number, Number>> serieses = chart.getData();
        chart.setCache(true);
        chart.setCacheHint(CacheHint.SPEED);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);

        int observationsPerFirm = -1; //while we are updating the chart, it's clearly possible that the model is still running
        //that would make later sellers' curves longer which is very silly. with this variable we make sure all curves are of the same length

        chart.getXAxis().setAutoRanging(true);
        chart.getXAxis().setLabel("Day");
        chart.getYAxis().setAutoRanging(true);
        chart.getYAxis().setLabel(dataType.name());
        //for every seller create a series
        mainloop:
        for(int firmNumber =0; firmNumber < sellers.size(); firmNumber++)
        {
            Firm seller = sellers.get(firmNumber);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seller.getName()); //name!



            // series.nodeProperty().get().setStyle("-fx-stroke: " + "red;");

            updateMessage("Collecting " + seller.getName() + " data");
            //grab the salesData
            SalesData salesData = seller.getSalesDepartment(goodBeingTraded).getData();
            if(salesData.numberOfObservations() ==0) //if there are no observations, don't bother
            {
                assert observationsPerFirm == -1;
                return chart;
            }


            //todo this might not work if firms join at different times
            //gather the observations
            double[] pricesObserved = salesData.getAllRecordedObservations(dataType);
            if(observationsPerFirm==-1) {
                observationsPerFirm = pricesObserved.length;
            }
            assert pricesObserved.length>=observationsPerFirm;
            //convert the array into data
            for(int i=0; i< observationsPerFirm; i++ ) {
                if (isCancelled())
                    break mainloop;
                series.getData().add(new XYChart.Data<>(salesData.getStartingDay()+i,pricesObserved[i]));
                updateProgress(firmNumber*observationsPerFirm+i,sellers.size()*observationsPerFirm);
            }
            //add the series
            serieses.add(series);




        }
        updateMessage("Drawing");
        //now color it (this is really annoying)


        for (int j = 0; j < chart.getData().size(); j++) {
            Set<Node> nodes = chart.lookupAll(".series" + j);
            String color = sellerMap.getFirmColor(sellers.get(j)).toString();
            color = color.replace("0x","#");
            for (Node n : nodes) {
                StringBuilder style = new StringBuilder();
                style.append("-fx-stroke: ").append(color).append("; ");
                style.append("-fx-background-color: ").append(color).append(", white").append(";");
                n.setStyle(style.toString());
            }
        }
        System.out.println("chart created succesfully");
        return chart;

    }

    /**
     * this is basically a "getResult()" without blocking anything. It's null until called
     */
    public LineChart<Number, Number> getChart() {
        return chart;
    }
}
