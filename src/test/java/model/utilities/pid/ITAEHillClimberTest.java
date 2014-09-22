package model.utilities.pid;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ITAEHillClimberTest
{


    @Test
    public void correctFrequency(){

        ITAEHillClimber climber = new ITAEHillClimber(mock(CounterITAE.class),100, 0);

        for(int i=1; i<1000; i++)
        {
            //should be NaN 99 times and then something else for 1
            if(i % 100 != 0)
                Assert.assertTrue(Double.isNaN(climber.maximizeStep()));
            else
                Assert.assertTrue(Double.isFinite(climber.maximizeStep()));
        }


    }

    //can find minimum (10)
    @Test
    public void minimum() throws Exception {


        double[] currentParameter = new double[]{0};
        CounterITAE counter = mock(CounterITAE.class);
        doAnswer(invocation -> Math.pow(currentParameter[0]-10,2)).when(counter).getITAE();

        ITAEHillClimber climber = new ITAEHillClimber(counter,1, 0);
        for(int i=0; i<100; i++)
            currentParameter[0] = climber.maximizeStep();

        double finalStep = climber.maximizeStep();

        Assert.assertEquals(10,finalStep,1); //delta=1 because it keeps moving up and down.




    }
}