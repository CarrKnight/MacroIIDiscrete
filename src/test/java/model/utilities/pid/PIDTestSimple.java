/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

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
        PIDController controller = new PIDController(1,1,0);
        controller.setOffset(100, true);

        //if the residual is positive, the mv should go up
        assertEquals(controller.getCurrentMV(),100f,.0001f);
        controller.adjustOnce(50,true);
        assertTrue(controller.getCurrentMV() > 100f);


        controller.setOffset(100, true);

        //if the residual is negative, the mv should go down
        assertEquals(controller.getCurrentMV(),100f,.0001f);
        controller.adjustOnce(-50,true);
        assertTrue(controller.getCurrentMV() < 100f);





    }

}
