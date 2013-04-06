package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;import org.junit.Test;

/**
 * <h4>Description</h4>
 * <p/> this just makes sure I always remember which direction MV changes when residual is positive!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-02
 * @see
 */
public class PIDTestSimple {

    @Test
    public void direction()
    {
        PIDController controller = new PIDController(1,1,0,new MersenneTwisterFast());
        controller.setOffset(100);

        //if the residual is positive, the mv should go up
        assertEquals(controller.getCurrentMV(),100f,.0001f);
        controller.adjustOnce(50,true);
        assertTrue(controller.getCurrentMV() > 100f);


        controller.setOffset(100);

        //if the residual is negative, the mv should go down
        assertEquals(controller.getCurrentMV(),100f,.0001f);
        controller.adjustOnce(-50,true);
        assertTrue(controller.getCurrentMV() < 100f);





    }

}
