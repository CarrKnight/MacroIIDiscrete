package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.CounterITAE;
import model.utilities.pid.PIDController;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PIDStickinessHillClimberTunerTest
{


    @Test
    public void schedulesProperly() throws Exception {

        MacroII model = mock(MacroII.class);
        PIDController controller = mock(PIDController.class);
        CounterITAE itae = mock(CounterITAE.class);

        PIDStickinessHillClimberTuner tuner =new PIDStickinessHillClimberTuner(model,controller,itae,1234);

        //should schedule itself in 1234 days
        verify(model).scheduleAnotherDay(PIDStickinessHillClimberTuner.WHEN_TO_COUNT, tuner, 1234);

        //restep it, it should reschedule itself!
        tuner.step(model);
        verify(model).scheduleTomorrow(PIDStickinessHillClimberTuner.WHEN_TO_COUNT,tuner);

        //turn it off
        reset(model);
        tuner.turnOff();
        tuner.step(model);
        verify(model,never()).scheduleTomorrow(PIDStickinessHillClimberTuner.WHEN_TO_COUNT, tuner);
    }


    @Test
    public void maximizesProperly()
    {
        MacroII model = mock(MacroII.class);
        PIDController controller = new PIDController(0,0,0,0);
        CounterITAE itae = mock(CounterITAE.class);
        doAnswer(invocation -> Math.pow(controller.getSpeed()-10,2)).when(itae).getITAE(); //the minimum is at 10!

        PIDStickinessHillClimberTuner tuner =new PIDStickinessHillClimberTuner(model,controller,itae,1,1);
        for(int i=0; i<100; i++)
            tuner.step(model);

        Assert.assertEquals(10,controller.getSpeed(),1); //error margin is high because hillclimber never stops.
    }



    @Test
    public void maximizesProperlyFromAbove()
    {
        MacroII model = mock(MacroII.class);
        PIDController controller = new PIDController(0,0,0,20);
        CounterITAE itae = mock(CounterITAE.class);
        doAnswer(invocation -> Math.pow(controller.getSpeed()-10,2)).when(itae).getITAE(); //the minimum is at 10!

        PIDStickinessHillClimberTuner tuner =new PIDStickinessHillClimberTuner(model,controller,itae,1,1);
        for(int i=0; i<100; i++)
            tuner.step(model);

        Assert.assertEquals(10,controller.getSpeed(),1); //error margin is high because hillclimber never stops.
    }

    @Test
    public void decorationWorks()
    {
        //make sure decoration works

        MacroII model = mock(MacroII.class);
        PIDController controller = new PIDController(0,0,0,0);
        CounterITAE itae = mock(CounterITAE.class);
        doAnswer(invocation -> Math.pow(controller.getSpeed()-10,2)).when(itae).getITAE(); //the minimum is at 10!

        PIDStickinessHillClimberTuner tuner =new PIDStickinessHillClimberTuner(model,controller,itae,1,1);
        tuner.adjust(new ControllerInput(10,1,3,4),true,model,null, ActionOrder.DAWN);

        verify(itae).setTarget(10);
    }
}