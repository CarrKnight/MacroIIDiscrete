/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.decorator.ExponentialFilterInputDecorator;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Steppable;

import static org.junit.Assert.assertEquals;
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
public class ExponentialFilterFirstInputDecoratorTest {

    //make sure input is adjusted


    float expectedInput;



    @Test
    public void testAdjust() throws Exception {

        //the controller is a mock that checks whether the input has been smoothed
        Controller controller = mock(Controller.class);

        //whenever you call adjust
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                //the second argument is the input of the controller
                Float input = ((ControllerInput) invocation.getArguments()[0]).getInput(0);
                //make sure it's equal to what we expect
                assertEquals(input,expectedInput,.01f);
                //print out for confirmation
                System.out.println("input:" + input + ", expected input: " + expectedInput);

                return null;

            }
        }).when(controller).adjust(any(ControllerInput.class),anyBoolean(),any(MacroII.class),any(Steppable.class),
                any(ActionOrder.class));



        //decorate the mock!
        ExponentialFilterInputDecorator filtered = new ExponentialFilterInputDecorator(controller);


        //initially put for 3 times the number "2" as input!
        //1
        float input = 2;
        expectedInput = 2; //the first input is not smoothed!
        when(controller.getCurrentMV()).thenReturn(4f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class), ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.0001f);  //this should be true at all times
        //2
        input = 2;
        expectedInput = 2; //input hasn't changed!
        when(controller.getCurrentMV()).thenReturn(4f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //3
        input = 2;
        expectedInput = 2; //input hasn't changed!
        when(controller.getCurrentMV()).thenReturn(4f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //now we are going to switch input to 3  (for 4 times)
        //1
        input = 3;
        expectedInput = 2.35f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //2
        input = 3;
        expectedInput = 2.5775f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //3
        input = 3;
        expectedInput = 2.725375f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //4
        input = 3;
        expectedInput = 2.82149375f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //now we are going to switch input to 4  (for 5 times)
        //1
        input = 4;
        expectedInput = 3.2339709375f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //2
        input = 4;
        expectedInput = 3.502081109375f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //3
        input = 4;
        expectedInput = 3.67635272109375f;

        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //4
        input = 4;
        expectedInput = 3.78962926871094f;

        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //5
        input = 4;
        expectedInput = 3.86325902466211f;

        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times

        //6
        input = 4;
        expectedInput = 3.91111836603037f;
        when(controller.getCurrentMV()).thenReturn(expectedInput*2f);
        filtered.adjust(ControllerInput.simplePIDTarget(0,input),true,mock(MacroII.class),mock(Steppable.class),ActionOrder.ADJUST_PRICES); //except for current all the other arguments are useless
        assertEquals(filtered.getCurrentMV(),controller.getCurrentMV(),.001f);  //this should be true at all times




    }


}
