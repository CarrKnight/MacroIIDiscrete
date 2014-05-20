/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model;

import model.utilities.Deactivatable;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
 * @version 2013-07-04
 * @see
 */
public class MacroIIFinishTest {


    @Test
    public void testRegisterDeactivable() throws Exception {
        //make sure the deactivable registering works
        Deactivatable d = mock(Deactivatable.class);

        MacroII macroII = new MacroII(1);
        macroII.registerDeactivable(d);

        macroII.start();
        macroII.finish();

        verify(d).turnOff();


    }
}
