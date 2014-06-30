/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;




//fitting it to some crap I made on libreoffice calc


import org.junit.Assert;
import org.junit.Test;

public class FirstOrderPlusDeadTimeTest {

    double input[] = new double[]{
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20

    };
    double output[] = new double[]{
            0 ,
            0  ,
            1   ,
            1.8  ,
            2.84  ,
            3.792  ,
            4.8096  ,
            5.79648  ,
            6.802624  ,
            7.7987712  ,
            8.80077056  ,
            9.799600128  ,
            10.8002340864 ,
            11.7998732083  ,
            12.8000721756   ,
            13.7999602065    ,
            14.8000223938     ,
            15.7999875625      ,
            16.8000069663       ,
            17.7999961193        ,
            18.8000021694


    };

    @Test
    public void simpleFOPTDTest() throws Exception {

        FirstOrderPlusDeadTime process = new FirstOrderPlusDeadTime(0,1,0.2,1);

        for(int i=0; i<input.length; i++)
        {
            System.out.println(output[i]);
            Assert.assertEquals(output[i],process.newStep(input[i]),.001d);
        }





    }
}