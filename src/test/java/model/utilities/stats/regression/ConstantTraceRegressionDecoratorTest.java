/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 * @version 2013-12-06
 * @see
 */
public class ConstantTraceRegressionDecoratorTest
{


    @Test
    public void testscaleCorrectly() throws Exception {
        KalmanBasedRecursiveRegression regression = mock(KalmanBasedRecursiveRegression.class);
        double[][] pMatrix = new double[3][3];
        int number = 0;
        for(int i=0; i<3; i++)
            for(int j=0;j<3; j++)
                pMatrix[i][j] = ++number;
        when(regression.getpCovariance()).thenReturn(pMatrix);
        when(regression.getTrace()).thenReturn(1d+5d+9d);


        final double[][] result = new double[3][3];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                double[][] toCopy = (double[][]) invocation.getArguments()[0];
                for(int i=0; i<3; i++)
                    System.arraycopy(toCopy[i], 0, result[i], 0, 3);
                return null;
            }
        }).when(regression).setPCovariance((double[][]) any());

        ConstantTraceRegressionDecorator tracer = new ConstantTraceRegressionDecorator(regression);
        tracer.setConstant(100);

        tracer.addObservation(100,1,2,3,4); // the arguments are meaningless

        // multiply everything by .99
        Assert.assertEquals(100,result[0][0] +result[1][1] +result[2][2],.0001d );
        Assert.assertEquals(1 * 100d/15, result[0][0], .001d);
        Assert.assertEquals(2 * 100d/15,result[0][1],.001d);
        Assert.assertEquals(3 * 100d/15,result[0][2],.001d);
        Assert.assertEquals(4 * 100d/15,result[1][0],.001d);
        Assert.assertEquals(5 * 100d/15,result[1][1],.001d);
        Assert.assertEquals(6 * 100d/15,result[1][2],.001d);
        Assert.assertEquals(7 * 100d/15,result[2][0],.001d);
        Assert.assertEquals(8 * 100d/15,result[2][1],.001d);
        Assert.assertEquals(9 * 100d/15,result[2][2],.001d);




    }
}
