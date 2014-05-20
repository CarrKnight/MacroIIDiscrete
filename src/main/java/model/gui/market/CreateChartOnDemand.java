/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import agents.firm.Firm;
import com.google.common.base.Preconditions;
import goods.GoodType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
        chart.setLegendVisible(false); //only because i can't control the stupid colors



        chart.getXAxis().setAutoRanging(true);
        chart.getXAxis().setLabel("Day");
        chart.getYAxis().setAutoRanging(true);
        chart.getYAxis().setLabel(dataType.name());

        //while we are updating the chart, it's clearly possible that the model is still running
        //that would make later sellers' curves longer which is very silly. with this variable we make sure all curves are of the same length
        //grab all the data immediately.
        final SalesData[] dataMap = new SalesData[sellers.size()];
        //AND equalize the end date
        int finalDate = Integer.MAX_VALUE; //get the min starting date
        int observationsPerFirm = -1;
        boolean atLeastOneStarted = false;
        //go through all firms, find a common ending-date and make sure there is some data to plot.
        for(int firmNumber =0; firmNumber < sellers.size(); firmNumber++)
        {
            updateMessage("Collecting " + sellers.get(firmNumber).getName() + " data");
            final SalesData data = sellers.get(firmNumber).getSalesDepartment(goodBeingTraded).getData();
            dataMap[firmNumber] = data;
            if(data.getStartingDay() >=0) //if it has a starting date
            {
                atLeastOneStarted = true;
                finalDate = Math.min(finalDate,data.getLastObservedDay());
                observationsPerFirm = Math.max(observationsPerFirm,data.numberOfObservations());
            }
        }
        //if there is no data don't bother
        if(!atLeastOneStarted || observationsPerFirm <=0)
        {
            return chart;
        }



        //now turn that data into a series
        mainloop:
        for(int firmNumber =0; firmNumber < sellers.size(); firmNumber++)
        {
            Firm seller = sellers.get(firmNumber);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(seller.getName()); //name!



            // series.nodeProperty().get().setStyle("-fx-stroke: " + "red;");

            updateMessage("Processing " + seller.getName() + " data");
            //grab the salesData
            SalesData salesData = dataMap[firmNumber];

            //todo this might not work if firms join at different times
            //gather the observations
            double[] pricesObserved = salesData.getAllRecordedObservations(dataType);

            assert pricesObserved.length>=observationsPerFirm;
            //convert the array into data
            for(int i=salesData.getStartingDay(); i<= finalDate; i++ ) {
                if (isCancelled())
                    break mainloop;
                series.getData().add(new XYChart.Data<>(i,pricesObserved[i-salesData.getStartingDay()]));
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
                System.out.println(style);
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
