/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import org.junit.Assert;
import org.junit.Test;

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
 * @version 2013-08-16
 * @see
 */
public class DailyObservationsTest
{

    @Test
    public void testObservationsAreReadInCorrectly() throws Exception {

        DailyObservations observations = new DailyObservations();

        observations.add(1d);
        observations.add(2d);
        observations.add(3d);

        Assert.assertEquals(observations.size(),3);
        Assert.assertEquals(observations.get(0),1d,.00001d);
        Assert.assertEquals(observations.get(0),1d,.00001d);
        Assert.assertEquals(observations.getObservationRecordedThisDay(0),1d,.00001d);
        Assert.assertEquals(observations.getObservationRecordedThisDay(2),3d,.00001d);
        Assert.assertArrayEquals(observations.getAllRecordedObservations(),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(observations.getObservationsRecordedTheseDays(new int[]{0,2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(observations.getObservationsRecordedTheseDays(0,1),
                new double[]{1d,2d},.0001d);

    }
}
