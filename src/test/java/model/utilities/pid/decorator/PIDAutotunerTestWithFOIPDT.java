/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import au.com.bytecode.opencsv.CSVReader;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.regression.KalmanFOPIDTRegressionWithKnownTimeDelay;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class PIDAutotunerTestWithFOIPDT
{


    @Test
    public void regressLikeInR() throws Exception {


        /////////////////////////////////////////
        //read csv
        //read data from file!
        CSVReader reader = new CSVReader(new FileReader( Paths.get("testresources", "FOIPDT.csv").toFile()));
        reader.readNext(); //ignore the header!

        ArrayList<Float> output = new ArrayList<>();
        ArrayList<Float> input = new ArrayList<>();


        String[] newLine;
        while(( newLine = reader.readNext()) != null)
        {

            output.add(Float.parseFloat(newLine[0]));
            input.add(Float.parseFloat(newLine[1]));
        }

        reader.close();


        /////////////////////////////////////////////////////////////////////////////////



        PIDController pid = mock(PIDController.class);
        when(pid.isControllingFlows()).thenReturn(false);

        PIDAutotuner pidAutotuner = new PIDAutotuner(pid, KalmanFOPIDTRegressionWithKnownTimeDelay::new,
                null);





        for(int i=0; i<100; i++)
        {
            when(pid.getCurrentMV()).thenReturn(input.get(i));
            pidAutotuner.adjust(new ControllerInput(0,0,0,output.get(i)),true,null,null,null);
        }

        Assert.assertEquals(0,pidAutotuner.getDelay());
        Assert.assertEquals(7.130,pidAutotuner.getTimeConstant(),.01f);




    }
}