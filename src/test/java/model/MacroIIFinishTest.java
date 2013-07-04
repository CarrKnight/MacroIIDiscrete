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

        MacroII macroII = new MacroII(1l);
        macroII.registerDeactivable(d);

        macroII.start();
        macroII.finish();

        verify(d).turnOff();


    }
}
