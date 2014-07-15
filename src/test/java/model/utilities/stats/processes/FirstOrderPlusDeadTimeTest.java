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
            0,
            0,
            0.8333333333,
            1.8055555556,
            2.8009259259,
            3.800154321,
            4.8000257202,
            5.8000042867 ,
            6.8000007144,
            7.8000001191,
            8.8000000198 ,
            9.8000000033 ,
            10.8000000006 ,
            11.8000000001 ,
            12.8 ,
            13.8 ,
            14.8 ,
            15.8  ,
            16.8   ,
            17.8 ,
            18.8



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