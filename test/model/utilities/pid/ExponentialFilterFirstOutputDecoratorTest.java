package model.utilities.pid;

import junit.framework.Assert;
import model.utilities.pid.decorator.ExponentialFilterOutputDecorator;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @version 2012-11-15
 * @see
 */
public class ExponentialFilterFirstOutputDecoratorTest {



    //we are going to use the same numbers as the exponential input filter.
    @Test
    public void testAdjust() {

        Controller fakeController = mock(Controller.class);



        //decorate the output
        Controller filtered = new ExponentialFilterOutputDecorator(fakeController,.35f);



        //initially put for 3 times the number "2" as input!
        //1
        float input = 2;
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),4f,.01f);
        //2
        input = 2;
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),4f,.01f);

        //3
        input = 2;
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),4f,.01f);


        //4 times the number "3" as input!
        //1
        input = 3;
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),4.7f,.01f);
        //2
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),5.155f,.01f);

        //3
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),5.45075f,.01f);


        //4
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),5.6429875f,.01f);


        //6 times the number "4" as input!
        //1
        input = 4;
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),6.467941875f,.01f);
        //2
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),7.00416221875f,.01f);

        //3
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),7.3527054421875f,.01f);


        //4
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),7.57925853742188f,.01f);

        //5
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),7.72651804932422f,.01f);


        //6
        when(fakeController.getCurrentMV()).thenReturn(input*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(SimState.class),mock(Steppable.class)); //except for current all the other arguments are useless
        Assert.assertEquals(filtered.getCurrentMV(),7.82223673206074f,.01f);



    }
}
