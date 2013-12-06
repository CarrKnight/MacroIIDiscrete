/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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
public class ExponentialForgettingRegressionDecoratorTest {


    @Test
    public void testRegularize() throws Exception {

        RecursiveLinearRegression regression = mock(RecursiveLinearRegression.class);
        double[][] pMatrix = new double[3][3];
        int number = 0;
        for(int i=0; i<3; i++)
            for(int j=0;j<3; j++)
                pMatrix[i][j] = ++number;
        when(regression.getpCovariance()).thenReturn(pMatrix);


        final double[][] result = new double[3][3];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                double[][] toCopy = (double[][]) invocation.getArguments()[0];
                for(int i=0; i<3; i++)
                    for(int j=0;j<3; j++)
                        result[i][j] = toCopy[i][j];
                return null;
            }
        }).when(regression).setPCovariance((double[][]) any());

        ExponentialForgettingRegressionDecorator forgetter = new ExponentialForgettingRegressionDecorator(regression);
        forgetter.setLambda(.99);

        forgetter.addObservation(100,1,2,3,4); // the arguments are meaningless

        // multiply everything by .99
        Assert.assertEquals(1/.99, result[0][0], .001d);
        Assert.assertEquals(2 /.99,result[0][1],.001d);
        Assert.assertEquals(3 /.99,result[0][2],.001d);
        Assert.assertEquals(4 / .99,result[1][0],.001d);
        Assert.assertEquals(5 / .99,result[1][1],.001d);
        Assert.assertEquals(6 / .99,result[1][2],.001d);
        Assert.assertEquals(7 / .99,result[2][0],.001d);
        Assert.assertEquals(8 / .99,result[2][1],.001d);
        Assert.assertEquals(9 / .99,result[2][2],.001d);


    }

}
