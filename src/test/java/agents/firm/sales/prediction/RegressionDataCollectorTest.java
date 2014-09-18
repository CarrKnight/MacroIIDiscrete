package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegressionDataCollectorTest {


    @Test
    public void collectsCorrectly() throws Exception {

        //initialize mocks
        SalesDepartment department = mock(SalesDepartment.class);
        SalesData data = mock(SalesData.class);
        when(department.getData()).thenReturn(data);
        when(department.numberOfObservations()).thenReturn(1);
        when(data.numberOfObservations()).thenReturn(1);
        //set up returns
        when(data.getLatestObservation(SalesDataType.CLOSING_PRICES)).
                thenReturn(100d);
        when(data.getLatestObservation(SalesDataType.HOW_MANY_TO_SELL)).
                thenReturn(50d);
        when(data.getLatestObservation(SalesDataType.SUPPLY_GAP)).
                thenReturn(0d);

        RegressionDataCollector<SalesDataType> haircut =
                new RegressionDataCollector<>(department,SalesDataType.CLOSING_PRICES,
                        SalesDataType.HOW_MANY_TO_SELL,SalesDataType.SUPPLY_GAP);
        //observe
        haircut.collect();
        Assert.assertTrue(haircut.isLastXValid());
        Assert.assertTrue(haircut.isLastYValid());
        Assert.assertEquals(haircut.getLastObservedX(), 100, .0001);
        Assert.assertEquals(haircut.getLastObservedY(),50,.0001);
        Assert.assertEquals(haircut.getLastObservedGap(), 0, .0001);

        //observe one more time
        when(data.getLatestObservation(SalesDataType.CLOSING_PRICES)).
                thenReturn(101d);
        when(data.getLatestObservation(SalesDataType.HOW_MANY_TO_SELL)).
                thenReturn(51d);
        when(data.getLatestObservation(SalesDataType.SUPPLY_GAP)).
                thenReturn(1d);
        haircut.collect();
        Assert.assertTrue(haircut.isLastXValid());
        Assert.assertTrue(haircut.isLastYValid());
        Assert.assertEquals(haircut.getLastObservedX(),101,.0001);
        Assert.assertEquals(haircut.getLastObservedY(),51,.0001);
        Assert.assertEquals(haircut.getLastObservedGap(),1,.0001);

    }

    @Test
    public void dataStartsInvalid(){

        RegressionDataCollector<SalesDataType> haircut =
                new RegressionDataCollector<>(mock(SalesDepartment.class),SalesDataType.CLOSING_PRICES,
                        SalesDataType.HOW_MANY_TO_SELL,SalesDataType.SUPPLY_GAP);

        Assert.assertFalse(haircut.isLastXValid());
        Assert.assertFalse(haircut.isLastYValid());
        Assert.assertFalse(haircut.isLastGapValid());

    }

    @Test
    public void dataValidatorWorks()
    {

        SalesDepartment department = mock(SalesDepartment.class);
        SalesData data = mock(SalesData.class);
        when(department.getData()).thenReturn(data);
        when(department.numberOfObservations()).thenReturn(1);
        when(data.numberOfObservations()).thenReturn(1);
        //they all return 0
        when(data.getLatestObservation(SalesDataType.CLOSING_PRICES)).
                thenReturn(0d);
        when(data.getLatestObservation(SalesDataType.HOW_MANY_TO_SELL)).
                thenReturn(0d);
        when(data.getLatestObservation(SalesDataType.SUPPLY_GAP)).
                thenReturn(0d);


        RegressionDataCollector<SalesDataType> collector =
                new RegressionDataCollector<>(department,SalesDataType.CLOSING_PRICES,
                        SalesDataType.HOW_MANY_TO_SELL,SalesDataType.SUPPLY_GAP);

        //set validators always true
        collector.setxValidator((x)->true);
        collector.setyValidator((y)->true);
        collector.setGapValidator((gap)->true);
        collector.setDataValidator((dept)->true);

        collector.collect();
        Assert.assertTrue(collector.isLastXValid());
        Assert.assertTrue(collector.isLastYValid());
        Assert.assertTrue(collector.isLastGapValid());
        Assert.assertTrue(collector.isLatestObservationValid());

        collector.setxValidator((x)->false);
        collector.collect();
        Assert.assertTrue(!collector.isLastXValid());
        Assert.assertTrue(collector.isLastYValid());
        Assert.assertTrue(collector.isLastGapValid());
        Assert.assertTrue(!collector.isLatestObservationValid());

        collector.setxValidator((x)->true);
        collector.setyValidator((y)->false);
        Assert.assertTrue(collector.isLastXValid());
        Assert.assertTrue(!collector.isLastYValid());
        Assert.assertTrue(collector.isLastGapValid());
        Assert.assertTrue(!collector.isLatestObservationValid());

        collector.setyValidator((y)->true);
        collector.setGapValidator((gap)->false);
        Assert.assertTrue(collector.isLastXValid());
        Assert.assertTrue(collector.isLastYValid());
        Assert.assertTrue(!collector.isLastGapValid());
        Assert.assertTrue(!collector.isLatestObservationValid());



    }
}