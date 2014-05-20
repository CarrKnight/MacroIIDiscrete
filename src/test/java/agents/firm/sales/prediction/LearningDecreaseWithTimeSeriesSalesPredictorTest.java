/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-07
 * @see
 */
public class LearningDecreaseWithTimeSeriesSalesPredictorTest {


    //read from csv and solve, please.
    /*
    @Test
    public void testing() throws IOException {

        System.out.println(Paths.get(System.getProperty("user.dir"),"src","test",
                "regressionTestConsumption.csv"));
        //read data from file!
        CSVReader reader = new CSVReader(new FileReader(
                Paths.get(System.getProperty("user.dir"),"src","test",
                        "regressionTestConsumption.csv").toFile()));
                        reader.readNext(); //ignore the header!

        ArrayList<Double> prices = new ArrayList<>(900);
        ArrayList<Double> consumption = new ArrayList<>(900);


        String[] newLine;
        while(( newLine = reader.readNext()) != null)
        {

            consumption.add(Double.parseDouble(newLine[1]));
            prices.add(Double.parseDouble(newLine[2]));
        }


        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(Doubles.toArray(consumption));
        when(observer.getPricesObservedAsArray()).thenReturn(Doubles.toArray(prices));
        when(observer.getNumberOfObservations()).thenReturn(prices.size());

        //create the predictor
        LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(observer);
        //force an update
        predictor.setUsingWeights(false); predictor.setCorrectingWithDeltaPrice(false);
        predictor.predictSalePriceAfterIncreasingProduction(mock(SalesDepartment.class), 1, 1);
        //0.8527228*price - 6.596947
        //notice here that there is a LOT of difference between the R results and my results.
        Assert.assertEquals(-0.09629347, predictor.extractSlopeOfDemandFromRegression(), .01);
        Assert.assertEquals(18.02149,predictor.extractInterceptOfDemandFromRegression(),.01);

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.getAveragedLastPrice()).thenReturn(100d);
        Assert.assertEquals(Math.round(100-1d/0.09629347d),predictor.predictSalePriceAfterIncreasingProduction(department, 100, 1));




    }


    //another solution, in this one I copy pasted the results!
    @Test
    public void testing2() throws IOException {

        //read data from file!


        double[] prices = new double[]{
                100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 98.0, 98.0, 98.0,
                98.0, 98.0, 98.0, 98.0, 98.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 96.0, 96.0, 96.0, 95.0,
                95.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 93.0, 93.0, 93.0, 93.0, 93.0, 92.0, 92.0, 92.0,
                92.0, 92.0, 92.0, 92.0, 91.0, 91.0, 91.0
        };
        double[] consumption = new double[]{
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 3.0, 3.0, 3.0, 3.0,
                3.0, 3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 6.0, 7.0, 7.0, 7.0,
                7.0, 7.0, 7.0, 7.0,
                7.0, 8.0, 8.0, 8.0, 8.0, 8.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 10.0, 10.0, 10.0
        };



        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(consumption);
        when(observer.getPricesObservedAsArray()).thenReturn(prices);
        when(observer.getNumberOfObservations()).thenReturn(prices.length);

        //create the predictor
        LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(observer);
        predictor.setUsingWeights(false);
        //force an update
        predictor.predictSalePriceAfterIncreasingProduction(mock(SalesDepartment.class), 1, 1);

        Assert.assertEquals(-1, predictor.extractSlopeOfDemandFromRegression(), .01);
        Assert.assertEquals(101,predictor.extractInterceptOfDemandFromRegression(),.01);

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.getAveragedLastPrice()).thenReturn(100d);
        Assert.assertEquals(99,predictor.predictSalePriceAfterIncreasingProduction(department, 100, 1));




    }

      */
}
