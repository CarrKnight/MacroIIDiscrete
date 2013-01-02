package tests;

import agents.firm.purchases.inventoryControl.Level;
import junit.framework.Assert;
import org.junit.Test;

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
        Assert.assertTrue(Level.DANGER.compareTo(Level.BARELY) == -1);
        Assert.assertTrue(Level.DANGER.compareTo(Level.ACCEPTABLE) == -2);
        Assert.assertTrue(Level.DANGER.compareTo(Level.TOOMUCH) == -3);
        Assert.assertTrue(Level.BARELY.compareTo(Level.DANGER) == 1);
        Assert.assertTrue(Level.BARELY.compareTo(Level.ACCEPTABLE) == -1);
        Assert.assertTrue(Level.BARELY.compareTo(Level.TOOMUCH) == -2);
        Assert.assertTrue(Level.ACCEPTABLE.compareTo(Level.DANGER) == 2);
        Assert.assertTrue(Level.ACCEPTABLE.compareTo(Level.BARELY) == 1);
        Assert.assertTrue(Level.ACCEPTABLE.compareTo(Level.TOOMUCH) == -1);
        Assert.assertTrue(Level.TOOMUCH.compareTo(Level.DANGER) == 3);
        Assert.assertTrue(Level.TOOMUCH.compareTo(Level.BARELY) == 2);
        Assert.assertTrue(Level.TOOMUCH.compareTo(Level.ACCEPTABLE) == 1);


        Assert.assertTrue(Level.ACCEPTABLE.compareTo(Level.ACCEPTABLE) == 0);



    }


}
