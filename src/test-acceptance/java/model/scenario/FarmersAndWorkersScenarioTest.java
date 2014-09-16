/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import org.junit.Assert;
import org.junit.Test;

public class FarmersAndWorkersScenarioTest {



    @Test
    public void forcedCompetitiveRun200People() throws Exception {


        for(int j=0; j< 5; j++) {
            final FarmersStatics.FarmersAndWorkersResult farmersAndWorkersResult = FarmersStatics.runForcedCompetitiveFarmersAndWorkersScenario(10,
                    1, 200, System.currentTimeMillis(), null, null, 3000);
            Assert.assertEquals(1100, farmersAndWorkersResult.getManufacturingProduction(), 50); //5% error allowed
            Assert.assertEquals(13995, farmersAndWorkersResult.getAgriculturalProduction(), 500);
            Assert.assertEquals(10.95, farmersAndWorkersResult.getManufacturingPrice(),5);

        }
    }


    @Test
    public void forcedCompetitiveRun50People() throws Exception {


        for(int j=0; j< 5; j++) {
            final FarmersStatics.FarmersAndWorkersResult farmersAndWorkersResult = FarmersStatics.runForcedCompetitiveFarmersAndWorkersScenario(10, 5, 50,
                    System.currentTimeMillis(), null, null, 6000);
            Assert.assertEquals(280, farmersAndWorkersResult.getManufacturingProduction(), 15); //5% error allowed
            Assert.assertEquals(869, farmersAndWorkersResult.getAgriculturalProduction(), 45);
            Assert.assertEquals(2.7, farmersAndWorkersResult.getManufacturingPrice(),.5);

        }
    }

    @Test
    public void CompetitiveRun50People() throws Exception {


        for(int j=0; j< 5; j++) {
            final FarmersStatics.FarmersAndWorkersResult farmersAndWorkersResult = FarmersStatics.runLearningFarmersAndWorkers(10, 5, 50,
                    System.currentTimeMillis(), null, null, 6000);
            Assert.assertEquals(280, farmersAndWorkersResult.getManufacturingProduction(), 15); //5% error allowed
            Assert.assertEquals(869, farmersAndWorkersResult.getAgriculturalProduction(), 45);
            Assert.assertEquals(2.7, farmersAndWorkersResult.getManufacturingPrice(),.5);

        }
    }


    @Test
    public void whereIsMyMoney() throws Exception {
            final FarmersStatics.FarmersAndWorkersResult farmersAndWorkersResult = FarmersStatics.runForcedCompetitiveFarmersAndWorkersScenario(10, 1, 50, 1402602013498l,
                    null, null, 6000);
        Assert.assertEquals(280, farmersAndWorkersResult.getManufacturingProduction(), 15); //5% error allowed
        Assert.assertEquals(869, farmersAndWorkersResult.getAgriculturalProduction(), 45);
        Assert.assertEquals(2.7, farmersAndWorkersResult.getManufacturingPrice(),.5);


    }


}