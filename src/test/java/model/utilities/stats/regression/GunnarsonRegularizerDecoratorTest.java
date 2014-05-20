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

import static org.mockito.Mockito.*;

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
public class GunnarsonRegularizerDecoratorTest {



    //P covariance is 1,2,3,4,5,6,7,8,9 on 3 rows
    //the regularized matrix wiht mu = 0.01 is
    //      [,1]     [,2]     [,3]
    //[1,] 0.7402891 1.689601 2.638913
    //[2,] 3.4314579 4.302386 5.173315
    //[3,] 6.1226267 6.915172 7.707716


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

        GunnarsonRegularizerDecorator regularizerDecorator = new GunnarsonRegularizerDecorator(regression);
        regularizerDecorator.setMu(.01d);

        regularizerDecorator.addObservation(100,1,2,3,4); // the arguments are meaningless

        //      [,0]     [,1]     [,2]
        //[0,] 0.7402891 1.689601 2.638913
        //[1,] 3.4314579 4.302386 5.173315
        //[2,] 6.1226267 6.915172 7.707716
        Assert.assertEquals(0.7402891,result[0][0],.001d);
        Assert.assertEquals(1.6896011,result[0][1],.001d);
        Assert.assertEquals(2.6389131,result[0][2],.001d);
        Assert.assertEquals(3.431458,result[1][0],.001d);
        Assert.assertEquals(4.302386,result[1][1],.001d);
        Assert.assertEquals(5.173315,result[1][2],.001d);
        Assert.assertEquals( 6.122627,result[2][0],.001d);
        Assert.assertEquals(6.915172 ,result[2][1],.001d);
        Assert.assertEquals(7.707716,result[2][2],.001d);


    }
}
