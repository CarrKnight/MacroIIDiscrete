package tests;

import agents.firm.purchases.inventoryControl.Level;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
 * @author Ernesto
 * @version 2012-08-05
 * @see
 */
public class InventoryLevelTest {

    @Test
    public void comparison(){
        assertTrue(Level.DANGER.compareTo(Level.BARELY) == -1);
        assertTrue(Level.DANGER.compareTo(Level.ACCEPTABLE) == -2);
        assertTrue(Level.DANGER.compareTo(Level.TOOMUCH) == -3);
        assertTrue(Level.BARELY.compareTo(Level.DANGER) == 1);
        assertTrue(Level.BARELY.compareTo(Level.ACCEPTABLE) == -1);
        assertTrue(Level.BARELY.compareTo(Level.TOOMUCH) == -2);
        assertTrue(Level.ACCEPTABLE.compareTo(Level.DANGER) == 2);
        assertTrue(Level.ACCEPTABLE.compareTo(Level.BARELY) == 1);
        assertTrue(Level.ACCEPTABLE.compareTo(Level.TOOMUCH) == -1);
        assertTrue(Level.TOOMUCH.compareTo(Level.DANGER) == 3);
        assertTrue(Level.TOOMUCH.compareTo(Level.BARELY) == 2);
        assertTrue(Level.TOOMUCH.compareTo(Level.ACCEPTABLE) == 1);


        assertTrue(Level.ACCEPTABLE.compareTo(Level.ACCEPTABLE) == 0);



    }


}
