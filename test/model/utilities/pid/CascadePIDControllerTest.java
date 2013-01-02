package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import junit.framework.Assert;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.Schedule;
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
 * @version 2012-12-16
 * @see
 */
public class CascadePIDControllerTest {

    /**
     * when the stock is low, the set point of the second PID should increase
     */
    @Test
    public void lowStockIncreaseSecondPIDTargetTest(){

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());


        //the input is below target
        for(int i=0; i<50; i++)
        {
            float secondTarget = cascade.getSecondTarget();
            int firstTarget = 100; //y*
            int firstInput = i; //y
            cascade.adjust(firstTarget,firstInput,0,true,null,null,null);
            Assert.assertTrue(cascade.getSecondTarget() > secondTarget); //the 2nd target should have increased



        }

        //same thing, but move target and keep input fixed
        //the input is below target
        for(int i=50; i<100; i++)
        {
            float secondTarget = cascade.getSecondTarget();
            int firstTarget = i; //y*
            int firstInput = 10; //y
            cascade.adjust(firstTarget,firstInput,0,true,null,null,null);
            Assert.assertTrue(cascade.getSecondTarget() > secondTarget); //the  2nd target should have increased



        }


    }


    /**
     * when the stock is too high, the set point of the second PID should decrease
     */
    @Test
    public void highStockDecreaseSecondPIDTargetTest(){

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());


        //the input is below target
        for(int i=200; i<250; i++)
        {
            float secondTarget = cascade.getSecondTarget();
            int firstTarget = 100; //y*
            int firstInput = i; //y
            cascade.adjust(firstTarget,firstInput,0,true,null,null,null);
            Assert.assertTrue(cascade.getSecondTarget() < secondTarget); //the 2nd target should have decreased



        }

        //same thing, but move target and keep input fixed
        //the input is below target
        for(int i=50; i<100; i++)
        {
            float secondTarget = cascade.getSecondTarget();
            int firstTarget = i; //y*
            int firstInput = 200; //y
            cascade.adjust(firstTarget,firstInput,0,true,null,null,null);
            Assert.assertTrue(cascade.getSecondTarget() < secondTarget); //the  2nd target should have decreased



        }


    }

    /**
     * when the stock is just right, the second pid target should not change
     */
    @Test
    public void correctStockMantainSecondPIDTargetTest(){

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());


        //the input is below target
        for(int i=200; i<250; i++)
        {
            float secondTarget = cascade.getSecondTarget();
            int firstTarget = i; //y*
            int firstInput = i; //y
            cascade.adjust(firstTarget,firstInput,0,true,null,null,null);
            Assert.assertTrue(cascade.getSecondTarget() == secondTarget); //the 2nd target should have decreased



        }



    }


    /**
     * We are going to feed in a flow that is always below the second target PID, we expect MV to go up and up
     */
    @Test
    public void lowFlowMeansIncreasingMVTest()
    {

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());



        //the input is below target
        for(int i=200; i<250; i++)
        {
            float oldMV = cascade.getCurrentMV();
            int firstTarget = i; //y*
            int firstInput = i; //y
            cascade.adjust(firstTarget,firstInput,-1,true,null,null,null);
            assert (cascade.getSecondTarget() == 0); //target should stay stuck at 0!
            Assert.assertTrue(cascade.getCurrentMV() > oldMV); //the MV should be going upward!



        }

    }

    /**
     * We are going to feed in a flow that is always ABOVE the second target PID, we expect MV to go down and down
     */
    @Test
    public void highFlowMeansDroppingMVTest()
    {

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());

        //start with a big push upward, so that the currentMV goes above 0
        cascade.adjust(100,0,100,true,null,null,null); //given the numbers, the target should have been pushed to 100
        assert (cascade.getSecondTarget() == 100);


        //the input is below target
        for(int i=200; i<250; i++)
        {
            float oldMV = cascade.getCurrentMV();
            int firstTarget = 100; //y*
            int firstInput = 100; //y
            cascade.adjust(firstTarget,firstInput,101,true,null,null,null); //flow above target!
            assert (cascade.getSecondTarget() > 0 &&  cascade.getSecondTarget() <= 100); //target should stay stuck at 100!
            Assert.assertTrue(cascade.getCurrentMV() < oldMV || oldMV == 0); //the MV should be going downward



        }

    }

    /**
     * Make sure the user gets stepped
     */
    @Test
    public void makeSureYouAreSteppedTest()
    {

        CascadePIDController cascade = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());
        MacroII model = new MacroII(1l);
        //put in a fake schedule
        Schedule schedule = mock(Schedule.class);
        model.schedule = schedule;

        cascade.adjust(0f,0f,0f,true,model,mock(Steppable.class), ActionOrder.DAWN);

        //intercept the schedule in, this is somewhat of bad code but what can I do? Schedule has just too many methods
        verify(schedule,times(1)).scheduleOnceIn(anyDouble(),any(Steppable.class),anyInt());



    }

    /**
     * Make sure we get exactly the same results  using controller input and the standard adjust method
     */
    @Test
    public void controllerInputToSimpleMethod()
    {
        CascadePIDController cascade1  = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());
        CascadePIDController cascade2 = new CascadePIDController(.5f,.5f,.01f,.5f,.5f,.01f,new MersenneTwisterFast());
        MersenneTwisterFast random = new MersenneTwisterFast();

        //step it 1000 times at random
        for(int i=0; i < 1000; i++)
        {
            float target = random.nextFloat();
            float input1 = random.nextFloat();
            float input2 = random.nextFloat();

            ControllerInput input = ControllerInput.cascadeInputCreation(target,input1,input2);
            cascade1.adjust(input,true,null,null,null);
            cascade2.adjust(target,input1,input2,true,null,null,null);

        }

        Assert.assertEquals(cascade1.getCurrentMV(),cascade2.getCurrentMV(),.00001f);


    }


    //test that windup stop has a real effect
    //test that we can turn off the windup stop of the second pid

}
