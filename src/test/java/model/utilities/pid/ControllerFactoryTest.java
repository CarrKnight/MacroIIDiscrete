/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.Steppable;


import static org.junit.Assert.*;
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
 * @version 2012-12-21
 * @see
 */
public class ControllerFactoryTest {

    @Test
    public void createPIDController()
    {
        MacroII model = mock(MacroII.class);

        when(model.drawProportionalGain()).thenReturn(100f);
        when(model.drawIntegrativeGain()).thenReturn(1f);
        when(model.drawDerivativeGain()).thenReturn(3f);


        Controller pid = ControllerFactory.buildController(PIDController.class,model);

        assertTrue(pid instanceof PIDController);
        assertEquals(pid.getClass(),PIDController.class);
        assertEquals(((PIDController) pid).getProportionalGain(),100f,.00001f);
        assertEquals(((PIDController) pid).getIntegralGain(),1f,.00001f);
        assertEquals(((PIDController) pid).getDerivativeGain(),3f,.00001f);



    }



    @Test
    public void createCascadeController()
    {
        MacroII model = mock(MacroII.class);

        when(model.drawProportionalGain()).thenReturn(100f);
        when(model.drawIntegrativeGain()).thenReturn(1f);
        when(model.drawDerivativeGain()).thenReturn(3f);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());


        Controller pid = ControllerFactory.buildController(CascadePIDController.class,model);

        assertTrue(pid instanceof CascadePIDController);
        assertEquals(pid.getClass(), CascadePIDController.class);
        assertEquals(((CascadePIDController) pid).getMasterProportionalGain(),100f,.00001f);
        assertEquals(((CascadePIDController) pid).getMasterIntegralGain(),1f,.00001f);
        assertEquals(((CascadePIDController) pid).getMasterDerivativeGain(),3f,.00001f);
        assertEquals(((CascadePIDController) pid).getSlaveProportionalGain(),100f,.00001f);
        assertEquals(((CascadePIDController) pid).getSlaveIntegralGain(),0f,.00001f); //zero, because the second is P by default
        assertEquals(((CascadePIDController) pid).getSlaveDerivativeGain(),0f,.00001f); //zero,  because the second is P by default






    }


    @Test
    public void failByPassingNull()
    {
        MacroII model = mock(MacroII.class);

        when(model.drawProportionalGain()).thenReturn(100f);
        when(model.drawIntegrativeGain()).thenReturn(1f);
        when(model.drawDerivativeGain()).thenReturn(3f);


        boolean illegalArgumentThrown = false;
        try
        {
            @SuppressWarnings("ConstantConditions")
            Controller pid = ControllerFactory.buildController(null,model);
            fail("Expected an exception here");
        }
        catch (IllegalArgumentException e)
        {
            illegalArgumentThrown = true;
        }


        assertTrue(illegalArgumentThrown);






    }




    @Test
    public void failByPassingUnrecognizableClass()
    {
        MacroII model = mock(MacroII.class);

        when(model.drawProportionalGain()).thenReturn(100f);
        when(model.drawIntegrativeGain()).thenReturn(1f);
        when(model.drawDerivativeGain()).thenReturn(3f);


        //create an unrecognizable class!
        class Unrecognizable implements Controller{
            /**
             * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
             *
             * @param input    the controller input object holding the state variables (set point, current value and so on)
             * @param isActive are we active?
             * @param simState a link to the model (to adjust yourself)
             * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
             */
            @Override
            public void adjust(ControllerInput input, boolean isActive,  MacroII simState,
                                Steppable user, ActionOrder phase) {
                throw new RuntimeException("not implemented yet!");
            }

            /**
             * Get the current u_t
             */
            @Override
            public float getCurrentMV() {
                throw new RuntimeException("not implemented yet!");
            }

            @Override
            public void setOffset(float initialPrice, boolean resetAfterSetting) {
                throw new RuntimeException("not implemented yet!");
            }

            /**
             * Get the "zero" of the controller
             *
             * @return the "zero" of the controller
             */
            @Override
            public float getOffset() {
                throw new RuntimeException("not implemented yet!");
            }

            /**
             * Set the sampling speed of the controller (how often it updates)
             *
             * @param samplingSpeed the sampling speed
             */
            @Override
            public void setSpeed(int samplingSpeed) {
                throw new RuntimeException("not implemented yet!");
            }

            /**
             * Get the sampling speed of the controller (how often it updates)
             *
             * @return the sampling speed
             */
            @Override
            public int getSpeed() {
                throw new RuntimeException("not implemented yet!");
            }

            /**
             * setting 3 parameters. I am using here the PID terminology even though it doesn't have to be the case.
             *
             * @param proportionalGain the first parameter
             * @param integralGain     the second parameter
             * @param derivativeGain   the third parameter
             */
            @Override
            public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
                throw new RuntimeException("not implemented yet!");
            }

            @Override
            public float getProportionalGain() {
                throw new RuntimeException("not implemented yet!");
            }

            @Override
            public float getIntegralGain() {
                throw new RuntimeException("not implemented yet!");
            }

            @Override
            public float getDerivativeGain() {
                throw new RuntimeException("not implemented yet!");
            }
        }


        boolean illegalArgumentThrown = false;
        try
        {
            Controller pid = ControllerFactory.buildController(Unrecognizable.class,model);
            fail("Expected an exception here");
        }
        catch (IllegalArgumentException e)
        {
            illegalArgumentThrown = true;
        }


        assertTrue(illegalArgumentThrown);






    }




}
