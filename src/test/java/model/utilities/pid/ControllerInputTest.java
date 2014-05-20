/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import org.junit.Test;

import static org.junit.Assert.*;


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
 * @version 2012-12-19
 * @see
 */
public class ControllerInputTest {


    @Test
    public void simplePIDCreation()
    {
        ControllerInput input = ControllerInput.simplePIDTarget(1f,2f); //target 1,  input 2

        assertEquals(input.getInput(0),2f,.0001f);
        assertEquals(input.getTarget(0),1f,.00001f);

        assertEquals(input.howManyInputs(),1);
        assertEquals(input.howManyTargets(),1);



    }

    @Test
    public void getAnInputThatDoesNotExist()
    {

        ControllerInput input = ControllerInput.simplePIDTarget(1f,2f);

        boolean illegalArgument = false;
        try{
            input.getInput(2);
            fail("expected an exception here!");
        }
        catch (IllegalArgumentException e)
        {
            illegalArgument = true;
        }

        assertTrue(illegalArgument);
    }

    @Test
    public void getATargetThatDoesNotExist()
    {

        ControllerInput input = ControllerInput.simplePIDTarget(1f,2f);

        boolean illegalArgument = false;
        try{
            input.getTarget(2);
            fail("expected an exception here!");
        }
        catch (IllegalArgumentException e)
        {
            illegalArgument = true;
        }

        assertTrue(illegalArgument);
    }

    @Test
    public void builderTest()
    {
         ControllerInput complicatedInput = new ControllerInput.ControllerInputBuilder().targets(1f).
                 inputs(1f, 2f, 3f, 4f, 5f, 6f).targets(10f,1f).build();

        assertEquals(complicatedInput.howManyInputs(),6);
        assertEquals(complicatedInput.howManyTargets(),3);
        assertEquals(complicatedInput.getTarget(1),10f,.0001f);


    }

    @Test
    public void cascadeInputCreation()
    {
        ControllerInput input = ControllerInput.cascadeInputCreation(1f,2f,3f); //target 1,  input1: 2, input2: 3

        assertEquals(input.getInput(1),3f,.0001f);
        assertEquals(input.getTarget(0),1f,.00001f);

        assertEquals(input.howManyInputs(),2);
        assertEquals(input.howManyTargets(),1);



    }


    /**
     * create an input with no targets, an exception should be thrown
     */
    @Test (expected=IllegalArgumentException.class)
    public void noTargetsCreation()
    {

        ControllerInput complicatedInput = new ControllerInput.ControllerInputBuilder().inputs(1f, 2f, 3f, 4f, 5f, 6f)
                .build();



    }

    /**
     * create an input with no inputs, an exception should be thrown
     */
    @Test (expected=IllegalArgumentException.class)
    public void noInputsCreation()
    {

        ControllerInput complicatedInput = new ControllerInput.ControllerInputBuilder().targets(1f, 2f, 3f, 4f, 5f, 6f)
                .build();



    }


    //test setters
    //spy that the target and control in PID are properly placed

}
