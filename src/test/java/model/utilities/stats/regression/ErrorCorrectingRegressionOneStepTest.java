/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ErrorCorrectingRegressionOneStepTest {

    @Test
    public void testRegressionFromCSV() throws Exception {
        final BufferedReader dataReader = Files.newBufferedReader(Paths.get("testresources", "ECModel.csv"));
        //throw away the header
        dataReader.readLine();

        //create the regresssion
        ErrorCorrectingRegressionOneStep regressionOneStep = new ErrorCorrectingRegressionOneStep(1);

        //feed it stuff
        while(dataReader.ready())
        {
            final String[] line = dataReader.readLine().split(",");
            regressionOneStep.addObservation(Double.parseDouble(line[0]),
                    Double.parseDouble(line[2]));
        }

        //regression should have worked somehow
        Assert.assertEquals(-0.698268d,regressionOneStep.getGain(),.01);

    }
}